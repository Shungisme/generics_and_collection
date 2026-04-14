import javax.swing.SwingUtilities;

import ui.LoginFrame;

public class App {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			LoginFrame loginFrame = new LoginFrame();
			loginFrame.setVisible(true);
		});
	}
}
