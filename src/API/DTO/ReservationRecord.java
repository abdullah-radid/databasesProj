package API.DTO;

import java.time.LocalDate;
import java.sql.Time;

public record ReservationRecord(int reservationId, int memberId, Integer roomId, String bookIsbn, LocalDate reservationDate, Time timeSlot, ReservationStatus status) {
    public enum ReservationStatus {
        Pending, Fulfilled, Cancelled
    }
}
