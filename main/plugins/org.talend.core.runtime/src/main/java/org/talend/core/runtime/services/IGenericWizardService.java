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
package org.talend.core.runtime.services;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.IService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;

/**
 * created by ycbai on 2015年9月10日 Detailled comment
 *
 */
public interface IGenericWizardService extends IService {

    /**
     * Create repository nodes by component service.
     * 
     * @param curParentNode
     */
    public List<RepositoryNode> createNodesFromComponentService(RepositoryNode curParentNode);

    /**
     * Get all the names of generic type.
     * 
     * @return
     */
    public List<String> getGenericTypeNames();

    /**
     * 
     * Estimate whether <code>repObjType</code> is a generic type or not.
     * 
     * @param repObjType
     * @return
     */
    public boolean isGenericType(ERepositoryObjectType repObjType);

    public boolean isGenericItem(Item item);

    public boolean isGenericConnection(Connection connection);

    /**
     * Get node image by node type name.
     * 
     * @param typeName
     * @return
     */
    public Image getNodeImage(String typeName);

    /**
     * Get wizard image by node type name.
     * 
     * @param typeName
     * @return
     */
    public Image getWiardImage(String typeName);

    /**
     *
     * Get metadata tables from connection
     * 
     * @param connection
     * @return
     */
    public List<MetadataTable> getMetadataTables(Connection connection);

    /**
     * Get the dynamic composite
     *
     * @param composite
     * @param sectionCategory
     * @param isCompactView
     * @return
     */
    public Composite creatDynamicComposite(Composite composite, Element element, EComponentCategory sectionCategory,
            boolean isCompactView);
}
