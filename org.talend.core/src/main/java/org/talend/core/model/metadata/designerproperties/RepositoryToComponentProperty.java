// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.metadata.designerproperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.i18n.Messages;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.EMetadataEncoding;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.MultiSchemasUtil;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Concept;
import org.talend.core.model.metadata.builder.connection.ConceptTarget;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.EbcdicConnection;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.metadata.builder.connection.FileConnection;
import org.talend.core.model.metadata.builder.connection.FileExcelConnection;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.metadata.builder.connection.LDAPSchemaConnection;
import org.talend.core.model.metadata.builder.connection.LdifFileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.PositionalFileConnection;
import org.talend.core.model.metadata.builder.connection.RegexpFileConnection;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionParameterColumn;
import org.talend.core.model.metadata.builder.connection.SAPFunctionParameterTable;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.metadata.builder.connection.SchemaTarget;
import org.talend.core.model.metadata.builder.connection.WSDLParameter;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XMLFileNode;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.builder.connection.XmlXPathLoopDescriptor;
import org.talend.core.model.metadata.designerproperties.PropertyConstants.CDCTypeMode;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.cwm.helper.ConnectionHelper;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class RepositoryToComponentProperty {

    public static Object getValue(Connection connection, String value, IMetadataTable table) {
        if (connection instanceof HL7Connection) {
            return getHL7Value((HL7Connection) connection, value);
        }

        if (connection instanceof FileConnection) {
            return getFileValue((FileConnection) connection, value);
        }
        if (connection instanceof XmlFileConnection) {
            return getXmlFileValue((XmlFileConnection) connection, value);
        }
        if (connection instanceof DatabaseConnection) {
            return getDatabaseValue((DatabaseConnection) connection, value);
        }

        if (connection instanceof FTPConnection) {
            return getFTPValue((FTPConnection) connection, value);
        }

        if (connection instanceof LDAPSchemaConnection) {
            return getLDAPValue((LDAPSchemaConnection) connection, value);
        }
        if (connection instanceof WSDLSchemaConnection) {
            return getWSDLValue((WSDLSchemaConnection) connection, value);
        }
        if (connection instanceof LdifFileConnection) {
            return getLdifFileValue((LdifFileConnection) connection, value);
        }
        if (connection instanceof FileExcelConnection) {
            return getExcelFileValue((FileExcelConnection) connection, value);
        }

        if (connection instanceof MDMConnection) {
            return getMDMValue((MDMConnection) connection, value, table);
        }

        if (connection instanceof SAPConnection) {
            return getSAPValue((SAPConnection) connection, value);
        }

        if (connection instanceof SalesforceSchemaConnection) {
            return getSalesforceSchemaValue((SalesforceSchemaConnection) connection, value);
        }

        return null;
    }

    /**
     * 
     * DOC YeXiaowei Comment method "getSAPInputAndOutputValue".
     * 
     * @param conn
     * @param value2
     * @param functionName
     * @param isInput
     * @return
     */
    public static void getSAPInputAndOutputValue(SAPConnection conn, List<Map<String, Object>> value2, String functionName,
            boolean isInput) {
        if (conn == null) {
            return;
        }
        SAPFunctionUnit unit = null;
        for (int i = 0; i < conn.getFuntions().size(); i++) {
            SAPFunctionUnit tmp = (SAPFunctionUnit) conn.getFuntions().get(i);
            if (tmp.getLabel().equals(functionName)) {
                unit = tmp;
                break;
            }
        }
        if (unit == null) {
            return;
        }

        SAPFunctionParameterTable table = isInput ? unit.getTestInputParameterTable() : unit.getOutputParameterTable();
        if (table == null || table.getColumns() == null || table.getColumns().isEmpty()) {
            return;
        }
        value2.clear();// Make sure for this
        for (int i = 0; i < table.getColumns().size(); i++) {
            SAPFunctionParameterColumn column = (SAPFunctionParameterColumn) table.getColumns().get(i);
            Map<String, Object> map = new HashMap<String, Object>();
            if (isInput) {
                String talendType = getTalendTypeFromJCOType(column.getDataType());
                if (talendType.contains("String")) { //$NON-NLS-1$
                    map.put("SAP_PARAMETER_VALUE", TalendTextUtils.addQuotes(column.getValue())); //$NON-NLS-1$
                } else {
                    map.put("SAP_PARAMETER_VALUE", column.getValue()); //$NON-NLS-1$
                }

            }
            map.put("SAP_PARAMETER_TYPE", column.getParameterType().replace('.', '_')); //$NON-NLS-1$
            map.put("SAP_TABLE_NAME", TalendTextUtils.addQuotes(column.getStructureOrTableName())); //$NON-NLS-1$
            map.put("SAP_PARAMETER_NAME", TalendTextUtils.addQuotes(column.getName())); //$NON-NLS-1$
            value2.add(map);
        }
    }

    /**
     * 
     * DOC xye Comment method "getSAPValuesForFunction".
     * 
     * @param conn
     * @param functionName
     * @param paramterName
     * @return
     */
    public static String getSAPValuesForFunction(SAPConnection conn, String functionName, String paramterName) {
        SAPFunctionUnit unit = null;
        if (conn == null) {
            return null;
        }
        for (int i = 0; i < conn.getFuntions().size(); i++) {
            unit = (SAPFunctionUnit) conn.getFuntions().get(i);
            if (unit.getName().equals(functionName)) {
                break;
            }
        }
        if (unit == null) {
            return null;
        }
        if (paramterName.equals("SAP_ITERATE_OUT_TYPE")) { //$NON-NLS-1$
            if (unit.getOutputType() != null) {
                return unit.getOutputType().replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else if (paramterName.equals("SAP_ITERATE_OUT_TABLENAME")) { //$NON-NLS-1$
            return TalendTextUtils.addQuotes(unit.getOutputTableName());
        }
        return null;
    }

    /**
     * 
     * DOC YeXiaowei Comment method "getTalendTypeFromJCOType".
     * 
     * @param jcoType
     * @return
     */
    private static String getTalendTypeFromJCOType(final String jcoType) {
        if (jcoType == null) {
            return ""; //$NON-NLS-1$
        }
        return MetadataTalendType.getMappingTypeRetriever("sap_id").getAdvicedDbToTalendTypes(jcoType).get(0).getTalendType(); //$NON-NLS-1$
    }

    /**
     * 
     * DOC YeXiaowei Comment method "getSAPValue".
     * 
     * @param connection
     * @param value
     * @return
     */
    public static Object getSAPValue(SAPConnection connection, String value) {

        if ("CLIENT".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getClient())) {
                return connection.getClient();
            } else {
                return TalendTextUtils.addQuotes(connection.getClient());
            }
        } else if ("USERID".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getUsername())) {
                return connection.getUsername();
            } else {
                return TalendTextUtils.addQuotes(connection.getUsername());
            }
        } else if ("PASSWORD".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getPassword())) {
                return connection.getPassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getPassword());
            }
        } else if ("LANGUAGE".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getLanguage())) {
                return connection.getLanguage();
            } else {
                return TalendTextUtils.addQuotes(connection.getLanguage());
            }
        } else if ("HOSTNAME".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getHost())) {
                return connection.getHost();
            } else {
                return TalendTextUtils.addQuotes(connection.getHost());
            }
        } else if ("SYSTEMNUMBER".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getSystemNumber())) {
                return connection.getSystemNumber();
            } else {
                return TalendTextUtils.addQuotes(connection.getSystemNumber());
            }
        }

        return null;
    }

    /**
     * DOC gcui Comment method "getHL7Value".
     * 
     * @param connection
     * @param value
     * @return
     */
    private static Object getHL7Value(HL7Connection connection, String value) {
        if ("FILE_PATH".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getFilePath())) {
                return connection.getFilePath();
            } else {
                return TalendTextUtils.addQuotes(connection.getFilePath());
            }
        } else if ("START_MSG".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getStartChar())) {
                return connection.getStartChar();
            } else {
                return connection.getStartChar();
            }
        } else if ("END_MSG".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getEndChar())) {
                return connection.getEndChar();
            } else {
                return connection.getEndChar();
            }
        }
        return null;
    }

    /**
     * DOC YeXiaowei Comment method "getSalesforceSchemaValue".
     * 
     * @param connection
     * @param value
     * @return
     */
    private static Object getSalesforceSchemaValue(SalesforceSchemaConnection connection, String value) {
        if ("ENDPOINT".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getWebServiceUrl())) {
                return connection.getWebServiceUrl();
            } else {
                return TalendTextUtils.addQuotes(connection.getWebServiceUrl());
            }
        } else if ("USER_NAME".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getUserName())) {
                return connection.getUserName();
            } else {
                return TalendTextUtils.addQuotes(connection.getUserName());
            }
        } else if ("PASSWORD".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getPassword())) {
                return connection.getPassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getPassword());
            }
        } else if ("CUSTOM_MODULE_NAME".equals(value)) { //$NON-NLS-1$
            return TalendTextUtils.addQuotes(connection.getModuleName());
        } else if ("MODULENAME".equals(value)) { //$NON-NLS-1$
            if (connection.isUseCustomModuleName()) {
                return "CustomModule"; //$NON-NLS-1$
            } else {
                return connection.getModuleName();
            }
        } else if ("QUERY_CONDITION".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getQueryCondition())) {
                return connection.getQueryCondition();
            } else {
                return TalendTextUtils.addQuotes(connection.getQueryCondition());
            }
            // add for feature 7507
        } else if ("BATCH_SIZE".equals(value)) { //$NON-NLS-1$
            return connection.getBatchSize();
        } else if ("UES_PROXY".equals(value)) { //$NON-NLS-1$
            return connection.isUseProxy();
        } else if ("PROXY_HOST".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getProxyHost())) {
                return connection.getProxyHost();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyHost());
            }
        } else if ("PROXY_PORT".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getProxyPort())) {
                return connection.getProxyPort();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyPort());
            }

        } else if ("PROXY_USERNAME".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getProxyUsername())) {
                return connection.getProxyUsername();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyUsername());
            }

        } else if ("PROXY_PASSWORD".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getProxyPassword())) {
                return connection.getProxyPassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyPassword());
            }
        } else if ("TIMEOUT".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getTimeOut())) {
                return connection.getTimeOut();
            } else {
                return TalendTextUtils.addQuotes(connection.getTimeOut());
            }
        }
        return null;
    }

    /**
     * DOC qzhang Comment method "getWSDLValue".
     * 
     * @param connection
     * @param value
     * @return
     */
    private static Object getWSDLValue(WSDLSchemaConnection connection, String value) {
        if ("ENDPOINT".equals(value)) { //$NON-NLS-1$
            if (!connection.isIsInputModel()) {
                return connection.getWSDL();
            }
            if (isContextMode(connection, connection.getWSDL())) {
                return connection.getWSDL();
            } else {
                return TalendTextUtils.addQuotes(connection.getWSDL());
            }
        } else if ("NEED_AUTH".equals(value)) { //$NON-NLS-1$
            return new Boolean(connection.isNeedAuth());
        } else if ("AUTH_USERNAME".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getUserName())) {
                return connection.getUserName();
            } else {
                return TalendTextUtils.addQuotes(connection.getUserName());
            }
        } else if ("AUTH_PASSWORD".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getPassword())) {
                return connection.getPassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getPassword());
            }
        } else if ("UES_PROXY".equals(value)) { //$NON-NLS-1$
            return new Boolean(connection.isUseProxy());
        } else if ("PROXY_HOST".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getProxyHost())) {
                return connection.getProxyHost();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyHost());
            }
        } else if ("PROXY_PORT".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getProxyPort())) {
                return connection.getProxyPort();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyPort());
            }
        } else if ("PROXY_USERNAME".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getProxyUser())) {
                return connection.getProxyUser();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyUser());
            }
        } else if ("PROXY_PASSWORD".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getProxyPassword())) {
                return connection.getProxyPassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyPassword());
            }
        } else if ("METHOD".equals(value)) { //$NON-NLS-1$
            if (!connection.isIsInputModel()) {
                return connection.getMethodName();
            }
            if (isContextMode(connection, connection.getMethodName())) {
                return connection.getMethodName();
            } else {
                return TalendTextUtils.addQuotes(connection.getMethodName());
            }
        } else if ("TIMEOUT".equals(value)) { //$NON-NLS-1$
            Integer timeOut = new Integer(connection.getTimeOut());
            return timeOut.toString();
        } else if ("WSDLURL".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getEndpointURI())) {
                return connection.getEndpointURI();
            } else {
                return TalendTextUtils.addQuotes(connection.getEndpointURI());
            }
        } else if (value.equals("ENCODING")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getEncoding())) {
                return connection.getEncoding();
            } else {
                if (connection.getEncoding() == null) {
                    // get the default encoding
                    return TalendTextUtils.addQuotes(EMetadataEncoding.getMetadataEncoding("").getName()); //$NON-NLS-1$
                } else {
                    return TalendTextUtils.addQuotes(connection.getEncoding());
                }
            }
        } else if ("PARAMS".equals(value)) {
            return connection.getParameters();
        } else if ("SERVICE_NS".equals(value)) {
            return connection.getServerNameSpace();
        } else if ("SERVICE_NAME".equals(value)) {
            return connection.getServerName();
        } else if ("PORT_NS".equals(value)) {
            return connection.getPortNameSpace();
        } else if ("PORT_NAME".equals(value)) {
            return connection.getPortName();
        } else if ("INPUT_PARAMS".equals(value)) {
            return getOutputWSDLValue(connection.getParameterValue());
        } else if ("OUTPUT_PARAMS".equals(value)) {
            return getOutputWSDLValue(connection.getOutputParameter());
        }
        return null;
    }

    public static List<Map<String, String>> getOutputWSDLValue(EList list) {
        List<Map<String, String>> newList = new ArrayList<Map<String, String>>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> map = new HashMap<String, String>();
            WSDLParameter node = (WSDLParameter) list.get(i);
            map.put("EXPRESSION", node.getExpression());
            map.put("COLUMN", node.getColumn());
            map.put("SOURCE", node.getSource());
            map.put("ELEMENT", node.getElement());
            map.put("PARAMETERINFO", node.getParameterInfo());
            map.put("PARAPARENT", node.getParameterInfoParent());
            newList.add(map);
        }
        return newList;

    }

    /**
     * DOC hwang Comment method "getMDMValue".
     * 
     * @param connection
     * @param value
     * @param node
     * @return
     */
    private static Object getMDMValue(MDMConnection connection, String value, IMetadataTable table) {
        if ("MDMURL".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getServer()) && isContextMode(connection, connection.getPort())) {
                return "http://" + connection.getServer() + ":" + connection.getPort() + "/talend/TalendPort";//$NON-NLS-1$//$NON-NLS-1$//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                return TalendTextUtils.addQuotes("http://" + connection.getServer() + ":" + connection.getPort()//$NON-NLS-1$//$NON-NLS-1$ //$NON-NLS-2$
                        + "/talend/TalendPort");//$NON-NLS-1$
            }
        } else if ("USERNAME".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getUsername())) {
                return connection.getUsername();
            } else {
                return TalendTextUtils.addQuotes(connection.getUsername());
            }
        } else if ("PASSWORD".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getPassword())) {
                return connection.getPassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getPassword());
            }
        } else if ("UNIVERSE".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getUniverse())) {
                return connection.getUniverse();
            } else {
                return TalendTextUtils.addQuotes(connection.getUniverse());
            }
        } else if ("CONCEPT".equals(value)) { //$NON-NLS-1$
            Concept concept = getConcept(connection, table);
            String conceptName = null;
            if (concept != null) {
                conceptName = concept.getLoopExpression();
                if (conceptName != null && conceptName.startsWith("/")) {
                    conceptName = conceptName.substring(1, conceptName.length());
                }
                return TalendTextUtils.addQuotes(conceptName);
            }
        } else if ("DATACLUSTER".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getDatacluster())) {
                return connection.getDatacluster();
            } else {
                return TalendTextUtils.addQuotes(connection.getDatacluster());
            }
        } else if (value.equals("XPATH_QUERY")) { //$NON-NLS-1$
            Concept concept = getConcept(connection, table);
            if (concept != null) {
                if (isContextMode(connection, concept.getLoopExpression())) {
                    return concept.getLoopExpression();
                } else {
                    return TalendTextUtils.addQuotes(concept.getLoopExpression());
                }
            }

        } else if ("DATAMODEL".equals(value)) {
            if (isContextMode(connection, connection.getDatamodel())) {
                return connection.getDatamodel();
            } else {
                return TalendTextUtils.addQuotes(connection.getDatamodel());
            }
        } else if ("ROOT".equals(value)) {
            Concept concept = getConcept(connection, table);
            if (concept != null) {
                return getOutputXmlValue(concept.getRoot());
            }
        } else if ("LOOP".equals(value)) {
            Concept concept = getConcept(connection, table);
            if (concept != null) {
                return getOutputXmlValue(concept.getLoop());
            }
        } else if ("GROUP".equals(value)) {
            Concept concept = getConcept(connection, table);
            if (concept != null) {
                return getOutputXmlValue(concept.getGroup());
            }
        }
        return null;
    }

    private static Concept getConcept(MDMConnection connection, IMetadataTable table) {
        if (table != null) {
            for (Concept concept : (List<Concept>) connection.getSchemas()) {
                // test if sourcename is null, this is only for compatibility with first mdm repository
                // released.
                if (concept != null && (concept.getLabel() == null || concept.getLabel().equals(table.getLabel()))) {
                    return concept;
                }
            }
        }
        return null;
    }

    private static String getStandardDbTypeFromConnection(String dbType) {

        // if (dbType.equals(EDatabaseTypeName.GODBC.getDisplayName())) {
        // return ODBC; MSODBC
        // }
        // if (dbType.equals(EDatabaseTypeName.MSODBC.getDisplayName())) {
        // return ODBC; MSODBC
        // }
        // if (dbType.equals(EDatabaseTypeName.MSSQL.getDisplayName())) {
        // return SQL_SERVER; MSSQL
        // }

        // if (dbType.equals(EDatabaseTypeName.INTERBASE.getDisplayName())) {
        // return INTERBASE; Interbase
        // }

        return EDatabaseTypeName.getTypeFromDbType(dbType).getProduct();

    }

    /**
     * DOC nrousseau Comment method "getDatabaseValue".
     * 
     * @param connection
     * @param value
     * @return
     */
    private static Object getDatabaseValue(DatabaseConnection connection, String value) {
        String databaseType = connection.getDatabaseType();
        if (value.equals("TYPE")) { //$NON-NLS-1$
            String typeByProduct = getStandardDbTypeFromConnection(databaseType);
            // See bug 4565
            if (databaseType.equals(EDatabaseTypeName.ORACLEFORSID.getDisplayName())) {
                // see StatsAndLogConstants
                // This connection is Oracle_SID
                return EDatabaseTypeName.ORACLEFORSID.getXmlName();
            } else if (databaseType.equals(EDatabaseTypeName.ORACLESN.getDisplayName())) {
                // This connection is Oracle_service_name
                return EDatabaseTypeName.ORACLESN.getXmlName();
            } else if (databaseType.equals(EDatabaseTypeName.ORACLE_OCI.getDisplayName())) {
                return EDatabaseTypeName.ORACLE_OCI.getXmlName();
            } else if (databaseType.equals(EDatabaseTypeName.MSSQL.getDisplayName())) {
                return EDatabaseTypeName.MSSQL.getXMLType(); // for component
            }

            else {
                return typeByProduct;
            }
        }

        if (value.equals("SERVER_NAME")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getServerName())) {
                return connection.getServerName();
            } else {
                return TalendTextUtils.addQuotes(connection.getServerName());
            }
        }
        if (value.equals("PORT")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getPort())) {
                return connection.getPort();
            } else {
                return TalendTextUtils.addQuotes(connection.getPort());
            }
        }
        if (value.equals("FRAMEWORK_TYPE")) { //$NON-NLS-1$
            if (isContextMode(connection, databaseType)) {
                if (databaseType.equals("JavaDB Embeded")) { //$NON-NLS-1$
                    return "EMBEDED"; //$NON-NLS-1$
                }
                if (databaseType.equals("JavaDB JCCJDBC")) { //$NON-NLS-1$
                    return "JCCJDBC"; //$NON-NLS-1$
                }
                if (databaseType.equals("JavaDB DerbyClient")) { //$NON-NLS-1$
                    return "DERBYCLIENT"; //$NON-NLS-1$
                }
            } else {
                if (databaseType.equals("JavaDB Embeded")) { //$NON-NLS-1$
                    return "EMBEDED"; //$NON-NLS-1$
                }
                if (databaseType.equals("JavaDB JCCJDBC")) { //$NON-NLS-1$
                    return "JCCJDBC"; //$NON-NLS-1$
                }
                if (databaseType.equals("JavaDB DerbyClient")) { //$NON-NLS-1$
                    return "DERBYCLIENT"; //$NON-NLS-1$
                }
            }
        }
        if (value.equals("SID")) { //$NON-NLS-1$
            if (("").equals(connection.getSID()) || connection.getSID() == null) { //$NON-NLS-1$
                if (isContextMode(connection, connection.getDatasourceName())) {
                    return connection.getDatasourceName();
                } else {
                    return TalendTextUtils.addQuotes(connection.getDatasourceName());
                }
            } else {
                if (isContextMode(connection, connection.getSID())) {
                    return connection.getSID();
                } else {
                    return TalendTextUtils.addQuotes(connection.getSID());
                }
            }
        }
        if (value.equals("DATASOURCE")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getDatasourceName())) {
                return connection.getDatasourceName();
            } else {
                return TalendTextUtils.addQuotes(connection.getDatasourceName());
            }
        }
        if (value.equals("USERNAME")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getUsername())) {
                return connection.getUsername();
            } else {
                return TalendTextUtils.addQuotes(connection.getUsername());
            }
        }
        if (value.equals("PASSWORD")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getPassword())) {
                return connection.getPassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getPassword());
            }
        }
        if (value.equals("NULL_CHAR")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getNullChar())) {
                return connection.getNullChar();
            } else {
                return TalendTextUtils.addQuotes(connection.getNullChar());
            }
        }
        if (value.equals("SCHEMA")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getUiSchema())) {
                return connection.getUiSchema();
            } else {
                return TalendTextUtils.addQuotes(connection.getUiSchema());
            }
        }
        if (value.equals("FILE")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getFileFieldName())) {
                return connection.getFileFieldName();
            } else {
                return TalendTextUtils.addQuotes(connection.getFileFieldName());
            }
        }
        if (value.equals("PROPERTIES_STRING")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getAdditionalParams())) {
                return connection.getAdditionalParams();
            } else {
                return TalendTextUtils.addQuotes(connection.getAdditionalParams());
            }
        }

        if (value.equals("DB_VERSION")) { //$NON-NLS-1$
            String dbVersionString = connection.getDbVersionString();
            if (EDatabaseConnTemplate.ACCESS.getDBDisplayName().equals(databaseType)) {
                // see bug 7262
                return dbVersionString.toUpperCase();
            } else {
                String driverValue = EDatabaseVersion4Drivers.getDriversStr(databaseType, dbVersionString);
                if (isContextMode(connection, dbVersionString)) {
                    return dbVersionString;
                } else {
                    return driverValue;
                }
            }
        }

        // add new class name property
        if (value.equals("DRIVER_CLASS")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getDriverClass())) {
                return connection.getDriverClass();
            } else {
                return TalendTextUtils.addQuotes(connection.getDriverClass());
            }
        }

        if (value.equals("URL")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getURL())) {
                return connection.getURL();
            } else {
                return TalendTextUtils.addQuotes(connection.getURL());
            }
        }

        // if (value.equals("DRIVER_PATH")) {
        // if (isContextMode(connection, connection.getDriverJarPath())) {
        // return connection.getDriverJarPath();
        // } else {
        // return TalendTextUtils.addQuotes(connection.getDriverJarPath());
        // }
        // }

        if (value.equals("DRIVER_JAR")) { //$NON-NLS-1$
            List<Map<String, Object>> value2 = new ArrayList<Map<String, Object>>();
            if (isContextMode(connection, connection.getDriverJarPath())) {
                Map<String, Object> line = new HashMap<String, Object>();
                line.put("JAR_NAME", connection.getDriverJarPath());
                value2.add(line);
            } else {
                String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
                String pathSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
                String defaultPath = userDir + pathSeparator + "lib" + pathSeparator + "java"; //$NON-NLS-1$ //$NON-NLS-2$
                String jarPath = connection.getDriverJarPath();

                if (jarPath == null) {
                    return null;
                }

                try {
                    Character comma = ';';
                    String[] jars = jarPath.split(comma.toString());
                    boolean deployed = false;
                    if (jars != null) {
                        for (String jar : jars) {
                            File file = Path.fromOSString(jar).toFile();
                            if (file.exists() && file.isFile()) {
                                String fileName = file.getName();
                                Map<String, Object> line = new HashMap<String, Object>();
                                line.put("JAR_NAME", fileName);
                                value2.add(line);
                                if (!new File(defaultPath + pathSeparator + fileName).exists()) {
                                    // deploy this library
                                    try {
                                        CorePlugin.getDefault().getLibrariesService().deployLibrary(file.toURL());
                                        deployed = true;
                                    } catch (IOException e) {
                                        ExceptionHandler.process(e);
                                        return null;
                                    }
                                }
                            }
                        }
                        if (deployed) {
                            CorePlugin.getDefault().getLibrariesService().resetModulesNeeded();
                        }
                    }

                } catch (Exception e) {
                    return null;
                }
            }
            return value2;

        }
        if (value.equals("CDC_TYPE_MODE")) { //$NON-NLS-1$
            return new Boolean(CDCTypeMode.LOG_MODE.getName().equals(connection.getCdcTypeMode()));
        }
        // add this for tJavaDB embeded "DB Root Path"
        if (value.equals("DIRECTORY")) {//$NON-NLS-1$
            if (isContextMode(connection, connection.getDBRootPath())) {
                return connection.getDBRootPath();
            } else {
                return TalendTextUtils.addQuotes(connection.getDBRootPath());
            }

        }
        // add for feature 11674
        if (value.equals("RUNNING_MODE")) {//$NON-NLS-1$       
            String runningMode = "HSQLDB_IN_MEMORY";//$NON-NLS-1$   
            if (EDatabaseTypeName.HSQLDB_IN_PROGRESS.getXmlName().equals(databaseType)) {
                runningMode = "HSQLDB_INPROGRESS_PERSISTENT";//$NON-NLS-1$   
            } else if (EDatabaseTypeName.HSQLDB_SERVER.getXmlName().equals(databaseType)) {
                runningMode = "HSQLDB_SERVER";//$NON-NLS-1$   
            } else if (EDatabaseTypeName.HSQLDB_WEBSERVER.getXmlName().equals(databaseType)) {
                runningMode = "HSQLDB_WEBSERVER";//$NON-NLS-1$   
            }
            return runningMode;
        }
        if (value.equals("DBPATH")) {//$NON-NLS-1$
            if (isContextMode(connection, connection.getDBRootPath())) {
                return connection.getDBRootPath();
            } else {
                return TalendTextUtils.addQuotes(connection.getDBRootPath());
            }
        }
        if (value.equals("DBNAME")) {//$NON-NLS-1$
            if (isContextMode(connection, connection.getDatasourceName())) {
                return connection.getDatasourceName();
            } else {
                return TalendTextUtils.addQuotes(connection.getDatasourceName());
            }
        }
        return null;
    }

    private static boolean isContextMode(Connection connection, String value) {
        if (connection == null || value == null) {
            return false;
        }
        if (connection.isContextMode() && ContextParameterUtils.isContainContextParam(value)) {
            return true;
        }
        return false;
    }

    /**
     * DOC nrousseau Comment method "getFileValue".
     * 
     * @param connection
     * @param value
     * @return
     */
    private static Object getFileValue(FileConnection connection, String value) {
        if (value.equals("FILE_PATH")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getFilePath())) {
                return connection.getFilePath();
            } else {
                if (connection.getFilePath() != null) {
                    Path p = new Path(connection.getFilePath());
                    return TalendTextUtils.addQuotes(p.toPortableString());
                } else {
                    return ""; //$NON-NLS-1$
                }
            }
        }
        if (value.equals("ROW_SEPARATOR")) { //$NON-NLS-1$
            return connection.getRowSeparatorValue();
        }
        if (value.equals("FIELD_SEPARATOR")) { //$NON-NLS-1$
            return connection.getFieldSeparatorValue();
        }
        if (value.equals("HEADER")) { //$NON-NLS-1$
            if (connection.isUseHeader()) {
                return connection.getHeaderValue();
            } else {
                return "0"; //$NON-NLS-1$
            }
        }
        if (value.equals("FOOTER")) { //$NON-NLS-1$
            if (connection.isUseFooter()) {
                return connection.getFooterValue();
            } else {
                return "0"; //$NON-NLS-1$
            }
        }
        if (value.equals("LIMIT")) { //$NON-NLS-1$
            if (connection.isUseLimit()) {
                return connection.getLimitValue();
            } else {
                return ""; //$NON-NLS-1$
            }
        }
        if (value.equals("ENCODING")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getEncoding())) {
                return connection.getEncoding();
            } else {
                if (connection.getEncoding() == null) {
                    // get the default encoding
                    return TalendTextUtils.addQuotes(EMetadataEncoding.getMetadataEncoding("").getName()); //$NON-NLS-1$
                } else {
                    return TalendTextUtils.addQuotes(connection.getEncoding());
                }
            }
        }
        if (value.equals("REMOVE_EMPTY_ROW")) { //$NON-NLS-1$
            return new Boolean(connection.isRemoveEmptyRow());
        }
        if (value.equals("CSV_OPTION")) { //$NON-NLS-1$
            return new Boolean(connection.isCsvOption());
        }
        if (connection instanceof DelimitedFileConnection) {
            return getDelimitedFileValue((DelimitedFileConnection) connection, value);
        }
        if (connection instanceof PositionalFileConnection) {
            return getPositionalFileValue((PositionalFileConnection) connection, value);
        }
        if (connection instanceof RegexpFileConnection) {
            return getRegexpFileValue((RegexpFileConnection) connection, value);
        }
        if (connection instanceof LdifFileConnection) {
            return getLdifFileValue((LdifFileConnection) connection, value);
        }
        if (connection instanceof FileExcelConnection) {
            return getExcelFileValue((FileExcelConnection) connection, value);
        }
        if (connection instanceof EbcdicConnection) {
            return getEBCDICFieldValue((EbcdicConnection) connection, value);
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getEBCDICFieldValue".
     * 
     */
    private static Object getEBCDICFieldValue(EbcdicConnection connection, String value) {
        if ("XC2J_FILE".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getFilePath())) {
                return connection.getMidFile();
            } else {
                Path p = new Path(connection.getMidFile());
                return TalendTextUtils.addQuotes(p.toPortableString());
            }
        }
        if ("DATA_FILE".equals(value)) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getDataFile())) {
                return connection.getDataFile();
            } else {
                Path p = new Path(""); //$NON-NLS-1$
                if (connection.getDataFile() != null) {
                    p = new Path(connection.getDataFile());
                }
                return TalendTextUtils.addQuotes(p.toPortableString());
            }
        }
        return null;
    }

    /**
     * DOC yexiaowei Comment method "getExcelFileValue".
     * 
     * @param connection
     * @param value
     * @return
     */
    private static Object getExcelFileValue(FileExcelConnection connection, String value) {
        if (value.equals("FILE_PATH")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getFilePath())) {
                return connection.getFilePath();
            } else {
                Path p = new Path(connection.getFilePath());
                return TalendTextUtils.addQuotes(p.toPortableString());
            }
        }
        if (value.equals("SHEET_NAME")) { //$NON-NLS-1$
            return TalendTextUtils.addQuotes(connection.getSheetName());
        }

        if (value.equals("SELECT_ALL_SHEETS")) { //$NON-NLS-1$
            return connection.isSelectAllSheets();
        }

        if (value.equals("FIRST_COLUMN")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getFirstColumn())) {
                return connection.getFirstColumn();
            } else {
                if (isPerlProject()) {
                    return TalendTextUtils.addQuotes(connection.getFirstColumn());
                } else {
                    return connection.getFirstColumn();
                }
            }
        }
        if (value.equals("LAST_COLUMN")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getLastColumn())) {
                return connection.getLastColumn();
            } else {
                if (isPerlProject()) {
                    if (connection.getLastColumn() != null && !connection.getLastColumn().equals("")) { //$NON-NLS-1$
                        return TalendTextUtils.addQuotes(connection.getLastColumn());
                    }
                } else {
                    return connection.getLastColumn();
                }
            }

        }
        if (value.equals("ADVANCED_SEPARATOR")) { //$NON-NLS-1$
            return connection.isAdvancedSpearator();
        }
        if (value.equals("THOUSANDS_SEPARATOR")) { //$NON-NLS-1$
            return connection.getThousandSeparator();
        }
        if (value.equals("DECIMAL_SEPARATOR")) { //$NON-NLS-1$
            return connection.getDecimalSeparator();
        }

        if (value.equals("SHEET_LIST")) { //$NON-NLS-1$
            return getExcelSheetTableValue(connection);
        }

        return null;
    }

    private static boolean isPerlProject() {
        ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
        return (codeLanguage == ECodeLanguage.PERL);
    }

    /**
     * DOC YeXiaowei Comment method "getExcelSheetTableValue".
     * 
     * @param connection
     */
    private static List<Map<String, Object>> getExcelSheetTableValue(FileExcelConnection connection) {
        ArrayList<String> list = connection.getSheetList();
        if (list == null || list.size() <= 0) {
            return null;
        }
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        for (String s : list) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("SHEETNAME", TalendTextUtils.addQuotes(s)); //$NON-NLS-1$
            maps.add(map);
        }
        return maps;

    }

    /**
     * DOC nrousseau Comment method "getPositionalFileValue".
     * 
     * @param connection
     * @param value
     * @return
     */
    private static Object getPositionalFileValue(PositionalFileConnection connection, String value) {
        if (value.equals("PATTERN")) { //$NON-NLS-1$
            return connection.getFieldSeparatorValue();
        }
        return null;
    }

    private static Object getDelimitedFileValue(DelimitedFileConnection connection, String value) {
        if (value.equals("ESCAPE_CHAR")) { //$NON-NLS-1$
            return connection.getEscapeChar();
        }
        if (value.equals("TEXT_ENCLOSURE")) { //$NON-NLS-1$
            return connection.getTextEnclosure();
        }

        if (value.equals("SPLITRECORD")) { //$NON-NLS-1$           
            return connection.isSplitRecord();
        }
        return null;
    }

    private static Object getRegexpFileValue(RegexpFileConnection connection, String value) {
        if (value.equals("ESCAPE_CHAR")) { //$NON-NLS-1$
            return connection.getEscapeChar();
        }
        if (value.equals("TEXT_ENCLOSURE")) { //$NON-NLS-1$
            return connection.getTextEnclosure();
        }
        if (value.equals("REGEXP")) { //$NON-NLS-1$
            return connection.getFieldSeparatorValue();
        }
        return null;
    }

    // added by nma to deal with .xsd file
    public static Object getXmlAndXSDFileValue(XmlFileConnection connection, String value) {
        EList list = connection.getSchema();
        XmlXPathLoopDescriptor xmlDesc = (XmlXPathLoopDescriptor) list.get(0);
        if (value.equals("FILE_PATH")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getXmlFilePath())) {
                return connection.getXmlFilePath();
            } else {
                Path p = new Path(connection.getXmlFilePath());
                if ((p.toPortableString()).endsWith("xsd")) { //$NON-NLS-1$
                    OpenXSDFileDialog openXSDFileDialog = new OpenXSDFileDialog(PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getShell());
                    openXSDFileDialog.setTitle(Messages.getString("RepositoryToComponentProperty.xmlFileSelection")); //$NON-NLS-1$
                    openXSDFileDialog.setPath(p);
                    int dialogValue = openXSDFileDialog.open();
                    if (dialogValue == 0)
                        return TalendTextUtils.addQuotes(openXSDFileDialog.portableValue);
                }
                return TalendTextUtils.addQuotes(p.toPortableString());
            }
        }
        if (value.equals("LIMIT")) { //$NON-NLS-1$
            if ((xmlDesc == null) || (xmlDesc.getLimitBoucle() == null)) {
                return ""; //$NON-NLS-1$
            } else {
                return xmlDesc.getLimitBoucle().toString();
            }
        }
        if (value.equals("XPATH_QUERY")) { //$NON-NLS-1$
            if (xmlDesc == null) {
                return ""; //$NON-NLS-1$
            } else {
                if (isContextMode(connection, xmlDesc.getAbsoluteXPathQuery())) {
                    return xmlDesc.getAbsoluteXPathQuery();
                } else {
                    return TalendTextUtils.addQuotes(xmlDesc.getAbsoluteXPathQuery());
                }
            }
        }
        if (value.equals("ENCODING")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getEncoding())) {
                return connection.getEncoding();
            } else {
                if (connection.getEncoding() == null) {
                    // get the default encoding
                    return TalendTextUtils.addQuotes(EMetadataEncoding.getMetadataEncoding("").getName()); //$NON-NLS-1$
                } else {
                    return TalendTextUtils.addQuotes(connection.getEncoding());
                }
            }
        }
        return null;
    }

    private static Object getXmlFileValue(XmlFileConnection connection, String value) {
        boolean isInputModel = connection.isInputModel();
        EList list;
        XmlXPathLoopDescriptor xmlDesc = null;
        if (isInputModel) {
            list = connection.getSchema();
            xmlDesc = (XmlXPathLoopDescriptor) list.get(0);
        }
        if (value.equals("FILE_PATH")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getXmlFilePath())) {
                return connection.getXmlFilePath();
            } else {
                Path p = new Path(connection.getXmlFilePath());
                return TalendTextUtils.addQuotes(p.toPortableString());
            }
        }
        if (value.equals("OUT_FILE_PATH")) {
            if (connection.getOutputFilePath() == null) {
                return "";
            }
            if (isContextMode(connection, connection.getOutputFilePath())) {
                return connection.getOutputFilePath();
            } else {
                Path p = new Path(connection.getOutputFilePath());
                return TalendTextUtils.addQuotes(p.toPortableString());
            }
        }
        if (value.equals("LIMIT")) { //$NON-NLS-1$
            if ((xmlDesc == null) || (xmlDesc.getLimitBoucle() == null)) {
                return ""; //$NON-NLS-1$
            } else {
                return xmlDesc.getLimitBoucle().toString();
            }
        }
        if (value.equals("XPATH_QUERY")) { //$NON-NLS-1$
            if (xmlDesc == null) {
                return ""; //$NON-NLS-1$
            } else {
                if (isContextMode(connection, xmlDesc.getAbsoluteXPathQuery())) {
                    return xmlDesc.getAbsoluteXPathQuery();
                } else {
                    return TalendTextUtils.addQuotes(xmlDesc.getAbsoluteXPathQuery());
                }
            }
        }
        if (value.equals("ENCODING")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getEncoding())) {
                return connection.getEncoding();
            } else {
                if (connection.getEncoding() == null) {
                    // get the default encoding
                    return TalendTextUtils.addQuotes(EMetadataEncoding.getMetadataEncoding("").getName()); //$NON-NLS-1$
                } else {
                    return TalendTextUtils.addQuotes(connection.getEncoding());
                }
            }
        }
        if (value.equals("ROOT")) {
            return getOutputXmlValue(connection.getRoot());
        }
        if (value.equals("GROUP")) {
            return getOutputXmlValue(connection.getGroup());
        }
        if (value.equals("LOOP")) {
            return getOutputXmlValue(connection.getLoop());
        }

        return null;
    }

    public static List<Map<String, String>> getOutputXmlValue(EList list) {
        List<Map<String, String>> newList = new ArrayList<Map<String, String>>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> map = new HashMap<String, String>();
            XMLFileNode node = (XMLFileNode) list.get(i);
            map.put("VALUE", node.getDefaultValue());
            map.put("ORDER", String.valueOf(node.getOrder()));
            map.put("PATH", node.getXMLPath());
            map.put("ATTRIBUTE", node.getAttribute());
            map.put("COLUMN", node.getRelatedColumn());
            newList.add(map);
        }
        return newList;

    }

    /**
     * qiang.zhang Comment method "getTableXMLMappingValue".
     * 
     * @param connection
     * @param tableInfo
     * @param metaTable
     */
    public static void getTableXMLMappingValue(Connection connection, List<Map<String, Object>> tableInfo,
            IMetadataTable metaTable) {
        if (connection instanceof XmlFileConnection) {
            XmlFileConnection xmlConnection = (XmlFileConnection) connection;
            if (xmlConnection.isInputModel()) {
                EList objectList = xmlConnection.getSchema();
                XmlXPathLoopDescriptor xmlDesc = (XmlXPathLoopDescriptor) objectList.get(0);
                List<SchemaTarget> schemaTargets = xmlDesc.getSchemaTargets();
                tableInfo.clear();
                List<IMetadataColumn> listColumns = metaTable.getListColumns();
                // for (IMetadataColumn metadataColumn : listColumns) {
                // for (SchemaTarget schema : schemaTargets) {
                // // add for bug 12034
                // String label = metadataColumn.getLabel();
                // String tagName = schema.getTagName();
                // if (label.equals(tagName)
                //                            || (label.length() > 1 && label.startsWith("_") && label.substring(1).equals(tagName) && KeywordsValidator //$NON-NLS-1$
                // .isKeyword(tagName))) {
                // Map<String, Object> map = new HashMap<String, Object>();
                //                        map.put("SCHEMA_COLUMN", tagName); //$NON-NLS-1$
                //                        map.put("QUERY", TalendTextUtils.addQuotes(schema.getRelativeXPathQuery())); //$NON-NLS-1$
                // tableInfo.add(map);
                // }
                // }
                // }
                for (SchemaTarget schema : schemaTargets) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("SCHEMA_COLUMN", schema.getTagName()); //$NON-NLS-1$
                    map.put("QUERY", TalendTextUtils.addQuotes(schema.getRelativeXPathQuery())); //$NON-NLS-1$
                    tableInfo.add(map);
                }
            }
        }
        if (connection instanceof MDMConnection) {
            MDMConnection xmlConnection = (MDMConnection) connection;
            EList objectList = xmlConnection.getSchemas();
            for (Concept concept : (List<Concept>) objectList) {
                if (concept.getLabel() == null || concept.getLabel().equals(metaTable.getLabel())) {
                    List<ConceptTarget> conceptTargets = concept.getConceptTargets();
                    tableInfo.clear();
                    List<IMetadataColumn> listColumns = metaTable.getListColumns();
                    for (IMetadataColumn metadataColumn : listColumns) {
                        for (ConceptTarget schema : conceptTargets) {
                            if (metadataColumn.getLabel().equals(schema.getTargetName())) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("SCHEMA_COLUMN", schema.getTargetName()); //$NON-NLS-1$
                                map.put("QUERY", TalendTextUtils.addQuotes(schema.getRelativeLoopExpression())); //$NON-NLS-1$
                                tableInfo.add(map);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public static void getTableXMLMappingValue(Connection connection, List<Map<String, Object>> tableInfo, INode node) {
        List<IMetadataTable> metaTables = node.getMetadataList();

        if (connection instanceof XmlFileConnection || connection instanceof MDMConnection) {
            getTableXMLMappingValue(connection, tableInfo, metaTables.get(0));
        } else if (connection instanceof HL7Connection) {
            List<IMetadataTable> newMetaTables = new ArrayList<IMetadataTable>(metaTables);
            HL7Connection hl7Connection = (HL7Connection) connection;
            tableInfo.clear();
            for (MetadataTable repTable : (Set<MetadataTable>) ConnectionHelper.getTables(connection)) {
                IMetadataTable metaTable = null;
                Iterator<IMetadataTable> iterator = newMetaTables.iterator();
                while (iterator.hasNext()) {
                    IMetadataTable nodeTable = iterator.next();
                    if (repTable.getLabel() != null && repTable.getLabel().equals(nodeTable.getLabel())) {
                        metaTable = nodeTable;
                        iterator.remove();
                        break;
                    }
                }
                String xpathValue = "";
                for (MetadataColumn col : (List<MetadataColumn>) repTable.getColumns()) {
                    String original = col.getOriginalField();
                    if (original != null && !"".equals(original)) {
                        if (original.indexOf(TalendTextUtils.LBRACKET) != -1) {
                            original = original.substring(0, original.indexOf(TalendTextUtils.LBRACKET));
                        }
                        original = TalendTextUtils.addQuotes(original);
                        xpathValue += original;
                    }
                    if (repTable.getColumns().indexOf(col) < repTable.getColumns().size() - 1) {
                        xpathValue += ",";
                    }
                }
                Map<String, Object> map = new HashMap<String, Object>();
                if (metaTable != null) {
                    map.put("SCHEMA", metaTable.getTableName());

                } else {
                    IMetadataTable convert = ConvertionHelper.convert(repTable);
                    String uinqueTableName = node.getProcess().generateUniqueConnectionName(
                            MultiSchemasUtil.getConnectionBaseName((String) repTable.getLabel()));
                    convert.setTableName(uinqueTableName);
                    // IProxyRepositoryFactory factory =
                    // CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
                    // IMetadataTable newMetadata = new org.talend.core.model.metadata.MetadataTable();
                    // newMetadata.setAttachedConnector(EConnectionType.FLOW_MAIN.getName());
                    // newMetadata.setTableName(uinqueTableName);
                    node.getProcess().addUniqueConnectionName(uinqueTableName);
                    node.getMetadataList().add(convert);
                    map.put("SCHEMA", uinqueTableName);
                }
                map.put("MAPPING", xpathValue);
                tableInfo.add(map);

            }
            if (!newMetaTables.isEmpty()) {
                metaTables.removeAll(newMetaTables);
                for (IMetadataTable table : newMetaTables) {
                    node.getProcess().removeUniqueConnectionName(table.getTableName());
                }
            }
        }

    }

    public static void getTableXmlFileValue(Connection connection, String value, IElementParameter param,
            List<Map<String, Object>> tableInfo, IMetadataTable metaTable) {
        if (connection instanceof XmlFileConnection) {
            XmlFileConnection xmlConnection = (XmlFileConnection) connection;
            EList objectList = xmlConnection.getSchema();
            XmlXPathLoopDescriptor xmlDesc = (XmlXPathLoopDescriptor) objectList.get(0);
            if (value.equals("XML_MAPPING")) { //$NON-NLS-1$
                if (xmlDesc == null) {
                    return;
                } else {
                    String[] list = param.getListRepositoryItems();

                    int column = 0;
                    boolean found = false;
                    for (int k = 0; (k < list.length) && (!found); k++) {
                        if (list[k].equals("XML_QUERY")) { //$NON-NLS-1$
                            column = k;
                            found = true;
                        }
                    }
                    EList schemaList = xmlDesc.getSchemaTargets();
                    String[] names = param.getListItemsDisplayCodeName();
                    for (int k = 0; k < schemaList.size(); k++) {
                        if (tableInfo.size() > k) {
                            Map<String, Object> line = tableInfo.get(k);
                            if (metaTable != null) {
                                if (metaTable.getListColumns().size() > k) {
                                    SchemaTarget schemaTarget = (SchemaTarget) schemaList.get(k);
                                    String strValue = TalendTextUtils.addQuotes(schemaTarget.getRelativeXPathQuery());
                                    line.put(names[column], strValue);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (connection instanceof MDMConnection) {
            MDMConnection mdmConnection = (MDMConnection) connection;
            EList objectList = mdmConnection.getSchemas();
            for (Concept concept : (List<Concept>) objectList) {
                if (concept.getLabel() == null || concept.getLabel().equals(metaTable.getLabel())) {
                    if (value.equals("XML_MAPPING")) { //$NON-NLS-1$
                        if (concept == null) {
                            return;
                        } else {
                            String[] list = param.getListRepositoryItems();

                            int column = 0;
                            boolean found = false;
                            for (int k = 0; (k < list.length) && (!found); k++) {
                                if (list[k].equals("XML_QUERY")) { //$NON-NLS-1$
                                    column = k;
                                    found = true;
                                }
                            }
                            EList conceptTargetsList = concept.getConceptTargets();
                            String[] names = param.getListItemsDisplayCodeName();
                            for (int k = 0; k < conceptTargetsList.size(); k++) {
                                if (tableInfo.size() > k) {
                                    Map<String, Object> line = tableInfo.get(k);
                                    if (metaTable != null) {
                                        if (metaTable.getListColumns().size() > k) {
                                            ConceptTarget conceptTarget = (ConceptTarget) conceptTargetsList.get(k);
                                            String strValue = TalendTextUtils
                                                    .addQuotes(conceptTarget.getRelativeLoopExpression());
                                            line.put(names[column], strValue);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private static Object getLdifFileValue(LdifFileConnection connection, String value) {
        if (value.equals("FILE_PATH")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getFilePath())) {
                return connection.getFilePath();
            } else {
                Path p = new Path(connection.getFilePath());
                return TalendTextUtils.addQuotes(p.toPortableString());
            }
        }
        return null;
    }

    /**
     * Gets repository value for LDAP schema.
     * 
     * @param connection
     * @param value
     * @return
     */
    private static Object getLDAPValue(LDAPSchemaConnection connection, String value) {

        if (value.equals("HOST")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getHost())) {
                return connection.getHost();
            } else {
                return TalendTextUtils.addQuotes(connection.getHost()).replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        if (value.equals("PORT")) { //$NON-NLS-1$
            return connection.getPort();
        }

        if (value.equals("BASEDN")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getSelectedDN())) {
                return connection.getSelectedDN();
            } else {
                return TalendTextUtils.addQuotes(connection.getSelectedDN()).replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        String protocol = connection.getProtocol();// Simple or Anonymous
        if (value.equals("PROTOCOL")) { //$NON-NLS-1$
            String encryptionMethodName = connection.getEncryptionMethodName();
            if (encryptionMethodName.equals("LDAPS(SSL)")) { //$NON-NLS-1$
                return "LDAPS"; //$NON-NLS-1$
            }
        }

        boolean useAuthen = connection.isUseAuthen();
        if (value.equals("AUTHENTIFICATION")) { //$NON-NLS-1$
            return new Boolean(useAuthen);
        }

        if (useAuthen && value.equals("USER")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getBindPrincipal())) {
                return connection.getBindPrincipal();
            } else {
                return TalendTextUtils.addQuotes(connection.getBindPrincipal()).replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (useAuthen && value.equals("PASSWD")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getBindPassword())) {
                return connection.getBindPassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getBindPassword()).replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (value.equals("FILTER")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getFilter())) {
                return connection.getFilter();
            } else {
                return TalendTextUtils.addQuotes(connection.getFilter());
            }
        }

        if (value.equals("MULTI_VALUE_SEPARATOR")) { //$NON-NLS-1$
            String separator = connection.getSeparator();
            return separator == null ? TalendTextUtils.addQuotes(",") : TalendTextUtils.addQuotes(separator); //$NON-NLS-1$
        }

        if (value.equals("COLUMN_COUNT_LIMIT")) { //$NON-NLS-1$
            return connection.getCountLimit();
        }

        if (value.equals("TIME_OUT_LIMIT")) { //$NON-NLS-1$
            return connection.getTimeOutLimit();
        }

        if (value.equals("ALIASES")) { //$NON-NLS-1$
            return connection.getAliases();
        }

        if (value.equals("REFERRALS")) { //$NON-NLS-1$
            return connection.getReferrals();
        }
        return null;
    }

    /**
     * DOC qiang.zhang Comment method "getXMLMappingValue".
     * 
     * @param repositoryConnection
     * @param metadataTable
     * @return
     */
    public static List<Map<String, Object>> getXMLMappingValue(Connection connection, List<IMetadataTable> metadataTables) {
        if (metadataTables == null || metadataTables.isEmpty()) {
            return new ArrayList<Map<String, Object>>();
        }

        if (connection instanceof XmlFileConnection) {
            IMetadataTable metadataTable = metadataTables.get(0);
            XmlFileConnection xmlConnection = (XmlFileConnection) connection;
            EList objectList = xmlConnection.getSchema();
            XmlXPathLoopDescriptor xmlDesc = (XmlXPathLoopDescriptor) objectList.get(0);
            if (metadataTable != null) {
                if (xmlDesc != null) {
                    List<SchemaTarget> schemaTargets = xmlDesc.getSchemaTargets();
                    List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
                    // for (IMetadataColumn col : metadataTable.getListColumns()) {
                    // Map<String, Object> map = new HashMap<String, Object>();
                    //                        map.put("QUERY", null); //$NON-NLS-1$
                    // for (int i = 0; i < schemaTargets.size(); i++) {
                    // SchemaTarget sch = schemaTargets.get(i);
                    // if (col.getLabel().equals(sch.getTagName())) {
                    // // map.put("SCHEMA_COLUMN", sch.getTagName());
                    //                                map.put("QUERY", TalendTextUtils.addQuotes(sch.getRelativeXPathQuery())); //$NON-NLS-1$
                    // }
                    // }
                    // maps.add(map);
                    // }

                    for (int i = 0; i < schemaTargets.size(); i++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        SchemaTarget sch = schemaTargets.get(i);
                        map.put("QUERY", TalendTextUtils.addQuotes(sch.getRelativeXPathQuery()));
                        maps.add(map);
                    }
                    return maps;
                }
            }
        }
        if (connection instanceof MDMConnection) {
            IMetadataTable metadataTable = metadataTables.get(0);
            MDMConnection xmlConnection = (MDMConnection) connection;
            EList objectList = xmlConnection.getSchemas();
            if (metadataTable != null) {
                for (Concept concept : (List<Concept>) objectList) {
                    // test if sourcename is null, this is only for compatibility with first mdm repository released.
                    if (concept != null && (concept.getLabel() == null || concept.getLabel().equals(metadataTable.getLabel()))) {
                        List<ConceptTarget> conceptTargets = concept.getConceptTargets();
                        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
                        for (IMetadataColumn col : metadataTable.getListColumns()) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("QUERY", null); //$NON-NLS-1$
                            for (int i = 0; i < conceptTargets.size(); i++) {
                                ConceptTarget cpt = conceptTargets.get(i);
                                if (col.getLabel().equals(cpt.getTargetName())) {
                                    // map.put("SCHEMA_COLUMN", sch.getTagName());
                                    map.put("QUERY", TalendTextUtils.addQuotes(cpt.getRelativeLoopExpression())); //$NON-NLS-1$
                                }
                            }
                            maps.add(map);
                        }
                        return maps;
                    }
                }
            }
        }
        if (connection instanceof HL7Connection) {
            HL7Connection hl7Connection = (HL7Connection) connection;
            Set objectList = ConnectionHelper.getTables(hl7Connection);
            List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
            for (IMetadataTable tableOfNode : metadataTables) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (MetadataTable table : (Set<MetadataTable>) objectList) {
                    if (table != null && (table.getLabel() == null || table.getLabel().equals(tableOfNode.getLabel()))) {
                        String xpathValue = "";
                        for (MetadataColumn col : (List<MetadataColumn>) table.getColumns()) {
                            String original = col.getOriginalField();
                            if (original != null && !"".equals(original)) {
                                if (original.indexOf(TalendTextUtils.LBRACKET) != -1) {
                                    original = original.substring(0, original.indexOf(TalendTextUtils.LBRACKET));
                                }
                                original = TalendTextUtils.addQuotes(original);
                                xpathValue += original;
                            }
                            if (table.getColumns().indexOf(col) < table.getColumns().size() - 1) {
                                xpathValue += ",";
                            }
                        }

                        map.put("MAPPING", xpathValue);
                        map.put("SCHEMA", tableOfNode.getTableName());
                        maps.add(map);
                    }
                }

            }
            return maps;
        }
        return null;
    }

    private static Object getFTPValue(FTPConnection connection, String value) {

        if (value.equals("SERVER_NAME")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getHost())) {
                return connection.getHost();
            } else {
                return TalendTextUtils.addQuotes(connection.getHost());
            }
        }
        if (value.equals("PORT")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getPort())) {
                return connection.getPort();
            } else {
                return TalendTextUtils.addQuotes(connection.getPort());
            }
        }
        if (value.equals("USERNAME")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getUsername())) {
                return connection.getUsername();
            } else {
                return TalendTextUtils.addQuotes(connection.getUsername());
            }
        }
        if (value.equals("PASSWORD")) { //$NON-NLS-1$
            if (isContextMode(connection, connection.getPassword())) {
                return connection.getPassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getPassword());
            }
        }
        if (value.equals("KEYSTORE_FILE")) {
            if (isContextMode(connection, connection.getKeystoreFile())) {
                return connection.getKeystoreFile();
            } else {
                return TalendTextUtils.addQuotes(connection.getKeystoreFile());
            }
        }

        if (value.equals("KEYSTROE_PASS")) {
            if (isContextMode(connection, connection.getKeystorePassword())) {
                return connection.getKeystorePassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getKeystorePassword());
            }
        }
        if (value.equals("AUTH_METHOD")) {
            if (isContextMode(connection, connection.getMethod())) {
                return connection.getMethod();
            } else {
                if (connection.getMethod().equals("Public key")) {
                    return "PUBLICKEY";
                } else if (connection.getMethod().equals("Password")) {
                    return "PASSWORD";
                }
                return TalendTextUtils.addQuotes(connection.getMethod());
            }
        }
        if (value.equals("SFTP")) {
            return connection.isSFTP();
        }
        if (value.equals("FTPS")) {
            return connection.isFTPS();
        }
        if (value.equals("CONNECT_MODE")) {
            if (connection.getMode() == null) {
                return "";
            }
            return connection.getMode().toUpperCase();
        }
        if (value.equals("ENCODING")) {
            if (isContextMode(connection, connection.getCustomEncode())) {
                return connection.getCustomEncode();
            } else {
                if (connection.getCustomEncode() == null) {
                    // get the default encoding
                    return TalendTextUtils.addQuotes(EMetadataEncoding.getMetadataEncoding("").getName()); //$NON-NLS-1$
                } else
                    return TalendTextUtils.addQuotes(connection.getCustomEncode());
            }
        }
        if (value.equals("USE_PROXY")) {
            return connection.isUsesocks();
        }
        if (value.equals("PROXY_HOST")) {
            if (isContextMode(connection, connection.getProxyhost())) {
                return connection.getProxyhost();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyhost());
            }
        }
        if (value.equals("PROXY_PORT")) {
            if (isContextMode(connection, connection.getProxyport())) {
                return connection.getProxyport();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyport());
            }
        }
        if (value.equals("PROXY_USERNAME")) {
            if (isContextMode(connection, connection.getProxyuser())) {
                return connection.getProxyuser();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxyuser());
            }
        }
        if (value.equals("PROXY_PASSWORD")) {
            if (isContextMode(connection, connection.getProxypassword())) {
                return connection.getProxypassword();
            } else {
                return TalendTextUtils.addQuotes(connection.getProxypassword());
            }
        }
        return null;
    }
}
