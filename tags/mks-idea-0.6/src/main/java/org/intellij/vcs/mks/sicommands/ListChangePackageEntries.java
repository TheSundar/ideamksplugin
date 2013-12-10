package org.intellij.vcs.mks.sicommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.intellij.vcs.mks.EncodingProvider;
import org.intellij.vcs.mks.MksChangePackage;
import org.intellij.vcs.mks.MksChangePackageEntry;
import org.jetbrains.annotations.NonNls;
import com.intellij.openapi.vcs.VcsException;

/**
 * @author Thibaut Fagart
 */
public class ListChangePackageEntries extends SiCLICommand {
    @NonNls
    public static final String COMMAND = "viewcp";
    private static final int TYPE = 0;
    private static final int MEMBER = 1;
    private static final int REVISION = 2;
    private static final int PROJECT = 3;

    public List<MksChangePackageEntry> changePackageEntries;
    public final  MksChangePackage mksChangePackage;

    public ListChangePackageEntries(List<VcsException> errors, EncodingProvider encodingProvider, MksChangePackage mksChangePackage) {
        super(errors, encodingProvider, COMMAND,
            (mksChangePackage.server == null) ?
                new String[]{"--fields=type,member,revision,project", mksChangePackage.getId()} :
                new String[]{"--fields=type,member,revision,project", "--hostname", mksChangePackage.server, mksChangePackage.getId()});
        this.mksChangePackage = mksChangePackage;

    }

    @Override
    public void execute() {
        ArrayList<MksChangePackageEntry> tempEntries = new ArrayList<MksChangePackageEntry>();
        try {
            String command = super.executeCommand();

            String[] lines = commandOutput.split("\n");
            int start = 0;
            while (shoudIgnore(lines[start])) {
                // skipping connecting/reconnecting lines
                start++;
            }
            // skipping the first 2 lines which display change package information
            String[] strings = lines[start].split("\t");
            if (strings.length != 2 || !mksChangePackage.getId().trim().equals(strings[0].trim())
                || !mksChangePackage.getSummary().trim().equals(strings[1].trim())) {
                String errorMessage = "unexpected si viewcp output {" + commandOutput + "}, expected line " + start + "{" + lines[start] + "} to be " +
                    "{" + mksChangePackage.getId() + "\t" + mksChangePackage.getSummary() + "\t}, while executing " + command;
                LOGGER.error(errorMessage);

                //noinspection ThrowableInstanceNeverThrown
                errors.add(new VcsException(errorMessage));
                return;
            }
            strings = lines[start + 1].split("\t");
            if (strings.length != 3 || !mksChangePackage.getOwner().trim().equals(strings[0].trim())
                || !mksChangePackage.getState().trim().equals(strings[2].trim())) {
                LOGGER.error("unexpected si viewcp output {" + commandOutput + "}, expected line " + (start + 1) + "{" + lines[start + 1] + "} to be" +
                    "{" + mksChangePackage.getOwner() + "\tmksCP.creationDate\t" + mksChangePackage.getState() + "}");
                //noinspection ThrowableInstanceNeverThrown
                errors.add(new VcsException("unexpected si viewcp output"));
                return;
            }
            // line 2 : owner, create date, state
            for (int i = start + 2; i < lines.length; i++) {
                String line = lines[i];
                if (!shoudIgnore(line)) {
                    String[] parts = line.split("\t");
//				System.out.println("ListChangePackageEntries " + Arrays.asList(parts));
                    tempEntries.add(new MksChangePackageEntry(parts[TYPE], parts[MEMBER], parts[REVISION], parts[PROJECT]));
                }
            }
            changePackageEntries = tempEntries;
        } catch (IOException e) {
            //noinspection ThrowableInstanceNeverThrown
            errors.add(new VcsException(e));
        }
    }


    public List<VcsException> getErrors() {
        return Collections.unmodifiableList(errors.subList(previousErrorCount, errors.size()));
    }

    @Override
    public String toString() {
        return "ListChangePackageEntrie["+mksChangePackage.getId()+"]";
    }
}
