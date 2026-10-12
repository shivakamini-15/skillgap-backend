package com.skillgap.repository;

import com.skillgap.entity.CareerRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRoleRepository extends JpaRepository<CareerRole, Long> {
    boolean existsByTitleIgnoreCase(String title);
}
