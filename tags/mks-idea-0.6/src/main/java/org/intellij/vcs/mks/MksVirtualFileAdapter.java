package org.intellij.vcs.mks;

import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;

/**
 * @author Thibaut Fagart
 */
class MksVirtualFileAdapter extends VirtualFileAdapter {
	private MksVcs mksVcs;

	public MksVirtualFileAdapter(MksVcs mksVcs) {
		super();	//To change body of overridden methods use File | Settings | File Templates.
		this.mksVcs = mksVcs;
	}

	@Override
	public void propertyChanged(VirtualFilePropertyEvent event) {
		super.propertyChanged(event);	//To change body of overridden methods use File | Settings | File Templates.
		mksVcs.debug("propertyChanged[" + event + "]");
	}

	@Override
	public void contentsChanged(VirtualFileEvent virtualFileEvent) {
		super.contentsChanged(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
		mksVcs.debug("contentsChanged[" + virtualFileEvent + "]");
	}

	@Override
	public void beforeContentsChange(VirtualFileEvent virtualFileEvent) {
		super.beforeContentsChange(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
		mksVcs.debug("beforeContentsChange[" + virtualFileEvent + "]");
	}

	@Override
	public void beforeFileDeletion(VirtualFileEvent virtualFileEvent) {
		super.beforeFileDeletion(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
		mksVcs.debug("beforeFileDeletion[" + virtualFileEvent + "]");
	}

	@Override
	public void fileCreated(VirtualFileEvent virtualFileEvent) {
		super.fileCreated(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
		mksVcs.debug("fileCreated[" + virtualFileEvent + "]");
	}

	@Override
	public void fileDeleted(VirtualFileEvent virtualFileEvent) {
		super.fileDeleted(virtualFileEvent);	//To change body of overridden methods use File | Settings | File Templates.
		mksVcs.debug("fileDeleted[" + virtualFileEvent + "]");
	}

	@Override
	public void beforePropertyChange(VirtualFilePropertyEvent event) {
		super.beforePropertyChange(event);	//To change body of overridden methods use File | Settings | File Templates.
		mksVcs.debug("beforePropertyChange[" + event + "]");
		//			if (event.getPropertyName().equalsIgnoreCase(VirtualFile.PROP_NAME)) {
		//				final VirtualFile file = event.getFile();
		//				final VirtualFile parent = file.getParent();
		//				try {
		//					try {
		//						TriclopsSiSandbox sandbox = null;
		//						sandbox = MKSHelper.getSandbox(file);
		//						TriclopsSiMembers members = new TriclopsSiMembers(MksVcs.CLIENT, sandbox);
		//
		//						members.addMember(new TriclopsSiMember(file.getPresentableUrl()));
		//						members.addMember(new TriclopsSiMember(file.getParent().getPresentableUrl()+"\\"+event.getNewValue()));
		//
		//						members.renameMember(TriclopsSiMembers.SI_RENAME_MEMBER_CONFIRM);
		//					} catch (TriclopsException e) {
		//						debug("rename member [" + file.getPath() + "] to [" + event.getNewValue() + "] failed", e);
		//						throw new VcsException("rename member [" + file.getPath() + "] to [" + event.getNewValue() + "] failed");
		//					}
		//				} catch (VcsException e) {
		//					debug(e.getMessage(), e);
		//				}
		//			}
	}

	@Override
	public void fileMoved(VirtualFileMoveEvent virtualFileMoveEvent) {
		super.fileMoved(virtualFileMoveEvent);	//To change body of overridden methods use File | Settings | File Templates.
		mksVcs.debug("fileMoved[" + virtualFileMoveEvent + "]");
	}

	@Override
	public void beforeFileMovement(VirtualFileMoveEvent virtualFileMoveEvent) {
		super.beforeFileMovement(virtualFileMoveEvent);
		mksVcs.debug("beforeFileMovement[" + virtualFileMoveEvent + "]");
	}
}
