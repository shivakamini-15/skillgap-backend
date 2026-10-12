package com.skillgap.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/** All request/response objects in one place (Java records). */
public final class Dtos {
    private Dtos() {}

    // ----- auth -----
    public record RegisterRequest(
            @NotBlank(message = "Full name is required") String fullName,
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
            String education,
            String careerInterests) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record AuthResponse(String token, Long id, String fullName, String email, String role) {}

    // ----- profile -----
    public record ProfileDto(Long id, String fullName, String email, String education,
                             String careerInterests, Long targetRoleId, String targetRoleTitle) {}

    public record ProfileUpdate(@NotBlank String fullName, String education, String careerInterests) {}

    // ----- catalog -----
    public record SkillDto(Long id, String name, String category, List<Long> prerequisiteIds) {}

    public record SkillRequest(@NotBlank(message = "Skill name is required") String name,
                               String category, List<Long> prerequisiteIds) {}

    public record RoleSkillDto(Long skillId, String skillName, double weight, int requiredLevel) {}

    public record RoleDto(Long id, String title, String description, List<RoleSkillDto> skills) {}

    public record RoleRequest(@NotBlank(message = "Role title is required") String title, String description) {}

    public record RoleSkillRequest(@NotNull Long skillId,
                                   @DecimalMin("0.1") @DecimalMax("10") double weight,
                                   @Min(1) @Max(5) int requiredLevel) {}

    public record ResourceRequest(@NotNull Long skillId, @NotBlank String title,
                                  @NotBlank String url, String type) {}

    public record ResourceDto(Long id, Long skillId, String skillName, String title, String url, String type) {}

    // ----- student skills -----
    public record StudentSkillRequest(@NotNull Long skillId, @Min(1) @Max(5) int level) {}

    public record StudentSkillDto(Long skillId, String skillName, String category, int level) {}

    // ----- analysis -----
    public record GapItem(Long skillId, String skillName, int requiredLevel, int currentLevel,
                          double weight, int gap, String status) {}

    public record GapReport(Long roleId, String roleTitle, double readinessScore, double alignmentScore,
                            int missingCount, int weakCount, int strongCount, List<GapItem> items) {}

    public record AlignmentItem(Long roleId, String roleTitle, double alignmentScore, double readinessScore) {}

    // ----- roadmap / progress -----
    public record RoadmapStepDto(Long id, int stepOrder, Long skillId, String skillName, double priorityScore,
                                 boolean completed, List<String> prerequisites, List<ResourceDto> resources) {}

    public record SnapshotDto(Long id, String roleTitle, double readinessScore, double alignmentScore,
                              LocalDateTime createdAt) {}

    public record ProgressDto(int completedSteps, int totalSteps, double firstReadiness,
                              double latestReadiness, List<SnapshotDto> history) {}

    // ----- admin analytics -----
    public record AnalyticsItem(String skillName, long studentsLacking, double averageLevel) {}
}
