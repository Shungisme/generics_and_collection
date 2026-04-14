package ui.panels;

import model.Book;
import model.BorrowSlip;
import model.Reader;
import service.BookService;
import service.BorrowService;
import service.ReaderService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsPanel extends JPanel {
	private final ReaderService readerService;
	private final BookService bookService;
	private final BorrowService borrowService;

	private final JLabel totalBooksLabel = createNumberLabel();
	private final JLabel totalReadersLabel = createNumberLabel();
	private final JLabel currentlyBorrowedBooksLabel = createNumberLabel();

	private final DefaultTableModel booksByGenreTableModel = new DefaultTableModel(new Object[] { "Genre", "Count" }, 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	private final DefaultTableModel readersByGenderTableModel = new DefaultTableModel(
			new Object[] { "Gender", "Count" }, 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	private final DefaultTableModel overdueReadersTableModel = new DefaultTableModel(
			new Object[] { "Reader ID", "Name", "Slip ID", "Days Overdue" }, 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	public StatisticsPanel(ReaderService readerService, BookService bookService, BorrowService borrowService) {
		this.readerService = readerService;
		this.bookService = bookService;
		this.borrowService = borrowService;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Total Books", buildTotalBooksTab());
		tabbedPane.addTab("Books by Genre", buildBooksByGenreTab());
		tabbedPane.addTab("Total Readers", buildTotalReadersTab());
		tabbedPane.addTab("Readers by Gender", buildReadersByGenderTab());
		tabbedPane.addTab("Currently Borrowed", buildCurrentlyBorrowedTab());
		tabbedPane.addTab("Overdue Readers", buildOverdueReadersTab());

		add(tabbedPane, BorderLayout.CENTER);

		refreshTotalBooks();
		refreshBooksByGenre();
		refreshTotalReaders();
		refreshReadersByGender();
		refreshCurrentlyBorrowedBooks();
		refreshOverdueReaders();
	}

	private JPanel buildTotalBooksTab() {
		JPanel panel = buildNumberPanel(totalBooksLabel, this::refreshTotalBooks);
		return panel;
	}

	private JPanel buildBooksByGenreTab() {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.add(new JScrollPane(new JTable(booksByGenreTableModel)), BorderLayout.CENTER);
		panel.add(buildRefreshActionPanel(this::refreshBooksByGenre), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel buildTotalReadersTab() {
		JPanel panel = buildNumberPanel(totalReadersLabel, this::refreshTotalReaders);
		return panel;
	}

	private JPanel buildReadersByGenderTab() {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.add(new JScrollPane(new JTable(readersByGenderTableModel)), BorderLayout.CENTER);
		panel.add(buildRefreshActionPanel(this::refreshReadersByGender), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel buildCurrentlyBorrowedTab() {
		JPanel panel = buildNumberPanel(currentlyBorrowedBooksLabel, this::refreshCurrentlyBorrowedBooks);
		return panel;
	}

	private JPanel buildOverdueReadersTab() {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.add(new JScrollPane(new JTable(overdueReadersTableModel)), BorderLayout.CENTER);
		panel.add(buildRefreshActionPanel(this::refreshOverdueReaders), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel buildNumberPanel(JLabel numberLabel, Runnable refreshAction) {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.add(numberLabel, BorderLayout.CENTER);
		panel.add(buildRefreshActionPanel(refreshAction), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel buildRefreshActionPanel(Runnable refreshAction) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> refreshAction.run());
		panel.add(refreshButton);
		return panel;
	}

	private JLabel createNumberLabel() {
		JLabel label = new JLabel("0", SwingConstants.CENTER);
		label.setFont(new Font("Segoe UI", Font.BOLD, 40));
		return label;
	}

	private void refreshTotalBooks() {
		int totalBooks = bookService.getAll().stream()
				.mapToInt(Book::getQuantity)
				.sum();
		totalBooksLabel.setText(String.valueOf(totalBooks));
	}

	private void refreshBooksByGenre() {
		Map<String, Integer> byGenre = new HashMap<>();
		for (Book book : bookService.getAll()) {
			String genre = normalize(book.getGenre(), "Unknown");
			byGenre.put(genre, byGenre.getOrDefault(genre, 0) + book.getQuantity());
		}

		booksByGenreTableModel.setRowCount(0);
		byGenre.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
				.forEach(entry -> booksByGenreTableModel.addRow(new Object[] { entry.getKey(), entry.getValue() }));
	}

	private void refreshTotalReaders() {
		totalReadersLabel.setText(String.valueOf(readerService.getAll().size()));
	}

	private void refreshReadersByGender() {
		int male = 0;
		int female = 0;
		int other = 0;

		for (Reader reader : readerService.getAll()) {
			String gender = normalize(reader.getGender(), "Other").toLowerCase();
			if ("male".equals(gender)) {
				male++;
			} else if ("female".equals(gender)) {
				female++;
			} else {
				other++;
			}
		}

		readersByGenderTableModel.setRowCount(0);
		readersByGenderTableModel.addRow(new Object[] { "Male", male });
		readersByGenderTableModel.addRow(new Object[] { "Female", female });
		readersByGenderTableModel.addRow(new Object[] { "Other", other });
	}

	private void refreshCurrentlyBorrowedBooks() {
		int borrowedCount = 0;
		for (BorrowSlip slip : borrowService.getAll()) {
			if (slip.getActualReturnDate() == null && slip.getIsbnList() != null) {
				borrowedCount += slip.getIsbnList().size();
			}
		}
		currentlyBorrowedBooksLabel.setText(String.valueOf(borrowedCount));
	}

	private void refreshOverdueReaders() {
		Map<String, Reader> readerById = new HashMap<>();
		for (Reader reader : readerService.getAll()) {
			readerById.put(reader.getReaderId(), reader);
		}

		overdueReadersTableModel.setRowCount(0);
		LocalDate today = LocalDate.now();
		List<BorrowSlip> slips = borrowService.getAll();
		for (BorrowSlip slip : slips) {
			if (slip.getActualReturnDate() != null || slip.getExpectedReturnDate() == null) {
				continue;
			}
			if (!slip.getExpectedReturnDate().isBefore(today)) {
				continue;
			}

			long daysOverdue = ChronoUnit.DAYS.between(slip.getExpectedReturnDate(), today);
			Reader reader = readerById.get(slip.getReaderId());
			String readerName = reader != null ? normalize(reader.getFullName(), "Unknown") : "Unknown";

			overdueReadersTableModel.addRow(new Object[] {
					slip.getReaderId(),
					readerName,
					slip.getSlipId(),
					daysOverdue
			});
		}
	}

	private String normalize(String value, String fallback) {
		if (value == null || value.trim().isEmpty()) {
			return fallback;
		}
		return value.trim();
	}
}
