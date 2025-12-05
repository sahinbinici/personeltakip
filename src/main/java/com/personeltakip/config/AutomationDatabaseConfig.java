package com.personeltakip.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.personeltakip.automation.repository",
        entityManagerFactoryRef = "automationEntityManagerFactory",
        transactionManagerRef = "automationTransactionManager"
)
public class AutomationDatabaseConfig {

    @Bean(name = "automationDataSource")
    public DataSource automationDataSource(
            @Value("${spring.datasource.automation.url}") String url,
            @Value("${spring.datasource.automation.username}") String username,
            @Value("${spring.datasource.automation.password}") String password,
            @Value("${spring.datasource.automation.driver-class-name}") String driverClassName) {
        return DataSourceBuilder.create()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean(name = "automationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean automationEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("automationDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        // Explicitly tell this datasource to NEVER touch the schema.
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        return builder
                .dataSource(dataSource)
                .packages("com.personeltakip.automation.model")
                .persistenceUnit("automation")
                .properties(properties)
                .build();
    }

    @Bean(name = "automationTransactionManager")
    public PlatformTransactionManager automationTransactionManager(
            @Qualifier("automationEntityManagerFactory") EntityManagerFactory automationEntityManagerFactory) {
        return new JpaTransactionManager(automationEntityManagerFactory);
    }
}
