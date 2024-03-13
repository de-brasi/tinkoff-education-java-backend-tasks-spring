package edu.java.scrapper;

import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

public class SimpleIntegrationTest extends IntegrationTest {
    @Test
    void simpleTest() throws SQLException {
        final String jdbcUrl = POSTGRES.getJdbcUrl();
        final String username = POSTGRES.getUsername();
        final String password = POSTGRES.getPassword();

        try (
            java.sql.Connection connection = DriverManager.getConnection(jdbcUrl, username, password)
        ) {
            assertThat(connection.getCatalog()).isEqualTo("scrapper");
        }
    }
}
