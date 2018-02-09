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
package org.talend.metadata.managment.hive.creator;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.ConnParameterKeys;
import org.talend.core.database.conn.DatabaseConnStrUtil;
import org.talend.core.database.conn.template.DbConnStrForHive;
import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.hadoop.EHadoopCategory;
import org.talend.core.hadoop.conf.EHadoopConfProperties;
import org.talend.core.hadoop.conf.EHadoopConfs;
import org.talend.core.hadoop.conf.EHadoopProperties;
import org.talend.core.hadoop.conf.HadoopDefaultConfsManager;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.connection.hive.HiveConnUtils;
import org.talend.core.model.metadata.connection.hive.HiveConnVersionInfo;
import org.talend.core.model.metadata.connection.hive.HiveServerVersionInfo;
import org.talend.core.model.metadata.connection.hive.HiveServerVersionUtils;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.metadata.managment.creator.AbstractHadoopDBConnectionCreator;

/**
 * created by ycbai on 2015年6月29日 Detailled comment
 *
 */
public class HiveConnectionCreator extends AbstractHadoopDBConnectionCreator {

    @Override
    public ConnectionItem create(Map<String, Map<String, String>> initParams) throws CoreException {
        DatabaseConnection connection = ConnectionFactory.eINSTANCE.createDatabaseConnection();
        Property connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
        setPropertyParameters(connectionProperty);

        DatabaseConnectionItem connectionItem = PropertiesFactory.eINSTANCE.createDatabaseConnectionItem();
        connectionItem.setProperty(connectionProperty);
        connectionItem.setConnection(connection);

        Map<String, String> paramsMap = new HashMap<>();
        retrieveCommonParameters(paramsMap);
        retrieveConnParameters(initParams, paramsMap);
        setParameters(connection, paramsMap);
        initializeConnectionParameters(connection);
        fillDefaultValues(connection);

        return connectionItem;
    }

    @Override
    public String getTypeName() {
        return EHadoopConfs.HIVE.getName();
    }

    @Override
    protected void retrieveCommonParameters(Map<String, String> paramsMap) {
        super.retrieveCommonParameters(paramsMap);
        paramsMap.put(ConnParameterKeys.CONN_PARA_KEY_DB_TYPE, EDatabaseConnTemplate.HIVE.getDBTypeName());
        paramsMap.put(ConnParameterKeys.CONN_PARA_KEY_DB_PRODUCT, EDatabaseTypeName.HIVE.getProduct());
    }

    private void retrieveConnParameters(Map<String, Map<String, String>> initParams, Map<String, String> paramsMap) {
        if (paramsMap == null) {
            return;
        }
        Map<String, String> params = initParams.get(getTypeName());
        if (params != null) {
            String hivePrincipal = params.get(EHadoopConfProperties.HIVE_SERVER2_AUTHENTICATION_KERBEROS_PRINCIPAL.getName());
            if (StringUtils.isNotEmpty(hivePrincipal)) {
                paramsMap.put(ConnParameterKeys.HIVE_AUTHENTICATION_HIVEPRINCIPLA, hivePrincipal);
            }
            String metastoreurl = params.get(EHadoopConfProperties.HIVE_METASTORE_URIS.getName());
            if (StringUtils.isNotEmpty(metastoreurl)) {
                paramsMap.put(ConnParameterKeys.HIVE_AUTHENTICATION_METASTOREURL, metastoreurl);
            }
            String username = params.get(EHadoopConfProperties.JAVAX_JDO_OPTION_CONNECTIONUSERNAME.getName());
            if (StringUtils.isNotEmpty(username)) {
                paramsMap.put(ConnParameterKeys.HIVE_AUTHENTICATION_USERNAME, username);
            }
            String password = params.get(EHadoopConfProperties.JAVAX_JDO_OPTION_CONNECTIONPASSWORD.getName());
            if (StringUtils.isNotEmpty(password)) {
                paramsMap.put(ConnParameterKeys.HIVE_AUTHENTICATION_PASSWORD, password);
            }
        }
    }

    private void fillDefaultValues(DatabaseConnection connection) {
        String distribution = connection.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_HIVE_DISTRIBUTION);
        String hiveVersion = connection.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_HIVE_VERSION);
        int distributionIndex = HiveConnUtils.getIndexOfDistribution(distribution == null ? null : distribution);
        int hiveVersionIndex = HiveConnUtils.getIndexOfHiveVersion(distribution == null ? null : distribution,
                hiveVersion == null ? null : hiveVersion);
        HiveConnVersionInfo hiveVersionObj = HiveConnUtils.getHiveVersionObj(distributionIndex, hiveVersionIndex);
        String[] hiveServerDisplayNames = HiveServerVersionUtils.extractAvailableArrayDisplayNames(hiveVersionObj);
        int indexofHiveServer = HiveServerVersionUtils.getIndexofHiveServer(hiveServerDisplayNames[0]);
        String hiveServer = HiveServerVersionUtils.extractKey(indexofHiveServer);
        connection.getParameters().put(ConnParameterKeys.HIVE_SERVER_VERSION, hiveServer);
        int hiveServerIndex = HiveConnUtils.getIndexOfHiveServer(hiveServer == null ? null : hiveServer);
        String[] hiveModeKeys = HiveConnUtils.getHiveModeKeys(distributionIndex, hiveVersionIndex, hiveServerIndex);
        String hiveMode = hiveModeKeys[0];
        connection.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_HIVE_MODE, hiveMode);
        int hiveModeIndex = HiveConnUtils.getIndexOfHiveMode(distribution == null ? null : distribution,
                hiveVersion == null ? null : hiveVersion, hiveMode == null ? null : hiveMode, hiveServer == null ? null
                        : hiveServer);
        boolean isEmbeddedMode = HiveConnUtils
                .isEmbeddedMode(distributionIndex, hiveVersionIndex, hiveModeIndex, hiveServerIndex);
        if (StringUtils.isEmpty(connection.getSID())) {
            String defaultDatabase = HadoopDefaultConfsManager.getInstance().getDefaultConfValue(distribution,
                    EHadoopCategory.HIVE.getName(), EHadoopProperties.DATABASE.getName());
            if (StringUtils.isNotEmpty(defaultDatabase)) {
                connection.setSID(defaultDatabase);
            }
        }
        if (StringUtils.isEmpty(connection.getPort())) {
            String defaultPort = null;
            if (isEmbeddedMode) {
                defaultPort = HadoopDefaultConfsManager.getInstance().getDefaultConfValue(distribution,
                        EHadoopCategory.HIVE.getName(), HiveConnVersionInfo.MODE_EMBEDDED.getKey(),
                        EHadoopProperties.PORT.getName());
            } else {
                defaultPort = HadoopDefaultConfsManager.getInstance().getDefaultConfValue(distribution,
                        EHadoopCategory.HIVE.getName(), HiveConnVersionInfo.MODE_STANDALONE.getKey(),
                        EHadoopProperties.PORT.getName());
            }
            if (StringUtils.isNotEmpty(defaultPort)) {
                connection.setPort(defaultPort);
            }
        }
        if (StringUtils.isEmpty(connection.getURL())) {
            String template = null;
            if (HiveServerVersionInfo.HIVE_SERVER_2.getKey().equals(hiveServer)) {
                template = DbConnStrForHive.URL_HIVE_2_TEMPLATE;
            } else {
                template = DbConnStrForHive.URL_HIVE_1_TEMPLATE;
            }
            String url = DatabaseConnStrUtil.getHiveURLString(connection, connection.getServerName(), connection.getPort(),
                    connection.getSID(), template);
            if (StringUtils.isNotEmpty(url)) {
                connection.setURL(url);
            }
        }
    }

}
