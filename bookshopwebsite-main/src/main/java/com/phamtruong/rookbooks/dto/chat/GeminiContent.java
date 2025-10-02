package com.phamtruong.rookbooks.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeminiContent {
    private List<GeminiPart> parts;
    private String role;
}
