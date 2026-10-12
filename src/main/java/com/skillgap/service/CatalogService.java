package com.skillgap.service;

import com.skillgap.dto.Dtos.*;
import com.skillgap.entity.CareerRole;
import com.skillgap.entity.Skill;
import com.skillgap.repository.CareerRoleRepository;
import com.skillgap.repository.RoleSkillRepository;
import com.skillgap.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Read-only view of the skills library and career roles; also holds the entity-to-DTO mappers. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {
    private final SkillRepository skillRepository;
    private final CareerRoleRepository roleRepository;
    private final RoleSkillRepository roleSkillRepository;

    public List<SkillDto> skills() {
        return skillRepository.findAll(Sort.by("name")).stream().map(this::skillDto).toList();
    }

    public List<RoleDto> roles() {
        return roleRepository.findAll(Sort.by("title")).stream().map(this::roleDto).toList();
    }

    public SkillDto skillDto(Skill s) {
        return new SkillDto(s.getId(), s.getName(), s.getCategory(),
                s.getPrerequisites().stream().map(Skill::getId).toList());
    }

    public RoleDto roleDto(CareerRole r) {
        List<RoleSkillDto> skills = roleSkillRepository.findByCareerRoleId(r.getId()).stream()
                .map(rs -> new RoleSkillDto(rs.getSkill().getId(), rs.getSkill().getName(),
                        rs.getWeight(), rs.getRequiredLevel()))
                .sorted((a, b) -> Double.compare(b.weight(), a.weight()))
                .toList();
        return new RoleDto(r.getId(), r.getTitle(), r.getDescription(), skills);
    }
}
