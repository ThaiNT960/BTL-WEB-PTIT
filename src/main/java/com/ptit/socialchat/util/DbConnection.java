package com.ptit.socialchat.util;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection {

    private static BasicDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            InputStream is = DbConnection.class.getClassLoader()
                    .getResourceAsStream("db.properties");
            props.load(is);

            dataSource = new BasicDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl(props.getProperty("spring.datasource.url"));
            dataSource.setUsername(props.getProperty("spring.datasource.username"));
            dataSource.setPassword(props.getProperty("spring.datasource.password"));
            dataSource.setInitialSize(5);
            dataSource.setMaxTotal(20);
            dataSource.setMaxIdle(10);
            dataSource.setMinIdle(2);
            dataSource.setValidationQuery("SELECT 1");
            dataSource.setTestOnBorrow(true);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Cannot initialize DB: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private DbConnection() {
    }
}
