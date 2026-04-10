package service;

import model.Book;
import model.BorrowSlip;
import model.Reader;
import utils.FileManager;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BorrowService {
	private static final DateTimeFormatter ID_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final long LATE_FEE_PER_DAY_PER_BOOK = 5000L;
	private static final double LOST_FEE_MULTIPLIER = 2.0;

	private final FileManager fileManager;
	private final ReaderService readerService;
	private final BookService bookService;
	private final List<BorrowSlip> slips;

	public BorrowService(FileManager fileManager, ReaderService readerService, BookService bookService) {
		this.fileManager = fileManager;
		this.readerService = readerService;
		this.bookService = bookService;
		this.slips = new ArrayList<>(fileManager.loadBorrowSlips());
	}

	public void addSlip(BorrowSlip slip) {
		slips.add(slip);
		persist();
	}

	public BorrowSlip getById(String slipId) {
		Optional<BorrowSlip> found = slips.stream()
				.filter(slip -> slip.getSlipId().equalsIgnoreCase(slipId))
				.findFirst();
		return found.orElse(null);
	}

	public List<BorrowSlip> getAll() {
		return new ArrayList<>(slips);
	}

	public Reader lookupReader(String readerId) {
		if (readerId == null || readerId.trim().isEmpty()) {
			return null;
		}
		return readerService.getById(readerId.trim());
	}

	public BorrowSlip createBorrowSlip(String readerId, LocalDate borrowDate, List<String> isbnList) {
		Reader reader = lookupReader(readerId);
		if (reader == null) {
			throw new IllegalArgumentException("Reader not found.");
		}
		if (reader.getCardExpiredDate() == null || reader.getCardExpiredDate().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("Reader card is expired.");
		}
		if (borrowDate == null) {
			throw new IllegalArgumentException("Borrow date is required.");
		}
		if (isbnList == null || isbnList.isEmpty()) {
			throw new IllegalArgumentException("At least one ISBN is required.");
		}

		List<String> cleanedIsbnList = new ArrayList<>();
		for (String isbn : isbnList) {
			if (isbn != null && !isbn.trim().isEmpty()) {
				cleanedIsbnList.add(isbn.trim());
			}
		}
		if (cleanedIsbnList.isEmpty()) {
			throw new IllegalArgumentException("At least one valid ISBN is required.");
		}

		bookService.validateBooksAvailable(cleanedIsbnList);
		bookService.decreaseQuantitiesForBorrow(cleanedIsbnList);

		String slipId = generateSlipId(borrowDate);
		BorrowSlip slip = new BorrowSlip(
				slipId,
				reader.getReaderId(),
				borrowDate,
				borrowDate.plusDays(7),
				null,
				cleanedIsbnList);

		slips.add(slip);
		persist();
		return slip;
	}

	private String generateSlipId(LocalDate borrowDate) {
		String datePart = borrowDate.format(ID_DATE_FORMAT);
		String prefix = "BS-" + datePart + "-";
		int maxSequence = 0;
		for (BorrowSlip slip : slips) {
			String slipId = slip.getSlipId();
			if (slipId != null && slipId.startsWith(prefix) && slipId.length() > prefix.length()) {
				try {
					int seq = Integer.parseInt(slipId.substring(prefix.length()));
					if (seq > maxSequence) {
						maxSequence = seq;
					}
				} catch (NumberFormatException ignored) {
					// Ignore malformed IDs from older data.
				}
			}
		}
		return prefix + String.format("%03d", maxSequence + 1);
	}

	public BorrowSlip findActiveSlip(String slipId, String readerId) {
		String normalizedSlipId = slipId == null ? "" : slipId.trim();
		String normalizedReaderId = readerId == null ? "" : readerId.trim();

		if (!normalizedSlipId.isEmpty()) {
			BorrowSlip slip = getById(normalizedSlipId);
			if (slip != null && slip.getActualReturnDate() == null) {
				return slip;
			}
			return null;
		}

		if (!normalizedReaderId.isEmpty()) {
			List<BorrowSlip> candidates = findActiveSlipsByReader(normalizedReaderId);
			return candidates.isEmpty() ? null : candidates.get(0);
		}

		return null;
	}

	public List<BorrowSlip> findActiveSlipsByReader(String readerId) {
		String normalizedReaderId = readerId == null ? "" : readerId.trim();
		if (normalizedReaderId.isEmpty()) {
			return new ArrayList<>();
		}

		return slips.stream()
				.filter(slip -> normalizedReaderId.equalsIgnoreCase(slip.getReaderId()))
				.filter(slip -> slip.getActualReturnDate() == null)
				.sorted(Comparator.comparing(BorrowSlip::getBorrowDate, Comparator.nullsLast(Comparator.reverseOrder())))
				.collect(Collectors.toList());
	}

	public Book getBookByIsbn(String isbn) {
		return bookService.getByIsbn(isbn);
	}

	public ReturnSummary calculateReturnSummary(String slipId, LocalDate actualReturnDate, List<Boolean> lostFlags) {
		if (actualReturnDate == null) {
			throw new IllegalArgumentException("Actual return date is required.");
		}

		BorrowSlip slip = getById(slipId);
		if (slip == null) {
			throw new IllegalArgumentException("Borrow slip not found.");
		}
		if (slip.getActualReturnDate() != null) {
			throw new IllegalArgumentException("Borrow slip has already been returned.");
		}

		List<String> isbnList = slip.getIsbnList() == null ? new ArrayList<>() : slip.getIsbnList();
		if (isbnList.isEmpty()) {
			throw new IllegalArgumentException("Borrow slip has no books.");
		}
		if (lostFlags == null || lostFlags.size() != isbnList.size()) {
			throw new IllegalArgumentException("Lost book flags are invalid.");
		}

		long overdueDays = 0;
		if (slip.getExpectedReturnDate() != null && actualReturnDate.isAfter(slip.getExpectedReturnDate())) {
			overdueDays = ChronoUnit.DAYS.between(slip.getExpectedReturnDate(), actualReturnDate);
		}

		long lateFee = overdueDays * LATE_FEE_PER_DAY_PER_BOOK * isbnList.size();
		double lostFee = 0;
		int lostCount = 0;
		List<String> returnableIsbnList = new ArrayList<>();

		for (int i = 0; i < isbnList.size(); i++) {
			String isbn = isbnList.get(i);
			Book book = bookService.getByIsbn(isbn);
			if (book == null) {
				throw new IllegalArgumentException("Book not found for ISBN: " + isbn);
			}

			boolean isLost = Boolean.TRUE.equals(lostFlags.get(i));
			if (isLost) {
				lostCount++;
				lostFee += book.getPrice() * LOST_FEE_MULTIPLIER;
			} else {
				returnableIsbnList.add(isbn);
			}
		}

		double totalFee = lateFee + lostFee;
		return new ReturnSummary(
				slip,
				actualReturnDate,
				overdueDays,
				lateFee,
				lostFee,
				totalFee,
				isbnList.size(),
				lostCount,
				returnableIsbnList);
	}

	public ReturnSummary confirmReturn(String slipId, LocalDate actualReturnDate, List<Boolean> lostFlags) {
		ReturnSummary summary = calculateReturnSummary(slipId, actualReturnDate, lostFlags);
		if (!summary.returnableIsbnList().isEmpty()) {
			bookService.increaseQuantitiesForReturn(summary.returnableIsbnList());
		}
		summary.slip().setActualReturnDate(actualReturnDate);
		persist();
		return summary;
	}

	public record ReturnSummary(
			BorrowSlip slip,
			LocalDate actualReturnDate,
			long overdueDays,
			long lateFee,
			double lostFee,
			double totalFee,
			int numberOfBooks,
			int lostBooks,
			List<String> returnableIsbnList) {
	}

	private void persist() {
		fileManager.saveBorrowSlips(slips);
	}
}
