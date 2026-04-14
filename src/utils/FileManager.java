package utils;

import model.Book;
import model.BorrowSlip;
import model.Reader;
import model.Librarian;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager {
	private static final String DELIMITER = "|";
	private static final String DEFAULT_DATA_DIR = "data";
	private static final String READERS_FILE = "readers.txt";
	private static final String BOOKS_FILE = "books.txt";
	private static final String BORROW_SLIPS_FILE = "borrowslips.txt";
	private static final String LIBRARIANS_FILE = "librarians.txt";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final Path dataDirectory;

	public FileManager() {
		this(DEFAULT_DATA_DIR);
	}

	public FileManager(String dataDirectory) {
		this.dataDirectory = Path.of(dataDirectory);
	}

	public List<String> readAllLines(String filePath) throws IOException {
		Path path = Path.of(filePath);
		if (!Files.exists(path)) {
			return new ArrayList<>();
		}
		return Files.readAllLines(path, StandardCharsets.UTF_8);
	}

	public void writeAllLines(String filePath, List<String> lines) throws IOException {
		Path path = Path.of(filePath);
		if (path.getParent() != null) {
			Files.createDirectories(path.getParent());
		}
		Files.write(path, lines, StandardCharsets.UTF_8);
	}

	public void appendLine(String filePath, String line) throws IOException {
		Path path = Path.of(filePath);
		if (path.getParent() != null) {
			Files.createDirectories(path.getParent());
		}
		if (!Files.exists(path)) {
			Files.write(path, List.of(line), StandardCharsets.UTF_8);
			return;
		}
		Files.write(path, List.of(line), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
	}

	public String toPipeLine(List<String> values) {
		return String.join(DELIMITER, values);
	}

	public List<String> fromPipeLine(String line) {
		return Arrays.asList(line.split("\\|", -1));
	}

	public List<Librarian> loadLibrarians() {
		List<Librarian> librarians = new ArrayList<>();
		Path filePath = dataDirectory.resolve(LIBRARIANS_FILE);

		try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				String[] parts = line.split("\\|", -1);
				if (parts.length < 2) {
					continue;
				}
				librarians.add(new Librarian(parts[0], parts[1], true));
			}
		} catch (FileNotFoundException | NoSuchFileException e) {
			return librarians;
		} catch (IOException e) {
			throw new RuntimeException("Failed to load librarians", e);
		}

		return librarians;
	}

	public void saveLibrarians(List<Librarian> librarians) {
		Path filePath = dataDirectory.resolve(LIBRARIANS_FILE);
		try {
			Files.createDirectories(dataDirectory);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create data directory", e);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
			for (Librarian librarian : librarians) {
				String line = librarian.getUsername() + DELIMITER + librarian.getPassword();
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to save librarians", e);
		}
	}

	public List<Reader> loadReaders() {
		List<Reader> readers = new ArrayList<>();
		Path filePath = dataDirectory.resolve(READERS_FILE);

		try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				String[] parts = line.split("\\|", -1);
				if (parts.length < 9) {
					continue;
				}

				Reader item = new Reader(
						parts[0],
						parts[1],
						parts[2],
						parseDate(parts[3]),
						parts[4],
						parts[5],
						parts[6],
						parseDate(parts[7]));
				if (!parts[8].isEmpty()) {
					item.setCardExpiredDate(parseDate(parts[8]));
				}
				readers.add(item);
			}
		} catch (FileNotFoundException | NoSuchFileException e) {
			return readers;
		} catch (IOException e) {
			throw new RuntimeException("Failed to load readers", e);
		}

		return readers;
	}

	public void saveReaders(List<Reader> readers) {
		Path filePath = dataDirectory.resolve(READERS_FILE);
		try {
			Files.createDirectories(dataDirectory);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create data directory", e);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
			for (Reader reader : readers) {
				String line = String.join(DELIMITER,
						nullSafe(reader.getReaderId()),
						nullSafe(reader.getFullName()),
						nullSafe(reader.getIdCard()),
						formatDate(reader.getDateOfBirth()),
						nullSafe(reader.getGender()),
						nullSafe(reader.getEmail()),
						nullSafe(reader.getAddress()),
						formatDate(reader.getCardCreatedDate()),
						formatDate(reader.getCardExpiredDate()));
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to save readers", e);
		}
	}

	public List<Book> loadBooks() {
		List<Book> books = new ArrayList<>();
		Path filePath = dataDirectory.resolve(BOOKS_FILE);

		try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				String[] parts = line.split("\\|", -1);
				if (parts.length < 8) {
					continue;
				}

				books.add(new Book(
						parts[0],
						parts[1],
						parts[2],
						parts[3],
						Integer.parseInt(parts[4]),
						parts[5],
						Double.parseDouble(parts[6]),
						Integer.parseInt(parts[7])));
			}
		} catch (FileNotFoundException | NoSuchFileException e) {
			return books;
		} catch (IOException e) {
			throw new RuntimeException("Failed to load books", e);
		}

		return books;
	}

	public void saveBooks(List<Book> books) {
		Path filePath = dataDirectory.resolve(BOOKS_FILE);
		try {
			Files.createDirectories(dataDirectory);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create data directory", e);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
			for (Book book : books) {
				String line = String.join(DELIMITER,
						nullSafe(book.getIsbn()),
						nullSafe(book.getTitle()),
						nullSafe(book.getAuthor()),
						nullSafe(book.getPublisher()),
						Integer.toString(book.getYearPublished()),
						nullSafe(book.getGenre()),
						Double.toString(book.getPrice()),
						Integer.toString(book.getQuantity()));
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to save books", e);
		}
	}

	public List<BorrowSlip> loadBorrowSlips() {
		List<BorrowSlip> slips = new ArrayList<>();
		Path filePath = dataDirectory.resolve(BORROW_SLIPS_FILE);

		try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				String[] parts = line.split("\\|", -1);
				if (parts.length < 6) {
					continue;
				}

				List<String> isbnList = new ArrayList<>();
				if (!parts[5].trim().isEmpty()) {
					String[] isbnParts = parts[5].split(",");
					for (String isbn : isbnParts) {
						isbnList.add(isbn.trim());
					}
				}

				slips.add(new BorrowSlip(
						parts[0],
						parts[1],
						parseDate(parts[2]),
						parseDate(parts[3]),
						parseDate(parts[4]),
						isbnList));
			}
		} catch (FileNotFoundException | NoSuchFileException e) {
			return slips;
		} catch (IOException e) {
			throw new RuntimeException("Failed to load borrow slips", e);
		}

		return slips;
	}

	private static String formatDate(LocalDate date) {
		return date == null ? "" : date.format(DATE_FORMATTER);
	}

	private static LocalDate parseDate(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return LocalDate.parse(value, DATE_FORMATTER);
	}

	private static String nullSafe(String value) {
		return value == null ? "" : value;
	}
}
