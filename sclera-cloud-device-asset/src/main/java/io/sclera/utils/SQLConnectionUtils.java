package io.sclera.utils;

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

    @Autowired
    DataSource dataSource;

    /**
     * Opens a new connection with auto-commit disabled to begin a manual transaction.
     */
    public Connection beginTransaction() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (connection != null) {
            System.out.println("setting auto commit false");
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
                System.out.println("Commiting conn");
                conn.commit();
            }
        } catch (SQLException e) {
            rollbackTransaction(conn);
            System.out.println("Commit failed");
        } finally {
            conn.setAutoCommit(true);
            conn.close();
            System.out.println("Closing conn");
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
            System.out.println("Rollback failed");
            ;
        } finally {
            conn.setAutoCommit(true);
        }
    }

}
