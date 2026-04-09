package service;

import model.Book;
import model.BorrowSlip;
import utils.FileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookService {
	private final FileManager fileManager;
	private final List<Book> books;

	public BookService(FileManager fileManager) {
		this.fileManager = fileManager;
		this.books = new ArrayList<>(fileManager.loadBooks());
	}

	public List<Book> getAll() {
		return new ArrayList<>(books);
	}

	public void addBook(Book book) {
		if (isIsbnExists(book.getIsbn())) {
			throw new IllegalArgumentException("ISBN already exists.");
		}
		books.add(book);
		persist();
	}

	public Book getByIsbn(String isbn) {
		Optional<Book> found = books.stream()
				.filter(book -> book.getIsbn().equalsIgnoreCase(isbn))
				.findFirst();
		return found.orElse(null);
	}

	public void updateBook(Book updatedBook) {
		for (int i = 0; i < books.size(); i++) {
			if (books.get(i).getIsbn().equalsIgnoreCase(updatedBook.getIsbn())) {
				books.set(i, updatedBook);
				persist();
				return;
			}
		}
		throw new IllegalArgumentException("Book not found.");
	}

	public void deleteBook(String isbn) {
		if (hasActiveBorrowSlipReference(isbn)) {
			throw new IllegalStateException("Cannot delete book. It is referenced by active borrow slips.");
		}
		boolean removed = books.removeIf(book -> book.getIsbn().equalsIgnoreCase(isbn));
		if (removed) {
			persist();
		}
	}

	public boolean isIsbnExists(String isbn) {
		return books.stream().anyMatch(book -> book.getIsbn().equalsIgnoreCase(isbn));
	}

	public List<Book> search(String isbnQuery, String titleQuery) {
		String isbn = isbnQuery == null ? "" : isbnQuery.trim();
		String title = titleQuery == null ? "" : titleQuery.trim().toLowerCase();

		return books.stream()
				.filter(book -> isbn.isEmpty() || book.getIsbn().equalsIgnoreCase(isbn))
				.filter(book -> title.isEmpty() || safeLower(book.getTitle()).contains(title))
				.collect(Collectors.toList());
	}

	private String safeLower(String value) {
		return value == null ? "" : value.toLowerCase();
	}

	public void validateBooksAvailable(List<String> isbnList) {
		Map<String, Integer> requestCountByIsbn = countByIsbn(isbnList);
		for (Map.Entry<String, Integer> entry : requestCountByIsbn.entrySet()) {
			Book book = getByIsbn(entry.getKey());
			if (book == null) {
				throw new IllegalArgumentException("Book does not exist: " + entry.getKey());
			}
			if (book.getQuantity() < entry.getValue()) {
				throw new IllegalArgumentException("Book is out of stock or insufficient quantity: " + entry.getKey());
			}
		}
	}

	public void decreaseQuantitiesForBorrow(List<String> isbnList) {
		Map<String, Integer> requestCountByIsbn = countByIsbn(isbnList);
		validateBooksAvailable(isbnList);
		for (Book book : books) {
			Integer requestCount = requestCountByIsbn.get(book.getIsbn());
			if (requestCount != null && requestCount > 0) {
				book.setQuantity(book.getQuantity() - requestCount);
			}
		}
		persist();
	}

	private Map<String, Integer> countByIsbn(List<String> isbnList) {
		Map<String, Integer> requestCountByIsbn = new HashMap<>();
		for (String isbn : isbnList) {
			String key = isbn == null ? "" : isbn.trim();
			if (key.isEmpty()) {
				continue;
			}
			requestCountByIsbn.put(key, requestCountByIsbn.getOrDefault(key, 0) + 1);
		}
		return requestCountByIsbn;
	}

	private boolean hasActiveBorrowSlipReference(String isbn) {
		List<BorrowSlip> borrowSlips = fileManager.loadBorrowSlips();
		for (BorrowSlip slip : borrowSlips) {
			boolean isActive = slip.getActualReturnDate() == null;
			if (isActive && slip.getIsbnList() != null) {
				for (String value : slip.getIsbnList()) {
					if (isbn.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void persist() {
		fileManager.saveBooks(books);
	}
}
