package org.intellij.vcs.mks;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thibaut Fagart
 */
public class MksConfigurableForm implements Configurable {
	private JTextField serverTextField;
	private JPanel mainPanel;
	private JTextField portTextField;
	private JTextField userTextField;
	private JTable MKSSICommandLineTable;
	private JPasswordField passwordField;
	private DefaultTableModel tableModel;
	private MksConfiguration configuration;
	private JComboBox charsetEditorCombo = new JComboBox();
	private static final String DEFAULT_ENCODING_VALUE = "<DEFAULT>";

	/**
	 * for design only
	 */
	public MksConfigurableForm() {
		this(null);
	}

	public MksConfigurableForm(Project myProject) {
		this.configuration = ApplicationManager.getApplication().getComponent(MksConfiguration.class);

		String[] charSetNames = Charset.availableCharsets().keySet().toArray(new String[0]);
		Arrays.sort(charSetNames);
		String[] charSetNamesWithDefault = new String[charSetNames.length + 1];
		charSetNamesWithDefault[0] = DEFAULT_ENCODING_VALUE;
		System.arraycopy(charSetNames, 0, charSetNamesWithDefault, 1, charSetNames.length);
		charsetEditorCombo.setModel(new DefaultComboBoxModel(charSetNamesWithDefault));
		setTableModel(createTableModel());
	}

	private MksConfiguration getDesignTimeConfiguration() {
		MksConfiguration configuration1 = new MksConfiguration();
		configuration1.SI_ENCODINGS.getMap().put("viewrevision", "ISO-8859-1");

		return configuration1;

	}

	private void setTableModel(@NotNull DefaultTableModel model) {
		tableModel = model;
		MKSSICommandLineTable.setModel(tableModel);
		MKSSICommandLineTable.getColumn("Encoding").setCellEditor(new DefaultCellEditor(charsetEditorCombo));
	}

	@NotNull
	@Nls
	private DefaultTableModel createTableModel() {

		@Nls String commandColumnName = "Command";
		@Nls String EncodingCommandName = "Encoding";
		return new DefaultTableModel(createTableData(getConfiguration()), new String[]{commandColumnName, EncodingCommandName}) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}
		};
	}

	private Object[][] createTableData(MksConfiguration configuration) {
		String[] knownCommands = MksVcs.getCommands();

		Object[][] result = new Object[knownCommands.length + 1][];
		result[0] = new String[]{"Default", configuration.defaultEncoding};
		int i = 1;
		for (String command : knownCommands) {
			String commandEncoding = configuration.SI_ENCODINGS.getMap().get(command);
			result[i++] = new String[]{command, (commandEncoding == null) ? DEFAULT_ENCODING_VALUE : commandEncoding};
		}
		return result;
	}

	@Nls
	public String getDisplayName() {
		return "MKS";
	}

	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	@Nullable
	public Icon getIcon() {
		return null;
	}

	public void apply() throws ConfigurationException {
		MksConfiguration configuration = getConfiguration();
		configuration.SERVER = serverTextField.getText();
		configuration.PORT = Integer.parseInt(portTextField.getText());
		configuration.USER = userTextField.getText();
		configuration.PASSWORD = new String(passwordField.getPassword());
//		configuration.PROJECT = myFldProject.getText();
		configuration.SI_ENCODINGS.setMap(getEncodingMap());
		configuration.defaultEncoding = getDefaultEncoding();
	}

	private String getDefaultEncoding() {
		return (String) tableModel.getValueAt(0, 1);
	}

	private Map<String, String> getEncodingMap() {
		Map<String, String> result = new HashMap<String, String>();
		for (int row = 1, max = tableModel.getRowCount(); row < max; row++) {
			String command = (String) tableModel.getValueAt(row, 0);
			String encoding = (String) tableModel.getValueAt(row, 1);
			if (!encoding.equals(DEFAULT_ENCODING_VALUE)) {
				result.put(command, encoding);
			} else {
				// ignoring as it is the default value
			}
		}
		return result;
	}

	public void disposeUIResources() {
	}

	public JComponent createComponent() {
		return mainPanel;
	}

	public void reset() {
		MksConfiguration configuration = getConfiguration();
		serverTextField.setText(configuration.SERVER);
		portTextField.setText(String.valueOf(configuration.PORT));
		userTextField.setText(configuration.USER);
		passwordField.setText(configuration.PASSWORD);
//		pro.setText(configuration.PROJECT);
		setTableModel(createTableModel());
	}

	private MksConfiguration getConfiguration() {
		return configuration;
	}

	public boolean isModified() {
		MksConfiguration configuration = getConfiguration();
		boolean equals = configuration.SERVER.equals(serverTextField.getText())
				&& configuration.PORT == Integer.parseInt(portTextField.getText())
				&& configuration.USER.equals(userTextField.getText())
				&& configuration.PASSWORD.equals(new String(passwordField.getPassword()))
//			&& configuration.PROJECT.equals(.getText())
				&& !isEncodingsModified(configuration);
		return !equals;
	}

	private boolean isEncodingsModified(MksConfiguration configuration) {
		// todo
		return true;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("MksConfigurableForm");
		frame.setContentPane(new MksConfigurableForm().mainPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}


}
