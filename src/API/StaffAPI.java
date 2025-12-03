package API;

import API.DTO.StaffRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class StaffAPI {

    public static List<StaffRecord> getAllStaff() throws SQLException {
        String sql = "SELECT * FROM Staff";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<StaffRecord> staffList = new ArrayList<>();
            while (rs.next()) {
                staffList.add(new StaffRecord(
                        rs.getInt("staff_id"),
                        rs.getString("Fname"),
                        rs.getString("Lname"),
                        rs.getString("email")));
            }
            return staffList;
        }
    }

    public static int addStaff(String fname, String lname, String email) throws SQLException {
        String sql = "INSERT INTO Staff (Fname, Lname, email) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fname);
            ps.setString(2, lname);
            ps.setString(3, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    return keys.getInt(1);
            }
        }
        return -1;
    }

    public static boolean updateStaff(int staffId, String email) throws SQLException {
        String sql = "UPDATE Staff SET email = ? WHERE staff_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, staffId);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteStaff(int staffId) throws SQLException {
        String sql = "DELETE FROM Staff WHERE staff_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            return ps.executeUpdate() > 0;
        }
    }
}