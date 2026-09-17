package com.avradeep.AttemptService.cleint;

import com.avradeep.AttemptService.dto.QuestionEvaluationResponse;
import com.avradeep.AttemptService.security.FeignTokenPropagationConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "QUESTION-SERVICE",
        configuration = FeignTokenPropagationConfig.class
)
public interface QuestionClient {

    @GetMapping("/api/questions/{quizId}/evaluation")
    List<QuestionEvaluationResponse> getQuestionsForEvaluation(
            @PathVariable("quizId") String quizId
    );
}
