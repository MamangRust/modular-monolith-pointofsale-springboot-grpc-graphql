package com.sanedge.pointofsale.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.order.models.Order;

public class OrderRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderQueryRepository queryRepository;

    @Autowired
    private OrderCommandRepository commandRepository;

    @Test
    void shouldCreateAndQueryOrder() {
        Order order = new Order();
        order.setCashierId(1L);
        order.setMerchantId(1L);
        order.setTotalPrice(250000L);

        Order saved = commandRepository.save(order);
        assertThat(saved.getOrderId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        Page<Order> page = queryRepository.findOrders(saved.getOrderId().toString(), PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getTotalPrice()).isEqualTo(250000L);

        Optional<Order> found = queryRepository.findOrderById(saved.getOrderId());
        assertThat(found).isPresent();
    }
}
