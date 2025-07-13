package com.arnab.apdclbackend.service;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseService {

    public Connection sql_connector() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/apdcl_cal";
        String user = "Arnab";
        String password = "Arnab9707@";
        return DriverManager.getConnection(url, user, password);
    }

    public double fetchEnergyConsumption(String appliance, String rating) throws SQLException {
        Connection conn = sql_connector();
        String query = "SELECT ar.energy_consumption FROM appliance_master am JOIN appliance_rating ar ON am.appliance_id = ar.appliance_id WHERE am.appliance_name = ? AND ar.rating = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, appliance);
        stmt.setString(2, rating);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getDouble("energy_consumption") : -1;
    }

    public double fetchEnergyCharges(String category) throws SQLException {
        Connection conn = sql_connector();
        String query = "SELECT rate_per_unit FROM energy_charges WHERE category = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, category);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getDouble("rate_per_unit") : -1;
    }

    public List<String> fetchAllAppliances() throws SQLException {
        List<String> appliances = new ArrayList<>();
        Connection conn = sql_connector();
        String query = "SELECT appliance_name FROM appliance_master";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            appliances.add(rs.getString("appliance_name"));
        }
        return appliances;
    }
    public List<String> fetchcategory() throws SQLException{
        List<String> appliances = new ArrayList<>();
        String query = "SELECT category FROM energy_charges";
        Connection conn = sql_connector();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            appliances.add(rs.getString("category"));
        }
        return appliances;

    }
}

