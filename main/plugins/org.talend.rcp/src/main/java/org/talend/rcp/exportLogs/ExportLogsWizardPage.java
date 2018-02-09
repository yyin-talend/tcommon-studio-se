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
package org.talend.rcp.exportLogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.rcp.i18n.Messages;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;

import com.sun.management.OperatingSystemMXBean;

/**
 * wzhang class global comment. Detailled comment
 */
public class ExportLogsWizardPage extends WizardPage {

    private Button logsFromArchiveRadio;

    private Text archivePathField;

    private Button browseArchivesButton;

    private String previouslyBrowsedArchive = ""; //$NON-NLS-1$

    private static final String[] FILE_EXPORT_MASK = { "*.zip", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

    private String lastPath;

    private Label label;

    private Button addLogsButton;

    private Button sysConfigButton;

    private static final int CPUTIME = 30;

    private static final int PERCENT = 100;

    private static final int FAULTLENGTH = 10;

    protected ExportLogsWizardPage(String pageName) {
        super(pageName);
        setDescription(Messages.getString("ExportLogsWizardPage.exportLog")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite workArea = new Composite(parent, SWT.NONE);
        setControl(workArea);
        workArea.setLayout(new GridLayout());
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        createLogsRoot(workArea);
    }

    private void createLogsRoot(Composite workArea) {
        Composite projectGroup = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        logsFromArchiveRadio = new Button(projectGroup, SWT.RADIO);
        // logsFromArchiveRadio.setText(DataTransferMessages.WizardProjectsImportPage_ArchiveSelectTitle);
        logsFromArchiveRadio.setText(Messages.getString("DataTransferMessages.WizardProjectsImportPage_ArchiveSelectTitle")); //$NON-NLS-1$

        archivePathField = new Text(projectGroup, SWT.BORDER);

        archivePathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        browseArchivesButton = new Button(projectGroup, SWT.PUSH);
        // browseArchivesButton.setText(DataTransferMessages.DataTransfer_browse);
        browseArchivesButton.setText(Messages.getString("DataTransferMessages.DataTransfer_browse")); //$NON-NLS-1$
        setButtonLayoutData(browseArchivesButton);

        archivePathField.setEnabled(false);
        browseArchivesButton.setEnabled(false);

        browseArchivesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleLocationArchiveButtonPressed();
            }
        });

        archivePathField.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    lastPath = archivePathField.getText().trim();
                }
            }
        });

        archivePathField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                lastPath = archivePathField.getText().trim();
            }
        });

        logsFromArchiveRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                archiveRadioSelected();
            }
        });

        label = new Label(workArea, SWT.NONE);
        label.setText(Messages.getString("ExportLogsWizardPage.selectItem")); //$NON-NLS-1$

        addLogsButton = new Button(workArea, SWT.CHECK);
        addLogsButton.setSelection(true);
        addLogsButton.setText(Messages.getString("ExportLogsWizardPage.addLog")); //$NON-NLS-1$

        sysConfigButton = new Button(workArea, SWT.CHECK);
        sysConfigButton.setSelection(true);
        sysConfigButton.setText(Messages.getString("ExportLogsWizardPage.sysConfig")); //$NON-NLS-1$

        addLogsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (addLogsButton.getSelection() == false && sysConfigButton.getSelection() == false) {
                    setPageComplete(false);
                } else {
                    setPageComplete(true);
                }
            }
        });

        sysConfigButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (addLogsButton.getSelection() == false && sysConfigButton.getSelection() == false) {
                    setPageComplete(false);
                } else {
                    setPageComplete(true);
                }
            }
        });
    }

    protected void handleLocationArchiveButtonPressed() {

        FileDialog dialog = new FileDialog(archivePathField.getShell(), SWT.SAVE);
        dialog.setFilterExtensions(FILE_EXPORT_MASK);
        // dialog.setText(DataTransferMessages.ArchiveExport_selectDestinationTitle);
        dialog.setText(Messages.getString("DataTransferMessages.ArchiveExport_selectDestinationTitle")); //$NON-NLS-1$

        String fileName = archivePathField.getText().trim();
        if (fileName.length() == 0) {
            fileName = previouslyBrowsedArchive;
        }

        if (fileName.length() == 0) {
            // dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString());
            dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
        } else {
            File path = new File(fileName);
            if (path.exists()) {
                dialog.setFilterPath(new Path(fileName).toOSString());
            }
        }

        String selectedArchive = dialog.open();
        if (selectedArchive != null) {
            previouslyBrowsedArchive = selectedArchive;
            archivePathField.setText(previouslyBrowsedArchive);
            lastPath = archivePathField.getText().trim();
        }
    }

    private void archiveRadioSelected() {
        if (logsFromArchiveRadio.getSelection()) {
            archivePathField.setEnabled(true);
            browseArchivesButton.setEnabled(true);
            lastPath = archivePathField.getText().trim();
            archivePathField.setFocus();
        }
    }

    public boolean performCancel() {
        return true;
    }

    public boolean performFinish() {
        if (!checkExportFile()) {
            return false;
        }
        if (sysConfigButton.getSelection()) {
            exportSysconfig(new File(lastPath));
        }
        if (addLogsButton.getSelection()) {
            try {
                exportLogs(new File(lastPath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ExportJobUtil.deleteTempFiles();

        return true;
    }

    private boolean checkExportFile() {
        if (lastPath == null || "".equals(lastPath.trim())) { //$NON-NLS-1$
            MessageDialog.openError(getShell(),
                    Messages.getString("ExportLogsWizardPage.error"), Messages.getString("ExportLogsWizardPage.errorMess")); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        return true;
    }

    private void exportLogs(File dest) throws Exception {
        String zipFile = dest.getAbsolutePath();

        String tmpFolder = ExportJobUtil.getTmpFolder();
        try {
            String logFile = Platform.getLogFileLocation().toOSString();
            String destFile = new File(tmpFolder + File.separator + new File(logFile).getName()).getAbsolutePath();
            ZipToFile.copyFile(logFile, destFile);
            ZipToFile.zipFile(tmpFolder, zipFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportSysconfig(File dest) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage memoryNoHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
        long used = memoryUsage.getUsed() / (1024 * 1024);
        String usedMemo = String.valueOf(used) + "MB"; //$NON-NLS-1$
        long max = memoryUsage.getMax() / (1024 * 1024);
        String maxMemo = String.valueOf(max) + "MB"; //$NON-NLS-1$
        long committed = memoryUsage.getCommitted() / (1024 * 1024);
        String committedMemo = String.valueOf(committed) + "MB"; //$NON-NLS-1$

        long noHeapUsed = memoryNoHeapUsage.getUsed() / (1024 * 1024);
        String noHeapUsedMemo = String.valueOf(noHeapUsed) + "MB"; //$NON-NLS-1$
        long noHeapMaxUnUsed = memoryNoHeapUsage.getMax() / (1024 * 1024);
        String noHeapMaxUnUsedMemo = String.valueOf(noHeapMaxUnUsed) + "MB"; //$NON-NLS-1$
        long noHeapCommitted = memoryNoHeapUsage.getCommitted() / (1024 * 1024);
        String noHeapCommittedMemo = String.valueOf(noHeapCommitted) + "MB"; //$NON-NLS-1$

        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalPhysicalMemo = osmxb.getTotalPhysicalMemorySize() / (1024 * 1024);
        String totalPhysicalMemorySize = String.valueOf(totalPhysicalMemo) + "MB"; //$NON-NLS-1$
        long freePhysicalMemo = osmxb.getFreePhysicalMemorySize() / (1024 * 1024);
        String freePhysicalMemorySize = String.valueOf(freePhysicalMemo) + "MB"; //$NON-NLS-1$

        // get thread count
        ThreadGroup parentThread;
        for (parentThread = Thread.currentThread().getThreadGroup(); parentThread.getParent() != null; parentThread = parentThread
                .getParent()) {
            ;
        }
        int totalThread = parentThread.activeCount();

        String totalThreadCount = String.valueOf(totalThread);

        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        // get CPU ratio, need a little long time.
        int cpuRatio = 0;
        if (osName.toLowerCase().startsWith("windows")) { //$NON-NLS-1$
            cpuRatio = (int) this.getCpuRatioForWindows();
        }
        String cpuUsed = String.valueOf(cpuRatio) + "%"; //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();

        Properties p = new Properties();
        p.putAll(System.getProperties());
        sb.append(" *** System properties\n"); //$NON-NLS-1$
        for (Entry<Object, Object> en : p.entrySet()) {
            sb.append(en.getKey() + "=" + en.getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        p.clear();
        p.put("Current CPU usage", cpuUsed); //$NON-NLS-1$
        p.put("Current thread count", totalThreadCount); //$NON-NLS-1$
        p.put("Total physical memory", totalPhysicalMemorySize); //$NON-NLS-1$
        p.put("Free physical memory", freePhysicalMemorySize); //$NON-NLS-1$
        p.put("Heap usage memory", usedMemo); //$NON-NLS-1$
        p.put("Heap maximum memory can be used", maxMemo); //$NON-NLS-1$
        p.put("Heap committed memory for JVM", committedMemo); //$NON-NLS-1$
        p.put("Non-Heap usage memory", noHeapUsedMemo); //$NON-NLS-1$
        p.put("Non-Heap maximum memory can be used", noHeapMaxUnUsedMemo); //$NON-NLS-1$
        p.put("Non-Heap committed memory for JVM", noHeapCommittedMemo); //$NON-NLS-1$

        sb.append("\n ***CPU&Memory properties\n"); //$NON-NLS-1$
        for (Entry<Object, Object> en : p.entrySet()) {
            sb.append(en.getKey() + "=" + en.getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String zipFile = dest.getAbsolutePath();
        String tmpFolder = ExportJobUtil.getTmpFolder();
        String destFile = new File(tmpFolder + File.separator + new File(".sysConfig")).getAbsolutePath(); //$NON-NLS-1$
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(destFile);
            out.write(sb.toString().getBytes());
            out.flush();
            if (!addLogsButton.getSelection()) {
                ZipToFile.zipFile(tmpFolder, zipFile);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double getCpuRatioForWindows() {
        try {
            String procCmd = System.getenv("windir") + "\\system32\\wbem\\wmic.exe process get Caption,CommandLine," //$NON-NLS-1$ //$NON-NLS-2$
                    + "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount"; //$NON-NLS-1$
            long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
            Thread.sleep(CPUTIME);
            long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
            if (c0 != null && c1 != null) {
                long idletime = c1[0] - c0[0];
                long busytime = c1[1] - c0[1];
                return Double.valueOf(PERCENT * (busytime) / (busytime + idletime)).doubleValue();
            } else {
                return 0.0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    private long[] readCpu(final Process proc) {
        long[] retn = new long[2];
        try {
            proc.getOutputStream().close();
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            if (line == null || line.length() < FAULTLENGTH) {
                return null;
            }
            int capidx = line.indexOf("Caption"); //$NON-NLS-1$
            int cmdidx = line.indexOf("CommandLine"); //$NON-NLS-1$
            int rocidx = line.indexOf("ReadOperationCount"); //$NON-NLS-1$
            int umtidx = line.indexOf("UserModeTime"); //$NON-NLS-1$
            int kmtidx = line.indexOf("KernelModeTime"); //$NON-NLS-1$
            int wocidx = line.indexOf("WriteOperationCount"); //$NON-NLS-1$
            long idletime = 0;
            long kneltime = 0;
            long usertime = 0;
            while ((line = input.readLine()) != null) {
                if (line.length() < wocidx) {
                    continue;
                }
                String caption = substring(line, capidx, cmdidx - 1).trim();
                String cmd = substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.indexOf("wmic.exe") >= 0) { //$NON-NLS-1$
                    continue;
                }
                if (caption.equals("System Idle Process") || caption.equals("System")) { //$NON-NLS-1$ //$NON-NLS-2$
                    idletime += Long.valueOf(substring(line, kmtidx, rocidx - 1).trim()).longValue();
                    idletime += Long.valueOf(substring(line, umtidx, wocidx - 1).trim()).longValue();
                    continue;
                }
                kneltime += Long.valueOf(substring(line, kmtidx, rocidx - 1).trim()).longValue();
                usertime += Long.valueOf(substring(line, umtidx, wocidx - 1).trim()).longValue();
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
            return retn;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                proc.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String substring(String src, int startIndex, int endIndex) {
        byte[] b = src.getBytes();
        String tgt = ""; //$NON-NLS-1$
        for (int i = startIndex; i <= endIndex; i++) {
            tgt += (char) b[i];
        }
        return tgt;
    }

}
