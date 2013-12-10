// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   MemberHistoryAction.java

package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiClient;
import mks.integrations.common.TriclopsSiMember;
import mks.integrations.common.TriclopsSiMembers;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.MksVcs;

// Referenced classes of package org.intellij.vcs.mks.actions:
//			BasicAction

public class MemberHistoryAction extends BasicAction
{

        	public MemberHistoryAction()
        	{
        	}

        	protected void perform(Project project, MksVcs vcs, VirtualFile file, DataContext dataContext)
        		throws VcsException
        	{
/*  21*/		if(!MksVcs.isValid())
/*  22*/			MksVcs.startClient();
/*  25*/		TriclopsSiSandbox sandbox = null;
/*  27*/		try
        		{
/*  27*/			sandbox = new TriclopsSiSandbox(MksVcs.CLIENT);
/*  28*/			sandbox.setIdeProjectPath(file.getPath() + "/");
/*  29*/			sandbox.validate();
        		}
/*  30*/		catch(TriclopsException e)
        		{
/*  31*/			throw new VcsException("History: Unable to locate sandbox.");
        		}
/*  34*/		TriclopsSiMembers members = null;
/*  36*/		try
        		{
/*  36*/			members = new TriclopsSiMembers(MksVcs.CLIENT, sandbox);
/*  37*/			members.addMember(new TriclopsSiMember(file.getPresentableUrl()));
/*  38*/			members.getMembersStatus();
        		}
/*  39*/		catch(TriclopsException e)
        		{
/*  40*/			throw new VcsException("History: Unable to obtain member history.");
        		}
/*  44*/		try
        		{
/*  44*/			if(MksVcs.CLIENT != null)
/*  45*/				members.openMemberArchiveView(0);
        		}
/*  47*/		catch(TriclopsException e)
        		{
/*  48*/			String message = sandbox.getSiClient().getErrorMessage();
/*  49*/			if(!"The command was cancelled.".equalsIgnoreCase(message))
/*  50*/				throw new VcsException("History Error: " + message);
        		}
        	}

        	protected String getActionName(AbstractVcs vcs)
        	{
/*  56*/		return "Member History";
        	}

        	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file)
        	{
/*  60*/		return !file.isDirectory() && file.isValid();
        	}
}
