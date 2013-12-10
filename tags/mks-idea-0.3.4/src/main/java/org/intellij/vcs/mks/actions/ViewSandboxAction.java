// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   ViewSandboxAction.java

package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiSandbox;
import org.intellij.vcs.mks.MksVcs;

// Referenced classes of package org.intellij.vcs.mks.actions:
//			BasicAction

public class ViewSandboxAction extends BasicAction
{

        	public ViewSandboxAction()
        	{
        	}

        	protected void perform(Project project, MksVcs vcs, VirtualFile file, DataContext dataContext)
        		throws VcsException
        	{
/*  19*/		if(!MksVcs.isValid())
/*  20*/			MksVcs.startClient();
/*  23*/		TriclopsSiSandbox sandbox = null;
/*  25*/		try
        		{
/*  25*/			sandbox = new TriclopsSiSandbox(MksVcs.CLIENT);
/*  26*/			sandbox.setIdeProjectPath(file.getPath() + "/");
/*  27*/			sandbox.validate();
        		}
/*  28*/		catch(TriclopsException e)
        		{
/*  29*/			throw new VcsException("ViewSandbox:  Unable to open sandbox.");
        		}
/*  33*/		try
        		{
/*  33*/			if(MksVcs.CLIENT != null)
/*  34*/				sandbox.openSandboxView(null);
        		}
/*  36*/		catch(TriclopsException e)
        		{
/*  37*/			throw new VcsException("ViewSandbox:  Unable to view sandbox.");
        		}
        	}

        	protected String getActionName(AbstractVcs vcs)
        	{
/*  42*/		return "View Sandbox";
        	}

        	protected boolean isEnabled(Project project, AbstractVcs vcs, VirtualFile file)
        	{
/*  46*/		return true;
        	}
}
