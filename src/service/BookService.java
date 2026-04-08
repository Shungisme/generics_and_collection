package service;

import model.Book;
import model.BorrowSlip;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
