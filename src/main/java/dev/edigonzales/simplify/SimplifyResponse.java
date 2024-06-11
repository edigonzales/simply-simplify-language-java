package dev.edigonzales.simplify;

import dev.edigonzales.service.AnalyzeResponse;

public record SimplifyResponse(String simplifiedText, AnalyzeResponse analyzeResponse) {}
