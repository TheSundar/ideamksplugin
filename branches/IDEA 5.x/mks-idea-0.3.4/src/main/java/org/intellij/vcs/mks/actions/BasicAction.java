// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   BasicAction.java

package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.TransactionRunnable;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Arrays;
import java.util.List;
import org.intellij.vcs.mks.MksConfiguration;
import org.intellij.vcs.mks.MksVcs;

public abstract class BasicAction extends AnAction
{

        	public BasicAction()
        	{
        	}

        	public void actionPerformed(AnActionEvent event)
        	{
/*  45*/		if(LOG.isDebugEnabled())
/*  46*/			LOG.debug("enter: actionPerformed(id='" + ActionManager.getInstance().getId(this) + "')");
/*  49*/		final DataContext dataContext = event.getDataContext();
/*  50*/		final Project project = (Project)dataContext.getData("project");
/*  51*/		configuration = (MksConfiguration)project.getComponent(org.intellij.vcs.mks.MksConfiguration.class);
/*  52*/		VcsManager manager = VcsManager.getInstance(project);
/*  53*/		final AbstractVcs vcs = manager.getActiveVcs();
/*  54*/		final VirtualFile files[] = (VirtualFile[])dataContext.getData("virtualFileArray");
/*  56*/		if(LOG.isDebugEnabled())
/*  57*/			LOG.debug("files='" + Arrays.asList(files) + "'");
/*  60*/		if(files == null || files.length == 0)
/*  61*/			return;
/*  64*/		FileDocumentManager.getInstance().saveAllDocuments();
/*  65*/		String actionName = getActionName(vcs);
/*  66*/		AbstractVcsHelper helper = AbstractVcsHelper.getInstance(project);
/*  67*/		com.intellij.openapi.localVcs.LvcsAction action = helper.startVcsAction(actionName);
/*  69*/		List exceptions = helper.runTransactionRunnable(vcs, new TransactionRunnable() {

        			public void run(List exceptions)
        			{
/*  72*/				for(int i = 0; i < files.length;)
        				{
/*  73*/					VirtualFile file = files[i];
/*  75*/					try
        					{
/*  75*/						execute(project, vcs, file, dataContext);
/*  76*/						continue;
        					}
/*  77*/					catch(VcsException ex)
        					{
/*  78*/						ex.setVirtualFile(file);
/*  79*/						exceptions.add(ex);
/*  80*/						i++;
/*  72*/						i++;
        					}
        				}

        			}

		}, null);
/*  88*/		MksVcs mksVcs = (MksVcs)vcs;
/*  89*/		mksVcs.showErrors(exceptions, actionName != null ? actionName : vcs.getDisplayName());
/*  91*/		if(actionName != null)
/*  92*/			helper.finishVcsAction(action);
        	}

        	public void update(AnActionEvent e)
        	{
/*  97*/		super.update(e);
/*  98*/		Presentation presentation = e.getPresentation();
/*  99*/		DataContext dataContext = e.getDataContext();
/* 100*/		Project project = (Project)dataContext.getData("project");
/* 102*/		if(project == null)
        		{
/* 103*/			presentation.setEnabled(false);
/* 104*/			presentation.setVisible(false);
/* 105*/			return;
        		}
/* 108*/		VcsManager manager = VcsManager.getInstance(project);
/* 110*/		if(manager == null)
        		{
/* 111*/			presentation.setEnabled(false);
/* 112*/			presentation.setVisible(false);
/* 113*/			return;
        		}
/* 116*/		AbstractVcs activeVcs = manager.getActiveVcs();
/* 118*/		if(activeVcs == null || !(activeVcs instanceof MksVcs))
        		{
/* 119*/			presentation.setEnabled(false);
/* 120*/			presentation.setVisible(false);
/* 121*/			return;
        		}
/* 124*/		VirtualFile files[] = (VirtualFile[])dataContext.getData("virtualFileArray");
/* 126*/		if(files == null || files.length == 0)
        		{
/* 127*/			presentation.setEnabled(false);
/* 128*/			presentation.setVisible(true);
/* 129*/			return;
        		}
/* 132*/		boolean enabled = true;
/* 134*/		for(int i = 0; i < files.length; i++)
        		{
/* 135*/			VirtualFile file = files[i];
/* 136*/			if(!isEnabled(project, activeVcs, file))
/* 137*/				enabled = false;
        		}

/* 141*/		presentation.setEnabled(enabled);
/* 142*/		presentation.setVisible(enabled);
        	}

        	private void execute(Project project, AbstractVcs activeVcs, final VirtualFile file, DataContext context)
        		throws VcsException
        	{
/* 147*/		if(file.isDirectory())
        		{
/* 148*/			perform(project, (MksVcs)activeVcs, file, context);
/* 150*/			ApplicationManager.getApplication().runWriteAction(new Runnable() {

        				public void run()
        				{
/* 153*/					file.refresh(false, true);
        				}

			});
/* 157*/			FileStatusManager.getInstance(project).fileStatusChanged(file);
/* 158*/			VirtualFile children[] = file.getChildren();
/* 160*/			for(int i = 0; i < children.length; i++)
        			{
/* 161*/				VirtualFile child = children[i];
/* 162*/				execute(project, activeVcs, child, context);
        			}

        		} else
        		{
/* 166*/			perform(project, (MksVcs)activeVcs, file, context);
/* 168*/			ApplicationManager.getApplication().runWriteAction(new Runnable() {

        				public void run()
        				{
/* 171*/					file.refresh(false, true);
        				}

			});
/* 175*/			FileStatusManager.getInstance(project).fileStatusChanged(file);
        		}
        	}

        	protected abstract String getActionName(AbstractVcs abstractvcs);

        	protected abstract boolean isEnabled(Project project, AbstractVcs abstractvcs, VirtualFile virtualfile);

        	protected abstract void perform(Project project, MksVcs mksvcs, VirtualFile virtualfile, DataContext datacontext)
        		throws VcsException;

        	protected MksConfiguration configuration;
        	private static final Logger LOG = Logger.getInstance("#org.intellij.vcs.mks.BasicAction");
        	protected static final String ACTION_CANCELLED_MSG = "The command was cancelled.";


}
