// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   AboutMksAction.java

package org.intellij.vcs.mks.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import mks.integrations.common.TriclopsException;
import mks.integrations.common.TriclopsSiClient;
import org.intellij.vcs.mks.MksVcs;

public class AboutMksAction extends AnAction
{

        	public AboutMksAction()
        	{
        	}

        	public void actionPerformed(AnActionEvent anActionEvent)
        	{
/*  15*/		if(!MksVcs.isValid())
/*  16*/			MksVcs.startClient();
/*  20*/		try
        		{
/*  20*/			if(MksVcs.CLIENT != null)
/*  21*/				MksVcs.CLIENT.aboutBox();
        		}
/*  23*/		catch(TriclopsException e) { }
        	}
}
