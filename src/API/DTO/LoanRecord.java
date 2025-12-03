package API.DTO;

import java.time.LocalDate;

public record LoanRecord(int loanId,
                         int memberId,
                         String isbn,
                         LocalDate issueDate,
                         LocalDate dueDate,
                         LocalDate returnDate) {}
