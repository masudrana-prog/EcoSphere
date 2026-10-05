package com.ecosphere.dto;
import java.util.Map;
public record CarbonSummary(double totalCo2Kg, Map<String,Double> byCategory, double treesNeededToOffsetPerYear, double offsetByVerifiedTreesKgPerYear, double netFootprintKg) {}
