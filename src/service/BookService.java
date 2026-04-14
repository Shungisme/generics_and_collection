package service;

import model.Book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookService {
	private final List<Book> books = new ArrayList<>();
	private final Map<String, Book> bookByIsbn = new HashMap<>();

	public void addBook(Book book) {
		books.add(book);
		bookByIsbn.put(book.getIsbn(), book);
	}

	public Book getByIsbn(String isbn) {
		return bookByIsbn.get(isbn);
	}

	public List<Book> getAll() {
		return new ArrayList<>(books);
	}
}
