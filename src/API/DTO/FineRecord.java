package API.DTO;

import java.time.LocalDate;

public record FineRecord(int fineId,
                         int loanId,
                         double amount,
                         FineStatus status,
                         LocalDate appliedDate) {
    public enum FineStatus {
        Paid, Unpaid
    }
}
