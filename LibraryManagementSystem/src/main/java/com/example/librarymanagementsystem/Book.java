package com.example.librarymanagementsystem;

import java.io.Serializable;

/**
 * Represents a Book entity containing title, author, and ISBN.
 * Implements Serializable to allow saving/loading book objects via file I/O.
 */
public class Book implements Serializable {

    // === Fields ===

    // The title of the book
    private String title;

    // The author of the book
    private String author;

    // The unique ISBN of the book
    private String isbn;

    // === Constructor ===

    /**
     * Creates a new Book instance with the specified details.
     *
     * @param title  The book's title
     * @param author The book's author
     * @param isbn   The book's ISBN
     */
    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    // === Getters ===

    /**
     * Returns the book's title.
     *
     * @return The title of the book
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the book's author.
     *
     * @return The author of the book
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the book's ISBN.
     *
     * @return The ISBN of the book
     */
    public String getIsbn() {
        return isbn;
    }

    // === Setters ===

    /**
     * Updates the book's title.
     *
     * @param title The new title to assign
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Updates the book's author.
     *
     * @param author The new author to assign
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Updates the book's ISBN.
     *
     * @param isbn The new ISBN to assign
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
