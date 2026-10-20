package com.sanedge.pointofsale.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.auth.models.RefreshToken;

@Repository
public interface RefreshTokenQueryRepository extends JpaRepository<RefreshToken, Long> {

        @Query(value = """
                        SELECT *
                        FROM refresh_tokens
                        WHERE user_id = :userId AND deleted_at IS NULL
                        ORDER BY created_at DESC
                        LIMIT 1
                        """, nativeQuery = true)
        Optional<RefreshToken> findByUserId(@Param("userId") Long userId);

        @Query(value = """
                        SELECT *
                        FROM refresh_tokens
                        WHERE token = :token AND deleted_at IS NULL
                        """, nativeQuery = true)
        Optional<RefreshToken> findByToken(@Param("token") String token);
}
