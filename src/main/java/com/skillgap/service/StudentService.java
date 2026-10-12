package com.skillgap.service;

import com.skillgap.dto.Dtos.*;
import com.skillgap.entity.CareerRole;
import com.skillgap.entity.Skill;
import com.skillgap.entity.StudentSkill;
import com.skillgap.entity.User;
import com.skillgap.exception.ResourceNotFoundException;
import com.skillgap.repository.CareerRoleRepository;
import com.skillgap.repository.SkillRepository;
import com.skillgap.repository.StudentSkillRepository;
import com.skillgap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Student profile, target role and the student's own skill list. */
@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {
    private final UserRepository userRepository;
    private final CareerRoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final StudentSkillRepository studentSkillRepository;

    public ProfileDto profile(User u) {
        CareerRole t = u.getTargetRole();
        return new ProfileDto(u.getId(), u.getFullName(), u.getEmail(), u.getEducation(),
                u.getCareerInterests(), t == null ? null : t.getId(), t == null ? null : t.getTitle());
    }

    public ProfileDto updateProfile(User u, ProfileUpdate req) {
        u.setFullName(req.fullName().trim());
        u.setEducation(req.education());
        u.setCareerInterests(req.careerInterests());
        return profile(userRepository.save(u));
    }

    public ProfileDto setTargetRole(User u, Long roleId) {
        CareerRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Career role not found"));
        u.setTargetRole(role);
        return profile(userRepository.save(u));
    }

    @Transactional(readOnly = true)
    public List<StudentSkillDto> skills(User u) {
        return studentSkillRepository.findByUserId(u.getId()).stream()
                .map(ss -> new StudentSkillDto(ss.getSkill().getId(), ss.getSkill().getName(),
                        ss.getSkill().getCategory(), ss.getLevel()))
                .sorted(Comparator.comparing(StudentSkillDto::skillName))
                .toList();
    }

    public List<StudentSkillDto> saveSkill(User u, StudentSkillRequest req) {
        Skill skill = skillRepository.findById(req.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        StudentSkill ss = studentSkillRepository.findByUserIdAndSkillId(u.getId(), skill.getId())
                .orElseGet(() -> {
                    StudentSkill n = new StudentSkill();
                    n.setUser(u);
                    n.setSkill(skill);
                    return n;
                });
        ss.setLevel(req.level());
        studentSkillRepository.save(ss);
        return skills(u);
    }

    public void removeSkill(User u, Long skillId) {
        studentSkillRepository.findByUserIdAndSkillId(u.getId(), skillId)
                .ifPresent(studentSkillRepository::delete);
    }
}
