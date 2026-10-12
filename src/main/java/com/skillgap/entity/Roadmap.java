package com.skillgap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One step of a student's personalised learning roadmap. */
@Entity
@Table(name = "roadmaps")
@Getter @Setter @NoArgsConstructor
public class Roadmap {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "priority_score", nullable = false)
    private double priorityScore;

    @Column(nullable = false)
    private boolean completed = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
