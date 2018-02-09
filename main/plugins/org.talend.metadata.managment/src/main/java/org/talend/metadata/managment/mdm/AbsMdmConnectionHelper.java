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
package org.talend.metadata.managment.mdm;

import java.io.File;
import java.util.List;

import org.apache.axis.client.Stub;
import org.talend.core.model.metadata.builder.connection.MDMConnection;

/**
 * created by wchen on Apr 15, 2015 Detailled comment
 *
 */
public abstract class AbsMdmConnectionHelper {

    public abstract Stub checkConnection(String url, String universe, String userName, String password) throws Exception;

    public abstract List<String> getPKs(Stub stub, String modelOrContainerMethod, String modelOrContainerClass, String pkRegex)
            throws Exception;

    public abstract void initConcept(MDMConnection mdmConn, File file) throws Exception;

    public abstract String getXsdSchema(Stub stub, String resName) throws Exception;

}
