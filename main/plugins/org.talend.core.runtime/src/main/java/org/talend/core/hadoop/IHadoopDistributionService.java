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
package org.talend.core.hadoop;

import org.talend.core.IService;
import org.talend.core.hadoop.version.EHadoopVersion4Drivers;


/**
 * created by cmeng on Jan 15, 2016
 * Detailled comment
 *
 */
public interface IHadoopDistributionService extends IService {

    public boolean isSupportSpark(EHadoopVersion4Drivers version);

    public boolean isSupportSparkStreaming(EHadoopVersion4Drivers version);
}
