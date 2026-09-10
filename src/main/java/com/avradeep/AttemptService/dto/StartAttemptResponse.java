package com.avradeep.AttemptService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StartAttemptResponse {
    private String attemptId;

    private String quizId;

    private Instant startTime;

    private Instant expiryTime;

    private List<AttemptQuestionResponse> questions;
}
