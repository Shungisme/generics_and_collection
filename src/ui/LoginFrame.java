package ui;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class LoginFrame extends JFrame {
	public LoginFrame() {
		setTitle("Library Management - Login");
		setSize(400, 250);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		add(new JLabel("Login Screen", JLabel.CENTER));
	}
}
