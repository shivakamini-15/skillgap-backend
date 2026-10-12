package com.skillgap.repository;

import com.skillgap.entity.RoleSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleSkillRepository extends JpaRepository<RoleSkill, Long> {
    List<RoleSkill> findByCareerRoleId(Long roleId);
    Optional<RoleSkill> findByCareerRoleIdAndSkillId(Long roleId, Long skillId);
    void deleteByCareerRoleId(Long roleId);
}
