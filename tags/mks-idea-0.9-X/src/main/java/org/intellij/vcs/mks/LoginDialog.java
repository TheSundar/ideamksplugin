package org.intellij.vcs.mks;

import java.awt.event.*;
import javax.swing.*;

public class LoginDialog extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField login;
	private JPasswordField password;
	private boolean wasCanceled = true;

	public LoginDialog(String hostAndPort) {
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);
		setTitle(MksBundle.message("mks.reconnect.to.server", hostAndPort));

		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});

// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void onOK() {
// add your code here
		wasCanceled = false;
		dispose();
	}

	private void onCancel() {
// add your code here if necessary
		dispose();
	}
	public boolean isCanceled() {
		return wasCanceled;
	}
	public static void main(String[] args) {
		LoginDialog dialog = new LoginDialog("toto:7001");
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

	public String getUser() {
		return this.login.getText();
	}
	public String getPassword () {
		return new String(this.password.getPassword());
	}
}
