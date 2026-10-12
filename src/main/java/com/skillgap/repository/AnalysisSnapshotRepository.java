package com.skillgap.repository;

import com.skillgap.entity.AnalysisSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisSnapshotRepository extends JpaRepository<AnalysisSnapshot, Long> {
    List<AnalysisSnapshot> findByUserIdOrderByCreatedAtAsc(Long userId);
}
