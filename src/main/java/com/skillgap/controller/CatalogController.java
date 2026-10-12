package com.skillgap.controller;

import com.skillgap.dto.Dtos.*;
import com.skillgap.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Readable by any logged-in user (students, mentors, admins). */
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    @GetMapping("/skills")
    public List<SkillDto> skills() { return catalogService.skills(); }

    @GetMapping("/roles")
    public List<RoleDto> roles() { return catalogService.roles(); }
}
