package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.model.MksServerInfo;

import java.util.ArrayList;

public class ListServersAPITest extends AbstractAPITest {

    public void testIt() {
        ListServersAPI command = new ListServersAPI(new ArrayList<VcsException>(), getMksCLIConfiguration()) {
            protected MKSAPIHelper getAPIHelper() {
                return apiHelper;
            }
        };
        command.execute();
        assertFalse("no servers returned", command.servers.isEmpty());
        for (MksServerInfo server : command.servers) {
            System.out.println(server);
        }
    }

}
