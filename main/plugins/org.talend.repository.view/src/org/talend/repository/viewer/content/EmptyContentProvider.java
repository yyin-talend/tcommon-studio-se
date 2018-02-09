// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.viewer.content;

import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

/**
 * this class has been create because of eclipse bug : https://bugs.eclipse.org/bugs/show_bug.cgi?id=82973 this can be
 * used by a dummy content provider definition based on a dummy property tester, in order to start the pluging and
 * register all the necessary adapters.
 * 
 * here is the content provider defintion:
 * 
 * @formatter:off <navigatorContent activeByDefault="true"
 * contentProvider="org.talend.repository.view.di.viewer.content.DummyContentProvider"
 * id="org.talend.repository.view.content.dummy.for.activation.di" name="name"> <enablement> <and> <instanceof
 * value="org.talend.repository.navigator.TalendRepositoryRoot"> </instanceof> <test forcePluginActivation="true" <!--
 * this is what make the plugin to get started and there for activator called if adapter need to be registered -->
 * property="org.talend.repository.node.alwaysFalse"> </test> </and> </enablement> </navigatorContent>
 * 
 * and here is the property tester definition, the class must be in the plugin that needs to be started and manifest
 * needs this line (Bundle-ActivationPolicy: lazy) <propertyTester class="org.talend.repository.tester.NodeTester"
 * id="org.talend.repository.view.di.activation.tester" namespace="org.talend.repository.node" properties="alwaysFalse"
 * type="org.talend.repository.navigator.TalendRepositoryRoot"> </propertyTester>
 * @formatter:on
 * 
 * 
 * 
 * */
public class EmptyContentProvider implements IPipelinedTreeContentProvider {

    @Override
    public void dispose() {
        // left empty on purpose
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // left empty on purpose

    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return new Object[0];

    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
     */
    @Override
    public void init(ICommonContentExtensionSite aConfig) {
        // left empty on purpose

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
     */
    @Override
    public void restoreState(IMemento aMemento) {
        // left empty on purpose

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento aMemento) {
        // left empty on purpose

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedChildren(java.lang.Object, java.util.Set)
     */
    @Override
    public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
        theCurrentChildren.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedElements(java.lang.Object, java.util.Set)
     */
    @Override
    public void getPipelinedElements(Object anInput, Set theCurrentElements) {
        theCurrentElements.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedParent(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
        return aSuggestedParent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptAdd(org.eclipse.ui.navigator.
     * PipelinedShapeModification)
     */
    @Override
    public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
        return anAddModification;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRemove(org.eclipse.ui.navigator.
     * PipelinedShapeModification)
     */
    @Override
    public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
        return aRemoveModification;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRefresh(org.eclipse.ui.navigator.
     * PipelinedViewerUpdate)
     */
    @Override
    public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptUpdate(org.eclipse.ui.navigator.PipelinedViewerUpdate
     * )
     */
    @Override
    public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
        return false;
    }

}
