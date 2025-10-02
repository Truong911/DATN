package com.phamtruong.rookbooks.dto.chat;

import lombok.Data;

import java.util.List;

@Data
public class GeminiResponse {
    private List<GeminiCandidateResponse> candidates;
    private UsageMetadata usageMetadata;
    private String modelVersion;
    private String responseId;
}

@Data
class UsageMetadata {
    private int promptTokenCount;
    private int candidatesTokenCount;
    private int totalTokenCount;
    private List<PromptTokensDetails> promptTokensDetails;
    private int thoughtsTokenCount;
}

@Data
class PromptTokensDetails {
    private String modality;
    private int tokenCount;
}