package com.skillgap.service;

import com.skillgap.dto.Dtos.*;
import com.skillgap.entity.*;
import com.skillgap.exception.ResourceNotFoundException;
import com.skillgap.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Roadmap generator.
 * 1. Priority of a gap = weight * (requiredLevel - currentLevel).
 * 2. Gaps are sorted by priority (highest first).
 * 3. For each gap, unmet prerequisites (proficiency < 2) are placed BEFORE it (depth-first),
 *    so the final order respects both importance and "learn this first" dependencies.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RoadmapService {
    private static final int PREREQUISITE_MIN_LEVEL = 2;
    private static final int DEFAULT_TARGET_LEVEL = 3;

    private final GapAnalysisService gapService;
    private final RoadmapRepository roadmapRepository;
    private final SkillRepository skillRepository;
    private final ResourceRepository resourceRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final RoleSkillRepository roleSkillRepository;
    private final ProgressService progressService;

    public List<RoadmapStepDto> generate(User user) {
        GapReport report = gapService.report(user);
        Map<Long, Integer> levels = gapService.levels(user.getId());

        LinkedHashMap<Long, Double> ordered = new LinkedHashMap<>();
        for (GapItem g : report.items()) {                 // already sorted by weight * gap
            if (g.gap() == 0) continue;
            Skill skill = skillRepository.findById(g.skillId()).orElseThrow();
            visit(skill, g.weight() * g.gap(), levels, ordered, new HashSet<>());
        }

        roadmapRepository.deleteByUserId(user.getId());
        roadmapRepository.flush();
        int order = 1;
        for (Map.Entry<Long, Double> e : ordered.entrySet()) {
            Roadmap step = new Roadmap();
            step.setUser(user);
            step.setSkill(skillRepository.getReferenceById(e.getKey()));
            step.setStepOrder(order++);
            step.setPriorityScore(GapAnalysisService.round2(e.getValue()));
            roadmapRepository.save(step);
        }
        return list(user);
    }

    private void visit(Skill skill, double priority, Map<Long, Integer> levels,
                       LinkedHashMap<Long, Double> out, Set<Long> path) {
        if (out.containsKey(skill.getId()) || !path.add(skill.getId())) return;   // done or cycle
        for (Skill pre : skill.getPrerequisites()) {
            if (levels.getOrDefault(pre.getId(), 0) < PREREQUISITE_MIN_LEVEL) {
                visit(pre, priority, levels, out, path);
            }
        }
        out.put(skill.getId(), priority);
    }

    @Transactional(readOnly = true)
    public List<RoadmapStepDto> list(User user) {
        return roadmapRepository.findByUserIdOrderByStepOrderAsc(user.getId()).stream()
                .map(this::toDto).toList();
    }

    public RoadmapStepDto complete(User user, Long stepId) {
        Roadmap step = roadmapRepository.findById(stepId)
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap step not found"));
        step.setCompleted(true);
        roadmapRepository.save(step);

        // Marking a skill as learned lifts proficiency to the role's required level (or 3 for a prerequisite).
        int target = DEFAULT_TARGET_LEVEL;
        if (user.getTargetRole() != null) {
            target = roleSkillRepository
                    .findByCareerRoleIdAndSkillId(user.getTargetRole().getId(), step.getSkill().getId())
                    .map(RoleSkill::getRequiredLevel).orElse(DEFAULT_TARGET_LEVEL);
        }
        StudentSkill ss = studentSkillRepository.findByUserIdAndSkillId(user.getId(), step.getSkill().getId())
                .orElseGet(() -> {
                    StudentSkill n = new StudentSkill();
                    n.setUser(user);
                    n.setSkill(step.getSkill());
                    return n;
                });
        ss.setLevel(Math.max(ss.getLevel(), target));
        studentSkillRepository.save(ss);

        if (user.getTargetRole() != null) {
            progressService.runAndSave(user);      // record the improved scores
        }
        return toDto(step);
    }

    private RoadmapStepDto toDto(Roadmap r) {
        Skill s = r.getSkill();
        List<ResourceDto> resources = resourceRepository.findBySkillId(s.getId()).stream()
                .map(x -> new ResourceDto(x.getId(), s.getId(), s.getName(), x.getTitle(), x.getUrl(), x.getType()))
                .toList();
        List<String> prereqs = s.getPrerequisites().stream().map(Skill::getName).sorted().toList();
        return new RoadmapStepDto(r.getId(), r.getStepOrder(), s.getId(), s.getName(),
                r.getPriorityScore(), r.isCompleted(), prereqs, resources);
    }
}
