package com.skillgap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"}))
@Getter @Setter @NoArgsConstructor
public class StudentSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    /** Proficiency 1 (beginner) to 5 (expert). */
    @Column(name = "skill_level", nullable = false)
    private int level;
}
