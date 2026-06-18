package io.sclera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * Utility for managing manual JDBC transactions (begin, commit and rollback) over the configured
 * data source.
 */
@Service
public class SQLConnectionUtils {

    private static final Logger log = LoggerFactory.getLogger(SQLConnectionUtils.class);

    @Autowired
    DataSource dataSource;

    /**
     * Opens a new connection with auto-commit disabled to begin a manual transaction.
     */
    public Connection beginTransaction() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (connection != null) {
            log.debug("{}", "setting auto commit false");
            connection.setAutoCommit(false);
        }
        return connection;
    }

    /**
     * Commits the transaction, rolling back on failure, and always restores auto-commit and closes
     * the connection.
     */
    public void commitTransaction(Connection conn) throws SQLException {
        try {
            if (conn != null) {
                log.debug("{}", "Commiting conn");
                conn.commit();
            }
        } catch (SQLException e) {
            rollbackTransaction(conn);
            log.debug("{}", "Commit failed");
        } finally {
            conn.setAutoCommit(true);
            conn.close();
            log.debug("{}", "Closing conn");
        }
    }

    /**
     * Rolls back the transaction and restores auto-commit on the connection.
     */
    public void rollbackTransaction(Connection conn) throws SQLException {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException e) {
            log.debug("{}", "Rollback failed");
            ;
        } finally {
            conn.setAutoCommit(true);
        }
    }

}
