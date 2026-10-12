package com.skillgap.controller;

import com.skillgap.dto.Dtos.*;
import com.skillgap.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin and mentor endpoints (see SecurityConfig: /api/admin/** needs ROLE_ADMIN or ROLE_MENTOR). */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    // ----- roles -----
    @PostMapping("/roles")
    public RoleDto createRole(@Valid @RequestBody RoleRequest req) { return adminService.createRole(req); }

    @PutMapping("/roles/{id}")
    public RoleDto updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest req) {
        return adminService.updateRole(id, req);
    }

    @DeleteMapping("/roles/{id}")
    public void deleteRole(@PathVariable Long id) { adminService.deleteRole(id); }

    @PutMapping("/roles/{id}/skills")
    public RoleDto setRoleSkills(@PathVariable Long id, @Valid @RequestBody List<@Valid RoleSkillRequest> skills) {
        return adminService.setRoleSkills(id, skills);
    }

    // ----- skills -----
    @PostMapping("/skills")
    public SkillDto createSkill(@Valid @RequestBody SkillRequest req) { return adminService.createSkill(req); }

    @PutMapping("/skills/{id}")
    public SkillDto updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequest req) {
        return adminService.updateSkill(id, req);
    }

    @DeleteMapping("/skills/{id}")
    public void deleteSkill(@PathVariable Long id) { adminService.deleteSkill(id); }

    // ----- resources -----
    @GetMapping("/resources")
    public List<ResourceDto> resources() { return adminService.resources(); }

    @PostMapping("/resources")
    public ResourceDto createResource(@Valid @RequestBody ResourceRequest req) { return adminService.createResource(req); }

    @PutMapping("/resources/{id}")
    public ResourceDto updateResource(@PathVariable Long id, @Valid @RequestBody ResourceRequest req) {
        return adminService.updateResource(id, req);
    }

    @DeleteMapping("/resources/{id}")
    public void deleteResource(@PathVariable Long id) { adminService.deleteResource(id); }

    // ----- analytics -----
    @GetMapping("/analytics/gaps")
    public List<AnalyticsItem> gaps() { return adminService.gapAnalytics(); }
}
