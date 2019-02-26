// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.service;

import java.util.List;
import java.util.Map;

import org.talend.core.IService;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public interface IAnonymizeService extends IService{
    
    public List<String[]> getDecomposeCommandLines(Map<String, Object> argMap) throws Exception;
    
    public boolean isAnonymize(String[] args);

}
