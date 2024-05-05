package com.vitaly.fakepaymentprovider.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestContainerConfig {
    private static final String IMAGE_VERSION = "postgres:16-alpine";
    private static final String DATABASE_NAME = "fpp_test";


    private static final PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER;

    static {
        POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>(IMAGE_VERSION)
                .withDatabaseName(DATABASE_NAME)
                .withReuse(true);
        POSTGRES_SQL_CONTAINER.start();
    }

    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer(){
        return POSTGRES_SQL_CONTAINER;
    }
}
