package com.sanedge.pointofsale.role.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.role.models.Role;

@Repository
public interface RoleQueryRepository extends JpaRepository<Role, Long> {
    @Query("""
                SELECT r FROM Role r
                WHERE (:keyword IS NULL OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY r.createdAt ASC
            """)
    Page<Role> findRoles(@Param("keyword") String keyword, Pageable pageable);

    Optional<Role> findByRoleName(String roleName);

    @Query("""
                SELECT r FROM Role r
                WHERE r.deletedAt IS NULL
                AND (:keyword IS NULL OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY r.createdAt ASC
            """)
    Page<Role> findActiveRoles(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT r FROM Role r
                WHERE r.deletedAt IS NOT NULL
                AND (:keyword IS NULL OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY r.deletedAt DESC
            """)
    Page<Role> findTrashedRoles(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT r FROM Role r
                JOIN UserRole ur ON ur.role.roleId = r.roleId
                WHERE ur.userId = :userId
                ORDER BY r.createdAt ASC
            """)
    List<Role> findUserRoles(@Param("userId") Long userId);
}
