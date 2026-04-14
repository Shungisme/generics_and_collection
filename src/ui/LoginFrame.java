package ui;

import service.AuthService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends JFrame {
	private static final int MAX_ATTEMPTS = 3;

	private final AuthService authService;
	private final JTextField usernameField = new JTextField(18);
	private final JPasswordField passwordField = new JPasswordField(18);
	private final JButton loginButton = new JButton("Login");
	private final JButton goToRegisterButton = new JButton("Create Account");
	private int attemptsLeft = MAX_ATTEMPTS;

	public LoginFrame(AuthService authService) {
		this.authService = authService;
		setTitle("Library Management - Login");
		setSize(460, 300);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBorder(new EmptyBorder(16, 18, 16, 18));
		contentPanel.setBackground(new Color(248, 250, 252));
		setContentPane(contentPanel);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Librarian Login", SwingConstants.CENTER);
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
		contentPanel.add(goToRegisterButton, gbc);

		gbc.gridx = 1;
		contentPanel.add(loginButton, gbc);

		goToRegisterButton.addActionListener(e -> openRegisterFrame());
		loginButton.addActionListener(e -> login());
		passwordField.addActionListener(e -> login());
	}

	private void login() {
		if (attemptsLeft <= 0) {
			JOptionPane.showMessageDialog(this, "Login is locked after 3 failed attempts.", "Locked",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String username = usernameField.getText();
		String password = new String(passwordField.getPassword());

		if (authService.login(username, password)) {
			MainFrame mainFrame = new MainFrame(authService);
			mainFrame.setVisible(true);
			dispose();
			return;
		}

		attemptsLeft--;
		passwordField.setText("");
		if (attemptsLeft <= 0) {
			setFormEnabled(false);
			JOptionPane.showMessageDialog(this, "Login failed. Maximum attempts reached. Account is locked.", "Locked",
					JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this,
					"Invalid username or password. Remaining attempts: " + attemptsLeft,
					"Login Failed",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setFormEnabled(boolean enabled) {
		usernameField.setEnabled(enabled);
		passwordField.setEnabled(enabled);
		loginButton.setEnabled(enabled);
		goToRegisterButton.setEnabled(enabled);
	}

	private void openRegisterFrame() {
		RegisterFrame registerFrame = new RegisterFrame(authService);
		registerFrame.setVisible(true);
		dispose();
	}
}
