package com.burnmetrix.dashboard.quote;

public record QuoteResponse(
        String quote,
        String author,
        String provider,
        String attribution) {
}
