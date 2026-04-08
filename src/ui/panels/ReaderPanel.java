package ui.panels;

import model.Reader;
import service.ReaderService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

public class ReaderPanel extends JPanel {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

	private final ReaderService readerService;
	private final DefaultTableModel tableModel;
	private final JTable readerTable;

	private final JTextField readerIdField = new JTextField(16);
	private final JTextField fullNameField = new JTextField(16);
	private final JTextField idCardField = new JTextField(16);
	private final JTextField dateOfBirthField = new JTextField(16);
	private final JComboBox<String> genderCombo = new JComboBox<>(new String[] { "Male", "Female", "Other" });
	private final JTextField emailField = new JTextField(16);
	private final JTextField addressField = new JTextField(16);
	private final JTextField cardCreatedDateField = new JTextField(16);
	private final JTextField cardExpiredDateField = new JTextField(16);

	private final JButton addButton = new JButton("Add Reader");
	private final JButton editButton = new JButton("Save Edit");
	private final JButton deleteButton = new JButton("Delete Reader");
	private final JButton clearButton = new JButton("Clear Form");

	public ReaderPanel(ReaderService readerService) {
		this.readerService = readerService;
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		tableModel = new DefaultTableModel(new Object[] {
				"Reader ID", "Full Name", "ID Card", "Date of Birth", "Gender", "Email", "Address", "Card Created",
				"Card Expired"
		}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		readerTable = new JTable(tableModel);
		readerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(readerTable), BorderLayout.CENTER);

		add(buildFormPanel(), BorderLayout.NORTH);
		add(buildActionPanel(), BorderLayout.SOUTH);

		readerTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				populateFormFromSelection();
			}
		});

		addButton.addActionListener(e -> addReader());
		editButton.addActionListener(e -> editReader());
		deleteButton.addActionListener(e -> deleteReader());
		clearButton.addActionListener(e -> clearForm());

		refreshTable(readerService.getAll());
		prepareNewReaderId();
	}

	private JPanel buildFormPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Reader Information"));

		readerIdField.setEditable(false);
		cardExpiredDateField.setEditable(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 6, 4, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addFormRow(panel, gbc, 0, "Reader ID:", readerIdField);
		addFormRow(panel, gbc, 1, "Full Name:", fullNameField);
		addFormRow(panel, gbc, 2, "ID Card:", idCardField);
		addFormRow(panel, gbc, 3, "Date of Birth (dd/MM/yyyy):", dateOfBirthField);
		addFormRow(panel, gbc, 4, "Gender:", genderCombo);
		addFormRow(panel, gbc, 5, "Email:", emailField);
		addFormRow(panel, gbc, 6, "Address:", addressField);
		addFormRow(panel, gbc, 7, "Card Created (dd/MM/yyyy):", cardCreatedDateField);
		addFormRow(panel, gbc, 8, "Card Expired:", cardExpiredDateField);

		return panel;
	}

	private JPanel buildActionPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(clearButton);
		panel.add(addButton);
		panel.add(editButton);
		panel.add(deleteButton);
		return panel;
	}

	private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component input) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;
		JLabel jLabel = new JLabel(label, SwingConstants.RIGHT);
		panel.add(jLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(input, gbc);
	}

	private void addReader() {
		try {
			Reader reader = buildReaderFromForm(true);
			readerService.addReader(reader);
			refreshTable(readerService.getAll());
			clearForm();
			JOptionPane.showMessageDialog(this, "Reader added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void editReader() {
		if (readerTable.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select a reader row to edit.", "No Selection",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			Reader reader = buildReaderFromForm(false);
			readerService.updateReader(reader);
			refreshTable(readerService.getAll());
			JOptionPane.showMessageDialog(this, "Reader updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteReader() {
		int row = readerTable.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Please select a reader row to delete.", "No Selection",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		String readerId = tableModel.getValueAt(row, 0).toString();
		int confirm = JOptionPane.showConfirmDialog(this,
				"Delete reader " + readerId + "?",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (confirm == JOptionPane.YES_OPTION) {
			readerService.deleteReader(readerId);
			refreshTable(readerService.getAll());
			clearForm();
		}
	}

	private Reader buildReaderFromForm(boolean isNew) {
		String readerId = isNew ? readerService.generateNextReaderId() : valueOf(readerIdField, "Reader ID");
		String fullName = valueOf(fullNameField, "Full Name");
		String idCard = valueOf(idCardField, "ID Card");
		LocalDate dateOfBirth = parseRequiredDate(dateOfBirthField.getText(), "Date of Birth");
		String gender = genderCombo.getSelectedItem() == null ? "" : genderCombo.getSelectedItem().toString();
		String email = valueOf(emailField, "Email");
		String address = valueOf(addressField, "Address");
		LocalDate cardCreatedDate = parseRequiredDate(cardCreatedDateField.getText(), "Card Created Date");

		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new IllegalArgumentException("Email format is invalid.");
		}

		Reader reader = new Reader(readerId, fullName, idCard, dateOfBirth, gender, email, address, cardCreatedDate);
		cardExpiredDateField.setText(formatDate(reader.getCardExpiredDate()));
		return reader;
	}

	private String valueOf(JTextField field, String fieldName) {
		String value = field.getText() == null ? "" : field.getText().trim();
		if (value.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		return value;
	}

	private LocalDate parseRequiredDate(String raw, String fieldName) {
		if (raw == null || raw.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return LocalDate.parse(raw.trim(), DATE_FORMAT);
		} catch (DateTimeParseException ex) {
			throw new IllegalArgumentException(fieldName + " must use format dd/MM/yyyy.");
		}
	}

	private void populateFormFromSelection() {
		int row = readerTable.getSelectedRow();
		if (row < 0) {
			return;
		}
		readerIdField.setText(tableModel.getValueAt(row, 0).toString());
		fullNameField.setText(tableModel.getValueAt(row, 1).toString());
		idCardField.setText(tableModel.getValueAt(row, 2).toString());
		dateOfBirthField.setText(tableModel.getValueAt(row, 3).toString());
		genderCombo.setSelectedItem(tableModel.getValueAt(row, 4).toString());
		emailField.setText(tableModel.getValueAt(row, 5).toString());
		addressField.setText(tableModel.getValueAt(row, 6).toString());
		cardCreatedDateField.setText(tableModel.getValueAt(row, 7).toString());
		cardExpiredDateField.setText(tableModel.getValueAt(row, 8).toString());
	}

	private void clearForm() {
		readerTable.clearSelection();
		fullNameField.setText("");
		idCardField.setText("");
		dateOfBirthField.setText("");
		genderCombo.setSelectedIndex(0);
		emailField.setText("");
		addressField.setText("");
		cardCreatedDateField.setText("");
		cardExpiredDateField.setText("");
		prepareNewReaderId();
	}

	private void prepareNewReaderId() {
		readerIdField.setText(readerService.generateNextReaderId());
	}

	private void refreshTable(List<Reader> readers) {
		tableModel.setRowCount(0);
		for (Reader reader : readers) {
			tableModel.addRow(new Object[] {
					reader.getReaderId(),
					reader.getFullName(),
					reader.getIdCard(),
					formatDate(reader.getDateOfBirth()),
					reader.getGender(),
					reader.getEmail(),
					reader.getAddress(),
					formatDate(reader.getCardCreatedDate()),
					formatDate(reader.getCardExpiredDate())
			});
		}
	}

	private String formatDate(LocalDate date) {
		return date == null ? "" : date.format(DATE_FORMAT);
	}
}
