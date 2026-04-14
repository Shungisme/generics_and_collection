package service;

import model.Reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReaderService {
	private final List<Reader> readers = new ArrayList<>();
	private final Map<String, Reader> readerById = new HashMap<>();

	public void addReader(Reader reader) {
		readers.add(reader);
		readerById.put(reader.getReaderId(), reader);
	}

	public Reader getById(String readerId) {
		return readerById.get(readerId);
	}

	public List<Reader> getAll() {
		return new ArrayList<>(readers);
	}
}
