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
package org.talend.designer.maven.tools.creator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.routines.IRoutinesService;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.designer.maven.template.MavenTemplateManager;
import org.talend.designer.maven.utils.PomUtil;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class CreateMavenRoutinePom extends CreateMavenBundleTemplatePom {

    public CreateMavenRoutinePom(IFile pomFile) {
        super(pomFile, IProjectSettingTemplateConstants.POM_ROUTINGS_TEMPLATE_FILE_NAME);
    }

    @Override
    protected Model createModel() {
        final Model routinesModel = MavenTemplateManager.getRoutinesTempalteModel();

        this.setGroupId(routinesModel.getGroupId());
        this.setArtifactId(routinesModel.getArtifactId());
        this.setVersion(routinesModel.getVersion());
        this.setName(routinesModel.getName());

        setAttributes(routinesModel);
        addProperties(routinesModel);

        PomUtil.checkParent(routinesModel, this.getPomFile());

        addDependencies(routinesModel);

        return routinesModel;
    }

    protected void addDependencies(Model model) {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRoutinesService.class)) {
            IRoutinesService routiensService = (IRoutinesService) GlobalServiceRegister.getDefault().getService(
                    IRoutinesService.class);
            Set<ModuleNeeded> runningModules = routiensService.getRunningModules();
            if (runningModules != null) {
                List<Dependency> existedDependencies = model.getDependencies();
                if (existedDependencies == null) {
                    existedDependencies = new ArrayList<Dependency>();
                    model.setDependencies(existedDependencies);
                }

                for (ModuleNeeded module : runningModules) {
                    Dependency dependency = PomUtil.createModuleDependency(module.getModuleName());
                    if (dependency != null) {
                        existedDependencies.add(dependency);
                    }
                }
            }
        }
    }

}
