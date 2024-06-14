package dev.edigonzales.simplify;

import dev.edigonzales.statistics.StatisticsResponse;

public record SimplifyResponse(String simplifiedText, StatisticsResponse sourceStatistics, StatisticsResponse targetStatistics) {}
