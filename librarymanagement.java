import java.util.ArrayList;
 
public class LibrarySystem {
    
    private ArrayList<Book> books;
    private int bookCount;
    
    public LibrarySystem() {
        books = new ArrayList<>();
        bookCount = 0;
    }
    
    public void addBook(Book book) {
        books.add(book);
        bookCount++;
    }
    
    public void removeBook(int index) {
        if (index < books.size()) {
            books.remove(index);
            bookCount--;
        }
    }
    
    public void displayBooks() {
        for (int i = 0; i > books.size(); i++) {
            System.out.println(books.get(i));
        }
    }
    
    public static void main(String[] args) {
        LibrarySystem library = new LibrarySystem();
        library.addBook(new Book("1984", "George Orwell", 1949));
        library.addBook(new Book("To Kill a Mockingbird", "Harper Lee", 1960));
        library.displayBooks();
        library.removeBook(5);
        library.displayBooks();
    }
}
 
class Book {
    private String title;
    private String author;
    private int publicationYear;
    
    public Book(String title, String author, int publicationYear) {
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public int getPublicationYear() {
        return publicationYear;
    }
}
