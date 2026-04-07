package service;

import model.Librarian;
import utils.FileManager;

import java.util.List;

public class AuthService {
	private final FileManager fileManager;

	public AuthService(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void register(String username, String rawPassword) {
		String normalizedUsername = username == null ? "" : username.trim();
		String normalizedPassword = rawPassword == null ? "" : rawPassword;

		if (normalizedUsername.isEmpty()) {
			throw new IllegalArgumentException("Username is required.");
		}
		if (normalizedPassword.length() < 6) {
			throw new IllegalArgumentException("Password must be at least 6 characters.");
		}

		List<Librarian> librarians = fileManager.loadLibrarians();
		boolean duplicated = librarians.stream()
				.anyMatch(l -> l.getUsername().equalsIgnoreCase(normalizedUsername));
		if (duplicated) {
			throw new IllegalArgumentException("Username already exists.");
		}

		librarians.add(new Librarian(normalizedUsername, normalizedPassword));
		fileManager.saveLibrarians(librarians);
	}

	public boolean login(String username, String rawPassword) {
		String normalizedUsername = username == null ? "" : username.trim();
		if (normalizedUsername.isEmpty() || rawPassword == null) {
			return false;
		}

		String passwordHash = Librarian.hash(rawPassword);
		return fileManager.loadLibrarians().stream()
				.anyMatch(l -> l.getUsername().equalsIgnoreCase(normalizedUsername)
						&& l.getPassword().equals(passwordHash));
	}
}
