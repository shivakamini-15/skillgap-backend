package com.skillgap.config;

import com.skillgap.entity.*;
import com.skillgap.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/** Seeds demo users, skills, career roles and learning resources on first start. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final SkillRepository skills;
    private final CareerRoleRepository roles;
    private final RoleSkillRepository roleSkills;
    private final ResourceRepository resources;
    private final StudentSkillRepository studentSkills;
    private final PasswordEncoder encoder;

    private final Map<String, Skill> byName = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        if (skills.count() == 0) {
            // ---- skills (name, category, prerequisites) ----
            skill("Java", "Programming");
            skill("Python", "Programming");
            skill("SQL", "Database");
            skill("MySQL", "Database", "SQL");
            skill("HTML/CSS", "Frontend");
            skill("JavaScript", "Frontend", "HTML/CSS");
            skill("React", "Frontend", "JavaScript");
            skill("Spring Boot", "Backend", "Java");
            skill("REST APIs", "Backend", "Java");
            skill("Git", "Tools");
            skill("Excel", "Analytics");
            skill("Statistics", "Analytics");
            skill("Data Visualization", "Analytics", "Excel");
            skill("Manual Testing", "Testing");
            skill("Test Case Design", "Testing", "Manual Testing");
            skill("Selenium", "Testing", "Java", "Manual Testing");
            skill("JUnit", "Testing", "Java");
            skill("API Testing", "Testing", "REST APIs");

            // ---- career roles: {skill, weight(1-10), required level(1-5)} ----
            role("Full Stack Developer", "Builds both the user interface and the server side of web applications.",
                    new Object[][]{{"Java", 9.0, 4}, {"Spring Boot", 9.0, 4}, {"REST APIs", 8.0, 4}, {"MySQL", 8.0, 3},
                            {"React", 8.0, 4}, {"JavaScript", 7.0, 4}, {"HTML/CSS", 6.0, 3}, {"Git", 6.0, 3}});
            role("Backend Developer", "Designs APIs, business logic and databases for server-side systems.",
                    new Object[][]{{"Java", 9.0, 5}, {"Spring Boot", 9.0, 4}, {"REST APIs", 8.0, 4}, {"MySQL", 7.0, 4},
                            {"SQL", 7.0, 4}, {"Git", 5.0, 3}, {"JUnit", 5.0, 3}});
            role("Data Analyst", "Turns raw data into insight using queries, statistics and dashboards.",
                    new Object[][]{{"SQL", 9.0, 4}, {"Excel", 8.0, 4}, {"Python", 8.0, 3}, {"Statistics", 8.0, 3},
                            {"Data Visualization", 8.0, 4}, {"MySQL", 5.0, 3}});
            role("Software Tester", "Verifies software quality through manual and automated testing.",
                    new Object[][]{{"Manual Testing", 9.0, 4}, {"Test Case Design", 8.0, 4}, {"Selenium", 8.0, 3},
                            {"JUnit", 7.0, 3}, {"API Testing", 6.0, 3}, {"Java", 6.0, 3}, {"SQL", 5.0, 3}, {"Git", 4.0, 2}});

            // ---- learning resources ----
            res("Java", "Dev.java: Learn Java", "https://dev.java/learn/", "Tutorial");
            res("Python", "The Python Tutorial", "https://docs.python.org/3/tutorial/", "Documentation");
            res("SQL", "SQL Tutorial (W3Schools)", "https://www.w3schools.com/sql/", "Tutorial");
            res("MySQL", "MySQL Reference Manual", "https://dev.mysql.com/doc/", "Documentation");
            res("HTML/CSS", "MDN Learn Web Development", "https://developer.mozilla.org/en-US/docs/Learn", "Course");
            res("JavaScript", "The Modern JavaScript Tutorial", "https://javascript.info/", "Tutorial");
            res("React", "React: Learn", "https://react.dev/learn", "Documentation");
            res("Spring Boot", "Spring Guides", "https://spring.io/guides", "Tutorial");
            res("REST APIs", "RESTful API Tutorial", "https://restfulapi.net/", "Tutorial");
            res("Git", "Pro Git Book", "https://git-scm.com/book", "Book");
            res("Statistics", "Khan Academy: Statistics and Probability", "https://www.khanacademy.org/math/statistics-probability", "Course");
            res("Selenium", "Selenium Documentation", "https://www.selenium.dev/documentation/", "Documentation");
            res("JUnit", "JUnit 5 User Guide", "https://junit.org/junit5/docs/current/user-guide/", "Documentation");
            res("API Testing", "Postman Learning Center", "https://learning.postman.com/", "Course");
            res("Manual Testing", "Software Testing Tutorial (Guru99)", "https://www.guru99.com/software-testing.html", "Tutorial");
        } else {
            skills.findAll().forEach(skill -> byName.put(skill.getName(), skill));
        }

        // ---- users ----
        user("Admin User", "admin@skillgap.com", "Admin@123", Role.ADMIN);
        user("Mentor User", "mentor@skillgap.com", "Mentor@123", Role.MENTOR);
        if (users.findByEmail("student@skillgap.com").isEmpty()) {
            User demo = user("Demo Student", "student@skillgap.com", "Student@123", Role.STUDENT);
            demo.setEducation("B.Tech, 3rd year");
            demo.setCareerInterests("Full stack development");
            demo.setTargetRole(roles.findAll().stream()
                    .filter(r -> r.getTitle().equals("Full Stack Developer")).findFirst().orElse(null));
            users.save(demo);
            give(demo, "Java", 4);
            give(demo, "HTML/CSS", 3);
            give(demo, "JavaScript", 2);
            give(demo, "SQL", 3);
            give(demo, "Git", 2);
        }
        if (users.findByEmail("demo.student@skillgap.com").isEmpty()) {
            User demo = user("Demo Student", "demo.student@skillgap.com", "Student@123", Role.STUDENT);
            demo.setEducation("B.Tech, 3rd year");
            demo.setCareerInterests("Full stack development");
            demo.setTargetRole(roles.findAll().stream()
                    .filter(r -> r.getTitle().equals("Full Stack Developer")).findFirst().orElse(null));
            users.save(demo);
            give(demo, "Java", 4);
            give(demo, "HTML/CSS", 3);
            give(demo, "JavaScript", 2);
            give(demo, "SQL", 3);
            give(demo, "Git", 2);
        }
    }

    private void skill(String name, String category, String... prerequisites) {
        Skill s = new Skill();
        s.setName(name);
        s.setCategory(category);
        for (String p : prerequisites) s.getPrerequisites().add(byName.get(p));
        byName.put(name, skills.save(s));
    }

    private void role(String title, String description, Object[][] requirements) {
        CareerRole r = new CareerRole();
        r.setTitle(title);
        r.setDescription(description);
        r = roles.save(r);
        for (Object[] q : requirements) {
            RoleSkill rs = new RoleSkill();
            rs.setCareerRole(r);
            rs.setSkill(byName.get((String) q[0]));
            rs.setWeight(((Number) q[1]).doubleValue());
            rs.setRequiredLevel(((Number) q[2]).intValue());
            roleSkills.save(rs);
        }
    }

    private void res(String skill, String title, String url, String type) {
        Resource r = new Resource();
        r.setSkill(byName.get(skill));
        r.setTitle(title);
        r.setUrl(url);
        r.setType(type);
        resources.save(r);
    }

    private User user(String name, String email, String password, Role role) {
        return users.findByEmail(email).orElseGet(() -> createUser(name, email, password, role));
    }

    private User createUser(String name, String email, String password, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        return users.save(u);
    }

    private void give(User u, String skill, int level) {
        StudentSkill ss = new StudentSkill();
        ss.setUser(u);
        ss.setSkill(byName.get(skill));
        ss.setLevel(level);
        studentSkills.save(ss);
    }
}
