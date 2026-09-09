package com.avradeep.AttemptService.repository;

import com.avradeep.AttemptService.entity.AttemptStatus;
import com.avradeep.AttemptService.entity.QuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuizAttemptRepository
        extends MongoRepository<QuizAttempt, String> {

    Optional<QuizAttempt> findTopByUserIdAndQuizIdAndStatusOrderByStartTimeDesc(
            String userId,
            String quizId,
            AttemptStatus status
    );
}
