package com.ecosphere.dto;
public record LeaderboardEntry(int rank, Long userId, String name, String location, int ecoPoints, int greenScore, long verifiedPlantations) {}
