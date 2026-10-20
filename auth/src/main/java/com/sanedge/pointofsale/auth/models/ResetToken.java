package com.sanedge.pointofsale.auth.models;

import java.sql.Timestamp;

import com.sanedge.pointofsale.common.models.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * One-time password-reset token (table {@code reset_tokens}, migration V11).
 *
 * <p>Port of the blueprint's {@code ResetToken}: a forgotten password issues a
 * short-lived token (15 minutes); issuing a new one deletes the previous row.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reset_tokens")
public class ResetToken extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reset_token_id")
    private Long resetTokenId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private Timestamp expiration;
}
