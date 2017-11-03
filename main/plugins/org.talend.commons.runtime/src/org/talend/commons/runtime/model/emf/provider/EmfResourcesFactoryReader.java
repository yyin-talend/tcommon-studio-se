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
package org.talend.commons.runtime.model.emf.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.runtime.extension.ExtensionRegistryReader;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class EmfResourcesFactoryReader extends ExtensionRegistryReader {

    public static final EmfResourcesFactoryReader INSTANCE = new EmfResourcesFactoryReader();

    private Map<String, Object> saveOptions = new HashMap<String, Object>();

    private Map<String, OptionProviderBean> saveOptionsProviders = new HashMap<String, OptionProviderBean>();

    private Map<String, Object> loadOptions = new HashMap<String, Object>();

    private Map<String, OptionProviderBean> loadOptionsProviders = new HashMap<String, OptionProviderBean>();

    class OptionProviderBean {

        String id, description, overrideId;

        EOptionProvider provider;
    }

    private EmfResourcesFactoryReader() {
        super(CommonsPlugin.PLUGIN_ID, "emfResourcesProvider"); //$NON-NLS-1$
        init();
    }

    void init() {
        readRegistry();

        cleanOverrideIds(saveOptions, saveOptionsProviders);
        cleanOverrideIds(loadOptions, loadOptionsProviders);
    }

    private Map<String, OptionProviderBean> cleanOverrideIds(Map<String, Object> optionMap,
            Map<String, OptionProviderBean> extensionMap) {
        Set<String> ids = new HashSet<String>();
        Map<String, OptionProviderBean> withoutOverrideMap = new HashMap<String, OptionProviderBean>(extensionMap);

        for (Map.Entry<String, OptionProviderBean> e : withoutOverrideMap.entrySet()) {
            final String overrideId = e.getValue().overrideId;
            if (overrideId != null) {
                ids.add(overrideId);
            }
        }

        // remove override ids
        final Iterator<String> removediterator = withoutOverrideMap.keySet().iterator();
        while (removediterator.hasNext()) {
            if (ids.contains(removediterator.next())) {
                removediterator.remove();
            }
        }

        for (Map.Entry<String, OptionProviderBean> entry : withoutOverrideMap.entrySet()) {
            final EOptionProvider optionProvider = entry.getValue().provider;
            optionMap.put(optionProvider.getName(), optionProvider.getValue());
        }

        return withoutOverrideMap;
    }

    public Map<String, Object> getSaveOptions(Resource resource) {
        return saveOptions;
    }

    public Map<String, Object> getLoadOptions(org.eclipse.emf.common.util.URI uri) {
        return loadOptions;
    }

    @Override
    protected boolean readElement(final IConfigurationElement element) {
        if ("saveOption".equals(element.getName())) { //$NON-NLS-1$
            SafeRunner.run(new RegistrySafeRunnable() {

                @Override
                public void run() throws Exception {
                    createProvider(saveOptionsProviders, element);
                }
            });
        }
        if ("loadOption".equals(element.getName())) { //$NON-NLS-1$
            SafeRunner.run(new RegistrySafeRunnable() {

                @Override
                public void run() throws Exception {
                    createProvider(loadOptionsProviders, element);
                }
            });
        }
        return false;
    }

    private void createProvider(Map<String, OptionProviderBean> map, IConfigurationElement element) throws CoreException {
        String id = element.getAttribute("id"); //$NON-NLS-1$
        String description = element.getAttribute("description"); //$NON-NLS-1$
        String overrideId = element.getAttribute("override"); //$NON-NLS-1$
        EOptionProvider provider = (EOptionProvider) element.createExecutableExtension("provider");//$NON-NLS-1$
        OptionProviderBean bean = new OptionProviderBean();
        bean.id = id;
        bean.description = description;
        bean.overrideId = overrideId;
        bean.provider = provider;
        map.put(id, bean);
    }

}
