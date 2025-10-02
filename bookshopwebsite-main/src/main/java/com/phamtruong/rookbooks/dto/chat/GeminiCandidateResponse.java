package com.phamtruong.rookbooks.dto.chat;

import lombok.Data;

@Data
public class GeminiCandidateResponse {
    private GeminiContentResponse content;
    private String finishReason;
    private Integer index;
}
