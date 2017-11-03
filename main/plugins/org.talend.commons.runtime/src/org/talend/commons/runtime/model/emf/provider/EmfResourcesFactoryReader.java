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
import org.talend.commons.CommonsPlugin;
import org.talend.commons.runtime.extension.ExtensionRegistryReader;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class EmfResourcesFactoryReader extends ExtensionRegistryReader {

    public static final EmfResourcesFactoryReader INSTANCE = new EmfResourcesFactoryReader();

    /*
     * id==> bean
     */
    private Map<String, OptionProviderBean> saveOptionsBeans = new HashMap<String, OptionProviderBean>();

    private Map<String, OptionProviderBean> loadOptionsBeans = new HashMap<String, OptionProviderBean>();

    /*
     * id ==> provider
     */
    private Map<String, EOptionProvider> saveOptionsProviders = new HashMap<String, EOptionProvider>();

    private Map<String, EOptionProvider> loadOptionsProviders = new HashMap<String, EOptionProvider>();

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

        cleanOverrideIds(saveOptionsProviders, saveOptionsBeans);
        cleanOverrideIds(loadOptionsProviders, loadOptionsBeans);
    }

    private Map<String, OptionProviderBean> cleanOverrideIds(Map<String, EOptionProvider> optionsProvidersMap,
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
            optionsProvidersMap.put(entry.getKey(), optionProvider);
        }

        return withoutOverrideMap;
    }

    public Map<String, Object> getSaveOptions(Object resource) {
        Map<String, Object> saveOptions = new HashMap<String, Object>();
        for (Map.Entry<String, EOptionProvider> entry : saveOptionsProviders.entrySet()) {
            final EOptionProvider provider = entry.getValue();
            if (provider.checkSave(resource)) {
                saveOptions.put(provider.getName(), provider.getValue());
            }
        }
        return saveOptions;
    }

    public Map<String, Object> getLoadOptions(Object resource) {
        Map<String, Object> loadOptions = new HashMap<String, Object>();
        for (Map.Entry<String, EOptionProvider> entry : loadOptionsProviders.entrySet()) {
            final EOptionProvider provider = entry.getValue();
            if (provider.checkLoad(resource)) {
                loadOptions.put(provider.getName(), provider.getValue());
            }
        }
        return loadOptions;
    }

    public Map<String, EOptionProvider> getSaveOptionsProviders() {
        return saveOptionsProviders;
    }

    public Map<String, EOptionProvider> getLoadOptionsProviders() {
        return loadOptionsProviders;
    }

    @Override
    protected boolean readElement(final IConfigurationElement element) {
        if ("saveOption".equals(element.getName())) { //$NON-NLS-1$
            SafeRunner.run(new RegistrySafeRunnable() {

                @Override
                public void run() throws Exception {
                    createProvider(saveOptionsBeans, element);
                }
            });
        }
        if ("loadOption".equals(element.getName())) { //$NON-NLS-1$
            SafeRunner.run(new RegistrySafeRunnable() {

                @Override
                public void run() throws Exception {
                    createProvider(loadOptionsBeans, element);
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
