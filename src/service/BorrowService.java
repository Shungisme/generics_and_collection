package service;

import model.BorrowSlip;
import model.Reader;
import utils.FileManager;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class BorrowService {
	private static final DateTimeFormatter ID_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

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
				.filter(slip -> slip.getSlipId().equals(slipId))
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

	private void persist() {
		fileManager.saveBorrowSlips(slips);
	}
}
