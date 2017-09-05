// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.ui.actions.MoveObjectAction;
import org.talend.core.repository.ui.view.RepositoryDropAdapter;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.viewer.content.listener.IRefreshNodePerspectiveListener;
import org.talend.repository.viewer.ui.provider.INavigatorContentServiceProvider;

/**
 * DOC sgandon class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 * 
 */
public class RepoViewCommonViewer extends CommonViewer implements INavigatorContentServiceProvider {

    private Map<String, Boolean> expanded = new HashMap<String, Boolean>();

    private final RepoViewCommonNavigator repViewCommonNavigator;

    private Listener dragDetectListener;

    private List<IRefreshNodePerspectiveListener> refreshNodeLisenters;

    /**
     * Getter for repViewCommonNavigator.
     * 
     * @return the repViewCommonNavigator
     */
    public RepoViewCommonNavigator getRepViewCommonNavigator() {
        return this.repViewCommonNavigator;
    }

    public RepoViewCommonViewer(RepoViewCommonNavigator repViewCommonNavigator, String aViewerId, Composite parent, int style) {
        super(aViewerId, parent, style);
        this.repViewCommonNavigator = repViewCommonNavigator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.CommonViewer#init()
     */
    @Override
    protected void init() {
        super.init();
        // updateNavigatorContentState();
    }

    // @SuppressWarnings("restriction")
    // private void updateNavigatorContentState() {
    // INavigatorContentService contentService = getNavigatorContentService();
    // String[] visibleExtensionIds = contentService.getVisibleExtensionIds();
    // List<String> needRemovedExtensionIds = RepositoryNodeFilterHelper.getExtensionIdsNeedRemove(visibleExtensionIds);
    // if (contentService.getActivationService() instanceof NavigatorActivationService) {
    // NavigatorActivationService activationService = (NavigatorActivationService)
    // contentService.getActivationService();
    // activationService.setActive(needRemovedExtensionIds.toArray(new String[needRemovedExtensionIds.size()]), false);
    // activationService.persistExtensionActivations();
    // }
    // }

    private RepositoryNode getRepositoryNode(Item node) {
        Object data = node.getData();
        RepositoryNode repositoryNode = null;
        if (data instanceof RepositoryNode) {
            repositoryNode = (RepositoryNode) data;
        }
        return repositoryNode;
    }

    private boolean idIsValid(IRepositoryNode repositoryNode) {
        String id = repositoryNode.getId();
        return id != null && !RepositoryNode.NO_ID.equals(id);
    }

    @Override
    protected void initDragAndDrop() {
        int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
        Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };

        this.addDragSupport(ops, transfers, new DragSourceAdapter() {

            private static final long FFFFFFFFL = 0xFFFFFFFFL;

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
             */
            @Override
            public void dragSetData(DragSourceEvent event) {
                repViewCommonNavigator.setNoNeedUpdate(true);
                event.data = LocalSelectionTransfer.getTransfer().getSelection();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
             */
            @Override
            public void dragStart(DragSourceEvent event) {
                ISelection selection = RepoViewCommonViewer.this.getSelection();

                for (Object obj : ((StructuredSelection) selection).toArray()) {
                    if (obj instanceof RepositoryNode) {
                        RepositoryNode sourceNode = (RepositoryNode) obj;

                        // As i don't know how to get event operation i test on MoveOperation
                        event.doit = MoveObjectAction.getInstance().validateAction(sourceNode, null, true);
                    }
                }

                LocalSelectionTransfer.getTransfer().setSelection(selection);
                LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & FFFFFFFFL);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.dnd.DragSourceAdapter#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
             */
            @Override
            public void dragFinished(DragSourceEvent event) {
                repViewCommonNavigator.dragFinished();
            }
        });
        RepositoryDropAdapter adapter = new RepositoryDropAdapter(this, getNavigatorContentService());
        adapter.setFeedbackEnabled(false);
        this.addDropSupport(ops | DND.DROP_DEFAULT, transfers, adapter);
    }

    @Override
    public void refresh(Object element) {
        if (repViewCommonNavigator != null && repViewCommonNavigator.isNoNeedUpdate()) {
            return;
        } else {
            super.refresh(element);
        }

    }

    @Override
    public INavigatorContentService getNavigatorContentService() {
        return super.getNavigatorContentService();
    }

    public void addRefreshNodePerspectiveLisenter(IRefreshNodePerspectiveListener listener) {
        if (refreshNodeLisenters == null) {
            refreshNodeLisenters = new ArrayList<IRefreshNodePerspectiveListener>();
        }
        if (listener != null) {
            refreshNodeLisenters.add(listener);
        }
    }

    public void removeRefreshNodePerspectiveLisenter(IRefreshNodePerspectiveListener listener) {
        if (refreshNodeLisenters == null) {
            return;
        }
        if (listener != null) {
            refreshNodeLisenters.remove(listener);
        }
    }

    public void fireRefreshNodePerspectiveLisenter() {
        if (refreshNodeLisenters != null) {
            for (IRefreshNodePerspectiveListener listener : refreshNodeLisenters) {
                listener.refreshNode();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.AbstractTreeViewer#getSortedChildren(java.lang.Object)
     */
    @Override
    protected Object[] getSortedChildren(Object parentElementOrTreePath) {
        if (parentElementOrTreePath instanceof TreePath) {
            Object lastSegm = ((TreePath) parentElementOrTreePath).getLastSegment();
            if (lastSegm instanceof ProjectRepositoryNode) {
                return getOverridedSortedChildren(parentElementOrTreePath);
            }
        }
        if (parentElementOrTreePath instanceof TalendRepositoryRoot) {
            return getOverridedSortedChildren(parentElementOrTreePath);
        }
        return super.getSortedChildren(parentElementOrTreePath);
    }

    protected Object[] getOverridedSortedChildren(Object parentElementOrTreePath) {
        Object[] objects = super.getSortedChildren(parentElementOrTreePath);
        List<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
        for (Object object : objects) {
            if (object instanceof RepositoryNode) {
                nodes.add((RepositoryNode) object);
            }
        }
        Comparator<RepositoryNode> myComparator = new Comparator<RepositoryNode>() {

            @Override
            public int compare(RepositoryNode o1, RepositoryNode o2) {
                ERepositoryObjectType type1 = o1.getContentType();
                ERepositoryObjectType type2 = o2.getContentType();
                if (type1 == null) { // null, will be front, seems only recycle bin will be null.
                    return 1;
                }
                if (type2 == null) {
                    return -1;
                }

                return type1.compareTo(type2);
            }
        };
        RepositoryNode[] nodesArray = nodes.toArray(new RepositoryNode[0]);
        Arrays.sort(nodesArray, myComparator);
        return nodesArray;
    }
}
