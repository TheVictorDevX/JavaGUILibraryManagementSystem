package com.example.librarymanagementsystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class to manage book operations such as adding, updating,
 * deleting, and handling file persistence for saving/loading book data.
 */
public class LibraryController {

    // === Fields ===

    // Internal list of books currently in memory
    private List<Book> bookList = new ArrayList<>();

    // File used to persist book data between sessions
    private static final String FILE_NAME = "books.dat";

    // === Core Book Operations ===

    /**
     * Adds a new book to the list if both title and author are non-empty.
     *
     * @param title  The book's title
     * @param author The book's author
     * @param isbn   The book's ISBN
     */
    public void addBook(String title, String author, String isbn) {
        if (!title.isEmpty() && !author.isEmpty()) {
            bookList.add(new Book(title, author, isbn));
        }
    }

    /**
     * Updates the properties of an existing book.
     *
     * @param oldBook   The book to be updated
     * @param newTitle  New title to set
     * @param newAuthor New author to set
     * @param newIsbn   New ISBN to set
     */
    public void updateBook(Book oldBook, String newTitle, String newAuthor, String newIsbn) {
        oldBook.setTitle(newTitle);
        oldBook.setAuthor(newAuthor);
        oldBook.setIsbn(newIsbn);
    }

    /**
     * Removes a specified book from the list.
     *
     * @param book The book to be removed
     */
    public void deleteBook(Book book) {
        getBooks().remove(book);
    }

    /**
     * Retrieves the current list of books.
     *
     * @return A list of all books in memory
     */
    public List<Book> getBooks() {
        return bookList;
    }

    /**
     * Checks if a given ISBN already exists in the list (excluding a specific book).
     * Useful for avoiding duplicate ISBNs during updates.
     *
     * @param isbn         The ISBN to check
     * @param excludeBook  The book to ignore in the check (useful when updating)
     * @return true if a duplicate is found, false otherwise
     */
    public boolean isDuplicateIsbn(String isbn, Book excludeBook) {
        for (Book book : getBooks()) {
            if (book.getIsbn().equals(isbn) && book != excludeBook) {
                return true;
            }
        }
        return false;
    }

    // === File Persistence ===

    /**
     * Serializes and saves the current book list to a local file.
     */
    public void saveToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(bookList); // Save the list of books
        } catch (IOException e) {
            e.printStackTrace(); // Log failure but do not crash
        }
    }

    /**
     * Loads the book list from the local file if available.
     * If loading fails, initializes an empty list instead.
     */
    public void loadFromFile() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            bookList = (List<Book>) in.readObject(); // Restore saved books
        } catch (IOException | ClassNotFoundException e) {
            bookList = new ArrayList<>(); // Fallback: start with empty list
        }
    }
}
