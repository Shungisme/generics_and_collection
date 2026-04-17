package ui.panels;

import model.BorrowSlip;
import model.Book;
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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BorrowPanel extends JPanel {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.##");

	private final BorrowService borrowService;

	// Create slip tab
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

	// Return slip tab
	private final JTextField searchSlipIdField = new JTextField(14);
	private final JTextField searchReaderIdField = new JTextField(14);
	private final JButton searchReaderButton = new JButton("Search by Reader");
	private final JTextField returnBorrowDateField = new JTextField(14);
	private final JTextField returnExpectedDateField = new JTextField(14);
	private final JTextField actualReturnDateField = new JTextField(14);
	private final JButton searchSlipButton = new JButton("Search Slip");
	private final JButton confirmReturnButton = new JButton("Confirm Return");
	private final JButton clearReturnButton = new JButton("Clear Return Form");

	private final DefaultTableModel activeSlipTableModel = new DefaultTableModel(new Object[] {
			"Slip ID", "Borrow Date", "Expected Return", "Books"
	}, 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	private final JTable activeSlipTable = new JTable(activeSlipTableModel);

	private final DefaultTableModel returnTableModel = new DefaultTableModel(new Object[] {
			"ISBN", "Title", "Price", "Lost?"
	}, 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 2) {
				return Double.class;
			}
			if (columnIndex == 3) {
				return Boolean.class;
			}
			return String.class;
		}
	};
	private final JTable returnTable = new JTable(returnTableModel);
	private final JTextField returnSlipIdField = new JTextField(16);
	private final JTextField overdueDaysField = new JTextField(8);
	private final JTextField lateFeeField = new JTextField(14);
	private final JTextField lostFeeField = new JTextField(14);
	private final JTextField totalFeeField = new JTextField(14);

	private BorrowSlip selectedReturnSlip;

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
		actualReturnDateField.setText(defaultBorrowDate.format(DATE_FORMATTER));

		returnSlipIdField.setEditable(false);
		returnBorrowDateField.setEditable(false);
		returnExpectedDateField.setEditable(false);
		overdueDaysField.setEditable(false);
		lateFeeField.setEditable(false);
		lostFeeField.setEditable(false);
		totalFeeField.setEditable(false);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Create Slip", buildCreateSlipPanel());
		tabbedPane.addTab("Return Slip", buildReturnSlipPanel());
		add(tabbedPane, BorderLayout.CENTER);

		lookupReaderButton.addActionListener(e -> lookupReader());
		addIsbnButton.addActionListener(e -> addIsbn());
		removeIsbnButton.addActionListener(e -> removeSelectedIsbn());
		confirmBorrowButton.addActionListener(e -> createBorrowSlip());
		clearButton.addActionListener(e -> clearForm());
		borrowDateField.addActionListener(e -> updateExpectedReturnDate());

		searchReaderButton.addActionListener(e -> searchActiveSlipsByReader());
		searchSlipButton.addActionListener(e -> searchSlipForReturn());
		searchSlipIdField.addActionListener(e -> searchSlipForReturn());
		searchReaderIdField.addActionListener(e -> searchActiveSlipsByReader());
		confirmReturnButton.addActionListener(e -> confirmReturn());
		clearReturnButton.addActionListener(e -> clearReturnForm());

		activeSlipTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int selectedRow = activeSlipTable.getSelectedRow();
				if (selectedRow >= 0) {
					String slipId = String.valueOf(activeSlipTableModel.getValueAt(selectedRow, 0));
					searchSlipIdField.setText(slipId);
					searchSlipForReturn();
				}
			}
		});

		returnTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 3) {
					refreshFeeSummary();
				}
			}
		});

		actualReturnDateField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				refreshFeeSummary();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				refreshFeeSummary();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				refreshFeeSummary();
			}
		});
	}

	private JPanel buildCreateSlipPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.add(buildTopFormPanel(), BorderLayout.NORTH);
		panel.add(buildIsbnPanel(), BorderLayout.CENTER);
		panel.add(buildActionPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel buildReturnSlipPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));

		JPanel topSection = new JPanel(new BorderLayout(8, 8));
		topSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
		topSection.add(buildReturnSearchPanel(), BorderLayout.NORTH);
		topSection.add(buildActiveSlipListPanel(), BorderLayout.CENTER);

		JPanel bottomSection = new JPanel(new BorderLayout(8, 8));
		bottomSection.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
		bottomSection.add(buildReturnDetailPanel(), BorderLayout.NORTH);
		bottomSection.add(new JScrollPane(returnTable), BorderLayout.CENTER);
		bottomSection.add(buildReturnFooterPanel(), BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSection, bottomSection);
		splitPane.setResizeWeight(0.4);
		splitPane.setDividerSize(14);
		splitPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		splitPane.setOneTouchExpandable(true);

		panel.add(splitPane, BorderLayout.CENTER);
		return panel;
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

	private JPanel buildReturnSearchPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Reader Lookup"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 6, 4, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addFormRow(panel, gbc, 0, "Reader ID:", searchReaderIdField, searchReaderButton);

		return panel;
	}

	private JPanel buildReturnDetailPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Return Slip Details"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 6, 4, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addFormRow(panel, gbc, 0, "Slip ID:", searchSlipIdField, searchSlipButton);
		addFormRow(panel, gbc, 1, "Matched Slip ID:", returnSlipIdField, null);
		addFormRow(panel, gbc, 2, "Borrow Date:", returnBorrowDateField, null);
		addFormRow(panel, gbc, 3, "Expected Return Date:", returnExpectedDateField, null);
		addFormRow(panel, gbc, 4, "Actual Return Date (dd/MM/yyyy):", actualReturnDateField, null);

		return panel;
	}

	private JPanel buildActiveSlipListPanel() {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.setBorder(BorderFactory.createTitledBorder("Active Slips of Reader"));
		panel.add(new JScrollPane(activeSlipTable), BorderLayout.CENTER);
		return panel;
	}

	private JPanel buildReturnFooterPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel feePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		feePanel.setBorder(BorderFactory.createTitledBorder("Fee Summary"));
		feePanel.add(new JLabel("Overdue Days:"));
		feePanel.add(overdueDaysField);
		feePanel.add(new JLabel("Late Fee:"));
		feePanel.add(lateFeeField);
		feePanel.add(new JLabel("Lost Fee:"));
		feePanel.add(lostFeeField);
		feePanel.add(new JLabel("Total Fee:"));
		feePanel.add(totalFeeField);

		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		actionPanel.add(clearReturnButton);
		actionPanel.add(confirmReturnButton);

		panel.add(feePanel, BorderLayout.CENTER);
		panel.add(actionPanel, BorderLayout.SOUTH);
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

	private void searchSlipForReturn() {
		String slipId = searchSlipIdField.getText() == null ? "" : searchSlipIdField.getText().trim();
		String readerId = searchReaderIdField.getText() == null ? "" : searchReaderIdField.getText().trim();

		if (slipId.isEmpty() && readerId.isEmpty()) {
			showError("Enter slip ID or search by reader first.");
			return;
		}

		if (slipId.isEmpty()) {
			if (activeSlipTableModel.getRowCount() == 0) {
				searchActiveSlipsByReader();
				if (activeSlipTableModel.getRowCount() == 0) {
					return;
				}
			}

			int selectedRow = activeSlipTable.getSelectedRow();
			if (selectedRow < 0) {
				selectedRow = 0;
				activeSlipTable.setRowSelectionInterval(0, 0);
			}
			slipId = String.valueOf(activeSlipTableModel.getValueAt(selectedRow, 0));
			searchSlipIdField.setText(slipId);
		}

		BorrowSlip found;
		found = borrowService.findActiveSlip(slipId, readerId.isEmpty() ? null : readerId);
		if (found == null && !readerId.isEmpty()) {
			showError("No active borrow slip found for the given slip ID and reader ID.");
			clearReturnSlipDetails();
			return;
		}

		if (found == null) {
			showError("No active borrow slip found.");
			clearReturnSlipDetails();
			return;
		}

		selectedReturnSlip = found;
		populateReturnSlipDetails(found);
		refreshFeeSummary();
	}

	private void searchActiveSlipsByReader() {
		String readerId = searchReaderIdField.getText() == null ? "" : searchReaderIdField.getText().trim();
		if (readerId.isEmpty()) {
			showError("Reader ID is required to search slips.");
			return;
		}

		List<BorrowSlip> activeSlips = borrowService.findActiveSlipsByReader(readerId);
		activeSlipTableModel.setRowCount(0);

		if (activeSlips.isEmpty()) {
			showError("No active borrow slip found for this reader.");
			clearReturnSlipDetails();
			return;
		}

		for (BorrowSlip slip : activeSlips) {
			int books = slip.getIsbnList() == null ? 0 : slip.getIsbnList().size();
			activeSlipTableModel.addRow(new Object[] {
					slip.getSlipId(),
					formatDate(slip.getBorrowDate()),
					formatDate(slip.getExpectedReturnDate()),
					books
			});
		}

		activeSlipTable.setRowSelectionInterval(0, 0);
	}

	private void populateReturnSlipDetails(BorrowSlip slip) {
		returnSlipIdField.setText(slip.getSlipId());
		searchReaderIdField.setText(slip.getReaderId());
		returnBorrowDateField.setText(formatDate(slip.getBorrowDate()));
		returnExpectedDateField.setText(formatDate(slip.getExpectedReturnDate()));
		actualReturnDateField.setText(formatDate(LocalDate.now()));

		returnTableModel.setRowCount(0);
		for (String isbn : slip.getIsbnList()) {
			Book book = borrowService.getBookByIsbn(isbn);
			String title = book == null ? "(Unknown Book)" : book.getTitle();
			double price = book == null ? 0.0 : book.getPrice();
			returnTableModel.addRow(new Object[] { isbn, title, price, Boolean.FALSE });
		}
	}

	private void refreshFeeSummary() {
		if (selectedReturnSlip == null) {
			clearFeeSummary();
			return;
		}

		try {
			LocalDate actualReturnDate = parseDateOrThrow(actualReturnDateField.getText(), "Actual return date");
			BorrowService.ReturnSummary summary = borrowService.calculateReturnSummary(
					selectedReturnSlip.getSlipId(),
					actualReturnDate,
					getLostFlagsFromTable());
			overdueDaysField.setText(Long.toString(summary.overdueDays()));
			lateFeeField.setText(formatMoney(summary.lateFee()));
			lostFeeField.setText(formatMoney(summary.lostFee()));
			totalFeeField.setText(formatMoney(summary.totalFee()));
		} catch (IllegalArgumentException ex) {
			clearFeeSummary();
		}
	}

	private void confirmReturn() {
		if (selectedReturnSlip == null) {
			showError("Please search and select a borrow slip first.");
			return;
		}

		try {
			LocalDate actualReturnDate = parseDateOrThrow(actualReturnDateField.getText(), "Actual return date");
			List<Boolean> lostFlags = getLostFlagsFromTable();
			BorrowService.ReturnSummary summary = borrowService.calculateReturnSummary(
					selectedReturnSlip.getSlipId(),
					actualReturnDate,
					lostFlags);

			String confirmMessage = "Return summary:\n"
					+ "- Slip ID: " + summary.slip().getSlipId() + "\n"
					+ "- Books: " + summary.numberOfBooks() + "\n"
					+ "- Overdue days: " + summary.overdueDays() + "\n"
					+ "- Late fee: " + formatMoney(summary.lateFee()) + "\n"
					+ "- Lost books: " + summary.lostBooks() + "\n"
					+ "- Lost fee: " + formatMoney(summary.lostFee()) + "\n"
					+ "- Total fee: " + formatMoney(summary.totalFee()) + "\n\n"
					+ "Confirm return?";

			int choice = JOptionPane.showConfirmDialog(
					this,
					confirmMessage,
					"Confirm Return",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			if (choice != JOptionPane.YES_OPTION) {
				return;
			}

			borrowService.confirmReturn(selectedReturnSlip.getSlipId(), actualReturnDate, lostFlags);
			JOptionPane.showMessageDialog(this, "Return processed successfully.", "Success",
					JOptionPane.INFORMATION_MESSAGE);
			clearReturnForm();
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

	private void clearReturnForm() {
		searchSlipIdField.setText("");
		searchReaderIdField.setText("");
		activeSlipTableModel.setRowCount(0);
		clearReturnSlipDetails();
	}

	private void clearReturnSlipDetails() {
		selectedReturnSlip = null;
		returnSlipIdField.setText("");
		returnBorrowDateField.setText("");
		returnExpectedDateField.setText("");
		actualReturnDateField.setText(formatDate(LocalDate.now()));
		returnTableModel.setRowCount(0);
		clearFeeSummary();
	}

	private void clearFeeSummary() {
		overdueDaysField.setText("");
		lateFeeField.setText("");
		lostFeeField.setText("");
		totalFeeField.setText("");
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

	private String formatMoney(double amount) {
		return CURRENCY_FORMAT.format(amount) + " VND";
	}

	private List<Boolean> getLostFlagsFromTable() {
		List<Boolean> flags = new ArrayList<>();
		for (int i = 0; i < returnTableModel.getRowCount(); i++) {
			Object value = returnTableModel.getValueAt(i, 3);
			flags.add(Boolean.TRUE.equals(value));
		}
		return flags;
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
	}
}
