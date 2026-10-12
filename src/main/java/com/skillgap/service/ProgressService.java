package com.skillgap.service;

import com.skillgap.dto.Dtos.*;
import com.skillgap.entity.AnalysisSnapshot;
import com.skillgap.entity.Roadmap;
import com.skillgap.entity.User;
import com.skillgap.repository.AnalysisSnapshotRepository;
import com.skillgap.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressService {
    private final GapAnalysisService gapService;
    private final AnalysisSnapshotRepository snapshotRepository;
    private final RoadmapRepository roadmapRepository;

    /** Runs the analysis for the target role and stores the scores so progress can be charted. */
    public GapReport runAndSave(User user) {
        GapReport report = gapService.report(user);
        AnalysisSnapshot s = new AnalysisSnapshot();
        s.setUser(user);
        s.setCareerRole(user.getTargetRole());
        s.setReadinessScore(report.readinessScore());
        s.setAlignmentScore(report.alignmentScore());
        snapshotRepository.save(s);
        return report;
    }

    @Transactional(readOnly = true)
    public ProgressDto progress(User user) {
        List<SnapshotDto> history = snapshotRepository.findByUserIdOrderByCreatedAtAsc(user.getId()).stream()
                .map(s -> new SnapshotDto(s.getId(), s.getCareerRole().getTitle(),
                        s.getReadinessScore(), s.getAlignmentScore(), s.getCreatedAt()))
                .toList();
        List<Roadmap> steps = roadmapRepository.findByUserIdOrderByStepOrderAsc(user.getId());
        int done = (int) steps.stream().filter(Roadmap::isCompleted).count();
        double first = history.isEmpty() ? 0 : history.get(0).readinessScore();
        double latest = history.isEmpty() ? 0 : history.get(history.size() - 1).readinessScore();
        return new ProgressDto(done, steps.size(), first, latest, history);
    }
}
