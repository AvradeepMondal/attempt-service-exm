package com.avradeep.AttemptService.controller;

import com.avradeep.AttemptService.dto.StartAttemptResponse;
import com.avradeep.AttemptService.dto.SubmitAttemptRequest;
import com.avradeep.AttemptService.service.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptService attemptService;

    private String getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getName();
    }

    @PostMapping("/start/{quizId}")
    public ResponseEntity<StartAttemptResponse> startAttempt(
            @PathVariable String quizId) {

        String userId = getCurrentUserId();

        StartAttemptResponse response =
                attemptService.startAttempt(
                        userId,
                        quizId
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<Void> submitAttempt(
            @PathVariable String attemptId,
            @RequestBody SubmitAttemptRequest request) {

        String userId = getCurrentUserId();

        attemptService.submitAttempt(
                userId,
                attemptId,
                request.getAnswers()
        );

        return ResponseEntity.ok().build();
    }

}
