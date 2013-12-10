// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(100) lnc 
// Source File Name:   MksConfiguration.java

package org.intellij.vcs.mks;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public class MksConfiguration
	implements JDOMExternalizable, ProjectComponent
{

        	public MksConfiguration()
        	{
/*  18*/		SERVER = "";
/*  19*/		PORT = 7001;
/*  20*/		USER = "";
/*  21*/		PASSWORD = "";
/*  22*/		SANDBOX = "";
/*  23*/		PROJECT = "";
        	}

        	public void projectClosed()
        	{
        	}

        	public void projectOpened()
        	{
        	}

        	public void disposeComponent()
        	{
        	}

        	public String getComponentName()
        	{
/*  36*/		return "MksConfiguration";
        	}

        	public void initComponent()
        	{
        	}

        	public void readExternal(Element element)
        		throws InvalidDataException
        	{
/*  43*/		DefaultJDOMExternalizer.readExternal(this, element);
        	}

        	public void writeExternal(Element element)
        		throws WriteExternalException
        	{
/*  47*/		DefaultJDOMExternalizer.writeExternal(this, element);
        	}

        	public String SERVER;
        	public int PORT;
        	public String USER;
        	public String PASSWORD;
        	public String SANDBOX;
        	public String PROJECT;
}
