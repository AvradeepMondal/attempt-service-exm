package com.avradeep.AttemptService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {
    @Id
    private String id;

    private String userId;

    private String quizId;

    private Instant startTime;

    private Instant submittedAt;

    private AttemptStatus status;

    private Map<String, Integer> answers; //answers selected by the client

    private List<QuestionSnapshot> questionSnapshot; //correct answer of each questions
}
