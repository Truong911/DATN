package com.phamtruong.rookbooks.service;

import com.phamtruong.rookbooks.dto.chat.ChatbotResponse;
import com.phamtruong.rookbooks.entity.Book;

import java.util.List;

public interface ChatbotService {
    ChatbotResponse processMessage(String message);

    List<Book> searchBooksByKeywords(List<String> keywords);
}
