package com.ecosphere.dto;
import java.time.LocalDateTime;
public record TrackingStep(String label, boolean done, LocalDateTime at, String note) {}
