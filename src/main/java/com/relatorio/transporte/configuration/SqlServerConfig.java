package com.relatorio.transporte.configuration;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.relatorio.transporte.repository.sqlserver",
        entityManagerFactoryRef = "sqlServerEntityManager",
        transactionManagerRef = "sqlServerTransactionManager"
)
public class SqlServerConfig {

    @Bean
    @ConfigurationProperties("app.datasource.sqlserver")
    public DataSourceProperties sqlServerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.sqlserver.hikari")
    public DataSource sqlServerDataSource() {
        return sqlServerDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean sqlServerEntityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("sqlServerDataSource") DataSource dataSource) {

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.show_sql", "true");

        return builder
                .dataSource(dataSource)
                .packages("com.relatorio.transporte.entity.sqlserver")
                .persistenceUnit("sqlserver")
                .properties(props)
                .build();
    }

    @Bean
    public PlatformTransactionManager sqlServerTransactionManager(
            @Qualifier("sqlServerEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
