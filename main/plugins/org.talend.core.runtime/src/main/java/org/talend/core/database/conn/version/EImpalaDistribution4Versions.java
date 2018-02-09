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
package org.talend.core.database.conn.version;

import java.util.ArrayList;
import java.util.List;

public enum EImpalaDistribution4Versions {

    CLOUDERA_CDH5_5(EImpalaDistributions.CLOUDERA, "Cloudera CDH5.5", EDatabaseVersion4Drivers.IMPALA_CDH5_5.getVersionValue()),

    CLOUDERA_CDH5_4(EImpalaDistributions.CLOUDERA, "Cloudera CDH5.4", EDatabaseVersion4Drivers.IMPALA_CDH5_4.getVersionValue()),

    CLOUDERA_CDH5_1(EImpalaDistributions.CLOUDERA, "Cloudera CDH5.1", EDatabaseVersion4Drivers.IMPALA_CDH5.getVersionValue()),

    CUSTOM(EImpalaDistributions.CUSTOM, "", "");

    private EImpalaDistributions distribution;

    private String versionDisplayName;

    private String versionValue;

    EImpalaDistribution4Versions(EImpalaDistributions distribution, String versionDisplayName, String versionValue) {
        this.distribution = distribution;
        this.versionDisplayName = versionDisplayName;
        this.versionValue = versionValue;
    }

    public static List<EImpalaDistribution4Versions> indexOfByDistribution(String distribution) {
        List<EImpalaDistribution4Versions> distribution4Versions = new ArrayList<EImpalaDistribution4Versions>();
        if (distribution != null) {
            for (EImpalaDistribution4Versions d4v : EImpalaDistribution4Versions.values()) {
                if (d4v.getDistribution().getName().equals(distribution)
                        || d4v.getDistribution().getDisplayName().equals(distribution)) {
                    distribution4Versions.add(d4v);
                }
            }
        }
        return distribution4Versions;
    }

    public static EImpalaDistribution4Versions indexOfByVersionDisplay(String displayName) {
        return indexOf(displayName, true);
    }

    public static EImpalaDistribution4Versions indexOfByVersion(String value) {
        return indexOf(value, false);
    }

    private static EImpalaDistribution4Versions indexOf(String name, boolean display) {
        if (name != null) {
            for (EImpalaDistribution4Versions version : EImpalaDistribution4Versions.values()) {
                if (display) {
                    if (name.equalsIgnoreCase(version.getVersionDisplayName())) {
                        return version;
                    }
                } else {
                    if (name.equalsIgnoreCase(version.getVersionValue())) {
                        return version;
                    }
                }
            }
        }
        return null;
    }

    public static List<String> getHadoopDistributionVersions(String distribution) {
        return getHadoopDistributionVersions(distribution, true);
    }

    public static List<String> getHadoopDistributionVersions(String distribution, boolean display) {
        List<String> result = new ArrayList<String>();
        List<EImpalaDistribution4Versions> d4vList = EImpalaDistribution4Versions.indexOfByDistribution(distribution);
        for (EImpalaDistribution4Versions d4v : d4vList) {
            if (display) {
                result.add(d4v.getVersionDisplayName());
            } else {
                result.add(d4v.getVersionValue());
            }
        }
        return result;
    }

    public EImpalaDistributions getDistribution() {
        return this.distribution;
    }

    public String getVersionDisplayName() {
        return this.versionDisplayName;
    }

    public String getVersionValue() {
        return this.versionValue;
    }
}
