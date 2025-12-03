package API;

import API.DTO.MemberRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Member specific queries.
 */
public final class MembersAPI {

    private MembersAPI() {
    }

    public static Optional<MemberRecord> findMemberById(int memberId) throws SQLException {
        String sql = "SELECT * FROM v_AllMembers WHERE member_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMember(rs));
                }
                return Optional.empty();
            }
        }
    }

    public static List<MemberRecord> getMembers(String memberType) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM v_AllMembers");
        boolean filter = memberType != null && !"All".equalsIgnoreCase(memberType);
        if (filter) {
            sql.append(" WHERE member_type = ?");
        }

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (filter) {
                ps.setString(1, memberType);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<MemberRecord> members = new ArrayList<>();
                while (rs.next()) {
                    members.add(mapMember(rs));
                }
                return members;
            }
        }
    }

    public static int createMember(String fname,
            String mname,
            String lname,
            String memberType,
            String email) throws SQLException {
        String sql = "INSERT INTO Member (Fname, Mname, Lname, member_type, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fname);
            ps.setString(2, mname);
            ps.setString(3, lname);
            ps.setString(4, memberType);
            ps.setString(5, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        }
    }

    public static boolean updateMemberContact(int memberId, String email) throws SQLException {
        String sql = "UPDATE Member SET email = ? WHERE member_id = ?";
        try (Connection conn = Database.getConnection()) {
            return Database.executeUpdate(conn, sql, email, memberId) > 0;
        }
    }

    private static MemberRecord mapMember(ResultSet rs) throws SQLException {
        return new MemberRecord(
                rs.getInt("member_id"),
                rs.getString("Fname"),
                "",
                rs.getString("Lname"),
                MemberRecord.MemberType.valueOf(rs.getString("member_type")),
                rs.getString("email"));
    }
}
