package com.ecosphere.dto;
import com.ecosphere.entity.Plantation; import java.util.List;
public record PlantationTracking(Plantation plantation, String currentStatus, List<TrackingStep> timeline) {}
