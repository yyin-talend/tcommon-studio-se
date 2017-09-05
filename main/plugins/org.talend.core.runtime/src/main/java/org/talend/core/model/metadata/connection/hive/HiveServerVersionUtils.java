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
package org.talend.core.model.metadata.connection.hive;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marvin Wang on Mar 25, 2013.
 */
public class HiveServerVersionUtils {

    /**
     * Extracts all display names for hive server version, all versions are from {@link HiveServerVersionInfo}. Added by
     * Marvin Wang on Mar 25, 2013.
     * 
     * @return
     */
    public static List<String> extractListDisplayNames() {
        HiveServerVersionInfo[] infos = HiveServerVersionInfo.values();
        List<String> displayNames = new ArrayList<String>();
        for (HiveServerVersionInfo info : infos) {
            displayNames.add(info.getDisplayName());
        }

        return displayNames;
    }

    public static List<String> extractAvailableListDisplayNames(HiveConnVersionInfo hadoopVersion) {
        HiveServerVersionInfo[] infos = HiveServerVersionInfo.values();
        List<String> keys = new ArrayList<String>();
        for (HiveServerVersionInfo info : infos) {
            if (hadoopVersion.isSupportHiveServerVersion(info)) {
                keys.add(info.getDisplayName());
            }
        }

        return keys;
    }

    /**
     * Extracts the array of display names, refer to this method {@link #extractListDisplayNames()}. Added by Marvin
     * Wang on Mar 25, 2013.
     * 
     * @return
     */
    public static String[] extractArrayDisplayNames() {
        List<String> list = extractListDisplayNames();
        return list.toArray(new String[list.size()]);
    }

    public static String[] extractAvailableArrayDisplayNames(HiveConnVersionInfo hadoopVersion) {
        List<String> list = extractAvailableListDisplayNames(hadoopVersion);
        return list.toArray(new String[list.size()]);
    }

    public static List<String> extractListKeys() {
        HiveServerVersionInfo[] infos = HiveServerVersionInfo.values();
        List<String> keys = new ArrayList<String>();
        for (HiveServerVersionInfo info : infos) {
            keys.add(info.getKey());
        }

        return keys;
    }

    /**
     * Gets the index of hive server by display name, if no index found, then return 0. Added by Marvin Wang on Mar 25,
     * 2013.
     * 
     * @param hiveServerDisplayName
     * @return 0 if there is no index found.
     */
    public static int getIndexofHiveServer(String hiveServerDisplayName) {
        HiveServerVersionInfo[] infos = HiveServerVersionInfo.values();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getDisplayName().equalsIgnoreCase(hiveServerDisplayName)) {
                return i;
            }
        }
        return 0;
    }

    public static int getIndexofHiveServerByKey(String hiveKey) {
        HiveServerVersionInfo[] infos = HiveServerVersionInfo.values();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getKey().equalsIgnoreCase(hiveKey)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Extracts the display name by the index. Added by Marvin Wang on Mar 25, 2013.
     * 
     * @param index
     * @return
     */
    public static String extractDisplayName(int index) {
        List<String> displayNames = extractListDisplayNames();
        if (index < 0) {
            return null;
        }
        return displayNames.get(index);
    }

    /**
     * Extracts the key which is mapped to t*component.xml. Added by Marvin Wang on Mar 26, 2013.
     * 
     * @param index
     * @return
     */
    public static String extractKey(int index) {
        List<String> keys = extractListKeys();
        if (index < 0) {
            return null;
        }
        return keys.get(index);
    }

}
