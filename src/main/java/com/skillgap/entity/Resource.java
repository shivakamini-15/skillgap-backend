package com.skillgap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resources")
@Getter @Setter @NoArgsConstructor
public class Resource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "resource_type")
    private String type;
}
