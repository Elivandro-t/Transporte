package com.relatorio.transporte.configuration;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.relatorio.transporte.repository.mysql",
        entityManagerFactoryRef = "mysqlEntityManager",
        transactionManagerRef = "mysqlTransactionManager"
)
public class MysqlConfig {

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.mysql.hikari")
    public DataSource mysqlDataSource() {
        return mysqlDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean mysqlEntityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mysqlDataSource") DataSource dataSource) {

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");

        return builder
                .dataSource(dataSource)
                .packages("com.relatorio.transporte.entity.mysql")
                .persistenceUnit("mysql")
                .properties(props)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
