// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.librariesmanager.ui.startup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.MojoType;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.nexus.TalendMavenResolver;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.librariesmanager.maven.ShareLibrareisHelper;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.librariesmanager.ui.i18n.Messages;
import org.talend.utils.io.FilesUtils;

/**
 * Share libs from local maven to svn lib or nexus server depends on TAC setup, created by wchen on 2015年7月31日 Detailled
 * comment
 *
 */
public class ShareMavenArtifactsOnStartup extends ShareLibrareisHelper {

    @Override
    public Map<ModuleNeeded, File> getFilesToShare(IProgressMonitor monitor) {
        Map<ModuleNeeded, File> files = new HashMap<>();
        SubMonitor mainSubMonitor = SubMonitor.convert(monitor, 1);
        mainSubMonitor.setTaskName(Messages.getString("ShareLibsJob.getFilesToShare")); //$NON-NLS-1$
        final List<ModuleNeeded> modulesNeeded = new ArrayList<>(ModulesNeededProvider.getModulesNeeded());
        ILibraryManagerService librariesService = GlobalServiceRegister.getDefault().getService(
                ILibraryManagerService.class);
        Set<String> filePaths = new HashSet<>();
        for (ModuleNeeded module : modulesNeeded) {
            if (monitor.isCanceled()) {
                return null;
            }
            // to skip dynamic distribution when startUp share
            MavenArtifact artifact = MavenUrlHelper.parseMvnUrl(module.getMavenUri());
            if (artifact != null) {
                if (StringUtils.isNotBlank(artifact.getRepositoryUrl())) {
                    continue;
                }
            }

            String jarPathFromMaven = librariesService.getJarPathFromMaven(module.getMavenUri());
            if (jarPathFromMaven == null) {
                String moduleLocation = module.getModuleLocaion();
                if (moduleLocation != null && librariesService.checkJarInstalledFromPlatform(moduleLocation)) {
                    librariesService.installModules(Arrays.asList(module), monitor);
                    jarPathFromMaven = librariesService.getJarPathFromMaven(module.getMavenUri());
                }
                if (jarPathFromMaven == null) {
                    continue;
                }
            }
            File jarFile = new File(jarPathFromMaven);
            if (jarFile.exists()) {
                if (!filePaths.contains(jarPathFromMaven)) {
                    files.put(module, jarFile);
                }
                filePaths.add(jarPathFromMaven);
            }
        }

        // get plugin artifacts to share
        Stream.of(MojoType.values()).forEach(m -> {
            String mvnUrl = MavenUrlHelper.generateMvnUrl(TalendMavenConstants.DEFAULT_CI_GROUP_ID, m.getArtifactId(),
                    VersionUtils.getMojoVersion(m), null, null);
            // try to resolve locally
            String localMvnUrl = mvnUrl.replace(MavenUrlHelper.MVN_PROTOCOL,
                    MavenUrlHelper.MVN_PROTOCOL + MavenConstants.LOCAL_RESOLUTION_URL + MavenUrlHelper.REPO_SEPERATOR);
            File file = null;
            try {
                file = TalendMavenResolver.resolve(localMvnUrl);
            } catch (IOException | RuntimeException e) {
                ExceptionHandler.process(e);
            }
            if (file != null) {
                ModuleNeeded module = new ModuleNeeded("", mvnUrl, "", true);
                files.put(module, file);
            }
        });

        mainSubMonitor.worked(1);
        return files;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.librariesmanager.utils.ShareLibrareisHelper#deployToLocalMavenOnExist(java.io.File,
     * org.talend.core.model.general.ModuleNeeded)
     */
    @Override
    public void shareToRepository(File file, MavenArtifact artifact) throws Exception {
        String name = file.getName();
        int indexOf = name.lastIndexOf(".");
        String pomPath = file.getParent();
        if (indexOf != -1) {
            pomPath = pomPath + "/" + name.substring(0, indexOf) + "." + MavenConstants.PACKAGING_POM;
        } else {
            pomPath = pomPath + name + "." + MavenConstants.PACKAGING_POM;
        }
        File pomFile = new File(pomPath);
        if (!pomFile.exists()) {
            File generatedPom = new File(PomUtil.generatePom(artifact));
            FilesUtils.copyFile(generatedPom, pomFile);
            FilesUtils.deleteFolder(generatedPom.getParentFile(), true);
        }
        deployer.deploy(file, artifact);
        // artifact.setType(MavenConstants.PACKAGING_POM);
        // deployer.deploy(pomFile, artifact);
    }

}
