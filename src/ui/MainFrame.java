package ui;

import service.AuthService;
import service.BookService;
import service.BorrowService;
import service.ReaderService;
import ui.panels.BookPanel;
import ui.panels.BorrowPanel;
import ui.panels.ReaderPanel;
import ui.panels.StatisticsPanel;
import utils.FileManager;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
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

		JTabbedPane tabbedPane = new JTabbedPane();
		FileManager fileManager = new FileManager();
		ReaderService readerService = new ReaderService(fileManager);
		BookService bookService = new BookService(fileManager);
		BorrowService borrowService = new BorrowService(fileManager, readerService, bookService);
		tabbedPane.addTab("Readers", new ReaderPanel(readerService));
		tabbedPane.addTab("Books", new BookPanel(bookService));
		tabbedPane.addTab("Borrow", new BorrowPanel(borrowService));
		tabbedPane.addTab("Statistics", new StatisticsPanel(readerService, bookService, borrowService));

		add(topPanel, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
	}

	private void logout() {
		LoginFrame loginFrame = new LoginFrame(authService);
		loginFrame.setVisible(true);
		dispose();
	}
}
