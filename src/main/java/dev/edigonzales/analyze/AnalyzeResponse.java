package dev.edigonzales.analyze;

import dev.edigonzales.statistics.StatisticsResponse;

public record AnalyzeResponse(String analyzeText, StatisticsResponse sourceStatistics) {}
