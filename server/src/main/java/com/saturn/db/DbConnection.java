package com.saturn.db;

import org.postgresql.ds.PGConnectionPoolDataSource;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Created by ivan on 1/28/16.
 *
 */
public class DbConnection {
    private PGConnectionPoolDataSource ds;


    private static DbConnection Instance = new DbConnection();
    private DbConnection()
    {
        try {
            InitialContext initContext = new InitialContext();

            ds = (PGConnectionPoolDataSource) initContext.lookup("java:comp/env/jdbc/saturn.userinfo");

        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            e.printStackTrace();
        }
    }

    public static DbConnection GetAccess()
    {
        return Instance;
    }

    public Connection getConnect() {
        Connection connection = null;
        try {
            connection = ds.getConnection();
        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnect(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
