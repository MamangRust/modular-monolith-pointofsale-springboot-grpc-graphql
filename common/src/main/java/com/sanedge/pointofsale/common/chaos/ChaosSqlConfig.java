package com.sanedge.pointofsale.common.chaos;

import org.hibernate.Session;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Registers {@link ChaosStatementInspector} with Hibernate.
 *
 * <p>Uses {@link HibernatePropertiesCustomizer} rather than a
 * {@code spring.jpa.properties.*} entry so the inspector can be constructed with
 * the {@link ChaosManager} bean — Hibernate instantiates inspectors
 * reflectively, which would otherwise need a static/DI-by-reflection workaround.
 *
 * <p>Gated on {@link Session} (a Hibernate ORM class) so non-JPA modules such as
 * gateway and email-service do not try to load Hibernate classes. The guard must
 * name a Hibernate ORM type: {@code HibernatePropertiesCustomizer} itself lives in
 * {@code spring-boot-autoconfigure} and is always present, and the JPA API jar is
 * a plain compile dependency of {@code common} while Hibernate ORM is
 * {@code optional} — so a module can have the JPA API without Hibernate and still
 * trip this configuration. Also gated on {@code chaos.sql.enabled} for an opt-out.
 */
@Configuration
@ConditionalOnClass(Session.class)
@ConditionalOnProperty(name = "chaos.sql.enabled", havingValue = "true", matchIfMissing = true)
public class ChaosSqlConfig {

    @Bean
    @Order
    public HibernatePropertiesCustomizer chaosStatementInspectorCustomizer(ChaosManager chaosManager) {
        ChaosStatementInspector inspector = new ChaosStatementInspector(chaosManager);
        return properties -> properties.put(AvailableSettings.STATEMENT_INSPECTOR, inspector);
    }
}
