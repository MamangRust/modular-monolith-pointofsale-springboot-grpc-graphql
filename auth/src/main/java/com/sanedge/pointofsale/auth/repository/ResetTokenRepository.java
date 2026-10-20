package com.sanedge.pointofsale.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.auth.models.ResetToken;

/** Password-reset token persistence. Port of the blueprint's repository. */
@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    @Query(value = """
            SELECT *
            FROM reset_tokens
            WHERE token = :token AND deleted_at IS NULL
            """, nativeQuery = true)
    Optional<ResetToken> findByToken(@Param("token") String token);

    @Modifying
    @Query(value = """
            DELETE FROM reset_tokens
            WHERE user_id = :userId
            """, nativeQuery = true)
    int deleteByUserId(@Param("userId") Long userId);
}
