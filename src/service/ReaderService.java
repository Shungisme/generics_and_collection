package service;

import model.Reader;
import utils.FileManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReaderService {
	private final FileManager fileManager;
	private final List<Reader> readers;

	public ReaderService(FileManager fileManager) {
		this.fileManager = fileManager;
		this.readers = new ArrayList<>(fileManager.loadReaders());
	}

	public List<Reader> getAll() {
		return new ArrayList<>(readers);
	}

	public String generateNextReaderId() {
		int maxId = readers.stream()
				.map(Reader::getReaderId)
				.filter(id -> id != null && id.matches("R\\\\d+"))
				.map(id -> Integer.parseInt(id.substring(1)))
				.max(Comparator.naturalOrder())
				.orElse(0);
		return String.format("R%03d", maxId + 1);
	}

	public void addReader(Reader reader) {
		if (reader == null || reader.getReaderId() == null || reader.getReaderId().trim().isEmpty()) {
			throw new IllegalArgumentException("Reader ID is required.");
		}
		if (isReaderIdExists(reader.getReaderId())) {
			throw new IllegalArgumentException("Reader ID already exists.");
		}
		readers.add(reader);
		persist();
	}

	public boolean isReaderIdExists(String readerId) {
		if (readerId == null || readerId.trim().isEmpty()) {
			return false;
		}
		String normalizedReaderId = readerId.trim();
		return readers.stream().anyMatch(reader -> normalizedReaderId.equalsIgnoreCase(reader.getReaderId()));
	}

	public Reader getById(String readerId) {
		Optional<Reader> found = readers.stream()
				.filter(r -> r.getReaderId().equalsIgnoreCase(readerId))
				.findFirst();
		return found.orElse(null);
	}

	public void updateReader(Reader updatedReader) {
		for (int i = 0; i < readers.size(); i++) {
			if (readers.get(i).getReaderId().equalsIgnoreCase(updatedReader.getReaderId())) {
				readers.set(i, updatedReader);
				persist();
				return;
			}
		}
		throw new IllegalArgumentException("Reader not found.");
	}

	public void deleteReader(String readerId) {
		boolean removed = readers.removeIf(r -> r.getReaderId().equalsIgnoreCase(readerId));
		if (removed) {
			persist();
		}
	}

	public List<Reader> search(String idCardQuery, String fullNameQuery) {
		String idCard = idCardQuery == null ? "" : idCardQuery.trim().toLowerCase();
		String fullName = fullNameQuery == null ? "" : fullNameQuery.trim().toLowerCase();

		return readers.stream()
				.filter(r -> idCard.isEmpty() || safeLower(r.getIdCard()).contains(idCard))
				.filter(r -> fullName.isEmpty() || safeLower(r.getFullName()).contains(fullName))
				.collect(Collectors.toList());
	}

	private String safeLower(String value) {
		return value == null ? "" : value.toLowerCase();
	}

	private void persist() {
		fileManager.saveReaders(readers);
	}
}
