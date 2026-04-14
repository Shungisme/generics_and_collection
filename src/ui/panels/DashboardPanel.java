package ui.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class DashboardPanel extends JPanel {
	public DashboardPanel() {
		setLayout(new BorderLayout());
		add(new JLabel("Library Dashboard", JLabel.CENTER), BorderLayout.CENTER);
	}
}
