import javax.swing.SwingUtilities;

import service.AuthService;
import ui.LoginFrame;
import utils.FileManager;

public class App {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			AuthService authService = new AuthService(new FileManager());
			LoginFrame loginFrame = new LoginFrame(authService);
			loginFrame.setVisible(true);
		});
	}
}
