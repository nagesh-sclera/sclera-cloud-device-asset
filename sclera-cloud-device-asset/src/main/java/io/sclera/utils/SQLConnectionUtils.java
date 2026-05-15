package io.sclera.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@Service
public class SQLConnectionUtils {

    @Autowired
    DataSource dataSource;

    public Connection beginTransaction() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (connection != null) {
            System.out.println("setting auto commit false");
            connection.setAutoCommit(false);
        }
        return connection;
    }

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
