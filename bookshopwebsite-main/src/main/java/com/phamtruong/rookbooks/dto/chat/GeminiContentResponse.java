package com.phamtruong.rookbooks.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeminiContentResponse extends GeminiContent{
    private String role;
}
