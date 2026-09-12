package com.avradeep.AttemptService.service;

import com.avradeep.AttemptService.cleint.QuestionClient;
import com.avradeep.AttemptService.cleint.QuizClient;
import com.avradeep.AttemptService.dto.AttemptQuestionResponse;
import com.avradeep.AttemptService.dto.QuestionEvaluationResponse;
import com.avradeep.AttemptService.dto.QuizResponse;
import com.avradeep.AttemptService.dto.StartAttemptResponse;
import com.avradeep.AttemptService.entity.AttemptStatus;
import com.avradeep.AttemptService.entity.QuestionSnapshot;
import com.avradeep.AttemptService.entity.QuizAttempt;
import com.avradeep.AttemptService.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttemptServiceImpl implements AttemptService{

    private final QuizClient quizClient;
    private final QuestionClient questionClient;
    private final QuizAttemptRepository quizAttemptRepository;

    @Value("${quiz.attempt.cooldown-minutes}")
    private long cooldownMinutes;

    @Override
    public StartAttemptResponse startAttempt(String userId, String quizId){
        log.info(
                "User {} is starting quiz {}",
                userId,
                quizId);

        /**
          Check if an active attempt already exists
         */

        boolean activeAttempt = quizAttemptRepository
                        .findTopByUserIdAndQuizIdAndStatusOrderByStartTimeDesc(
                                userId,
                                quizId,
                                AttemptStatus.IN_PROGRESS)
                        .isPresent();

        if (activeAttempt) {
            throw new IllegalStateException(
                    "You already have an active attempt for this quiz");
        }

        /**
          Check cooldown
        */
        quizAttemptRepository
                .findTopByUserIdAndQuizIdAndStatusInOrderBySubmittedAtDesc(
                        userId,
                        quizId,
                        List.of(
                                AttemptStatus.SUBMITTED,
                                AttemptStatus.EXPIRED
                        )
                )
                .ifPresent(lastAttempt -> {

                    if (lastAttempt.getSubmittedAt() == null) {
                        return;
                    }

                    Instant cooldownEnd =
                            lastAttempt.getSubmittedAt()
                                    .plus(
                                            Duration.ofMinutes(cooldownMinutes)
                                    );

                    if (Instant.now().isBefore(cooldownEnd)) {
                        throw new IllegalStateException(
                                "Cooldown is still active");
                    }
                });

        /**
         * Get quiz details from Quiz-Service
         */

        if (quizId == null || quizId.isBlank()) {
            throw new IllegalArgumentException(
                    "Quiz ID cannot be null or empty"
            );
        }

        log.info("Fetching details of quiz: {}", quizId);

        QuizResponse quiz =
                quizClient.getQuizById(quizId);

        if (quiz == null) {
            throw new IllegalStateException(
                    "Quiz-Service returned an empty response"
            );
        }

        if (quiz.getTimeLimit() == null || quiz.getTimeLimit() <= 0) {
            throw new IllegalStateException(
                    "Invalid quiz time limit"
            );
        }

        /**
          Get questions + correct answers from QUESTION-SERVICE
         */

        List<QuestionEvaluationResponse> questions =
                questionClient.getQuestionsForEvaluation(quizId);

        if (questions == null || questions.isEmpty()) {
            throw new IllegalStateException(
                    "No questions found for this quiz"
            );
        }

        /**
          Calculate attempt timing
         */
        Instant startTime = Instant.now();

        Instant expiryTime =
                startTime.plus(
                        Duration.ofMinutes(
                                quiz.getTimeLimit())
                );

        /**
          Create internal question snapshot
        */
        List<QuestionSnapshot> snapshot =
                questions.stream()
                        .map(question ->
                                QuestionSnapshot.builder()
                                        .questionId(question.getId())
                                        .correctAnswer(
                                                question.getCorrectAnswer()
                                        )
                                        .build()
                        )
                        .toList();

        /**
          Create and save attempt
         */
        QuizAttempt attempt =
                QuizAttempt.builder()
                        .userId(userId)
                        .quizId(quizId)
                        .startTime(startTime)
                        .status(AttemptStatus.IN_PROGRESS)
                        .questionSnapshot(snapshot)
                        .build();

        QuizAttempt savedAttempt =
                quizAttemptRepository.save(attempt);

        log.info(
                "Attempt: {} created for user: {} for quiz: {}",
                savedAttempt.getId(),
                userId,
                quizId
        );

        /**
          Build safe response for a client
         */
        List<AttemptQuestionResponse> responseQuestions =
                questions.stream()
                        .map(question ->
                                AttemptQuestionResponse.builder()
                                        .questionId(question.getId())
                                        .questionText(
                                                question.getQuestionText()
                                        )
                                        .options(question.getOptions())
                                        .build()
                        )
                        .toList();

        return StartAttemptResponse.builder()
                .attemptId(savedAttempt.getId())
                .quizId(quizId)
                .startTime(startTime)
                .expiryTime(expiryTime)
                .questions(responseQuestions)
                .build();
    }

    @Override
    public void submitAttempt(
            String userId,
            String attemptId,
            Map<String, Integer> answers) {

        log.info(
                "User {} is submitting attempt {}",
                userId,
                attemptId
        );

        // 1. Validate input
        if (attemptId == null || attemptId.isBlank()) {
            throw new IllegalArgumentException(
                    "Attempt ID cannot be null or empty");
        }

        if (answers == null) {
            throw new IllegalArgumentException(
                    "Answers cannot be null");
        }

        // 2. Find attempt
        QuizAttempt attempt =
                quizAttemptRepository.findById(attemptId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Attempt not found")
                        );

        // 3. Verify ownership
        if (!attempt.getUserId().equals(userId)) {
            throw new IllegalStateException(
                    "You are not allowed to submit this attempt");
        }

        // 4. Verify an attempt is still active
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "This attempt is already completed");
        }

        // 5. Get quiz details
        QuizResponse quiz =
                quizClient.getQuizById(attempt.getQuizId());

        if (quiz == null) {
            throw new IllegalStateException(
                    "Quiz-Service returned an empty response");
        }

        if (quiz.getTimeLimit() == null
                || quiz.getTimeLimit() <= 0) {

            throw new IllegalStateException(
                    "Invalid quiz time limit"
            );
        }

        // 6. Calculate expiry time
        Instant expiryTime =
                attempt.getStartTime()
                        .plus(
                                Duration.ofMinutes(
                                        quiz.getTimeLimit()
                                )
                        );

        Instant now = Instant.now();

        // 7. Store client answers
        attempt.setAnswers(answers);

        // 8. Determine completion status
        if (!now.isBefore(expiryTime)) {

            attempt.setStatus(AttemptStatus.EXPIRED);

            log.info(
                    "Attempt {} expired before submission",
                    attemptId
            );

        } else {

            attempt.setStatus(AttemptStatus.SUBMITTED);

            log.info(
                    "Attempt {} submitted successfully",
                    attemptId
            );
        }

        // 9. Completion timestamp
        attempt.setSubmittedAt(now);

        // 10. Save
        quizAttemptRepository.save(attempt);

        log.info(
                "Attempt {} completed with status {}",
                attemptId,
                attempt.getStatus()
        );
    }
}
