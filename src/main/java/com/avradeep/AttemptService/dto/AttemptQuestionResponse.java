package com.avradeep.AttemptService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttemptQuestionResponse {
    private String questionId;

    private String questionText;

    private List<String> options;
}
