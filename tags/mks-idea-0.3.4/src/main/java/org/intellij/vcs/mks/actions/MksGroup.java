// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   MksGroup.java

package org.intellij.vcs.mks.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.actions.StandardVcsGroup;
import org.intellij.vcs.mks.MksVcs;

public class MksGroup extends StandardVcsGroup
{

        	public MksGroup()
        	{
        	}

        	public AbstractVcs getVcs(Project project)
        	{
/*  10*/		return (MksVcs)MksVcs.getInstance(project);
        	}
}
