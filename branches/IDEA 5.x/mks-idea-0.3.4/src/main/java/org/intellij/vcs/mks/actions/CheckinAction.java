// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   CheckinAction.java

package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiClient;
import mks.integrations.common.TriclopsSiMember;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.MksVcs;

// Referenced classes of package org.intellij.vcs.mks.actions:
//			BasicAction

public class CheckinAction extends BasicAction
{

        	public CheckinAction()
        	{
        	}

        	protected void perform(Project project, MksVcs vcs, VirtualFile file, DataContext dataContext)
        		throws VcsException
        	{
/*  23*/		if(!MksVcs.isValid())
/*  24*/			MksVcs.startClient();
/*  27*/		TriclopsSiSandbox sandbox = null;
/*  29*/		try
        		{
/*  29*/			sandbox = new TriclopsSiSandbox(MksVcs.CLIENT);
/*  30*/			sandbox.setIdeProjectPath(file.getPath() + "/");
/*  31*/			sandbox.validate();
        		}
/*  32*/		catch(TriclopsException e)
        		{
/*  33*/			throw new VcsException("Check In: Sandbox is invalid.");
        		}
/*  36*/		TriclopsSiMembers members = null;
/*  38*/		try
        		{
/*  38*/			members = new TriclopsSiMembers(MksVcs.CLIENT, sandbox);
/*  39*/			members.addMember(new TriclopsSiMember(file.getPresentableUrl()));
/*  40*/			members.getMembersStatus();
        		}
/*  41*/		catch(TriclopsException e)
        		{
/*  42*/			throw new VcsException("Check In: Unable to get member status from sandbox.");
        		}
/*  46*/		try
        		{
/*  46*/			if(MksVcs.CLIENT != null)
        			{
/*  47*/				members.checkinMembers(0);
/*  48*/				members.getMembersStatus();
        			}
        		}
/*  50*/		catch(TriclopsException e)
        		{
/*  51*/			String message = sandbox.getSiClient().getErrorMessage();
/*  52*/			if(!"The command was cancelled.".equalsIgnoreCase(message))
/*  53*/				throw new VcsException("Checkin Error for file " + file.getName() + ": " + message);
        		}
/*  57*/		AbstractVcsHelper.getInstance(project).markFileAsUpToDate(file);
/*  58*/		WindowManager.getInstance().getStatusBar(project).setInfo("CheckIn complete.");
        	}

        	protected String getActionName(AbstractVcs vcs)
        	{
/*  62*/		return "Check In";
        	}

        	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file)
        	{
/*  66*/		return true;
        	}
}
