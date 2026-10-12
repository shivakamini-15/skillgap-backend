package com.skillgap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One required skill of a career role: importance weight (0.1-10) and required proficiency (1-5). */
@Entity
@Table(name = "role_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"career_role_id", "skill_id"}))
@Getter @Setter @NoArgsConstructor
public class RoleSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "career_role_id")
    private CareerRole careerRole;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(nullable = false)
    private double weight;

    @Column(name = "required_level", nullable = false)
    private int requiredLevel;
}
