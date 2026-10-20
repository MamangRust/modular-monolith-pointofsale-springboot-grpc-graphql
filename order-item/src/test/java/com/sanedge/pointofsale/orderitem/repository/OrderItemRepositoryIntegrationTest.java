package com.sanedge.pointofsale.orderitem.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.orderitem.models.OrderItem;
import com.sanedge.pointofsale.orderitem.repository.OrderItemRepository;

public class OrderItemRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void shouldFindAllAndSaveOrderItem() {
        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        item.setProductId(11L);
        item.setQuantity(5);
        item.setPrice(100);
        item = orderItemRepository.save(item);

        entityManager.flush();
        entityManager.clear();

        List<OrderItem> items = orderItemRepository.findOrderItemByOrder(1L);
        assertThat(items).isNotEmpty();
        
        orderItemRepository.delete(item);
        entityManager.flush();
        entityManager.clear();

        assertThat(orderItemRepository.findById(item.getOrderItemId())).isEmpty();
    }
}
