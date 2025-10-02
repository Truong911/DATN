package com.phamtruong.rookbooks.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phamtruong.rookbooks.config.OpenAIConfig;
import com.phamtruong.rookbooks.dto.chat.*;
import com.phamtruong.rookbooks.entity.Book;
import com.phamtruong.rookbooks.repository.BookRepository;
import com.phamtruong.rookbooks.service.ChatbotService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ChatbotServiceImpl implements ChatbotService {

    RestTemplate restTemplate;
    OpenAIConfig openAIConfig;
    BookRepository bookRepository;

    private static final Logger logger = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";

    @Override
    public ChatbotResponse processMessage(String message) {
        try {
            String systemPrompt = "You are a helpful assistant for a bookstore called 'Ann Books'. " +
                    "Your job is to recommend books to customers based on their queries. " +
                    "Respond with a short helpful answer recommending relevant books, then add a list of keywords " +
                    "to be used for searching books in our database. " +
                    "IMPORTANT: Format keywords in a new line at the end using EXACTLY this format:\n" +
                    "KEYWORDS: keyword1, keyword2, keyword3\n" +
                    "Do not include anything else after the keywords line.";

            GeminiRequest geminiRequest = new GeminiRequest(List.of(
                    new GeminiContent(List.of(new GeminiPart(systemPrompt)), "user"),
                    new GeminiContent(List.of(new GeminiPart(message)), "user")
            ));


            // Ensure proper serialization and validate the payload size
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(geminiRequest);

// Validate the size of the request body
            if (requestBody.length() > 10000) { // Example size limit, adjust as needed
                throw new IllegalArgumentException("Request payload is too large");
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            String url = UriComponentsBuilder.fromHttpUrl(GEMINI_API_URL)
                    .queryParam("key", openAIConfig.getOpenaiApiKey())
                    .toUriString();

            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(url, entity, GeminiResponse.class);

            if (response.getBody() != null && response.getBody().getCandidates() != null && !response.getBody().getCandidates().isEmpty()) {
                String aiResponse = response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();

                List<String> keywords = extractKeywords(aiResponse);
                String cleanResponse = removeKeywordsSection(aiResponse);

                List<Book> recommendedBooks = getBooksByCategory(message);

                if (recommendedBooks.isEmpty()) {
                    recommendedBooks = searchBooksByKeywords(keywords);
                }

                if (recommendedBooks.isEmpty()) {
                    if (containsCategoryRequest(message)) {
                        return new ChatbotResponse(
                                "Xin lỗi, tôi không tìm thấy sách nào phù hợp với thể loại bạn yêu cầu. " +
                                        "Có thể chúng tôi chưa có sách thuộc thể loại này hoặc bạn có thể thử tìm với từ khóa khác. " +
                                        cleanResponse + "\n\nDưới đây là một số sách phổ biến bạn có thể quan tâm:",
                                getPopularBooks(5)
                        );
                    } else {
                        return new ChatbotResponse(
                                "Xin lỗi, tôi không tìm thấy sách nào phù hợp với yêu cầu của bạn. " +
                                        cleanResponse + "\n\nBạn có thể thử tìm với từ khóa khác hoặc xem một số sách phổ biến dưới đây:",
                                getPopularBooks(5)
                        );
                    }
                }

                return new ChatbotResponse(cleanResponse, recommendedBooks);
            }

            return getFallbackResponse(message);

        } catch (HttpClientErrorException e) {
            logger.error("Gemini API error: {}", e.getMessage());

            if (e.getRawStatusCode() == 429) {
                logger.error("Gemini API quota exceeded");
                return getQuotaExceededResponse(message);
            }

            return getFallbackResponse(message);
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
            return getFallbackResponse(message);
        }
    }

    private boolean containsCategoryRequest(String message) {
        String lowercaseMsg = message.toLowerCase();
        return lowercaseMsg.contains("thể loại") ||
                lowercaseMsg.contains("loại sách") ||
                lowercaseMsg.contains("genre") ||
                lowercaseMsg.contains("chủ đề");
    }

    private List<Book> getBooksByCategory(String message) {
        // Try to find a category that matches the message
        List<Book> allBooks = bookRepository.findAllByActiveFlag(true);
        List<String> categoryNames = allBooks.stream()
                .filter(book -> book.getCategory() != null)
                .map(book -> book.getCategory().getName().toLowerCase())
                .distinct()
                .collect(Collectors.toList());

        String lowercaseMsg = message.toLowerCase();

        for (String categoryName : categoryNames) {
            if (lowercaseMsg.contains(categoryName)) {
                return allBooks.stream()
                        .filter(book -> book.getCategory() != null &&
                                book.getCategory().getName().toLowerCase().equals(categoryName))
                        .limit(5)
                        .collect(Collectors.toList());
            }
        }

        // Check for common genre keywords if no category match
        Map<String, List<String>> genreKeywords = new HashMap<>();
        genreKeywords.put("tiểu thuyết", Arrays.asList("novel", "fiction", "tiểu thuyết"));
        genreKeywords.put("truyện", Arrays.asList("story", "truyện", "tales"));
        genreKeywords.put("khoa học", Arrays.asList("science", "khoa học", "scientific"));
        genreKeywords.put("lịch sử", Arrays.asList("history", "lịch sử", "historical"));
        genreKeywords.put("kinh doanh", Arrays.asList("business", "kinh doanh", "marketing"));
        genreKeywords.put("tâm lý", Arrays.asList("psychology", "tâm lý", "mental"));
        genreKeywords.put("self-help", Arrays.asList("self-help", "phát triển bản thân", "self improvement"));
        genreKeywords.put("thiếu nhi", Arrays.asList("children", "thiếu nhi", "kids", "trẻ em"));

        for (Map.Entry<String, List<String>> entry : genreKeywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowercaseMsg.contains(keyword)) {
                    // Search for books with this genre keyword
                    List<Book> books = searchBooksByKeywords(Collections.singletonList(entry.getKey()));
                    if (!books.isEmpty()) {
                        return books;
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    private ChatbotResponse getQuotaExceededResponse(String message) {
        String response = "Xin lỗi, hệ thống trợ lý sách của chúng tôi đang tạm thời quá tải. " +
                "Tôi sẽ cố gắng tìm một số sách phù hợp với yêu cầu của bạn dựa trên từ khóa.";

        List<String> keywords = extractKeywordsFromUserMessage(message);

        List<Book> recommendedBooks = searchBooksByKeywords(keywords);

        return new ChatbotResponse(response, recommendedBooks);
    }

    private ChatbotResponse getFallbackResponse(String message) {
        String response = "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. " +
                "Dưới đây là một số sách phổ biến mà bạn có thể quan tâm:";

        List<String> keywords = extractKeywordsFromUserMessage(message);

        List<Book> recommendedBooks;
        if (keywords.isEmpty()) {
            recommendedBooks = getPopularBooks(5);
        } else {
            recommendedBooks = searchBooksByKeywords(keywords);

            // If no books found with keywords, get popular books
            if (recommendedBooks.isEmpty()) {
                recommendedBooks = getPopularBooks(5);
            }
        }

        return new ChatbotResponse(response, recommendedBooks);
    }

    private List<Book> getPopularBooks(int limit) {
        return bookRepository.findByActiveFlagOrderByBuyCountDesc(true);
    }

    private List<String> extractKeywordsFromUserMessage(String message) {

        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "a", "an", "the", "and", "or", "but", "is", "are", "was", "were",
                "be", "been", "being", "have", "has", "had", "do", "does", "did",
                "to", "at", "by", "for", "with", "about", "against", "between", "into",
                "through", "during", "before", "after", "above", "below", "from", "up",
                "down", "in", "out", "on", "off", "over", "under", "again", "further",
                "then", "once", "here", "there", "when", "where", "why", "how", "all",
                "any", "both", "each", "few", "more", "most", "other", "some", "such",
                "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very",
                "can", "will", "just", "should", "now", "tôi", "bạn", "anh", "chị", "của",
                "và", "hoặc", "nhưng", "là", "có", "không", "đã", "sẽ", "đang", "cần",
                "muốn", "thích", "yêu", "ghét", "cho", "với", "về", "từ", "đến", "trong",
                "ngoài", "trên", "dưới", "khi", "nếu", "vì", "bởi", "tại", "sao", "làm",
                "gì", "ai", "hỏi", "trả lời", "giúp", "xin", "vui lòng", "cảm ơn", "xin chào",
                "tạm biệt", "gợi ý", "đề xuất", "sách", "cuốn", "quyển", "đọc", "mua", "bán"
        ));

        return Arrays.stream(message.toLowerCase().split("\\s+"))
                .filter(word -> !stopWords.contains(word) && word.length() > 2)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchBooksByKeywords(List<String> keywords) {
        if (keywords.isEmpty()) {
            return new ArrayList<>();
        }

        List<Book> allBooks = bookRepository.findAllByActiveFlag(true);

        return allBooks.stream()
                .filter(book -> matchesKeywords(book, keywords))
                .limit(5)
                .collect(Collectors.toList());
    }

    private boolean matchesKeywords(Book book, List<String> keywords) {
        String bookInfo = (book.getTitle() + " " +
                book.getAuthor() + " " +
                book.getPublisher() + " " +
                (book.getCategory() != null ? book.getCategory().getName() : "") + " " +
                book.getDescription()).toLowerCase();

        for (String keyword : keywords) {
            if (bookInfo.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private List<String> extractKeywords(String content) {
        Pattern pattern = Pattern.compile("(?i)KEYWORDS:\\s*(.*)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String[] split = matcher.group(1).split(",");
            return Arrays.stream(split).map(String::trim).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String removeKeywordsSection(String response) {
        return response.replaceAll("(?i)KEYWORDS:.*", "").trim();
    }
}
