package com.sanedge.pointofsale.merchant.models;

import com.sanedge.pointofsale.merchant.domain.requests.CreateMerchantRequest;
import com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantRequest;
import com.sanedge.pointofsale.common.enums.Status;
import com.sanedge.pointofsale.common.models.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "merchants")
public class Merchant extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String name;

    private String description;
    private String address;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    public static Merchant fromCreateRequest(CreateMerchantRequest req) {
        Merchant merchant = new Merchant();
        merchant.setUserId(req.getUserId().longValue());
        merchant.setName(req.getName());
        merchant.setDescription(req.getDescription());
        merchant.setAddress(req.getAddress());
        merchant.setContactEmail(req.getContactEmail());
        merchant.setContactPhone(req.getContactPhone());

        try {
            merchant.setStatus(Status.valueOf(req.getStatus().toLowerCase()));
        } catch (IllegalArgumentException e) {
            merchant.setStatus(Status.PENDING);
        }

        return merchant;
    }

    public void updateFromRequest(UpdateMerchantRequest req) {
        this.userId = req.getUserId().longValue();
        this.name = req.getName();
        this.description = req.getDescription();
        this.address = req.getAddress();
        this.contactEmail = req.getContactEmail();
        this.contactPhone = req.getContactPhone();

        try {
            this.status = Status.valueOf(req.getStatus().toLowerCase());
        } catch (IllegalArgumentException e) {
            this.setStatus(Status.PENDING);
        }
    }
}
