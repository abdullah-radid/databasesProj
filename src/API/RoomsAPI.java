package API;

import API.DTO.RoomRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class RoomsAPI {

    public static List<RoomRecord> getAllRooms() throws SQLException {
        String sql = "SELECT * FROM Room";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<RoomRecord> rooms = new ArrayList<>();
            while (rs.next()) {
                rooms.add(new RoomRecord(
                        rs.getInt("room_id"),
                        rs.getString("room_name"),
                        rs.getInt("capacity")));
            }
            return rooms;
        }
    }

    public static int addRoom(String roomName, int capacity) throws SQLException {
        String sql = "INSERT INTO Room (room_name, capacity) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roomName);
            ps.setInt(2, capacity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    return keys.getInt(1);
            }
        }
        return -1;
    }
}