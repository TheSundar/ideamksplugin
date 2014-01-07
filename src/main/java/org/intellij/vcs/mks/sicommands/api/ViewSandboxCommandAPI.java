package org.intellij.vcs.mks.sicommands.api;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.vcsUtil.VcsUtil;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.response.*;
import com.mks.api.si.SIModelTypeName;
import org.intellij.vcs.mks.MksCLIConfiguration;
import org.intellij.vcs.mks.MksRevisionNumber;
import org.intellij.vcs.mks.model.MksMemberState;
import org.intellij.vcs.mks.sicommands.SandboxInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Known WorkItem types : work-in-progress, archived, variant-subsandbox, deferred-add, deferred-check-in, subsandbox
 */
public class ViewSandboxCommandAPI extends SiAPICommand {
    private static final Set<String> SandboxModelTypes = new HashSet<String>(Arrays.asList(SIModelTypeName.SI_FORMER_SUBSANDBOX, SIModelTypeName.SI_FORMER_SANDBOX, SIModelTypeName.SI_SUBSANDBOX, SIModelTypeName.SI_SANDBOX));
    private static final String COMMAND = "viewsandbox";
    @NotNull private String sandboxPath;
    protected final Map<String, MksMemberState> memberStates = new HashMap<String, MksMemberState>();

    public ViewSandboxCommandAPI(@NotNull List<VcsException> errors, @NotNull MksCLIConfiguration mksCLIConfiguration, @NotNull String sandboxPjPath) {
        super(errors, COMMAND, mksCLIConfiguration);
       this.sandboxPath = sandboxPjPath;
    }

    @Override
    public void execute() {
        try {
            Command command = createCommand();
            final Response response = executeCommand(command);

            final SubRoutineIterator routineIterator = response.getSubRoutines();
            while (routineIterator.hasNext()) {
                final SubRoutine subRoutine = routineIterator.next();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("routine " + subRoutine);
                }
            }
            final WorkItemIterator workItems = response.getWorkItems();
            while (workItems.hasNext()) {
                final WorkItem item = workItems.next();
                if (shoulSkip(item)) {
                    continue;
                }
                String memberName = item.getField("name").getValueAsString();

                MksMemberState memberState = createState(item);
                setState(memberName, memberState);
            }
        } catch (APIException e) {
            errors.add(new VcsException(e));
        } catch (VcsException e) {
            errors.add(new VcsException(e));
        }

    }

    protected Command createCommand() {
        Command command = new Command(Command.SI);
        command.setCommandName("viewsandbox");
        command.addOption(new Option("sandbox", sandboxPath));
        MultiValue mv = new MultiValue( "," );
        mv.add( "name" );
        mv.add( "context" );
        mv.add( "wfdelta" );
        mv.add( "memberarchive" );
        mv.add( "memberrev" );
        mv.add( "workingrev" );
        mv.add( "locker" );
        mv.add( "workingcpid" );
        mv.add( "revsyncdelta" );
        command.addOption(new Option("fields", mv));
        command.addOption(new Option("recurse"));
        return command;
    }

    private boolean shoulSkip(WorkItem item) {
        return SandboxModelTypes.contains(item.getModelType());
    }

    protected void setState(@NotNull final String name, @NotNull final MksMemberState memberState) {
        memberStates.put(VcsUtil.getFilePath(name).getPath(), memberState);
    }

    protected MksMemberState createState(WorkItem item) throws VcsException {
        // we confuse missing files and locally modified without checkout here
        String memberType = item.getField("type").getValueAsString();
        String type = item.getField("type").getValueAsString();
        String memberRev = item.getField("memberrev").getValueAsString();
        String workingrev= item.getField("workingrev").getValueAsString();
        String locker = item.getField("locker").getValueAsString();
        boolean isLockedByMe ;
        try {
            isLockedByMe= ((Item) item.getField("workingLockInfo").getValue()).getField("lockedByMe").getBoolean();
        } catch (Exception e) {
            isLockedByMe = false;
        }
        String workingCpid;
        try {
            Item cpid = (Item) item.getField("workingcpid").getValue();
            workingCpid = cpid.getContext("id");
        } catch (Exception e) {
            workingCpid = null;
        }
        boolean isWorkingFileChanged = false;
        boolean isLocalFileMissing = false;
        boolean isNewWorkingFile = false;
        boolean isRevSyncDelta = false;
        try {
            Item wfdelta = (Item) item.getField("wfdelta").getValue();
            isWorkingFileChanged = wfdelta.getField("isDelta").getBoolean();
            isLocalFileMissing = wfdelta.getField("noWorkingFile").getBoolean();
            isNewWorkingFile = wfdelta.getField("newWorkingFile").getBoolean();
        } catch (Exception e) {
        }
        try {
            Item revsyncdelta = (Item) item.getField("revsyncdelta").getValue();
            isRevSyncDelta = revsyncdelta.getField("isDelta").getBoolean();
        } catch (Exception e) {
        }

        if (type.startsWith("deferred")) {
            Date memberTimestamp = null; // unused currently
            if ("deferred-add".equals(type)) {
                    return new MksMemberState(null, null, workingCpid,
                            MksMemberState.Status.ADDED, memberTimestamp);
                } else if ("deferred-drop".equals(type)) {
                    return new MksMemberState((MksRevisionNumber.createRevision(workingrev)), (MksRevisionNumber.createRevision(memberRev)), workingCpid,
                            MksMemberState.Status.DROPPED, memberTimestamp);
                } else if ("deferred-check-in".equals(type)) {
                    return new MksMemberState((MksRevisionNumber.createRevision(workingrev)), (MksRevisionNumber.createRevision(memberRev)), workingCpid,
                            MksMemberState.Status.CHECKED_OUT, memberTimestamp);
                } else {
                    LOGGER.warn(this + " : deferred operation (" + type + ") not supported at moment, returning 'unknown'");
                    return new MksMemberState((MksRevisionNumber.createRevision(workingrev)),
                            (MksRevisionNumber.createRevision(memberRev)), workingCpid,
                            MksMemberState.Status.UNKNOWN, memberTimestamp);
                }

        } else if (isWorkingFileChanged) {
            MksMemberState.Status status;
            if (isLockedByMe) {
                status = MksMemberState.Status.CHECKED_OUT;
            } else {
                status = MksMemberState.Status.MODIFIED_WITHOUT_CHECKOUT;
            }
            return new MksMemberState((createRevision(workingrev)), (createRevision(memberRev)), workingCpid,status);
        } else if (isRevSyncDelta && !memberRev.equals(workingrev)) {
            return new MksMemberState((createRevision(workingrev)), (createRevision(memberRev)), workingCpid,MksMemberState.Status.SYNC);
        } else {
            MksMemberState.Status status;
            if (isLocalFileMissing) {
                status = MksMemberState.Status.MISSING;
            } else  if (isNewWorkingFile) {
                status = MksMemberState.Status.UNVERSIONED;
            } else {
                status = MksMemberState.Status.NOT_CHANGED;
            }


            return new MksMemberState((createRevision(workingrev)), (createRevision(memberRev)), workingCpid,status);
        }
/*
        if (null != locker) {
            String fullName = ((Item) item.getField("locker").getValue()).getField("fullName").getValueAsString();
        }

        MksMemberState.Status status;
*/
/*      } else if (memberRev == null && workingRev != null */
/* TODO && DROPPED_TYPE.equals(type)*//*
) {
            // todo check
            return new MksMemberState((createRevision(workingRev)), (createRevision(memberRev)), workingCpid,
                    MksMemberState.Status.REMOTELY_DROPPED);
*/
    }

    private VcsRevisionNumber createRevision(String workingRev) throws VcsException {
        return MksRevisionNumber.createRevision(workingRev);
    }
    public Map<String, MksMemberState> getMemberStates() {
        return Collections.unmodifiableMap(memberStates);
    }

}
