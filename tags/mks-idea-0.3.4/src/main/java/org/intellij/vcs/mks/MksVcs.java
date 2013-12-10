// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   MksVcs.java

package org.intellij.vcs.mks;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiClient;

// Referenced classes of package org.intellij.vcs.mks:
//			MksConfigurable

public class MksVcs extends AbstractVcs
	implements ProjectComponent
{

        	public MksVcs(Project project)
        	{
/*  37*/		super(project);
        	}

        	public void initComponent()
        	{
        	}

        	public void disposeComponent()
        	{
        	}

        	public String getComponentName()
        	{
/*  47*/		return "MKS";
        	}

        	public void projectOpened()
        	{
        	}

        	public void projectClosed()
        	{
        	}

        	public void addDirectory(String s, String s1, Object obj)
        		throws VcsException
        	{
        	}

        	public void addFile(String s, String s1, Object obj)
        		throws VcsException
        	{
        	}

        	public void checkinFile(String s, Object obj)
        		throws VcsException
        	{
        	}

        	public Configurable getConfigurable()
        	{
/*  69*/		return new MksConfigurable(myProject);
        	}

        	public String getDisplayName()
        	{
/*  73*/		return "MKS";
        	}

        	public byte[] getFileContent(String path)
        		throws VcsException
        	{
/*  77*/		return new byte[0];
        	}

        	public String getName()
        	{
/*  81*/		return "MKS";
        	}

        	public void removeDirectory(String s, Object obj)
        		throws VcsException
        	{
        	}

        	public void removeFile(String s, Object obj)
        		throws VcsException
        	{
        	}

        	public void start()
        		throws VcsException
        	{
/*  93*/		super.start();
/*  94*/		if(!isValid())
/*  95*/			startClient();
/*  97*/		initToolWindow();
        	}

        	public void shutdown()
        		throws VcsException
        	{
/* 101*/		super.shutdown();
/* 102*/		unregisterToolWindow();
        	}

        	public static boolean isValid()
        	{
/* 106*/		return isClientValid;
        	}

        	public static void startClient()
        	{
/* 110*/		if(!isClientLoaded)
/* 112*/			try
        			{
/* 112*/				System.loadLibrary("mkscmapi");
/* 113*/				isClientLoaded = true;
/* 114*/				CLIENT = new TriclopsSiClient();
/* 115*/				if(!CLIENT.isIntegrityClientRunning())
/* 116*/					CLIENT.initialize("IntelliJ IDEA", 1, 1);
/* 118*/				isClientValid = true;
        			}
/* 119*/			catch(Throwable t)
        			{
/* 120*/				isClientLoaded = false;
/* 121*/				isClientValid = false;
        			}
        	}

        	public static void logError(String msg)
        	{
/* 128*/		try
        		{
/* 128*/			CLIENT.log("IDEA Integration", 2, 4, msg);
        		}
/* 129*/		catch(TriclopsException e) { }
        	}

        	public void showErrors(java.util.List list, String action)
        	{
/* 134*/		if(list.size() > 0)
        		{
/* 135*/			StringBuffer buffer = new StringBuffer(mksTextArea.getText());
/* 136*/			buffer.append("\n");
/* 137*/			buffer.append(action + " Error: ");
        			VcsException e;
/* 138*/			for(Iterator iterator = list.iterator(); iterator.hasNext(); buffer.append(e.getMessage()))
        			{
/* 139*/				e = (VcsException)iterator.next();
/* 140*/				buffer.append("\n");
        			}

/* 143*/			mksTextArea.setText(buffer.toString());
        		}
        	}

        	private void initToolWindow()
        	{
/* 148*/		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
/* 149*/		mksContentPanel = new JPanel(new BorderLayout());
/* 150*/		mksTextArea = new JTextPane();
/* 151*/		mksTextArea.setEditable(false);
/* 152*/		javax.swing.text.Style def = StyleContext.getDefaultStyleContext().getStyle("default");
/* 153*/		javax.swing.text.Style regular = mksTextArea.addStyle("REGULAR", def);
/* 154*/		StyleConstants.setFontFamily(def, "SansSerif");
/* 155*/		javax.swing.text.Style s = mksTextArea.addStyle("ITALIC", regular);
/* 156*/		StyleConstants.setItalic(s, true);
/* 157*/		s = mksTextArea.addStyle("BOLD", regular);
/* 158*/		StyleConstants.setBold(s, true);
/* 159*/		mksContentPanel.add(new JScrollPane(mksTextArea), "Center");
/* 160*/		mksToolWindow = toolWindowManager.registerToolWindow("MKS", mksContentPanel, ToolWindowAnchor.BOTTOM);
/* 161*/		java.net.URL iconUrl = getClass().getResource("/icons/mks.gif");
/* 162*/		javax.swing.Icon icn = new ImageIcon(iconUrl);
/* 163*/		mksToolWindow.setIcon(icn);
/* 164*/		final JPopupMenu menu = new JPopupMenu();
/* 165*/		JMenuItem item = new JMenuItem("Clear");
/* 166*/		item.addActionListener(new ActionListener() {

        			public void actionPerformed(ActionEvent e)
        			{
/* 169*/				mksTextArea.setText("");
        			}

		});
/* 172*/		menu.add(item);
/* 173*/		mksTextArea.addMouseListener(new MouseAdapter() {

        			public void mousePressed(MouseEvent e)
        			{
/* 176*/				maybeShowPopup(e, menu);
        			}

        			public void mouseReleased(MouseEvent e)
        			{
/* 180*/				maybeShowPopup(e, menu);
        			}

		});
        	}

        	private void maybeShowPopup(MouseEvent e, JPopupMenu menu)
        	{
/* 186*/		if(e.isPopupTrigger())
/* 187*/			menu.show(mksTextArea, e.getX(), e.getY());
        	}

        	private void unregisterToolWindow()
        	{
/* 192*/		ToolWindowManager.getInstance(myProject).unregisterToolWindow("MKS");
/* 193*/		mksToolWindow = null;
        	}

        	public static AbstractVcs getInstance(Project project)
        	{
/* 197*/		return (MksVcs)project.getComponent(org.intellij.vcs.mks.MksVcs.class);
        	}

        	public static TriclopsSiClient CLIENT;
        	public static final String TOOL_WINDOW_ID = "MKS";
        	private static final int MAJOR_VERSION = 1;
        	private static final int MINOR_VERSION = 1;
        	protected static final String CLIENT_LIBRARY_NAME = "mkscmapi";
        	protected static boolean isClientLoaded = false;
        	private static boolean isClientValid = false;
        	private ToolWindow mksToolWindow;
        	private JPanel mksContentPanel;
        	private JTextPane mksTextArea;



}
