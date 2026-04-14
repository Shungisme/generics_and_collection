package ui.panels;

import model.BorrowSlip;
import model.Reader;
import service.BorrowService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BorrowPanel extends JPanel {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final BorrowService borrowService;

	private final JTextField readerIdField = new JTextField(14);
	private final JTextField readerNameField = new JTextField(20);
	private final JTextField cardExpiryField = new JTextField(14);
	private final JTextField borrowDateField = new JTextField(14);
	private final JTextField expectedReturnDateField = new JTextField(14);
	private final JTextField isbnInputField = new JTextField(14);

	private final JButton lookupReaderButton = new JButton("Lookup Reader");
	private final JButton addIsbnButton = new JButton("Add ISBN");
	private final JButton removeIsbnButton = new JButton("Remove Selected ISBN");
	private final JButton confirmBorrowButton = new JButton("Create Borrow Slip");
	private final JButton clearButton = new JButton("Clear");

	private final DefaultListModel<String> isbnListModel = new DefaultListModel<>();
	private final JList<String> isbnList = new JList<>(isbnListModel);

	private Reader currentReader;

	public BorrowPanel(BorrowService borrowService) {
		this.borrowService = borrowService;
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		readerNameField.setEditable(false);
		cardExpiryField.setEditable(false);
		expectedReturnDateField.setEditable(false);

		LocalDate defaultBorrowDate = LocalDate.now();
		borrowDateField.setText(defaultBorrowDate.format(DATE_FORMATTER));
		expectedReturnDateField.setText(defaultBorrowDate.plusDays(7).format(DATE_FORMATTER));

		add(buildTopFormPanel(), BorderLayout.NORTH);
		add(buildIsbnPanel(), BorderLayout.CENTER);
		add(buildActionPanel(), BorderLayout.SOUTH);

		lookupReaderButton.addActionListener(e -> lookupReader());
		addIsbnButton.addActionListener(e -> addIsbn());
		removeIsbnButton.addActionListener(e -> removeSelectedIsbn());
		confirmBorrowButton.addActionListener(e -> createBorrowSlip());
		clearButton.addActionListener(e -> clearForm());
		borrowDateField.addActionListener(e -> updateExpectedReturnDate());
	}

	private JPanel buildTopFormPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Borrow Information"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 6, 4, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addFormRow(panel, gbc, 0, "Reader ID:", readerIdField, lookupReaderButton);
		addFormRow(panel, gbc, 1, "Reader Name:", readerNameField, null);
		addFormRow(panel, gbc, 2, "Card Expiry:", cardExpiryField, null);
		addFormRow(panel, gbc, 3, "Borrow Date (dd/MM/yyyy):", borrowDateField, null);
		addFormRow(panel, gbc, 4, "Expected Return Date:", expectedReturnDateField, null);

		return panel;
	}

	private JPanel buildIsbnPanel() {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.setBorder(BorderFactory.createTitledBorder("Borrowed ISBN List"));

		JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		inputPanel.add(new JLabel("ISBN:"));
		inputPanel.add(isbnInputField);
		inputPanel.add(addIsbnButton);
		inputPanel.add(removeIsbnButton);

		panel.add(inputPanel, BorderLayout.NORTH);
		panel.add(new JScrollPane(isbnList), BorderLayout.CENTER);

		return panel;
	}

	private JPanel buildActionPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(clearButton);
		panel.add(confirmBorrowButton);
		return panel;
	}

	private void addFormRow(
			JPanel panel,
			GridBagConstraints gbc,
			int row,
			String label,
			java.awt.Component field,
			java.awt.Component action) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;
		panel.add(new JLabel(label, SwingConstants.RIGHT), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(field, gbc);

		if (action != null) {
			gbc.gridx = 2;
			gbc.weightx = 0;
			panel.add(action, gbc);
		}
	}

	private void lookupReader() {
		String readerId = readerIdField.getText() == null ? "" : readerIdField.getText().trim();
		if (readerId.isEmpty()) {
			showError("Reader ID is required.");
			return;
		}

		Reader reader = borrowService.lookupReader(readerId);
		if (reader == null) {
			currentReader = null;
			readerNameField.setText("");
			cardExpiryField.setText("");
			showError("Reader not found.");
			return;
		}

		currentReader = reader;
		readerNameField.setText(reader.getFullName());
		cardExpiryField.setText(formatDate(reader.getCardExpiredDate()));
	}

	private void addIsbn() {
		String isbn = isbnInputField.getText() == null ? "" : isbnInputField.getText().trim();
		if (isbn.isEmpty()) {
			showError("ISBN is required.");
			return;
		}
		isbnListModel.addElement(isbn);
		isbnInputField.setText("");
	}

	private void removeSelectedIsbn() {
		int index = isbnList.getSelectedIndex();
		if (index >= 0) {
			isbnListModel.remove(index);
		}
	}

	private void createBorrowSlip() {
		try {
			if (currentReader == null || !currentReader.getReaderId().equalsIgnoreCase(readerIdField.getText().trim())) {
				lookupReader();
				if (currentReader == null) {
					return;
				}
			}
			LocalDate borrowDate = parseDateOrThrow(borrowDateField.getText(), "Borrow date");
			updateExpectedReturnDate();

			if (isbnListModel.isEmpty()) {
				showError("At least one ISBN is required.");
				return;
			}

			java.util.List<String> isbnValues = new java.util.ArrayList<>();
			for (int i = 0; i < isbnListModel.size(); i++) {
				isbnValues.add(isbnListModel.get(i));
			}

			BorrowSlip createdSlip = borrowService.createBorrowSlip(currentReader.getReaderId(), borrowDate, isbnValues);
			JOptionPane.showMessageDialog(
					this,
					"Borrow slip created successfully. Slip ID: " + createdSlip.getSlipId(),
					"Success",
					JOptionPane.INFORMATION_MESSAGE);
			clearForm();
		} catch (IllegalArgumentException ex) {
			showError(ex.getMessage());
		}
	}

	private void clearForm() {
		readerIdField.setText("");
		readerNameField.setText("");
		cardExpiryField.setText("");
		currentReader = null;
		LocalDate now = LocalDate.now();
		borrowDateField.setText(now.format(DATE_FORMATTER));
		expectedReturnDateField.setText(now.plusDays(7).format(DATE_FORMATTER));
		isbnInputField.setText("");
		isbnListModel.clear();
	}

	private void updateExpectedReturnDate() {
		try {
			LocalDate borrowDate = parseDateOrThrow(borrowDateField.getText(), "Borrow date");
			expectedReturnDateField.setText(borrowDate.plusDays(7).format(DATE_FORMATTER));
		} catch (IllegalArgumentException ex) {
			expectedReturnDateField.setText("");
		}
	}

	private LocalDate parseDateOrThrow(String rawValue, String fieldName) {
		if (rawValue == null || rawValue.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return LocalDate.parse(rawValue.trim(), DATE_FORMATTER);
		} catch (DateTimeParseException ex) {
			throw new IllegalArgumentException(fieldName + " must follow format dd/MM/yyyy.");
		}
	}

	private String formatDate(LocalDate value) {
		return value == null ? "" : value.format(DATE_FORMATTER);
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
	}
}
