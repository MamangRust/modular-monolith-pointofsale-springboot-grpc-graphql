package com.sanedge.pointofsale.merchant.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sanedge.pointofsale.merchant.models.Merchant;
import com.sanedge.pointofsale.merchant.repository.MerchantCommandRepository;
import com.sanedge.pointofsale.merchant.repository.MerchantQueryRepository;
import com.sanedge.pointofsale.pb.user.ApiResponseUser;
import com.sanedge.pointofsale.pb.user.FindByEmailUserRequest;
import com.sanedge.pointofsale.pb.user.UserQueryServiceGrpc;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final MerchantQueryRepository merchantQueryRepository;
    private final MerchantCommandRepository merchantCommandRepository;
    private final UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryServiceBlockingStub;

    @Override
    public void run(String... args) {
        if (merchantQueryRepository.count() > 0) {
            return;
        }

        Integer adminId = resolveAdminUserId();
        if (adminId == null) {
            log.warn("Skipping merchant seed: admin user not found");
            return;
        }

        Merchant merchant = new Merchant();
        merchant.setUserId(adminId.longValue());
        merchant.setName("Sanedge Official Store");
        merchant.setDescription("Official store for Sanedge products");
        merchant.setAddress("Jakarta, Indonesia");
        merchant.setContactEmail("contact@sanedge.com");
        merchant.setContactPhone("08123456789");

        merchantCommandRepository.save(merchant);
        log.info("Merchant seeded.");
    }

    private Integer resolveAdminUserId() {
        try {
            ApiResponseUser res = userQueryServiceBlockingStub.findByEmail(
                    FindByEmailUserRequest.newBuilder().setEmail("admin@sanedge.com").build());
            if (!res.hasData()) {
                return null;
            }
            return res.getData().getId();
        } catch (StatusRuntimeException e) {
            log.warn("Skipping merchant seed: admin user lookup failed with {}", e.getStatus().getCode());
            return null;
        }
    }
}
