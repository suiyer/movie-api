package tmdb;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;

/**
 * DatabaseConnectionManager sets up a connection to the MySQL database using the connection parameters provided in the
 * config.properties file. It also sets up a connection pool and is responsible for providing db connections to the
 * application.
 */
public class DatabaseConnectionManager {
    //URI to use when connecting to database.
    private String dbUriFormat = "jdbc:mysql://%s/%s?%s";

    private static DatabaseConnectionManager singleton;
    private BasicDataSource connectionPool;


    /**
     * Creates a ConnectionPool from the provided database properties file.
     */
    private DatabaseConnectionManager() {
        try {
            Properties config = loadConfig("config.properties");

            connectionPool = new BasicDataSource();
            connectionPool.setUsername(config.getProperty("username"));
            connectionPool.setPassword(config.getProperty("password"));
            connectionPool.setUrl(String.format(dbUriFormat,
                    config.getProperty("hostname"),
                    config.getProperty("database"),
                    "zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC"));
            connectionPool.setInitialSize(2);
            connectionPool.setMaxTotal(5);

        } catch (IOException e) {
            throw new IllegalStateException("Error while obtaining a connection to the database: " + e);
        }
    }

    /*
     * Returns a singleton instance.
     */
    public static DatabaseConnectionManager getInstance() {
        if (singleton == null) {
            singleton = new DatabaseConnectionManager();
        }
        return singleton;
    }

    /**
     * Attempts to load properties file with database configuration. Must
     * include username, password, database, and hostname.
     *
     * @param configPath path to database properties file
     * @return database properties
     * @throws IOException if unable to properly parse properties file
     * @throws FileNotFoundException if properties file not found
     */
    private Properties loadConfig(String configPath)
            throws FileNotFoundException, IOException {

        Set<String> requiredKeys = new HashSet<>();
        requiredKeys.add("username");
        requiredKeys.add("password");
        requiredKeys.add("database");
        requiredKeys.add("hostname");

        Properties config = new Properties();
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader.getResource(configPath) == null) {
            throw new FileNotFoundException();
        }
        File configFile = new File(classLoader.getResource((configPath)).getFile());
        config.load(new FileReader(configFile));

        if (!config.keySet().containsAll(requiredKeys)) {
            String error = "Must provide the following in properties file: ";
            throw new InvalidPropertiesFormatException(error + requiredKeys);
        }

        return config;
    }

    /**
     * Attempts to connect to database using loaded configuration.
     *
     * @return database connection
     * @throws SQLException if unable to establish database connection
     */
    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }
}
