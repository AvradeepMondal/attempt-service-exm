package com.avradeep.AttemptService.repository;

import com.avradeep.AttemptService.entity.AttemptStatus;
import com.avradeep.AttemptService.entity.QuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Optional;

public interface QuizAttemptRepository
        extends MongoRepository<QuizAttempt, String> {

    // Find currently active attempt
    Optional<QuizAttempt> findTopByUserIdAndQuizIdAndStatusOrderByStartTimeDesc(
            String userId,
            String quizId,
            AttemptStatus status
    );

    // Find latest completed attempt
    Optional<QuizAttempt> findTopByUserIdAndQuizIdAndStatusInOrderBySubmittedAtDesc(
            String userId,
            String quizId,
            Collection<AttemptStatus> statuses
    );
}
