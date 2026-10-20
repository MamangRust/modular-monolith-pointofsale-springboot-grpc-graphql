package com.sanedge.pointofsale.product.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sanedge.pointofsale.pb.category.CategoryQueryServiceGrpc;
import com.sanedge.pointofsale.pb.category.FindAllCategoryRequest;
import com.sanedge.pointofsale.pb.merchant.FindAllMerchantRequest;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.pointofsale.product.models.Product;
import com.sanedge.pointofsale.product.repository.ProductCommandRepository;
import com.sanedge.pointofsale.product.repository.ProductQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final ProductQueryRepository productQueryRepository;
    private final ProductCommandRepository productCommandRepository;
    private final MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub;
    private final CategoryQueryServiceGrpc.CategoryQueryServiceBlockingStub categoryQueryStub;

    @Override
    public void run(String... args) {
        try {
            seedProducts();
            log.info("Database seeding completed successfully.");
        } catch (Exception e) {
            log.error("Error during database seeding: {}", e.getMessage());
        }
    }

    private void seedProducts() {
        if (productQueryRepository.count() != 0) {
            return;
        }

        Long merchantId = resolveMerchantId();
        Long categoryId = resolveCategoryId();

        if (merchantId == null || categoryId == null) {
            log.warn("Skipping product seeding: merchantId={}, categoryId={}", merchantId, categoryId);
            return;
        }

        Product product = new Product();
        product.setMerchantId(merchantId);
        product.setCategoryId(categoryId);
        product.setName("Smartphone X");
        product.setDescription("Latest smartphone with advanced features");
        product.setPrice(12000000);
        product.setCountInStock(50);
        product.setBrand("BrandX");
        product.setWeight(200);
        product.setSlugProduct("smartphone-x");
        product.setImageProduct("http://example.com/smartphone_x.jpg");

        productCommandRepository.save(product);
        log.info("Products seeded.");
    }

    private Long resolveMerchantId() {
        try {
            var res = merchantQueryStub.findAll(FindAllMerchantRequest.newBuilder()
                    .setPage(1)
                    .setPageSize(1)
                    .setSearch("")
                    .build());
            if (res.getDataCount() > 0) {
                return (long) res.getData(0).getMerchantId();
            }
        } catch (Exception e) {
            log.warn("Unable to resolve merchantId via gRPC: {}", e.getMessage());
        }
        return null;
    }

    private Long resolveCategoryId() {
        try {
            var res = categoryQueryStub.findAll(FindAllCategoryRequest.newBuilder()
                    .setPage(1)
                    .setPageSize(1)
                    .setSearch("")
                    .build());
            if (res.getDataCount() > 0) {
                return (long) res.getData(0).getCategoryId();
            }
        } catch (Exception e) {
            log.warn("Unable to resolve categoryId via gRPC: {}", e.getMessage());
        }
        return null;
    }
}
