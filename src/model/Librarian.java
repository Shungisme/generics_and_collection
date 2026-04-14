package model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Librarian {
	private String username;
	private String password;

	public Librarian(String username, String rawPassword) {
		this.username = username;
		setPassword(rawPassword);
	}

	public Librarian(String username, String password, boolean isHashedPassword) {
		this.username = username;
		if (isHashedPassword) {
			this.password = password;
		} else {
			setPassword(password);
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String rawPassword) {
		this.password = hash(rawPassword);
	}

	public static String hash(String rawPassword) {
		if (rawPassword == null) {
			return null;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
			StringBuilder hashed = new StringBuilder();
			for (byte b : hashBytes) {
				hashed.append(String.format("%02x", b));
			}
			return hashed.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm is not available", e);
		}
	}

	@Override
	public String toString() {
		return "Librarian{" +
				"username='" + username + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
