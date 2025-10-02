package com.phamtruong.rookbooks.service;

import com.phamtruong.rookbooks.entity.Book;

import java.util.List;

public interface RecommendationService {

    List<Book> getRecommendationsForUser(Long userId, int limit);

    List<Book> getSimilarBooks(Long bookId, int limit);

}
