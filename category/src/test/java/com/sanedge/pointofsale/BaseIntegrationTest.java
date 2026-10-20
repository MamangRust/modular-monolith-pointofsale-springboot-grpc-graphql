package com.sanedge.pointofsale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

/**
 * Module-local test base for the category service.
 *
 * <p>
 * The category statistics queries are native SQL that joins the
 * {@code products}/{@code orders}/{@code order_items} tables. Those tables are
 * owned by other modules, so the category test tree cannot use their JPA
 * entities. Instead the tests seed them through {@link JdbcTemplate} (DDL lives
 * in {@code init.sql}) while still exercising the real category repositories.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected JdbcTemplate jdbcTemplate;
}
