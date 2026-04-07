package ui;

import service.AuthService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class RegisterFrame extends JFrame {
	private final AuthService authService;
	private final JTextField usernameField = new JTextField(18);
	private final JPasswordField passwordField = new JPasswordField(18);
	private final JPasswordField confirmPasswordField = new JPasswordField(18);
	private final JButton registerButton = new JButton("Register");
	private final JButton backToLoginButton = new JButton("Back to Login");

	public RegisterFrame(AuthService authService) {
		this.authService = authService;
		setTitle("Library Management - Register");
		setSize(500, 340);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
		contentPanel.setBackground(new Color(248, 250, 252));
		setContentPane(contentPanel);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Create Librarian Account", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		contentPanel.add(titleLabel, gbc);

		gbc.gridwidth = 1;
		gbc.gridy = 1;
		gbc.gridx = 0;
		contentPanel.add(new JLabel("Username:"), gbc);

		gbc.gridx = 1;
		contentPanel.add(usernameField, gbc);

		gbc.gridy = 2;
		gbc.gridx = 0;
		contentPanel.add(new JLabel("Password:"), gbc);

		gbc.gridx = 1;
		contentPanel.add(passwordField, gbc);

		gbc.gridy = 3;
		gbc.gridx = 0;
		contentPanel.add(new JLabel("Confirm Password:"), gbc);

		gbc.gridx = 1;
		contentPanel.add(confirmPasswordField, gbc);

		gbc.gridy = 4;
		gbc.gridx = 0;
		contentPanel.add(backToLoginButton, gbc);

		gbc.gridx = 1;
		contentPanel.add(registerButton, gbc);

		registerButton.addActionListener(e -> register());
		backToLoginButton.addActionListener(e -> openLoginFrame());
		confirmPasswordField.addActionListener(e -> register());
	}

	private void register() {
		String username = usernameField.getText();
		String password = new String(passwordField.getPassword());
		String confirmPassword = new String(confirmPasswordField.getPassword());

		if (!password.equals(confirmPassword)) {
			JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			authService.register(username, password);
			JOptionPane.showMessageDialog(this, "Registration successful. Please log in.", "Success",
					JOptionPane.INFORMATION_MESSAGE);
			openLoginFrame();
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void openLoginFrame() {
		LoginFrame loginFrame = new LoginFrame(authService);
		loginFrame.setVisible(true);
		dispose();
	}
}
