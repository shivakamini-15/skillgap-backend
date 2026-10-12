package com.skillgap.service;

import com.skillgap.dto.Dtos.*;
import com.skillgap.entity.CareerRole;
import com.skillgap.entity.User;
import com.skillgap.repository.CareerRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Ranks every career role by cosine alignment with the student's skill vector. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlignmentService {
    private final CareerRoleRepository roleRepository;
    private final GapAnalysisService gapService;

    public List<AlignmentItem> rank(User user) {
        Map<Long, Integer> levels = gapService.levels(user.getId());
        List<AlignmentItem> ranking = new ArrayList<>();
        for (CareerRole role : roleRepository.findAll()) {
            GapReport r = gapService.analyse(levels, role);
            ranking.add(new AlignmentItem(role.getId(), role.getTitle(), r.alignmentScore(), r.readinessScore()));
        }
        ranking.sort(Comparator.comparingDouble(AlignmentItem::alignmentScore).reversed());
        return ranking;
    }
}
