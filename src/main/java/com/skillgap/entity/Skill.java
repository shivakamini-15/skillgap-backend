package com.skillgap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
@Getter @Setter @NoArgsConstructor
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String category;

    /** Skills that should be learned before this one (used for roadmap ordering). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "skill_prerequisites",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id"))
    private Set<Skill> prerequisites = new HashSet<>();
}
