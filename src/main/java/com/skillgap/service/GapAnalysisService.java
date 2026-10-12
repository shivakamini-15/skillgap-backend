package com.skillgap.service;

import com.skillgap.dto.Dtos.*;
import com.skillgap.entity.CareerRole;
import com.skillgap.entity.RoleSkill;
import com.skillgap.entity.StudentSkill;
import com.skillgap.entity.User;
import com.skillgap.exception.BadRequestException;
import com.skillgap.repository.RoleSkillRepository;
import com.skillgap.repository.StudentSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Core analysis engine.
 *
 * Student vector  s[k] = proficiency (0-5) of skill k.
 * Role vector     r[k] = weight(k) * requiredLevel(k)  (0 when the role does not need skill k).
 *
 * Readiness  = sum( w_k * min(s_k, req_k) / req_k ) / sum( w_k )  * 100
 * Alignment  = cosine(s, r) = (s . r) / (|s| |r|)                   * 100
 * Gap status = MISSING (s=0), WEAK (0 < s < req), STRONG (s >= req)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GapAnalysisService {
    private final RoleSkillRepository roleSkillRepository;
    private final StudentSkillRepository studentSkillRepository;

    /** skillId -> proficiency for one student. */
    public Map<Long, Integer> levels(Long userId) {
        Map<Long, Integer> map = new HashMap<>();
        for (StudentSkill ss : studentSkillRepository.findByUserId(userId)) {
            map.put(ss.getSkill().getId(), ss.getLevel());
        }
        return map;
    }

    /** Report for the student's currently selected target role. */
    public GapReport report(User user) {
        CareerRole role = user.getTargetRole();
        if (role == null) {
            throw new BadRequestException("Choose a target career role first");
        }
        return analyse(levels(user.getId()), role);
    }

    public GapReport analyse(Map<Long, Integer> levels, CareerRole role) {
        List<RoleSkill> requirements = roleSkillRepository.findByCareerRoleId(role.getId());
        List<GapItem> items = new ArrayList<>();
        double totalWeight = 0, earned = 0;
        int missing = 0, weak = 0, strong = 0;

        for (RoleSkill rs : requirements) {
            int current = levels.getOrDefault(rs.getSkill().getId(), 0);
            int required = rs.getRequiredLevel();
            double w = rs.getWeight();
            totalWeight += w;
            earned += w * Math.min(current, required) / (double) required;

            String status;
            if (current == 0) { status = "MISSING"; missing++; }
            else if (current < required) { status = "WEAK"; weak++; }
            else { status = "STRONG"; strong++; }

            items.add(new GapItem(rs.getSkill().getId(), rs.getSkill().getName(), required, current,
                    w, Math.max(0, required - current), status));
        }
        // biggest weighted gaps first
        items.sort((a, b) -> Double.compare(b.weight() * b.gap(), a.weight() * a.gap()));

        double readiness = totalWeight == 0 ? 0 : round2(earned / totalWeight * 100);
        double alignment = round2(cosine(levels, requirements) * 100);
        return new GapReport(role.getId(), role.getTitle(), readiness, alignment, missing, weak, strong, items);
    }

    /** Cosine similarity between the student's skill vector and a role's weighted requirement vector. */
    public double cosine(Map<Long, Integer> levels, List<RoleSkill> requirements) {
        double dot = 0, roleNorm = 0, studentNorm = 0;
        for (RoleSkill rs : requirements) {
            double r = rs.getWeight() * rs.getRequiredLevel();
            double s = levels.getOrDefault(rs.getSkill().getId(), 0);
            dot += s * r;
            roleNorm += r * r;
        }
        for (int v : levels.values()) {
            studentNorm += (double) v * v;
        }
        if (studentNorm == 0 || roleNorm == 0) return 0;
        return dot / (Math.sqrt(studentNorm) * Math.sqrt(roleNorm));
    }

    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
