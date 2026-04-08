package service;

import model.Reader;
import utils.FileManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
		readers.add(reader);
		persist();
	}

	public Reader getById(String readerId) {
		Optional<Reader> found = readers.stream()
				.filter(r -> r.getReaderId().equals(readerId))
				.findFirst();
		return found.orElse(null);
	}

	public void updateReader(Reader updatedReader) {
		for (int i = 0; i < readers.size(); i++) {
			if (readers.get(i).getReaderId().equals(updatedReader.getReaderId())) {
				readers.set(i, updatedReader);
				persist();
				return;
			}
		}
		throw new IllegalArgumentException("Reader not found.");
	}

	public void deleteReader(String readerId) {
		boolean removed = readers.removeIf(r -> r.getReaderId().equals(readerId));
		if (removed) {
			persist();
		}
	}

	private void persist() {
		fileManager.saveReaders(readers);
	}
}
