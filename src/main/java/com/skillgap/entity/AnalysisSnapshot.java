package com.skillgap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Stores each analysis run so improvement over time can be charted. */
@Entity
@Table(name = "analysis_snapshots")
@Getter @Setter @NoArgsConstructor
public class AnalysisSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "career_role_id")
    private CareerRole careerRole;

    @Column(nullable = false)
    private double readinessScore;

    @Column(nullable = false)
    private double alignmentScore;

    private LocalDateTime createdAt = LocalDateTime.now();
}
