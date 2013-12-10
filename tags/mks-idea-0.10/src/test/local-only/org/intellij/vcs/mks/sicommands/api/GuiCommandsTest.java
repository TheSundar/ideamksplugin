package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.OptionList;
import com.mks.api.response.APIException;
import org.intellij.vcs.mks.MKSAPIHelper;
import org.intellij.vcs.mks.model.MksChangePackage;

import java.util.ArrayList;

public class GuiCommandsTest extends AbstractAPITest {

    public void testCheckout() throws APIException {
        final CmdRunner runner = apiHelper.getSession().createCmdRunner();
        Command command = new Command(Command.SI);
        command.setCommandName("co");
        command.addSelection("C:\\Users\\A6253567\\sandboxes\\GIVR\\mapper\\idv-ha-services\\src\\main\\resources\\idv-ha-services\\gct\\deliverPRC-outbound.xml");
        command.addSelection("C:\\Users\\A6253567\\sandboxes\\GIVR\\mapper\\idv-ha-services\\src\\test\\resources\\commons-logging.properties");
        command.addOption(new Option("gui"));

        runner.execute(command);
    }
    public void testViewPackage() {

        ViewChangePackageAPICommand command = new ViewChangePackageAPICommand(new ArrayList<VcsException>(), getMksCLIConfiguration(), new MksChangePackage("vhvhcl50.us.hsbc", "430549:1", "79301750", "open", null)) {
            @Override
            protected MKSAPIHelper getAPIHelper() {
                return apiHelper;
            }
        };


        command.execute();

    }

    public void testViewSandbox() throws APIException {
        this.apiHelper.getSICommands().siSandboxView("C:\\Users\\A6253567\\sandboxes\\GIVR\\mapper\\idv-ha-services\\src\\main\\resources\\idv-ha-services\\gct");
    }

    public void testAddMember() throws APIException {
        String[] members = new String[] {"C:\\Users\\A6253567\\sandboxes\\GIVR\\mapper\\idv-ha-services\\src\\test\\resources\\response.xml", "C:\\Users\\A6253567\\sandboxes\\GIVR\\mapper\\idv-ha-services\\src\\test\\resources\\fmn\\response.xml"};
        final CmdRunner runner =  apiHelper.getSession().createCmdRunner();
        Command command = new Command(Command.SI);
        command.setCommandName("add");
        command.addOption(new Option("gui"));
        command.addOption(new Option("cwd", "C:\\Users\\A6253567\\sandboxes\\GIVR\\mapper\\idv-ha-services"));
        for (int i = 0; i < members.length; i++) {
            String member = members[i];
            command.addSelection(member);
        }

        runner.execute(command);

    }
}
