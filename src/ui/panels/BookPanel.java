package ui.panels;

import model.Book;
import service.BookService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Year;
import java.util.List;

public class BookPanel extends JPanel {
	private final BookService bookService;
	private final DefaultTableModel tableModel;
	private final JTable bookTable;

	private final JTextField isbnField = new JTextField(16);
	private final JTextField titleField = new JTextField(16);
	private final JTextField authorField = new JTextField(16);
	private final JTextField publisherField = new JTextField(16);
	private final JTextField yearField = new JTextField(16);
	private final JTextField genreField = new JTextField(16);
	private final JTextField priceField = new JTextField(16);
	private final JTextField quantityField = new JTextField(16);

	private final JButton addButton = new JButton("Add Book");
	private final JButton editButton = new JButton("Save Edit");
	private final JButton deleteButton = new JButton("Delete Book");
	private final JButton clearButton = new JButton("Clear Form");

	public BookPanel(BookService bookService) {
		this.bookService = bookService;
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		tableModel = new DefaultTableModel(new Object[] {
				"ISBN", "Title", "Author", "Publisher", "Year", "Genre", "Price", "Quantity"
		}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		bookTable = new JTable(tableModel);
		bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(bookTable), BorderLayout.CENTER);
		add(buildFormPanel(), BorderLayout.NORTH);
		add(buildActionPanel(), BorderLayout.SOUTH);

		bookTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				populateFormFromSelection();
			}
		});

		addButton.addActionListener(e -> addBook());
		editButton.addActionListener(e -> editBook());
		deleteButton.addActionListener(e -> deleteBook());
		clearButton.addActionListener(e -> clearForm());

		refreshTable(bookService.getAll());
	}

	private JPanel buildFormPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Book Information"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 6, 4, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addFormRow(panel, gbc, 0, "ISBN:", isbnField);
		addFormRow(panel, gbc, 1, "Title:", titleField);
		addFormRow(panel, gbc, 2, "Author:", authorField);
		addFormRow(panel, gbc, 3, "Publisher:", publisherField);
		addFormRow(panel, gbc, 4, "Year Published:", yearField);
		addFormRow(panel, gbc, 5, "Genre:", genreField);
		addFormRow(panel, gbc, 6, "Price:", priceField);
		addFormRow(panel, gbc, 7, "Quantity:", quantityField);

		return panel;
	}

	private JPanel buildActionPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(clearButton);
		panel.add(addButton);
		panel.add(editButton);
		panel.add(deleteButton);
		return panel;
	}

	private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component input) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;
		panel.add(new JLabel(label, SwingConstants.RIGHT), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(input, gbc);
	}

	private void addBook() {
		try {
			Book book = buildBookFromForm(true);
			bookService.addBook(book);
			refreshTable(bookService.getAll());
			clearForm();
			JOptionPane.showMessageDialog(this, "Book added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void editBook() {
		if (bookTable.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select a book row to edit.", "No Selection",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			Book book = buildBookFromForm(false);
			bookService.updateBook(book);
			refreshTable(bookService.getAll());
			JOptionPane.showMessageDialog(this, "Book updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteBook() {
		int row = bookTable.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Please select a book row to delete.", "No Selection",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		String isbn = tableModel.getValueAt(row, 0).toString();
		int confirm = JOptionPane.showConfirmDialog(
				this,
				"Delete book with ISBN " + isbn + "?",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			try {
				bookService.deleteBook(isbn);
				refreshTable(bookService.getAll());
				clearForm();
			} catch (IllegalStateException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Not Allowed", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private Book buildBookFromForm(boolean isNew) {
		String isbn = requiredText(isbnField, "ISBN");
		String title = requiredText(titleField, "Title");
		String author = requiredText(authorField, "Author");
		String publisher = requiredText(publisherField, "Publisher");
		String genre = requiredText(genreField, "Genre");

		int year = parseYear(yearField.getText().trim());
		double price = parsePrice(priceField.getText().trim());
		int quantity = parseQuantity(quantityField.getText().trim());

		if (isNew && bookService.isIsbnExists(isbn)) {
			throw new IllegalArgumentException("ISBN already exists.");
		}

		return new Book(isbn, title, author, publisher, year, genre, price, quantity);
	}

	private String requiredText(JTextField field, String fieldName) {
		String value = field.getText() == null ? "" : field.getText().trim();
		if (value.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		return value;
	}

	private int parseYear(String value) {
		int year;
		try {
			year = Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Year must be a valid number.");
		}

		int currentYear = Year.now().getValue();
		if (year < 1000 || year > currentYear + 1) {
			throw new IllegalArgumentException("Year is out of valid range.");
		}
		return year;
	}

	private double parsePrice(String value) {
		double price;
		try {
			price = Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Price must be a valid number.");
		}
		if (price <= 0) {
			throw new IllegalArgumentException("Price must be greater than 0.");
		}
		return price;
	}

	private int parseQuantity(String value) {
		int quantity;
		try {
			quantity = Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Quantity must be a valid integer.");
		}
		if (quantity < 0) {
			throw new IllegalArgumentException("Quantity must be greater than or equal to 0.");
		}
		return quantity;
	}

	private void populateFormFromSelection() {
		int row = bookTable.getSelectedRow();
		if (row < 0) {
			return;
		}

		isbnField.setText(tableModel.getValueAt(row, 0).toString());
		titleField.setText(tableModel.getValueAt(row, 1).toString());
		authorField.setText(tableModel.getValueAt(row, 2).toString());
		publisherField.setText(tableModel.getValueAt(row, 3).toString());
		yearField.setText(tableModel.getValueAt(row, 4).toString());
		genreField.setText(tableModel.getValueAt(row, 5).toString());
		priceField.setText(tableModel.getValueAt(row, 6).toString());
		quantityField.setText(tableModel.getValueAt(row, 7).toString());
		isbnField.setEditable(false);
	}

	private void clearForm() {
		bookTable.clearSelection();
		isbnField.setText("");
		titleField.setText("");
		authorField.setText("");
		publisherField.setText("");
		yearField.setText("");
		genreField.setText("");
		priceField.setText("");
		quantityField.setText("");
		isbnField.setEditable(true);
	}

	private void refreshTable(List<Book> books) {
		tableModel.setRowCount(0);
		for (Book book : books) {
			tableModel.addRow(new Object[] {
					book.getIsbn(),
					book.getTitle(),
					book.getAuthor(),
					book.getPublisher(),
					book.getYearPublished(),
					book.getGenre(),
					book.getPrice(),
					book.getQuantity()
			});
		}
	}
}
