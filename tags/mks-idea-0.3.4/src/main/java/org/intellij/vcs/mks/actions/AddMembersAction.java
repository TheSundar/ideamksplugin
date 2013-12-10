// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   AddMembersAction.java

package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
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

public class AddMembersAction extends BasicAction
{

        	public AddMembersAction()
        	{
        	}

        	protected void perform(Project project, MksVcs vcs, VirtualFile file, DataContext dataContext)
        		throws VcsException
        	{
/*  22*/		if(!MksVcs.isValid())
/*  23*/			MksVcs.startClient();
/*  26*/		TriclopsSiSandbox sandbox = null;
/*  28*/		try
        		{
/*  28*/			sandbox = new TriclopsSiSandbox(MksVcs.CLIENT);
/*  29*/			sandbox.setIdeProjectPath(file.getPath() + "/");
/*  30*/			sandbox.validate();
        		}
/*  31*/		catch(TriclopsException e)
        		{
/*  32*/			throw new VcsException("Add: Unable to open sandbox.");
        		}
/*  35*/		TriclopsSiMembers members = null;
/*  37*/		try
        		{
/*  37*/			members = new TriclopsSiMembers(MksVcs.CLIENT, sandbox);
/*  38*/			members.addMember(new TriclopsSiMember(file.getPath()));
/*  39*/			members.getMembersStatus();
        		}
/*  40*/		catch(TriclopsException e)
        		{
/*  41*/			throw new VcsException("Add: Unable to access member(s).");
        		}
/*  45*/		try
        		{
/*  45*/			if(MksVcs.CLIENT != null)
        			{
/*  46*/				members.addMembers(0);
/*  47*/				members.getMembersStatus();
        			}
        		}
/*  49*/		catch(TriclopsException e)
        		{
/*  50*/			String message = sandbox.getSiClient().getErrorMessage();
/*  51*/			if(!"The command was cancelled.".equalsIgnoreCase(message))
/*  52*/				throw new VcsException("Add Error: " + message);
        		}
/*  56*/		WindowManager.getInstance().getStatusBar(project).setInfo("Member(s) added.");
        	}

        	protected String getActionName(AbstractVcs vcs)
        	{
/*  60*/		return "Adding files to " + vcs.getName();
        	}

        	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file)
        	{
/*  64*/		return true;
        	}
}
