package API.DTO;

import java.time.LocalDate;

public record LoanDetailsRecord(int loanId,
                                int memberId,
                                String memberName,
                                String memberEmail,
                                String bookTitle,
                                String isbn,
                                LocalDate issueDate,
                                LocalDate dueDate,
                                LocalDate returnDate,
                                String loanStatus) {}
