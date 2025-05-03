package com.example.librarymanagementsystem;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    // === CONTROLLERS & STATE ===
    private final LibraryController controller = new LibraryController();
    private final ObservableList<Book> masterBookList = FXCollections.observableArrayList();
    private final TableView<Book> tableView = new TableView<>();
    private final Label statsLabel = new Label("Stats: ");
    private Book selectedBook = null;

    // === CHARTS ===
    private final PieChart titleInitialChart = new PieChart();
    private BarChart<String, Number> booksPerAuthorChart;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller.loadFromFile(); // Load saved books from file

        // === FORM INPUT FIELDS ===
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField authorField = new TextField();
        authorField.setPromptText("Author");

        TextField isbnField = new TextField();
        isbnField.setPromptText("ISBN");

        // === ACTION BUTTONS ===
        Button addButton = new Button("Add Book");
        Button updateButton = new Button("Update Book");
        Button deleteButton = new Button("Delete Book");
        Button importButton = new Button("\uD83D\uDCE5 Import CSV");
        Button exportButton = new Button("\uD83D\uDCE4 Export CSV");

        // === BUTTON ACTIONS ===

        // Add Book
        addButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String isbn = isbnField.getText().trim();

            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled.");
                return;
            }

            if (controller.isDuplicateIsbn(isbn, null)) {
                showAlert(Alert.AlertType.ERROR, "Duplicate ISBN", "A book with this ISBN already exists.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Add Book");
            confirm.setHeaderText("Add this book?");
            confirm.setContentText(title + " by " + author + "\nISBN: " + isbn);

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    controller.addBook(title, author, isbn);
                    refreshTable();
                    titleField.clear();
                    authorField.clear();
                    isbnField.clear();
                }
            });
        });

        // Update Book
        updateButton.setOnAction(e -> {
            if (selectedBook != null) {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String isbn = isbnField.getText().trim();

                if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled.");
                    return;
                }

                if (controller.isDuplicateIsbn(isbn, selectedBook)) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate ISBN", "Another book with this ISBN already exists.");
                    return;
                }

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Update Book");
                confirm.setHeaderText("Confirm update?");
                confirm.setContentText("New Title: " + title + "\nNew Author: " + author + "\nNew ISBN: " + isbn);

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        controller.updateBook(selectedBook, title, author, isbn);
                        refreshTable();
                        tableView.refresh();
                        titleField.clear();
                        authorField.clear();
                        isbnField.clear();
                        selectedBook = null;
                        tableView.getSelectionModel().clearSelection();
                    }
                });
            }
        });

        // Delete Book
        deleteButton.setOnAction(e -> {
            if (selectedBook != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Book");
                alert.setHeaderText("Are you sure?");
                alert.setContentText(selectedBook.getTitle() + " by " + selectedBook.getAuthor());

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        controller.deleteBook(selectedBook);
                        refreshTable();
                        tableView.refresh();
                        titleField.clear();
                        authorField.clear();
                        isbnField.clear();
                        selectedBook = null;
                        tableView.getSelectionModel().clearSelection();
                    }
                });
            }
        });

        // Import CSV
        importButton.setOnAction(e -> importBooksFromCSV());

        // Export CSV
        exportButton.setOnAction(e -> exportBooksToCSV());

        // === TABLE SETUP ===
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        tableView.getColumns().addAll(titleCol, authorCol, isbnCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Populate fields when row is selected
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedBook = newSel;
                titleField.setText(newSel.getTitle());
                authorField.setText(newSel.getAuthor());
                isbnField.setText(newSel.getIsbn());
            }
        });

        // === SEARCH BAR ===
        TextField searchField = new TextField();
        searchField.setPromptText("Search books...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));

        // === CHART SETUP ===
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Author");
        yAxis.setLabel("Books");
        booksPerAuthorChart = new BarChart<>(xAxis, yAxis);
        booksPerAuthorChart.setTitle("Books per Author");
        booksPerAuthorChart.setLegendVisible(false);
        titleInitialChart.setTitle("Books by Title Initial");

        // === LAYOUT ===
        HBox buttons = new HBox(10, addButton, updateButton, deleteButton);
        HBox fileButtons = new HBox(10, importButton, exportButton);
        VBox form = new VBox(10, titleField, authorField, isbnField, buttons, fileButtons);

        VBox topSection = new VBox(10, searchField, form, statsLabel);
        topSection.setPadding(new Insets(10));

        tableView.setPrefHeight(300);

        HBox chartSection = new HBox(20, booksPerAuthorChart, titleInitialChart);
        chartSection.setPadding(new Insets(10));
        chartSection.setPrefHeight(300);
        chartSection.setStyle("-fx-background-color: #f9f9f9;");

        BorderPane root = new BorderPane();
        root.setTop(topSection);
        root.setCenter(tableView);
        root.setBottom(chartSection);

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("\uD83D\uDCD6 Library Management System");
        primaryStage.show();

        refreshTable();
        primaryStage.setOnCloseRequest(e -> controller.saveToFile());
    }

    // === REFRESH TABLE, STATS, CHARTS ===
    private void refreshTable() {
        masterBookList.setAll(controller.getBooks());
        filterTable("");

        int totalBooks = masterBookList.size();
        long uniqueAuthors = masterBookList.stream().map(Book::getAuthor).distinct().count();
        statsLabel.setText("\uD83D\uDCDA Total Books: " + totalBooks + " | \uD83D\uDC64 Unique Authors: " + uniqueAuthors);

        booksPerAuthorChart.getData().clear();
        Map<String, Integer> authorCounts = new HashMap<>();
        for (Book book : masterBookList) {
            authorCounts.put(book.getAuthor(), authorCounts.getOrDefault(book.getAuthor(), 0) + 1);
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        authorCounts.forEach((author, count) -> series.getData().add(new XYChart.Data<>(author, count)));
        booksPerAuthorChart.getData().add(series);

        titleInitialChart.getData().clear();
        Map<String, Integer> initials = new HashMap<>();
        for (Book book : masterBookList) {
            String initial = book.getTitle().substring(0, 1).toUpperCase();
            initials.put(initial, initials.getOrDefault(initial, 0) + 1);
        }
        initials.forEach((letter, count) -> titleInitialChart.getData().add(new PieChart.Data(letter, count)));
    }

    // === FILTER TABLE BY QUERY ===
    private void filterTable(String query) {
        ObservableList<Book> filtered = FXCollections.observableArrayList();
        for (Book book : masterBookList) {
            if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(query.toLowerCase()) ||
                    book.getIsbn().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(book);
            }
        }
        tableView.setItems(filtered);
    }

    // === SHOW ALERT ===
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // === IMPORT BOOKS FROM CSV ===
    private void importBooksFromCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Books from CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int addedCount = 0, skippedCount = 0;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String title = parts[0].trim();
                        String author = parts[1].trim();
                        String isbn = parts[2].trim();

                        if (!title.isEmpty() && !author.isEmpty() && !isbn.isEmpty()) {
                            if (!controller.isDuplicateIsbn(isbn, null)) {
                                controller.addBook(title, author, isbn);
                                addedCount++;
                            } else {
                                skippedCount++;
                            }
                        }
                    }
                }
                refreshTable();
                showAlert(Alert.AlertType.INFORMATION, "Import Complete",
                        "Books added: " + addedCount + "\nSkipped (duplicate ISBN): " + skippedCount);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Import Error", "Failed to read CSV file.");
            }
        }
    }

    // === EXPORT BOOKS TO CSV ===
    private void exportBooksToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Books to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Book book : controller.getBooks()) {
                    writer.write(book.getTitle() + "," + book.getAuthor() + "," + book.getIsbn());
                    writer.newLine();
                }
                showAlert(Alert.AlertType.INFORMATION, "Export Complete", "Books exported successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to write CSV file.");
            }
        }
    }
}
