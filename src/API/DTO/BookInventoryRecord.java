package API.DTO;

public record BookInventoryRecord(String isbn, String title, String author, String category, String edition, int copyId, String location, BookCopyRecord.BookCopyStatus status) {
}
