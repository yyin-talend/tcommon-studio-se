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
package org.talend.core.ui.metadata.celleditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;

/**
 * ggu class global comment. Detailled comment
 */
public class ModuleListCellEditor extends DialogCellEditor {

    private final IElementParameter param, tableParam;

    private AbstractDataTableEditorView<?> tableEditorView;

    public ModuleListCellEditor(Composite parent, IElementParameter param, IElementParameter tableParam) {
        super(parent, SWT.NONE);
        this.param = param;
        this.tableParam = tableParam;
    }

    private void executeCommand(Command cmd) {
        IProcess2 process = null;
        if (tableParam != null) {
            if (tableParam.getElement() instanceof IProcess2) {
                process = (IProcess2) tableParam.getElement();
            } else if (tableParam.getElement() instanceof INode) {
                if (((INode) tableParam.getElement()).getProcess() instanceof IProcess2) {
                    process = (IProcess2) ((INode) tableParam.getElement()).getProcess();
                }
            }
        }

        boolean executed = false;
        if (process != null && process instanceof IGEFProcess) {
            IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
            if (designerCoreUIService != null) {
                executed = designerCoreUIService.executeCommand((IGEFProcess) process, cmd);
            }
        }
        if (!executed) {
            cmd.execute();
        }
    }

    public AbstractDataTableEditorView<?> getTableEditorView() {
        return this.tableEditorView;
    }

    private TableViewer getTableViewer() {
        if (this.tableEditorView != null) {
            AbstractExtendedTableViewer<?> extendedTableViewer = this.tableEditorView.getExtendedTableViewer();
            if (extendedTableViewer != null) {
                TableViewerCreator<?> tableViewerCreator = extendedTableViewer.getTableViewerCreator();
                if (tableViewerCreator != null) {
                    return tableViewerCreator.getTableViewer();
                }
            }
        }
        return null;
    }

    public void setTableEditorView(AbstractDataTableEditorView<?> tableEditorView) {
        this.tableEditorView = tableEditorView;
    }

    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        String value = (String) getValue();
        ModuleListDialog dialog = new ModuleListDialog(cellEditorWindow.getShell(), value, this.param, false);
        if (dialog.open() == Window.OK) {
            String selecteModule = dialog.getSelecteModule();
            if (selecteModule != null && (value == null || !value.equals(selecteModule))) {
                int index = 0;
                if (getTableViewer() != null) {
                    index = getTableViewer().getTable().getSelectionIndex();
                }
                // enable to refresh component setting after change modules.
                IElement element = this.tableParam.getElement();
                if (element != null) {
                    IElementParameter updateComponentsParam = element.getElementParameter("UPDATE_COMPONENTS"); //$NON-NLS-1$
                    if (updateComponentsParam != null) {
                        updateComponentsParam.setValue(Boolean.TRUE);
                    }
                }
                //
                executeCommand(new ModelChangeCommand(tableParam, param.getName(), selecteModule, index));
                if (getTableViewer() != null) {
                    getTableViewer().refresh(true);
                }
                return selecteModule;
            }
        }
        return null;
    }

    /**
     * 
     * ggu ModelChangeCommand class global comment. Detailled comment
     */
    class ModelChangeCommand extends Command {

        private final IElementParameter param;

        private final String columnName;

        private final String value;

        private final int index;

        public ModelChangeCommand(IElementParameter param, String columnName, String value, int index) {
            super();
            this.param = param;
            this.columnName = columnName;
            this.value = value;
            this.index = index;
        }

        @Override
        public void execute() {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> values = (List<Map<String, Object>>) param.getValue();
            if (values == null) {
                values = new ArrayList<Map<String, Object>>();
                param.setValue(values);
            }
            if (values.size() > index) {
                Map<String, Object> line = values.get(index);
                if (line != null) {
                    line.put(columnName, value);
                }
            }
            // CorePlugin.getDefault().getLibrariesService().resetModulesNeeded();
        }

        @Override
        public void undo() {
            super.undo();
        }

    }

}
