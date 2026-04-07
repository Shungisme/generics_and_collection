package ui;

import service.AuthService;
import ui.panels.DashboardPanel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class MainFrame extends JFrame {
	private final AuthService authService;

	public MainFrame(AuthService authService) {
		this.authService = authService;
		setTitle("Library Management System");
		setSize(900, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(e -> logout());
		topPanel.add(logoutButton);

		add(topPanel, BorderLayout.NORTH);
		add(new DashboardPanel(), BorderLayout.CENTER);
	}

	private void logout() {
		LoginFrame loginFrame = new LoginFrame(authService);
		loginFrame.setVisible(true);
		dispose();
	}
}
