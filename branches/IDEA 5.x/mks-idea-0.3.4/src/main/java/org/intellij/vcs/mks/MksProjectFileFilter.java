// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   MksProjectFileFilter.java

package org.intellij.vcs.mks;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class MksProjectFileFilter extends FileFilter
{

        	public MksProjectFileFilter()
        	{
        	}

        	public String getDescription()
        	{
/*  13*/		return "Source Integrity Project (*.pj)";
        	}

        	public boolean accept(File file)
        	{
/*  17*/		if(file.isDirectory())
/*  18*/			return true;
/*  21*/		String extension = getExtension(file);
/*  22*/		if(extension != null)
/*  23*/			return "pj".equalsIgnoreCase(extension);
/*  25*/		else
/*  25*/			return false;
        	}

        	private static String getExtension(File f)
        	{
/*  30*/		String ext = null;
/*  31*/		String s = f.getName();
/*  32*/		int i = s.lastIndexOf('.');
/*  34*/		if(i > 0 && i < s.length() - 1)
/*  35*/			ext = s.substring(i + 1).toLowerCase();
/*  37*/		return ext;
        	}

        	private static final String MKS_PROJECT_FILE_EXT = "pj";
        	private static final String MKS_PROJECT_FILE_DESC = "Source Integrity Project (*.pj)";
}
