package com.sanedge.pointofsale.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.role.models.Role;

@Repository
public interface RoleCommandRepository extends JpaRepository<Role, Long>, RoleCommandRepositoryCustom {
}
