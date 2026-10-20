package com.sanedge.pointofsale.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.auth.models.RefreshToken;

import jakarta.transaction.Transactional;

@Repository
public interface RefreshTokenCommandRepository extends JpaRepository<RefreshToken, Long> {
        @Modifying
        @Transactional
        @Query(value = """
                        DELETE FROM refresh_tokens
                        WHERE token = :token
                        """, nativeQuery = true)
        int deleteByToken(@Param("token") String token);

        @Modifying
        @Transactional
        @Query(value = """
                        DELETE FROM refresh_tokens
                        WHERE user_id = :userId
                        """, nativeQuery = true)
        int deleteByUserId(@Param("userId") Long userId);
}
