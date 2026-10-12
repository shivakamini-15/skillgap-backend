package com.skillgap.service;

import com.skillgap.dto.Dtos.*;
import com.skillgap.entity.*;
import com.skillgap.exception.BadRequestException;
import com.skillgap.exception.ResourceNotFoundException;
import com.skillgap.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Admin / mentor operations: roles, skills, role-skill weights, resources and gap analytics. */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
    private final CareerRoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final RoleSkillRepository roleSkillRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final CatalogService catalog;

    // ---------- career roles ----------
    public RoleDto createRole(RoleRequest req) {
        if (roleRepository.existsByTitleIgnoreCase(req.title().trim())) {
            throw new BadRequestException("A role with this title already exists");
        }
        CareerRole r = new CareerRole();
        r.setTitle(req.title().trim());
        r.setDescription(req.description());
        return catalog.roleDto(roleRepository.save(r));
    }

    public RoleDto updateRole(Long id, RoleRequest req) {
        CareerRole r = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career role not found"));
        r.setTitle(req.title().trim());
        r.setDescription(req.description());
        return catalog.roleDto(roleRepository.save(r));
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) throw new ResourceNotFoundException("Career role not found");
        roleSkillRepository.deleteByCareerRoleId(id);
        roleRepository.deleteById(id);
        roleRepository.flush();     // surfaces a constraint error now if students still target this role
    }

    /** Replaces the full list of required skills (with weights and levels) of one role. */
    public RoleDto setRoleSkills(Long roleId, List<RoleSkillRequest> requests) {
        CareerRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Career role not found"));
        Map<Long, RoleSkillRequest> unique = new LinkedHashMap<>();
        for (RoleSkillRequest q : requests) unique.put(q.skillId(), q);

        roleSkillRepository.deleteByCareerRoleId(roleId);
        roleSkillRepository.flush();
        for (RoleSkillRequest q : unique.values()) {
            Skill skill = skillRepository.findById(q.skillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill " + q.skillId() + " not found"));
            RoleSkill rs = new RoleSkill();
            rs.setCareerRole(role);
            rs.setSkill(skill);
            rs.setWeight(q.weight());
            rs.setRequiredLevel(q.requiredLevel());
            roleSkillRepository.save(rs);
        }
        return catalog.roleDto(role);
    }

    // ---------- skills ----------
    public SkillDto createSkill(SkillRequest req) {
        if (skillRepository.existsByNameIgnoreCase(req.name().trim())) {
            throw new BadRequestException("This skill already exists");
        }
        Skill s = new Skill();
        apply(s, req);
        return catalog.skillDto(skillRepository.save(s));
    }

    public SkillDto updateSkill(Long id, SkillRequest req) {
        Skill s = skillRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        apply(s, req);
        return catalog.skillDto(skillRepository.save(s));
    }

    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) throw new ResourceNotFoundException("Skill not found");
        skillRepository.deleteById(id);
        skillRepository.flush();
    }

    private void apply(Skill s, SkillRequest req) {
        s.setName(req.name().trim());
        s.setCategory(req.category());
        Set<Skill> prereqs = new HashSet<>();
        if (req.prerequisiteIds() != null) {
            for (Long pid : req.prerequisiteIds()) {
                if (pid.equals(s.getId())) continue;       // a skill cannot require itself
                prereqs.add(skillRepository.findById(pid)
                        .orElseThrow(() -> new ResourceNotFoundException("Prerequisite skill not found")));
            }
        }
        s.setPrerequisites(prereqs);
    }

    // ---------- resources ----------
    @Transactional(readOnly = true)
    public List<ResourceDto> resources() {
        return resourceRepository.findAll().stream().map(this::resourceDto)
                .sorted(Comparator.comparing(ResourceDto::skillName)).toList();
    }

    public ResourceDto createResource(ResourceRequest req) {
        Resource r = new Resource();
        return saveResource(r, req);
    }

    public ResourceDto updateResource(Long id, ResourceRequest req) {
        Resource r = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        return saveResource(r, req);
    }

    public void deleteResource(Long id) {
        if (!resourceRepository.existsById(id)) throw new ResourceNotFoundException("Resource not found");
        resourceRepository.deleteById(id);
    }

    private ResourceDto saveResource(Resource r, ResourceRequest req) {
        r.setSkill(skillRepository.findById(req.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found")));
        r.setTitle(req.title().trim());
        r.setUrl(req.url().trim());
        r.setType(req.type());
        return resourceDto(resourceRepository.save(r));
    }

    private ResourceDto resourceDto(Resource r) {
        return new ResourceDto(r.getId(), r.getSkill().getId(), r.getSkill().getName(),
                r.getTitle(), r.getUrl(), r.getType());
    }

    // ---------- analytics: which required skills are most often below the required level ----------
    @Transactional(readOnly = true)
    public List<AnalyticsItem> gapAnalytics() {
        Map<String, double[]> stats = new HashMap<>();      // name -> {lacking, levelSum, count}
        for (User student : userRepository.findByRole(Role.STUDENT)) {
            if (student.getTargetRole() == null) continue;
            Map<Long, Integer> levels = new HashMap<>();
            for (StudentSkill ss : studentSkillRepository.findByUserId(student.getId())) {
                levels.put(ss.getSkill().getId(), ss.getLevel());
            }
            for (RoleSkill rs : roleSkillRepository.findByCareerRoleId(student.getTargetRole().getId())) {
                int level = levels.getOrDefault(rs.getSkill().getId(), 0);
                double[] a = stats.computeIfAbsent(rs.getSkill().getName(), k -> new double[3]);
                if (level < rs.getRequiredLevel()) a[0]++;
                a[1] += level;
                a[2]++;
            }
        }
        return stats.entrySet().stream()
                .map(e -> new AnalyticsItem(e.getKey(), (long) e.getValue()[0],
                        GapAnalysisService.round2(e.getValue()[1] / e.getValue()[2])))
                .sorted(Comparator.comparingLong(AnalyticsItem::studentsLacking).reversed())
                .limit(10).toList();
    }
}
