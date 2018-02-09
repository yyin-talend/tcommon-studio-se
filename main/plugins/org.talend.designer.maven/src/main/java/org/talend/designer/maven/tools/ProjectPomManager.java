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
package org.talend.designer.maven.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.talend.core.model.process.JobInfo;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.template.MavenTemplateManager;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.IProcessor;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ProjectPomManager {

    protected static final MavenModelManager MODEL_MANAGER = MavenPlugin.getMavenModelManager();

    private IFile projectPomFile;

    /**
     * true by default, update all
     */
    private boolean updateModules = true;

    private boolean updateDependencies = true;

    public ProjectPomManager(IProject project) {
        super();
        Assert.isNotNull(project);
        this.projectPomFile = project.getFile(TalendMavenConstants.POM_FILE_NAME);
    }

    public void setUpdateModules(boolean updateModules) {
        this.updateModules = updateModules;
    }

    public void setUpdateDependencies(boolean updateDependencies) {
        this.updateDependencies = updateDependencies;
    }

    protected boolean isUpdateModules() {
        return updateModules;
    }

    protected boolean isUpdateDependencies() {
        return updateDependencies;
    }

    public void update(IProgressMonitor monitor, IProcessor processor) throws Exception {
        projectPomFile.getProject().refreshLocal(IResource.DEPTH_ONE, monitor);
        if (!projectPomFile.exists()) {// delete by user manually?
            // create it or nothing to do?
            return;
        }
        Model projectModel = MODEL_MANAGER.readMavenModel(projectPomFile);

        // attributes
        updateAttributes(monitor, processor, projectModel);
        // modules
        updateModulesList(monitor, processor, projectModel);
        // dependencies
        updateDependencies(monitor, processor, projectModel);

        PomUtil.savePom(monitor, projectModel, projectPomFile);

        projectPomFile.getProject().refreshLocal(IResource.DEPTH_ONE, monitor);
    }

    /**
     * 
     * update the main attributes for project pom.
     * 
     */
    protected void updateAttributes(IProgressMonitor monitor, IProcessor processor, Model projectModel) throws Exception {
        Model templateModel = MavenTemplateManager.getCodeProjectTemplateModel();

        projectModel.setGroupId(templateModel.getGroupId());
        projectModel.setArtifactId(templateModel.getArtifactId());
        projectModel.setVersion(templateModel.getVersion());
        projectModel.setName(templateModel.getName());
        projectModel.setPackaging(templateModel.getPackaging());
    }

    /**
     * 
     * update the modules list for project pom.
     * 
     * The routines should be added always.
     */
    protected void updateModulesList(IProgressMonitor monitor, IProcessor processor, Model projectModel) throws Exception {
        if (!isUpdateModules()) {
            return;
        }
        List<String> modulesList = new ArrayList<String>();
        // add routines always.
        String routinesModule = PomUtil.getPomFileName(TalendMavenConstants.DEFAULT_ROUTINES_ARTIFACT_ID);
        modulesList.add(routinesModule);

        if (processor != null) {
            for (JobInfo childJob : processor.getBuildChildrenJobs()) {
                modulesList.add(PomUtil.getPomFileName(childJob.getJobName(), childJob.getJobVersion()));
            }
            modulesList.add(PomUtil.getPomFileName(processor.getProperty().getLabel(), processor.getProperty().getVersion()));
        }
        // check the modules
        List<String> modules = projectModel.getModules();
        if (modules == null) {
            modules = new ArrayList<String>();
            projectModel.setModules(modules);
        } else if (!modules.isEmpty()) {
            modules.clear(); // clean all?
        }

        modules.addAll(modulesList);

        /*
         * need update the parent for each modules.
         */
        IProject project = projectPomFile.getProject();
        project.refreshLocal(IResource.DEPTH_ONE, monitor);

        for (String module : modules) {
            IFile file = project.getFile(module);
            if (file.exists()) {
                Model model = MODEL_MANAGER.readMavenModel(file);
                Parent parent = model.getParent();
                // only check same parent.
                if (parent != null) {
                    if (parent.getGroupId().equals(projectModel.getGroupId())) {
                        continue; // not same
                    }

                    PomUtil.checkParent(model, file);
                    PomUtil.savePom(monitor, model, file);
                }
            }
        }
    }

    /**
     * If standard job and base job pom file existed, will use the dependences of job pom directly.
     */
    protected void updateDependencies(IProgressMonitor monitor, IProcessor processor, Model projectModel) throws Exception {
        if (!isUpdateDependencies()) {
            return;
        }
        IFile basePomFile = getBasePomFile();
        if (isStandardJob() && basePomFile != null && basePomFile.getLocation().toFile().exists()) {
            if (!basePomFile.exists()) {
                basePomFile.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
            }
            Model jobModel = MODEL_MANAGER.readMavenModel(basePomFile);

            List<Dependency> withoutChildrenJobDependencies = new ArrayList<Dependency>(jobModel.getDependencies());
            // org.talend.job
            final String jobGroupPrefix = PomIdsHelper.getJobGroupId((String) null);

            Iterator<Dependency> iterator = withoutChildrenJobDependencies.iterator();
            while (iterator.hasNext()) {
                Dependency d = iterator.next();
                if (d.getGroupId().startsWith(jobGroupPrefix)) {
                    // remove the children job's dependencies
                    iterator.remove();
                }
            }

            // fresh is false, make sure all jobs can be compile ok
            ProcessorDependenciesManager.updateDependencies(monitor, projectModel, withoutChildrenJobDependencies, false);

        } else if (processor != null) {// try get the dependencies from processor directly.
            ProcessorDependenciesManager processorDependenciesManager = new ProcessorDependenciesManager(processor);
            processorDependenciesManager.updateDependencies(monitor, projectModel);
        } else {
            // if no processor and without base pom, just read routines dependencies.
            IFile routinesPomFile = projectPomFile.getProject().getFile(
                    PomUtil.getPomFileName(TalendMavenConstants.DEFAULT_ROUTINES_ARTIFACT_ID));
            Model routinesModel = MavenTemplateManager.getRoutinesTempalteModel();
            if (routinesPomFile.exists()) {
                routinesModel = MODEL_MANAGER.readMavenModel(routinesPomFile);
            }
            // only add the routines dependencies.
            List<Dependency> routinesDependencies = routinesModel.getDependencies();
            ProcessorDependenciesManager.updateDependencies(monitor, projectModel, routinesDependencies, true);
        }
    }

    protected boolean isStandardJob() {
        return true;
    }

    protected IFile getBasePomFile() {
        return null;
    }

}
