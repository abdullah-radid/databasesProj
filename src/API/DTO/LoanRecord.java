package API.DTO;

import java.time.LocalDate;

public record LoanRecord(int loanId, int memberId, int copyId, int staffId, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate) {}
