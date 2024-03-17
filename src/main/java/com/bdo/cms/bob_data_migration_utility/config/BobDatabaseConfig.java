package com.bdo.cms.bob_data_migration_utility.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource("file:${bobdmu.config.location}/connection.properties")
public class BobDatabaseConfig {

    @Value("${jdbc.driverClass.oracle}")
    private String driverClass;

    @Value("${jdbc.jdbcUrl.bob.cibut}")
    private String cibutUrl;

    @Value("${jdbc.user.bob.cibut}")
    private String cibutUsername;

    @Value("${jdbc.password.bob.cibut}")
    private String cibutPassword;

    @Value("${jdbc.jdbcUrl.bob.cib}")
    private String cibUrl;

    @Value("${jdbc.user.bob.cib}")
    private String cibUsername;

    @Value("${jdbc.password.bob.cib}")
    private String cibPassword;


    @Bean
    public BasicDataSource cibutDb() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driverClass);
        basicDataSource.setUrl(cibutUrl);
        basicDataSource.setUsername(cibutUsername);
        basicDataSource.setPassword(cibutPassword);
        return basicDataSource;
    }

    @Bean
    public BasicDataSource cibDb() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driverClass);
        basicDataSource.setUrl(cibUrl);
        basicDataSource.setUsername(cibUsername);
        basicDataSource.setPassword(cibPassword);
        return basicDataSource;
    }
}
