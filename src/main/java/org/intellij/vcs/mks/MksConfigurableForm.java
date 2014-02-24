package org.intellij.vcs.mks;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.intellij.vcs.mks.model.MksServerInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Thibaut Fagart
 */
public class MksConfigurableForm implements Configurable {
    private JPanel mainPanel;
    private JTable MKSSICommandLineTable;
    private JTextArea ignoredServersTA;
    private JTextField datePatternString;
    private JCheckBox resyncNonMembers;
    private DefaultTableModel tableModel;
    private MksConfiguration configuration;
    private JComboBox charsetEditorCombo = new JComboBox();
    private static final String DEFAULT_ENCODING_VALUE = "<DEFAULT>";

    public MksConfigurableForm(@NotNull final MksConfiguration configuration) {
        this.configuration = configuration;
        this.resyncNonMembers.setSelected(configuration.isSynchronizeNonMembers());
        initSupportedCharsets();
        reset();
    }

    private void initIgnoredServers() {
        final String ignoredServers = configuration.getIgnoredServers();
        ignoredServersTA.setText(ignoredServers);
    }

    private void initCommands() {
        setTableModel(createTableModel());
    }

    private void initSupportedCharsets() {
        final Set<String> stringSet = Charset.availableCharsets().keySet();
        final String[] charSetNames = stringSet.toArray(new String[stringSet.size()]);
        Arrays.sort(charSetNames);
        final String[] charSetNamesWithDefault = new String[charSetNames.length + 1];
        charSetNamesWithDefault[0] = MksConfigurableForm.DEFAULT_ENCODING_VALUE;
        System.arraycopy(charSetNames, 0, charSetNamesWithDefault, 1, charSetNames.length);
        charsetEditorCombo.setModel(new DefaultComboBoxModel(charSetNamesWithDefault));
    }

    private void setTableModel(@NotNull final DefaultTableModel model) {
        this.tableModel = model;
        MKSSICommandLineTable.setModel(this.tableModel);
        MKSSICommandLineTable.getColumn("Encoding").setCellEditor(new DefaultCellEditor(this.charsetEditorCombo));
    }

    @NotNull
    @Nls
    private DefaultTableModel createTableModel() {

        @Nls final String commandColumnName = "Command";
        @Nls final String EncodingCommandName = "Encoding";
        return new DefaultTableModel(createTableData(getConfiguration()),
                new String[]{commandColumnName, EncodingCommandName}) {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return column == 1;
            }
        };
    }

    private Object[][] createTableData(final MksConfiguration configuration) {
        final String[] knownCommands = MksVcs.getCommands();

        final Object[][] result = new Object[knownCommands.length + 1][];
        result[0] = new String[]{"Default", configuration.defaultEncoding};
        int i = 1;
        for (final String command : knownCommands) {
            final String commandEncoding = configuration.getSiEncodings().get(command);
            result[i++] = new String[]{command,
                    (commandEncoding == null) ? MksConfigurableForm.DEFAULT_ENCODING_VALUE : commandEncoding};
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
        final MksConfiguration configuration = getConfiguration();
//		configuration.PROJECT = myFldProject.getText();
        configuration.setSiEncodings(getEncodingMap());
        configuration.defaultEncoding = getDefaultEncoding();
        configuration.setSynchronizeNonMembers(this.resyncNonMembers.isSelected());
        try {
            configuration.setDatePattern(validateDatePattern());
        } catch (Exception e) {
            throw new ConfigurationException(
                    "Bad date pattern " + this.datePatternString.getText() + ", must be a valid" +
                            " java dateFormat pattern");

        }
        final List<MksServerInfo> ignoredServersListOld = parseIgnoredServers(configuration.getIgnoredServers());
        final List<MksServerInfo> ignoredServersListNew;
        try {
            ignoredServersListNew = parseIgnoredServers(ignoredServersTA.getText());
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(e.getMessage());
        }

        for (final MksServerInfo serverInfo : ignoredServersListOld) {
            if (!ignoredServersListNew.contains(serverInfo)) {
                configuration.serverIsSiServer(serverInfo, true);
            }
        }
        for (final MksServerInfo serverInfo : ignoredServersListNew) {
            if (!ignoredServersListOld.contains(serverInfo)) {
                configuration.serverIsSiServer(serverInfo, false);
            }
        }
    }

    private String validateDatePattern() {
        DateFormat format = new SimpleDateFormat(this.datePatternString.getText());
        format.format(new Date());
        return this.datePatternString.getText();
    }

    private List<MksServerInfo> parseIgnoredServers(final String serverList) {
        final StringTokenizer tok = new StringTokenizer(serverList, ",", false);
        final ArrayList<MksServerInfo> ret = new ArrayList<MksServerInfo>();
        while (tok.hasMoreTokens()) {
            final StringTokenizer tok2 = new StringTokenizer(tok.nextToken(), ":");
            if (tok2.countTokens() != 2) {
                throw new IllegalArgumentException(
                        "bad server list, it has to be a comma separated list of <host:port>, example \"myServer1:7001,myServer2:7001\"");
            }
            ret.add(new MksServerInfo("anon", tok2.nextToken(), tok2.nextToken()));
        }
        return ret;

    }

    private String getDefaultEncoding() {
        return (String) tableModel.getValueAt(0, 1);
    }

    private Map<String, String> getEncodingMap() {
        final Map<String, String> result = new HashMap<String, String>();
        for (int row = 1, max = tableModel.getRowCount(); row < max; row++) {
            final String command = (String) tableModel.getValueAt(row, 0);
            final String encoding = (String) tableModel.getValueAt(row, 1);
            if (!encoding.equals(MksConfigurableForm.DEFAULT_ENCODING_VALUE)) {
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
        return this.mainPanel;
    }

    public final void reset() {
        initCommands();
        initIgnoredServers();
        initDatePattern();
    }

    private void initDatePattern() {
        this.datePatternString.setText(configuration.getDatePattern());
    }

    private MksConfiguration getConfiguration() {
        return this.configuration;
    }

    public boolean isModified() {
        final MksConfiguration configuration = getConfiguration();
        boolean isDateChanged;
        try {
            isDateChanged = !validateDatePattern().equals(configuration.getDatePattern());
        } catch (Exception e) {
            isDateChanged = true;
        }
        return isResyncNonMembersModified(configuration)
                || isEncodingsModified(configuration)
                || (!configuration.getIgnoredServers().equals(ignoredServersTA.getText()))
                || isDateChanged
//			&& configuration.PROJECT.equals(.getText())
                ;
    }

    private boolean isResyncNonMembersModified(MksConfiguration configuration) {
        return configuration.isSynchronizeNonMembers() && this.resyncNonMembers.isSelected();
    }

    private boolean isEncodingsModified(final MksConfiguration configuration) {
        return true;
    }

    public static void main(final String[] args) {
        final JFrame frame = new JFrame("MksConfigurableForm");
        final MksConfiguration config = new MksConfiguration();
        config.serverIsSiServer(new MksServerInfo("anon", "ignoredServer", "7001"), false);
        frame.setContentPane(new MksConfigurableForm(config).mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new FormLayout("fill:d:noGrow,fill:532px:grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow"));
        final JLabel label1 = new JLabel();
        label1.setText("MKS SI command line encodings");
        label1.setDisplayedMnemonic('E');
        label1.setDisplayedMnemonicIndex(20);
        CellConstraints cc = new CellConstraints();
        mainPanel.add(label1, cc.xyw(1, 3, 2));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, cc.xyw(1, 5, 2, CellConstraints.FILL, CellConstraints.FILL));
        MKSSICommandLineTable = new JTable();
        scrollPane1.setViewportView(MKSSICommandLineTable);
        final JLabel label2 = new JLabel();
        label2.setText("Ignored servers (comma separated list of <host:port>)");
        label2.setDisplayedMnemonic('I');
        label2.setDisplayedMnemonicIndex(0);
        mainPanel.add(label2, cc.xy(2, 7));
        final JScrollPane scrollPane2 = new JScrollPane();
        mainPanel.add(scrollPane2, cc.xy(2, 9, CellConstraints.DEFAULT, CellConstraints.FILL));
        ignoredServersTA = new JTextArea();
        ignoredServersTA.setRows(3);
        scrollPane2.setViewportView(ignoredServersTA);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow"));
        mainPanel.add(panel1, cc.xy(2, 1));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("org/intellij/vcs/mks/mksBundle").getString("config.date_pattern"));
        label3.setToolTipText(ResourceBundle.getBundle("org/intellij/vcs/mks/mksBundle").getString("config.date_pattern.hover"));
        panel1.add(label3, cc.xy(1, 3));
        datePatternString = new JTextField();
        panel1.add(datePatternString, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        resyncNonMembers = new JCheckBox();
        resyncNonMembers.setHorizontalAlignment(10);
        resyncNonMembers.setHorizontalTextPosition(10);
        resyncNonMembers.setText("");
        panel1.add(resyncNonMembers, cc.xy(3, 1));
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, ResourceBundle.getBundle("org/intellij/vcs/mks/mksBundle").getString("config.resyncNonMembers"));
        label4.setToolTipText(ResourceBundle.getBundle("org/intellij/vcs/mks/mksBundle").getString("config.resyncNonMembers.hover"));
        panel1.add(label4, cc.xy(1, 1));
        label1.setLabelFor(scrollPane1);
        label2.setLabelFor(ignoredServersTA);
        label3.setLabelFor(datePatternString);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
