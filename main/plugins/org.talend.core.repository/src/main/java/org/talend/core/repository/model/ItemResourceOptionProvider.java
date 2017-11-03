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
package org.talend.core.repository.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.talend.commons.runtime.model.emf.provider.OptionProvider;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ItemResourceOptionProvider extends OptionProvider {

    public static final String ITEM_VALUE = "item_value"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.runtime.model.emf.provider.EOptionProvider#getName()
     */
    @Override
    public String getName() {
        return XMLResource.OPTION_RESOURCE_HANDLER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.runtime.model.emf.provider.EOptionProvider#getValue()
     */
    @Override
    public Object getValue() {
        return new XMLResource.ResourceHandler() {

            @Override
            public void preLoad(XMLResource resource, InputStream inputStream, Map<?, ?> options) {
                //

            }

            @Override
            public void postLoad(XMLResource resource, InputStream inputStream, Map<?, ?> options) {
                //

            }

            @SuppressWarnings("rawtypes")
            @Override
            public void preSave(XMLResource resource, OutputStream outputStream, Map<?, ?> options) {
                final Property prop = (Property) EcoreUtil.getObjectByType(resource.getContents(),
                        PropertiesPackage.eINSTANCE.getProperty());
                if (prop != null) {
                    final Iterator iterator = prop.getAdditionalProperties().keySet().iterator();
                    while (iterator.hasNext()) {
                        if (ITEM_VALUE.equals(iterator.next())) {
                            iterator.remove();
                        }
                    }
                }

            }

            @Override
            public void postSave(XMLResource resource, OutputStream outputStream, Map<?, ?> options) {
                //

            }

        };
    }

}
