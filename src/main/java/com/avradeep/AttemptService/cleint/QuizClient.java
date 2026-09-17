package com.avradeep.AttemptService.cleint;

import com.avradeep.AttemptService.dto.QuizResponse;
import com.avradeep.AttemptService.security.FeignTokenPropagationConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "QUIZ-SERVICE",
        configuration = FeignTokenPropagationConfig.class
)
public interface QuizClient {

    @GetMapping("/api/quiz/{quizId}")
    QuizResponse getQuizById(
            @PathVariable("quizId") String quizId
    );
}
