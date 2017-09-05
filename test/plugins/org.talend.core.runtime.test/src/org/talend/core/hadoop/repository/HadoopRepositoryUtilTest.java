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
package org.talend.core.hadoop.repository;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

/**
 * created by ycbai on 2016年2月18日 Detailled comment
 *
 */
public class HadoopRepositoryUtilTest {

    private static final String QUOTE = "\""; //$NON-NLS-1$

    private final static String PROPERTY_VALUE = "VALUE"; //$NON-NLS-1$

    @Test
    public void testGetHadoopPropertiesList() {
        String propertiesJsonStr = "[{\"PROPERTY\":\"prop1\",\"VALUE\":\"value1\"},{\"PROPERTY\":\"prop2\",\"VALUE\":\"value2\"}]"; //$NON-NLS-1$

        List<Map<String, Object>> propertiesList = HadoopRepositoryUtil.getHadoopPropertiesList(propertiesJsonStr, false, false);
        checkPropertiesQuotes(propertiesList, false, false);

        propertiesList = HadoopRepositoryUtil.getHadoopPropertiesList(propertiesJsonStr, false, true);
        checkPropertiesQuotes(propertiesList, false, true);

        propertiesList = HadoopRepositoryUtil.getHadoopPropertiesList(propertiesJsonStr, true, false);
        checkPropertiesQuotes(propertiesList, true, false);

        propertiesList = HadoopRepositoryUtil.getHadoopPropertiesList(propertiesJsonStr, true, true);
        checkPropertiesQuotes(propertiesList, true, true);

        assertEquals(2, propertiesList.size());
    }

    private void checkPropertiesQuotes(List<Map<String, Object>> propertiesList, boolean isContextMode, boolean needQuotes) {
        for (Map<String, Object> properties : propertiesList) {
            Iterator<Entry<String, Object>> propertiesIterator = properties.entrySet().iterator();
            while (propertiesIterator.hasNext()) {
                Entry<String, Object> propertiesEntry = propertiesIterator.next();
                String key = propertiesEntry.getKey();
                String value = String.valueOf(propertiesEntry.getValue());
                if (needQuotes) {
                    if (isContextMode && PROPERTY_VALUE.equals(key)) {
                        assertTrue(!isContainsQuotes(value));
                    } else {
                        assertTrue(isContainsQuotes(value));
                    }
                } else {
                    assertTrue(!isContainsQuotes(value));
                }
            }
        }
    }

    private boolean isContainsQuotes(String str) {
        return str != null && str.length() > 1 && str.startsWith(QUOTE) && str.endsWith(QUOTE);
    }

}
