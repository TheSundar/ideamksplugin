package org.intellij.vcs.mks;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class MksConfigurable
	implements Configurable
{

        	public MksConfigurable(Project p)
        	{
		theCurrentProject = p;
        	}

        	public String getDisplayName()
        	{
		return null;
        	}

        	public String getHelpTopic()
        	{
		return null;
        	}

        	public Icon getIcon()
        	{
		return null;
        	}

        	public void reset()
        	{
		MksConfiguration configuration = (MksConfiguration)theCurrentProject.getComponent(org.intellij.vcs.mks.MksConfiguration.class);
		myFldServer.setText(configuration.SERVER);
		myFldPort.setText(String.valueOf(configuration.PORT));
		myFldUser.setText(configuration.USER);
		myFldPassword.setText(configuration.PASSWORD);
		myFldProject.setText(configuration.PROJECT);
        	}

        	public void apply()
        		throws ConfigurationException
        	{
		MksConfiguration configuration = (MksConfiguration)theCurrentProject.getComponent(org.intellij.vcs.mks.MksConfiguration.class);
		configuration.SERVER = myFldServer.getText();
		configuration.PORT = Integer.parseInt(myFldPort.getText());
		configuration.USER = myFldUser.getText();
		configuration.PASSWORD = new String(myFldPassword.getPassword());
		configuration.PROJECT = myFldProject.getText();
        	}

        	public boolean isModified()
        	{
		MksConfiguration configuration = (MksConfiguration)theCurrentProject.getComponent(org.intellij.vcs.mks.MksConfiguration.class);
		boolean equals = configuration.SERVER.equals(myFldServer.getText()) && configuration.PORT == Integer.parseInt(myFldPort.getText()) && configuration.USER.equals(myFldUser.getText()) && configuration.PASSWORD.equals(new String(myFldPassword.getPassword())) && configuration.PROJECT.equals(myFldProject.getText());
		return !equals;
        	}

        	public void disposeUIResources()
        	{
		myComponent = null;
        	}

        	public JComponent createComponent()
        	{
		myComponent = new JPanel();
		myComponent.setLayout(new GridBagLayout());
		GridBagConstraints gb = new GridBagConstraints();
		Insets defaultInsets = new Insets(5, 5, 5, 0);
		gb.insets = defaultInsets;
		gb.weightx = 0.0D;
		gb.weighty = 0.0D;
		gb.gridx = 0;
		gb.gridy = 0;
		gb.fill = 2;
		JLabel serverLabel = new JLabel("Server: ");
		add(serverLabel, gb);
		gb.gridx++;
		myFldServer = new JTextField();
		Dimension fieldPreferredSize = new Dimension(100, myFldServer.getPreferredSize().height);
		myFldServer.setPreferredSize(fieldPreferredSize);
		add(myFldServer, gb);
		gb.gridx += 3;
		JLabel portLabel = new JLabel("Port: ");
		add(portLabel, gb);
		gb.gridx++;
		myFldPort = new JTextField();
		myFldPort.setPreferredSize(fieldPreferredSize);
		add(myFldPort, gb);
		gb.gridy++;
		gb.gridx = 0;
		JLabel userLabel = new JLabel("User: ");
		add(userLabel, gb);
		gb.gridx++;
		myFldUser = new JTextField();
		myFldUser.setPreferredSize(fieldPreferredSize);
		add(myFldUser, gb);
		gb.gridx += 3;
		JLabel passwordLabel = new JLabel("Password: ");
		add(passwordLabel, gb);
		gb.gridx++;
		myFldPassword = new JPasswordField();
		myFldPassword.setPreferredSize(fieldPreferredSize);
		add(myFldPassword, gb);
		gb.gridx = 0;
		gb.gridy++;
		JLabel projectLabel = new JLabel("Project: ");
		add(projectLabel, gb);
		gb.gridx++;
		gb.gridwidth = 5;
		myFldProject = new JTextField();
		myFldProject.setPreferredSize(fieldPreferredSize);
		add(myFldProject, gb);
		gb.insets = defaultInsets;
		gb.gridx += 5;
		gb.gridwidth = 1;
		gb.insets = new Insets(0, 0, 0, 0);
		JButton btnProject = new JButton("...");
		Dimension d = new Dimension(myFldProject.getPreferredSize().height, myFldProject.getPreferredSize().height);
		btnProject.setPreferredSize(d);
		btnProject.setMinimumSize(d);
		btnProject.setMaximumSize(d);
		add(btnProject, gb);
		gb.insets = defaultInsets;
		btnProject.addActionListener(new ActionListener() {

        			public void actionPerformed(ActionEvent e)
        			{
				selectProject();
        			}

		});
		gb.gridx = 10;
		gb.gridy++;
		gb.weightx = 1.0D;
		gb.weighty = 1.0D;
		gb.fill = 1;
		add(new JPanel(), gb);
		return myComponent;
        	}

        	private void add(JComponent component, GridBagConstraints gb)
        	{
		myComponent.add(component, gb);
        	}

        	private void selectProject()
        	{
		JFileChooser fileChooser = null;
		if(myFldProject.getText() == null || myFldProject.getText().equals(""))
			fileChooser = new JFileChooser();
		else
			fileChooser = new JFileChooser(myFldProject.getText());
		fileChooser.setDialogTitle("Select Project Folder");
		fileChooser.setFileFilter(new MksProjectFileFilter());
		fileChooser.setFileSelectionMode(0);
		File fileToOpen = null;
		do
        		{
			int res = fileChooser.showOpenDialog(myComponent);
			if(res != 0)
				return;
			fileToOpen = fileChooser.getSelectedFile();
			if(!fileToOpen.exists())
        			{
				JOptionPane.showMessageDialog(myComponent, MessageFormat.format("File {0} does not exist.", new Object[] {
					fileToOpen.toString()
        				}), "File Error", 0);
        			} else
        			{
				myFldProject.setText(fileToOpen.getPath());
				return;
        			}
        		} while(true);
        	}

        	private Project theCurrentProject;
        	private JPanel myComponent;
        	private JTextField myFldServer;
        	private JTextField myFldPort;
        	private JTextField myFldUser;
        	private JPasswordField myFldPassword;
        	private JTextField myFldProject;

}
