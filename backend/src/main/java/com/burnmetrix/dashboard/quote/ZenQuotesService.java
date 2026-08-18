package com.burnmetrix.dashboard.quote;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ZenQuotesService implements QuoteService {

    private static final String API_URL = "https://zenquotes.io/api/today";
    private static final String PROVIDER = "ZenQuotes";
    private static final String ATTRIBUTION = "Inspirational quotes provided by ZenQuotes";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private LocalDate cachedDate;
    private QuoteResponse cachedQuote;

    public ZenQuotesService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public synchronized QuoteResponse today() {
        LocalDate today = LocalDate.now();
        if (today.equals(cachedDate) && cachedQuote != null) {
            return cachedQuote;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400 || response.body().isBlank()) {
                return unavailable();
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode first = root.isArray() && !root.isEmpty() ? root.get(0) : objectMapper.createObjectNode();
            String quote = text(first.path("q").asText(""));
            String author = text(first.path("a").asText(""));
            if (quote == null) {
                return unavailable();
            }
            cachedDate = today;
            cachedQuote = new QuoteResponse(quote, author, PROVIDER, ATTRIBUTION);
            return cachedQuote;
        } catch (Exception ignored) {
            return unavailable();
        }
    }

    private static QuoteResponse unavailable() {
        return new QuoteResponse(null, null, PROVIDER, ATTRIBUTION);
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
