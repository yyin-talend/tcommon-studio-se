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
package org.talend.core.runtime.repository.build;

import org.talend.core.model.properties.Item;
import org.talend.repository.documentation.ExportFileResource;

/**
 * DOC ggu class global comment. Detailled comment
 */
public final class BuildExportManager {

    private static final BuildExportManager instance = new BuildExportManager();

    private static final BuildExportRegistryReader reader = new BuildExportRegistryReader();

    private BuildExportManager() {
        super();
    }

    public static BuildExportManager getInstance() {
        return instance;
    }

    IBuildExportDependenciesProvider[] getDependenciesProviders() {
        return reader.getDependenciesProviders();
    }

    public void exportDependencies(ExportFileResource resource, Item item) {
        IBuildExportDependenciesProvider[] dependenciesProviders = getDependenciesProviders();
        if (dependenciesProviders != null) {
            for (IBuildExportDependenciesProvider provider : dependenciesProviders) {
                provider.exportDependencies(resource, item);
            }
        }
    }
}
