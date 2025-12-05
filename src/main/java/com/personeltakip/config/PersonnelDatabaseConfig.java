package com.personeltakip.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        basePackages = "com.personeltakip.repository",
        entityManagerFactoryRef = "personnelEntityManagerFactory",
        transactionManagerRef = "personnelTransactionManager"
)
public class PersonnelDatabaseConfig {

    @Primary
    @Bean(name = "personnelDataSource")
    public DataSource personnelDataSource(
            @Value("${spring.datasource.personnel.url}") String url,
            @Value("${spring.datasource.personnel.username}") String username,
            @Value("${spring.datasource.personnel.password}") String password,
            @Value("${spring.datasource.personnel.driver-class-name}") String driverClassName) {
        return DataSourceBuilder.create()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Primary
    @Bean(name = "personnelEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean personnelEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("personnelDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        // Explicitly tell this datasource to CREATE/UPDATE its tables.
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        return builder
                .dataSource(dataSource)
                .packages("com.personeltakip.model")
                .persistenceUnit("personnel")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "personnelTransactionManager")
    public PlatformTransactionManager personnelTransactionManager(
            @Qualifier("personnelEntityManagerFactory") EntityManagerFactory personnelEntityManagerFactory) {
        return new JpaTransactionManager(personnelEntityManagerFactory);
    }
}
