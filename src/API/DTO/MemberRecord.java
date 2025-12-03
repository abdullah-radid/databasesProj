package API.DTO;

public record MemberRecord(int memberId, String fname, String mname, String lname, MemberType memberType, String email) {
    public enum MemberType {
        Student, Faculty, Public
    }
}
