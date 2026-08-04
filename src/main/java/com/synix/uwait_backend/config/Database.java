package com.synix.uwait_backend.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static void main(String[] args) {
        String url =
                "jdbc:postgresql://ep-royal-cloud-afolerby-pooler.c-2.us-west-2.aws.neon.tech:5432/neondb" +
                        "?sslmode=require&channel_binding=require";
        String user = "neondb_owner";
        String password = "npg_rKA4XzCI8VQM";


        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println(" Database Connected successfully!");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}
