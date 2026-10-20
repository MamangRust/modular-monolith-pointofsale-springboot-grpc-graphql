package com.sanedge.pointofsale.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.user.models.User;

import jakarta.transaction.Transactional;
import java.sql.Timestamp;

@Repository
public interface UserCommandRepository extends JpaRepository<User, Long> {

  @Transactional
  default User trashed(Long userId) {
      return findById(userId).map(user -> {
          if (user.getDeletedAt() == null) {
              user.setDeletedAt(new Timestamp(System.currentTimeMillis()));
              return save(user);
          }
          return user;
      }).orElse(null);
  }

  @Transactional
  default User restore(Long userId) {
      return findById(userId).map(user -> {
          if (user.getDeletedAt() != null) {
              user.setDeletedAt(null);
              return save(user);
          }
          return user;
      }).orElse(null);
  }

  @Modifying
  @Transactional
  @Query("DELETE FROM User u WHERE u.userId = :userId AND u.deletedAt IS NOT NULL")
  int deletePermanent(@Param("userId") Long userId);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.deletedAt = NULL WHERE u.deletedAt IS NOT NULL")
  int restoreAllDeleted();

  @Modifying
  @Transactional
  @Query("DELETE FROM User u WHERE u.deletedAt IS NOT NULL")
  int deleteAllDeleted();
}
