package org.intellij.vcs.mks;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public class MksConfiguration
        implements JDOMExternalizable, ProjectComponent {

    public MksConfiguration() {
        SERVER = "";
        PORT = 7001;
        USER = "";
        PASSWORD = "";
        SANDBOX = "";
        PROJECT = "";
    }

    public void projectClosed() {
    }

    public void projectOpened() {
    }

    public void disposeComponent() {
    }

    public String getComponentName() {
        return "MksConfiguration";
    }

    public void initComponent() {
    }

    public void readExternal(Element element)
            throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element)
            throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

    public String SERVER;
    public int PORT;
    public String USER;
    public String PASSWORD;
    public String SANDBOX;
    public String PROJECT;
}
