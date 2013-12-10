// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   MemberDifferencesAction.java

package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
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

public class MemberDifferencesAction extends BasicAction
{

        	public MemberDifferencesAction()
        	{
        	}

        	protected void perform(Project project, MksVcs vcs, VirtualFile file, DataContext dataContext)
        		throws VcsException
        	{
/*  20*/		if(!MksVcs.isValid())
/*  21*/			MksVcs.startClient();
/*  24*/		TriclopsSiSandbox sandbox = null;
/*  26*/		try
        		{
/*  26*/			sandbox = new TriclopsSiSandbox(MksVcs.CLIENT);
/*  27*/			sandbox.setIdeProjectPath(file.getPath() + "/");
/*  28*/			sandbox.validate();
        		}
/*  29*/		catch(TriclopsException e)
        		{
/*  30*/			throw new VcsException("Diff: Sandbox is invalid.");
        		}
/*  33*/		TriclopsSiMembers members = null;
/*  35*/		try
        		{
/*  35*/			members = new TriclopsSiMembers(MksVcs.CLIENT, sandbox);
/*  36*/			members.addMember(new TriclopsSiMember(file.getPresentableUrl()));
/*  37*/			members.getMembersStatus();
        		}
/*  38*/		catch(TriclopsException e)
        		{
/*  39*/			throw new VcsException("Diff: Unable to access member(s).");
        		}
/*  43*/		try
        		{
/*  43*/			if(MksVcs.CLIENT != null)
/*  44*/				members.openMemberDiffView(0);
        		}
/*  46*/		catch(TriclopsException e)
        		{
/*  47*/			String message = sandbox.getSiClient().getErrorMessage();
/*  48*/			if(!"The command was cancelled.".equalsIgnoreCase(message))
/*  49*/				throw new VcsException("Diff Error: " + message);
        		}
        	}

        	protected String getActionName(AbstractVcs vcs)
        	{
/*  55*/		return null;
        	}

        	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file)
        	{
/*  59*/		return !file.isDirectory() && FileStatusManager.getInstance(project).getStatus(file) != FileStatus.ADDED;
        	}
}
