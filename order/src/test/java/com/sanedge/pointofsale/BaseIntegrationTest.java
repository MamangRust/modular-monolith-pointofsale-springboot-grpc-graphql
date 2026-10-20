package com.sanedge.pointofsale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

/**
 * Module-local test base for the order service. Tests need a managed
 * {@link EntityManager} to flush/clear the persistence context between
 * assertions; all fixtures are created through the module's own repositories
 * (cross-service calls are provided as {@code @MockitoBean} stubs by the
 * individual tests).
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected EntityManager entityManager;
}
