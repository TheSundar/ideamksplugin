// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   MksConfigurable.java

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

// Referenced classes of package org.intellij.vcs.mks:
//			MksConfiguration, MksProjectFileFilter

public class MksConfigurable
	implements Configurable
{

        	public MksConfigurable(Project p)
        	{
/*  21*/		theCurrentProject = p;
        	}

        	public String getDisplayName()
        	{
/*  25*/		return null;
        	}

        	public String getHelpTopic()
        	{
/*  29*/		return null;
        	}

        	public Icon getIcon()
        	{
/*  33*/		return null;
        	}

        	public void reset()
        	{
/*  37*/		MksConfiguration configuration = (MksConfiguration)theCurrentProject.getComponent(org.intellij.vcs.mks.MksConfiguration.class);
/*  38*/		myFldServer.setText(configuration.SERVER);
/*  39*/		myFldPort.setText(String.valueOf(configuration.PORT));
/*  40*/		myFldUser.setText(configuration.USER);
/*  41*/		myFldPassword.setText(configuration.PASSWORD);
/*  42*/		myFldProject.setText(configuration.PROJECT);
        	}

        	public void apply()
        		throws ConfigurationException
        	{
/*  46*/		MksConfiguration configuration = (MksConfiguration)theCurrentProject.getComponent(org.intellij.vcs.mks.MksConfiguration.class);
/*  47*/		configuration.SERVER = myFldServer.getText();
/*  48*/		configuration.PORT = Integer.parseInt(myFldPort.getText());
/*  49*/		configuration.USER = myFldUser.getText();
/*  50*/		configuration.PASSWORD = new String(myFldPassword.getPassword());
/*  51*/		configuration.PROJECT = myFldProject.getText();
        	}

        	public boolean isModified()
        	{
/*  55*/		MksConfiguration configuration = (MksConfiguration)theCurrentProject.getComponent(org.intellij.vcs.mks.MksConfiguration.class);
/*  56*/		boolean equals = configuration.SERVER.equals(myFldServer.getText()) && configuration.PORT == Integer.parseInt(myFldPort.getText()) && configuration.USER.equals(myFldUser.getText()) && configuration.PASSWORD.equals(new String(myFldPassword.getPassword())) && configuration.PROJECT.equals(myFldProject.getText());
/*  62*/		return !equals;
        	}

        	public void disposeUIResources()
        	{
/*  66*/		myComponent = null;
        	}

        	public JComponent createComponent()
        	{
/*  70*/		myComponent = new JPanel();
/*  71*/		myComponent.setLayout(new GridBagLayout());
/*  72*/		GridBagConstraints gb = new GridBagConstraints();
/*  73*/		Insets defaultInsets = new Insets(5, 5, 5, 0);
/*  74*/		gb.insets = defaultInsets;
/*  75*/		gb.weightx = 0.0D;
/*  76*/		gb.weighty = 0.0D;
/*  77*/		gb.gridx = 0;
/*  78*/		gb.gridy = 0;
/*  79*/		gb.fill = 2;
/*  80*/		JLabel serverLabel = new JLabel("Server: ");
/*  81*/		add(serverLabel, gb);
/*  82*/		gb.gridx++;
/*  83*/		myFldServer = new JTextField();
/*  84*/		Dimension fieldPreferredSize = new Dimension(100, myFldServer.getPreferredSize().height);
/*  85*/		myFldServer.setPreferredSize(fieldPreferredSize);
/*  86*/		add(myFldServer, gb);
/*  87*/		gb.gridx += 3;
/*  88*/		JLabel portLabel = new JLabel("Port: ");
/*  89*/		add(portLabel, gb);
/*  90*/		gb.gridx++;
/*  91*/		myFldPort = new JTextField();
/*  92*/		myFldPort.setPreferredSize(fieldPreferredSize);
/*  93*/		add(myFldPort, gb);
/*  94*/		gb.gridy++;
/*  95*/		gb.gridx = 0;
/*  96*/		JLabel userLabel = new JLabel("User: ");
/*  97*/		add(userLabel, gb);
/*  98*/		gb.gridx++;
/*  99*/		myFldUser = new JTextField();
/* 100*/		myFldUser.setPreferredSize(fieldPreferredSize);
/* 101*/		add(myFldUser, gb);
/* 102*/		gb.gridx += 3;
/* 103*/		JLabel passwordLabel = new JLabel("Password: ");
/* 104*/		add(passwordLabel, gb);
/* 105*/		gb.gridx++;
/* 106*/		myFldPassword = new JPasswordField();
/* 107*/		myFldPassword.setPreferredSize(fieldPreferredSize);
/* 108*/		add(myFldPassword, gb);
/* 109*/		gb.gridx = 0;
/* 110*/		gb.gridy++;
/* 111*/		JLabel projectLabel = new JLabel("Project: ");
/* 112*/		add(projectLabel, gb);
/* 113*/		gb.gridx++;
/* 114*/		gb.gridwidth = 5;
/* 115*/		myFldProject = new JTextField();
/* 116*/		myFldProject.setPreferredSize(fieldPreferredSize);
/* 117*/		add(myFldProject, gb);
/* 118*/		gb.insets = defaultInsets;
/* 119*/		gb.gridx += 5;
/* 120*/		gb.gridwidth = 1;
/* 121*/		gb.insets = new Insets(0, 0, 0, 0);
/* 122*/		JButton btnProject = new JButton("...");
/* 123*/		Dimension d = new Dimension(myFldProject.getPreferredSize().height, myFldProject.getPreferredSize().height);
/* 124*/		btnProject.setPreferredSize(d);
/* 125*/		btnProject.setMinimumSize(d);
/* 126*/		btnProject.setMaximumSize(d);
/* 127*/		add(btnProject, gb);
/* 128*/		gb.insets = defaultInsets;
/* 129*/		btnProject.addActionListener(new ActionListener() {

        			public void actionPerformed(ActionEvent e)
        			{
/* 132*/				selectProject();
        			}

		});
/* 136*/		gb.gridx = 10;
/* 137*/		gb.gridy++;
/* 138*/		gb.weightx = 1.0D;
/* 139*/		gb.weighty = 1.0D;
/* 140*/		gb.fill = 1;
/* 141*/		add(new JPanel(), gb);
/* 142*/		return myComponent;
        	}

        	private void add(JComponent component, GridBagConstraints gb)
        	{
/* 146*/		myComponent.add(component, gb);
        	}

        	private void selectProject()
        	{
/* 150*/		JFileChooser fileChooser = null;
/* 151*/		if(myFldProject.getText() == null || myFldProject.getText().equals(""))
/* 152*/			fileChooser = new JFileChooser();
/* 154*/		else
/* 154*/			fileChooser = new JFileChooser(myFldProject.getText());
/* 157*/		fileChooser.setDialogTitle("Select Project Folder");
/* 158*/		fileChooser.setFileFilter(new MksProjectFileFilter());
/* 159*/		fileChooser.setFileSelectionMode(0);
/* 160*/		File fileToOpen = null;
/* 162*/		do
        		{
/* 162*/			int res = fileChooser.showOpenDialog(myComponent);
/* 163*/			if(res != 0)
/* 164*/				return;
/* 166*/			fileToOpen = fileChooser.getSelectedFile();
/* 167*/			if(!fileToOpen.exists())
        			{
/* 168*/				JOptionPane.showMessageDialog(myComponent, MessageFormat.format("File {0} does not exist.", new Object[] {
/* 168*/					fileToOpen.toString()
        				}), "File Error", 0);
        			} else
        			{
/* 172*/				myFldProject.setText(fileToOpen.getPath());
/* 173*/				return;
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
