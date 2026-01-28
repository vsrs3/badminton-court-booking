package com.bcb.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author dattr
 */
public class DBContext {

    protected Connection connect;

    public DBContext() {
        try {
            String url = "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=badminton_court_booking;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";
            String username = "sa";
            String password = "123";

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            //make connection
            connect = DriverManager.getConnection(url, username, password);
            System.out.println("Successfull connection!");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    //get
    public Connection getConnection() {
        return this.connect;
    }

    //close
    public void closeConnection() {
        try {
            if (connect != null && !connect.isClosed()) {
                connect.close();
                System.out.println("Successfull closing!");
            }

        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        DBContext db = new DBContext();
    }
}
