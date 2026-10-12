package com.skillgap.repository;

import com.skillgap.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    List<Roadmap> findByUserIdOrderByStepOrderAsc(Long userId);
    void deleteByUserId(Long userId);
}
