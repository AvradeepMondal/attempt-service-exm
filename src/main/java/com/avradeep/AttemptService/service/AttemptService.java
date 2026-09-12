package com.avradeep.AttemptService.service;

import com.avradeep.AttemptService.dto.StartAttemptResponse;
import java.util.Map;

public interface AttemptService {

    StartAttemptResponse startAttempt(String userId, String quizId);

    void submitAttempt(
            String userId,
            String attemptId,
            Map<String, Integer> answers
    );
}
