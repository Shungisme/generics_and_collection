package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager {
	private static final String DELIMITER = "|";

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
		return Arrays.asList(line.split("\\\\|", -1));
	}
}
