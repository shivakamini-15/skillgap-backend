package com.skillgap.controller;

import com.skillgap.dto.Dtos.*;
import com.skillgap.security.CurrentUser;
import com.skillgap.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final CurrentUser currentUser;
    private final StudentService studentService;
    private final GapAnalysisService gapService;
    private final AlignmentService alignmentService;
    private final RoadmapService roadmapService;
    private final ProgressService progressService;

    // ----- profile -----
    @GetMapping("/profile")
    public ProfileDto profile() { return studentService.profile(currentUser.get()); }

    @PutMapping("/profile")
    public ProfileDto updateProfile(@Valid @RequestBody ProfileUpdate req) {
        return studentService.updateProfile(currentUser.get(), req);
    }

    @PutMapping("/target-role/{roleId}")
    public ProfileDto setTarget(@PathVariable Long roleId) {
        return studentService.setTargetRole(currentUser.get(), roleId);
    }

    // ----- my skills -----
    @GetMapping("/skills")
    public List<StudentSkillDto> skills() { return studentService.skills(currentUser.get()); }

    @PostMapping("/skills")
    public List<StudentSkillDto> saveSkill(@Valid @RequestBody StudentSkillRequest req) {
        return studentService.saveSkill(currentUser.get(), req);
    }

    @DeleteMapping("/skills/{skillId}")
    public void removeSkill(@PathVariable Long skillId) {
        studentService.removeSkill(currentUser.get(), skillId);
    }

    // ----- analysis -----
    @GetMapping("/analysis/gap")
    public GapReport gap() { return gapService.report(currentUser.get()); }

    @PostMapping("/analysis/run")
    public GapReport run() { return progressService.runAndSave(currentUser.get()); }

    @GetMapping("/alignment")
    public List<AlignmentItem> alignment() { return alignmentService.rank(currentUser.get()); }

    // ----- roadmap -----
    @PostMapping("/roadmap/generate")
    public List<RoadmapStepDto> generateRoadmap() { return roadmapService.generate(currentUser.get()); }

    @GetMapping("/roadmap")
    public List<RoadmapStepDto> roadmap() { return roadmapService.list(currentUser.get()); }

    @PutMapping("/roadmap/{stepId}/complete")
    public RoadmapStepDto complete(@PathVariable Long stepId) {
        return roadmapService.complete(currentUser.get(), stepId);
    }

    // ----- progress -----
    @GetMapping("/progress")
    public ProgressDto progress() { return progressService.progress(currentUser.get()); }
}
