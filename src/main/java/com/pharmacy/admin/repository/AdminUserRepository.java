package com.pharmacy.admin.repository;

import com.pharmacy.admin.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
}
