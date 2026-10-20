package com.sanedge.pointofsale.product.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.adapter.merchant.MerchantQueryPort;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.product.domain.requests.CreateProductRequest;
import com.sanedge.pointofsale.product.domain.requests.UpdateProductRequest;
import com.sanedge.pointofsale.product.domain.responses.ProductResponse;
import com.sanedge.pointofsale.product.domain.responses.ProductResponseDeleteAt;
import com.sanedge.pointofsale.product.models.Product;
import com.sanedge.pointofsale.product.repository.ProductCommandRepository;
import com.sanedge.pointofsale.product.repository.ProductQueryRepository;
import com.sanedge.pointofsale.product.service.ProductCommandService;

import io.opentelemetry.api.common.Attributes;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductCommandImplService implements ProductCommandService {
    private final ProductCommandRepository productCommandRepository;
    private final ProductQueryRepository productQueryRepository;
    private final MerchantQueryPort merchantPort;
    private final Validator validator;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<ProductResponse> createProduct(CreateProductRequest req) {
        Attributes attrs = Attributes.builder()
                .put("product.name", req.getName() != null ? req.getName() : "")
                .build();

        return tracingMetrics.traceAndMeasure("createProduct", "create_product", attrs, () -> {
            log.info("🆕 Creating product: {}", req.getName());
            try {
                validateRequest(req);
                validateMerchantExists(req.getMerchantId());

                Product product = new Product();
                product.setMerchantId(req.getMerchantId().longValue());
                product.setCategoryId(req.getCategoryId().longValue());
                product.setName(req.getName());
                product.setDescription(req.getDescription());
                product.setPrice(req.getPrice());
                product.setCountInStock(req.getCountInStock());
                product.setBrand(req.getBrand());
                product.setWeight(req.getWeight());
                product.setSlugProduct(req.getSlugProduct());
                product.setImageProduct(req.getImageProduct());

                Product saved = productCommandRepository.save(product);

                return ApiResponse.<ProductResponse>builder()
                        .status("success")
                        .message("Product created successfully")
                        .data(ProductResponse.from(saved))
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to create product: {}", req.getName(), e);
                return ApiResponse.<ProductResponse>builder()
                        .status("error")
                        .message("Failed to create product")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<ProductResponse> updateProduct(UpdateProductRequest req) {
        Attributes attrs = Attributes.builder()
                .put("product.id", String.valueOf(req.getProductId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateProduct", "update_product", attrs, () -> {
            log.info("✏️ Updating product ID: {}", req.getProductId());
            try {
                validateRequest(req);
                validateMerchantExists(req.getMerchantId());

                Product product = productQueryRepository.findProductById(req.getProductId().longValue())
                        .orElse(null);
                if (product == null) {
                    return ApiResponse.<ProductResponse>builder()
                            .status("error")
                            .message("Product not found")
                            .data(null)
                            .build();
                }

                if (req.getImageProduct() != null && !req.getImageProduct().isEmpty()) {
                    product.setImageProduct(req.getImageProduct());
                }

                product.setMerchantId(req.getMerchantId().longValue());
                product.setCategoryId(req.getCategoryId().longValue());
                product.setName(req.getName());
                product.setDescription(req.getDescription());
                product.setPrice(req.getPrice());
                product.setCountInStock(req.getCountInStock());
                product.setBrand(req.getBrand());
                product.setWeight(req.getWeight());
                product.setSlugProduct(req.getSlugProduct());

                Product updated = productCommandRepository.save(product);

                return ApiResponse.<ProductResponse>builder()
                        .status("success")
                        .message("Product updated successfully")
                        .data(ProductResponse.from(updated))
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to update product ID: {}", req.getProductId(), e);
                return ApiResponse.<ProductResponse>builder()
                        .status("error")
                        .message("Failed to update product")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<ProductResponseDeleteAt> trashedProduct(Integer productId) {
        Attributes attrs = Attributes.builder()
                .put("product.id", String.valueOf(productId))
                .build();

        return tracingMetrics.traceAndMeasure("trashedProduct", "trashed_product", attrs, () -> {
            log.info("🗑️ Trashing product ID: {}", productId);
            try {
                Product product = productCommandRepository.trashed(productId.longValue());
                return ApiResponse.<ProductResponseDeleteAt>builder()
                        .status("success")
                        .message("Product trashed successfully")
                        .data(ProductResponseDeleteAt.from(product))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash product ID: {}", productId, e);
                return ApiResponse.<ProductResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to trash product")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<ProductResponseDeleteAt> restoreProduct(Integer productId) {
        Attributes attrs = Attributes.builder()
                .put("product.id", String.valueOf(productId))
                .build();

        return tracingMetrics.traceAndMeasure("restoreProduct", "restore_product", attrs, () -> {
            log.info("♻️ Restoring product ID: {}", productId);
            try {
                Product product = productCommandRepository.restore(productId.longValue());
                return ApiResponse.<ProductResponseDeleteAt>builder()
                        .status("success")
                        .message("Product restored successfully")
                        .data(ProductResponseDeleteAt.from(product))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore product ID: {}", productId, e);
                return ApiResponse.<ProductResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to restore product")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteProductPermanent(Integer productId) {
        Attributes attrs = Attributes.builder()
                .put("product.id", String.valueOf(productId))
                .build();

        return tracingMetrics.traceAndMeasure("deleteProductPermanent", "delete_product_permanent", attrs, () -> {
            log.info("🧨 Permanently deleting product ID: {}", productId);
            try {
                productCommandRepository.deletePermanent(productId.longValue());
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("Product permanently deleted")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete product ID: {}", productId, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to permanently delete product")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAllProducts() {
        return tracingMetrics.traceAndMeasure("restoreAllProducts", "restore_all_products", () -> {
            log.info("🔄 Restoring ALL trashed products");
            try {
                productCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("All products restored successfully")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all products", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to restore all products")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAllProductsPermanent() {
        return tracingMetrics.traceAndMeasure("deleteAllProductsPermanent", "delete_all_products_permanent", () -> {
            log.info("💣 Permanently deleting ALL trashed products");
            try {
                productCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("All products permanently deleted")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all products", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to delete all products")
                        .data(false)
                        .build();
            }
        });
    }

    private void validateMerchantExists(Integer merchantId) {
        merchantPort.findByMerchantId(merchantId);
    }

    private <T> void validateRequest(T req) {
        Set<ConstraintViolation<T>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }
            log.error("Validation failed: {}", sb);
            throw new ConstraintViolationException("Validation failed: " + sb, violations);
        }
    }
}
