package com.skillgap.repository;

import com.skillgap.entity.StudentSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, Long> {
    List<StudentSkill> findByUserId(Long userId);
    Optional<StudentSkill> findByUserIdAndSkillId(Long userId, Long skillId);
}
