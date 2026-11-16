package com.airwise.backend.http;

import com.airwise.backend.flightsearch.application.ProviderIssue;

public record ProviderIssueView(String code, String message) {
    public static ProviderIssueView from(ProviderIssue issue) {
        return new ProviderIssueView(issue.code().name(), ProviderIssue.of(issue.code()).message());
    }
}
