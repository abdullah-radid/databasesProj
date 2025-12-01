package API.DTO;

public record BookCopyRecord(int copyId, String isbn, String shelfLocation, CopyType copyType, BookCopyStatus status) {
    public enum CopyType {
        Physical, Online
    }

    public enum BookCopyStatus {
        Available, Loaned, Lost, Reserved
    }
}
