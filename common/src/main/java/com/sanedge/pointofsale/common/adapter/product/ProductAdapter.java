package com.sanedge.pointofsale.common.adapter.product;

import com.sanedge.pointofsale.common.adapter.model.Product;
import com.sanedge.pointofsale.common.adapter.model.UpdateProductCmd;
import com.sanedge.pointofsale.common.adapter.support.AdapterException;
import com.sanedge.pointofsale.common.adapter.support.ProtoTime;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.product.FindByIdProductRequest;
import com.sanedge.pointofsale.pb.product.ProductCommandServiceGrpc.ProductCommandServiceBlockingStub;
import com.sanedge.pointofsale.pb.product.ProductQueryServiceGrpc.ProductQueryServiceBlockingStub;
import com.sanedge.pointofsale.pb.product.ProductResponse;
import com.sanedge.pointofsale.pb.product.UpdateProductRequest;

public class ProductAdapter implements ProductQueryPort, ProductCommandPort {

    private final ProductQueryServiceBlockingStub query;

    private final ProductCommandServiceBlockingStub command;

    public ProductAdapter(ProductQueryServiceBlockingStub query, ProductCommandServiceBlockingStub command) {
        this.query = query;
        this.command = command;
    }

    @Override
    public Product findById(int productId) {
        var resp = query.findById(FindByIdProductRequest.newBuilder().setProductId(productId).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        return toProduct(resp.getData());
    }

    @Override
    public Product update(UpdateProductCmd cmd) {
        var resp = command.updateProduct(UpdateProductRequest.newBuilder()
                .setProductId(cmd.productId())
                .setMerchantId(cmd.merchantId())
                .setCategoryId(cmd.categoryId())
                .setName(cmd.name())
                .setDescription(cmd.description())
                .setPrice(cmd.price())
                .setCountInStock(cmd.countInStock())
                .setBrand(cmd.brand())
                .setWeight(String.valueOf(cmd.weight()))
                .setRating(cmd.rating())
                .setSlugProduct(cmd.slugProduct())
                .setImageProduct(cmd.imageProduct())
                .setBarcode(cmd.barcode())
                .build());
        if (resp == null || !resp.hasData()
                || !"success".equalsIgnoreCase(resp.getStatus())) {
            throw new AdapterException("Failed to update product: "
                    + (resp == null ? "no response" : resp.getMessage()));
        }
        return toProduct(resp.getData());
    }

    private static Product toProduct(ProductResponse p) {
        if (p == null) {
            return null;
        }
        return new Product(p.getProductId(), p.getMerchantId(), p.getCategoryId(), p.getName(), p.getDescription(),
                p.getPrice(), p.getCountInStock(), p.getBrand(), parseIntOrZero(p.getWeight()), p.getRating(),
                p.getSlugProduct(), p.getImageProduct(), p.getBarcode(), ProtoTime.parse(p.getCreatedAt()),
                ProtoTime.parse(p.getUpdatedAt()));
    }

    // The Spring product proto models weight as a string (the Quarkus one used
    // int32); parse it back to the domain int, defaulting to 0 when absent.
    private static int parseIntOrZero(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
