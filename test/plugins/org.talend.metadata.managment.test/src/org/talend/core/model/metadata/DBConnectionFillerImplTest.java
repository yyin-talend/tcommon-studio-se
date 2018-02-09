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
package org.talend.core.model.metadata;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.support.membermodification.MemberMatcher.*;
import static org.powermock.api.support.membermodification.MemberModifier.*;

import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.talend.commons.utils.workbench.extensions.ExtensionImplementationProvider;
import org.talend.commons.utils.workbench.extensions.IExtensionPointLimiter;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.MetadataConnection;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataFromDataBase;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.metadata.builder.database.PluginConstant;
import org.talend.core.model.metadata.builder.database.TableInfoParameters;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.CatalogHelper;
import org.talend.cwm.helper.ColumnSetHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.cwm.helper.SchemaHelper;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.relational.TdTable;
import org.talend.metadata.managment.model.DBConnectionFillerImpl;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.utils.sql.ConnectionUtils;
import org.talend.utils.sql.metadata.constants.GetColumn;
import org.talend.utils.sql.metadata.constants.GetTable;
import org.talend.utils.sql.metadata.constants.MetaDataConstants;
import org.talend.utils.sql.metadata.constants.TableType;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;

import orgomg.cwm.objectmodel.core.TaggedValue;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.ColumnSet;
import orgomg.cwm.resource.relational.Schema;

/**
 * DOC qiongli class global comment. Detailled comment
 */

@PrepareForTest({ StringUtils.class, ExtractMetaDataFromDataBase.class, MetadataConnectionUtils.class, PackageHelper.class,
        ConnectionHelper.class, ProxyRepositoryFactory.class, ExtensionImplementationProvider.class, CatalogHelper.class,
        SchemaHelper.class, ConnectionUtils.class, ColumnSetHelper.class, ExtractMetaDataUtils.class, ConvertionHelper.class })
public class DBConnectionFillerImplTest {

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    private DBConnectionFillerImpl dBConnectionFillerImpl;

    private DatabaseConnection dbConnection;

    @Before
    public void createDBConnectionFillerImpl() throws Exception {
        dBConnectionFillerImpl = new DBConnectionFillerImpl();
        Driver driver = mock(Driver.class);
        DBConnectionFillerImpl.setDriver(driver);
        dBConnectionFillerImpl.setLinked(true);
        dbConnection = ConnectionFactory.eINSTANCE.createDatabaseConnection();
    }

    /**
     * DOC yyin Comment method "setUp".
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        createDBConnectionFillerImpl();
    }

    /**
     * 
     * test fill columns for odbc teradata in method "fillColumns(ColumnSet colSet, DatabaseMetaData dbJDBCMetadata,
     * List<String> columnFilter, String columnPattern)".
     * 
     * @param columnSet
     * @throws SQLException
     */
    private void testFillColumnCommon_TeraOdbc(ColumnSet columnSet) throws SQLException {

        // ColumnSet columnSet = orgomg.cwm.resource.relational.RelationalFactory.eINSTANCE.createColumnSet();
        columnSet.setName("table1");//$NON-NLS-1$
        DatabaseMetaData dbJDBCMetadata = mock(DatabaseMetaData.class);
        List<String> columnFilter = new ArrayList<String>();
        Schema schema = mock(Schema.class);
        when(schema.getName()).thenReturn("talend");//$NON-NLS-1$
        PowerMockito.mockStatic(CatalogHelper.class);
        when(CatalogHelper.getParentCatalog(columnSet)).thenReturn(null);
        PowerMockito.mockStatic(SchemaHelper.class);
        when(SchemaHelper.getParentSchema(columnSet)).thenReturn(schema);
        stub(method(MetadataConnectionUtils.class, "isSybase", DatabaseMetaData.class)).toReturn(false);//$NON-NLS-1$
        stub(method(MetadataConnectionUtils.class, "isMssql", DatabaseMetaData.class)).toReturn(false);//$NON-NLS-1$
        stub(method(MetadataConnectionUtils.class, "isOracle", DatabaseMetaData.class)).toReturn(true);//$NON-NLS-1$
        PowerMockito.mockStatic(ConnectionUtils.class);
        when(ConnectionUtils.isOdbcTeradata(dbJDBCMetadata)).thenReturn(true);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getString(GetColumn.COLUMN_NAME.name())).thenReturn("column1").thenReturn("column2");//$NON-NLS-1$ //$NON-NLS-2$
        when(rs.getString(GetColumn.TYPE_NAME.name())).thenReturn("VARCHAR");//$NON-NLS-1$
        when(rs.getInt(GetColumn.NULLABLE.name())).thenReturn(0);
        when(rs.getInt(GetColumn.DATA_TYPE.name())).thenReturn(1);

        when(rs.getString(GetColumn.REMARKS.name())).thenReturn("");//$NON-NLS-1$
        when(rs.getString(GetColumn.IS_NULLABLE.name())).thenReturn("YES");//$NON-NLS-1$
        when(dbJDBCMetadata.getColumns(anyString(), anyString(), anyString(), anyString())).thenReturn(rs);

        PowerMockito.mockStatic(ConnectionHelper.class);
        when(ConnectionHelper.getConnection(columnSet)).thenReturn(dbConnection);

        stub(method(MetadataToolHelper.class, "validateValueForDBType", String.class)).toReturn("VARCHAR");//$NON-NLS-1$ //$NON-NLS-2$
        PowerMockito.mockStatic(ColumnSetHelper.class);
        stub(method(ColumnSetHelper.class, "addColumns")).toReturn(true);//$NON-NLS-1$

        List<TdColumn> fillColumns = dBConnectionFillerImpl.fillColumns(columnSet, dbJDBCMetadata, columnFilter, null);
        assertNotNull(fillColumns);
        assertTrue(fillColumns.size() == 2);
        // for odbc teradata,should assert like this
        for (TdColumn tdColumn : fillColumns) {
            assertEquals(tdColumn.getSqlDataType().getNumericPrecision(), 0);
            assertEquals(tdColumn.getSqlDataType().getNumericPrecisionRadix(), 0);
            assertEquals(tdColumn.getLength(), 0);
            assertNull(null, tdColumn.getInitialValue().getBody());
        }

    }

    /**
     * 
     * test fill columns for method "fillColumns(ColumnSet colSet, DatabaseMetaData dbJDBCMetadata, List<String>
     * columnFilter, String columnPattern)"
     * 
     * @throws SQLException
     */
    @Test
    public void testFillColumns_1() throws SQLException {

        ColumnSet columnSet = orgomg.cwm.resource.relational.RelationalFactory.eINSTANCE.createColumnSet();
        columnSet.setName("table1");//$NON-NLS-1$
        DatabaseMetaData dbJDBCMetadata = mock(DatabaseMetaData.class);
        List<String> columnFilter = new ArrayList<String>();
        Schema schema = mock(Schema.class);
        when(schema.getName()).thenReturn("talend");//$NON-NLS-1$
        PowerMockito.mockStatic(CatalogHelper.class);
        when(CatalogHelper.getParentCatalog(columnSet)).thenReturn(null);
        PowerMockito.mockStatic(SchemaHelper.class);
        when(SchemaHelper.getParentSchema(columnSet)).thenReturn(schema);
        stub(method(MetadataConnectionUtils.class, "isSybase", DatabaseMetaData.class)).toReturn(false);//$NON-NLS-1$
        stub(method(MetadataConnectionUtils.class, "isMssql", DatabaseMetaData.class)).toReturn(false);//$NON-NLS-1$
        stub(method(MetadataConnectionUtils.class, "isOracle", DatabaseMetaData.class)).toReturn(true);//$NON-NLS-1$
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getString(GetColumn.COLUMN_NAME.name())).thenReturn("column1").thenReturn("column2");//$NON-NLS-1$ //$NON-NLS-2$
        when(rs.getString(GetColumn.TYPE_NAME.name())).thenReturn("VARCHAR");//$NON-NLS-1$
        when(rs.getInt(GetColumn.NULLABLE.name())).thenReturn(0);
        when(rs.getInt(GetColumn.DATA_TYPE.name())).thenReturn(1);

        when(rs.getString(GetColumn.REMARKS.name())).thenReturn("");//$NON-NLS-1$
        when(rs.getString(GetColumn.IS_NULLABLE.name())).thenReturn("YES");//$NON-NLS-1$
        when(dbJDBCMetadata.getColumns(anyString(), anyString(), anyString(), anyString())).thenReturn(rs);

        dbConnection.setDbmsId(null);
        PowerMockito.mockStatic(ConnectionHelper.class);
        when(ConnectionHelper.getConnection(columnSet)).thenReturn(dbConnection);

        stub(method(MetadataToolHelper.class, "validateValueForDBType", String.class)).toReturn("VARCHAR");//$NON-NLS-1$ //$NON-NLS-2$
        PowerMockito.mockStatic(ColumnSetHelper.class);
        stub(method(ColumnSetHelper.class, "addColumns")).toReturn(true);//$NON-NLS-1$

        List<TdColumn> fillColumns = dBConnectionFillerImpl.fillColumns(columnSet, dbJDBCMetadata, columnFilter, null);
        assertNotNull(fillColumns);
        assertTrue(fillColumns.size() == 2);

    }

    /**
     * test filling columns when the odbc teradata DBConnection is not null.
     * {@link org.talend.metadata.managment.model.DBConnectionFillerImpl#fillColumns(orgomg.cwm.resource.relational.ColumnSet, java.sql.DatabaseMetaData, java.util.List, java.lang.String)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testFillColumnTeraODBC1() throws Exception {
        ColumnSet columnSet = orgomg.cwm.resource.relational.RelationalFactory.eINSTANCE.createColumnSet();
        PowerMockito.mockStatic(ConnectionHelper.class);
        when(ConnectionHelper.getConnection(columnSet)).thenReturn(dbConnection);

        try {
            testFillColumnCommon_TeraOdbc(columnSet);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * test filling columns when the odbc teradata DBConnection is null.
     * {@link org.talend.metadata.managment.model.DBConnectionFillerImpl#fillColumns(orgomg.cwm.resource.relational.ColumnSet, java.sql.DatabaseMetaData, java.util.List, java.lang.String)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testFillColumnTeraODBC2() throws Exception {
        ColumnSet columnSet = orgomg.cwm.resource.relational.RelationalFactory.eINSTANCE.createColumnSet();
        PowerMockito.mockStatic(ConnectionHelper.class);
        when(ConnectionHelper.getConnection(columnSet)).thenReturn(null);
        try {
            testFillColumnCommon_TeraOdbc(columnSet);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

/**
     * Test filling catalogs for odbc teredata .
     * {@link org.talend.metadata.managment.model.DBConnectionFillerImpl#fillCatalogs(Connection, DatabaseMetaData, IMetadataConnection, List)
     * @throws SQLException
     */
    @Test
    public void testFillCatalogs_AS400() throws SQLException {
        // mock ReturnCode sql.Connection
        java.sql.Connection mockSqlConn = Mockito.mock(java.sql.Connection.class);
        Mockito.when(mockSqlConn.getCatalog()).thenReturn("tbi"); //$NON-NLS-1$
        // ~mock
        // mock ResultSet
        ResultSet mockCatalogResults = Mockito.mock(ResultSet.class);
        Mockito.when(mockCatalogResults.next()).thenReturn(true, false);
        Mockito.when(mockCatalogResults.getString(MetaDataConstants.TABLE_CAT.name())).thenReturn("tbi"); //$NON-NLS-1$
        ResultSet mockSchemaResults = Mockito.mock(ResultSet.class);
        Mockito.when(mockSchemaResults.next()).thenReturn(true, false);
        Mockito.when(mockSchemaResults.getString(MetaDataConstants.TABLE_SCHEM.name())).thenReturn("dbo"); //$NON-NLS-1$
        // ~Result
        // mock JDBC Metadata
        DatabaseMetaData dbJDBCMetadata = mock(DatabaseMetaData.class);
        Mockito.when(dbJDBCMetadata.getDatabaseProductName()).thenReturn(EDatabaseTypeName.AS400.getProduct());
        Mockito.when(dbJDBCMetadata.getDriverName()).thenReturn("com.ibm.as400.access.AS400JDBCDriver"); //$NON-NLS-1$
        Mockito.when(dbJDBCMetadata.getCatalogs()).thenReturn(mockCatalogResults);
        Mockito.when(dbJDBCMetadata.getConnection()).thenReturn(mockSqlConn);

        Mockito.when(dbJDBCMetadata.getSchemas()).thenReturn(mockSchemaResults);
        // ~JDBC Metadata
        //        stub(method(ConnectionUtils.class, "isOdbcTeradata", DatabaseMetaData.class)).toReturn(true); //$NON-NLS-1$
        // mock DatabaseConnection
        List<String> catalogFilter = new ArrayList<String>();
        DatabaseConnection dbConnection = mock(DatabaseConnection.class);
        Mockito.when(dbConnection.getSID()).thenReturn(""); //$NON-NLS-1$
        Mockito.when(dbConnection.getDatabaseType()).thenReturn(EDatabaseTypeName.AS400.getDisplayName());
        Mockito.when(dbConnection.getUiSchema()).thenReturn(""); //$NON-NLS-1$
        // ~DatabaseConnection

        // mock MetadataConnection
        IMetadataConnection metadaConnection = Mockito.mock(MetadataConnection.class);
        Mockito.when(metadaConnection.getDatabase()).thenReturn(""); //$NON-NLS-1$
        // ~MetadataConnection

        // mock ConvertionHelper
        PowerMockito.mockStatic(ConvertionHelper.class);
        Mockito.when(ConvertionHelper.convert(dbConnection)).thenReturn(metadaConnection);
        // Mockito.when(ExtractMetaDataUtils.getDatabaseMetaData(mockSqlConn, EDatabaseTypeName.IBMDB2ZOS.getXmlName(),
        // false, ""))
        // .thenCallRealMethod();
        // ~ConvertionHelper
        // mock ConnectionHelper
        PowerMockito.mockStatic(ConnectionHelper.class);
        Mockito.when(ConnectionHelper.getTables(dbConnection)).thenReturn(new HashSet<MetadataTable>());
        // Mockito.when(ExtractMetaDataUtils.getDatabaseMetaData(mockSqlConn, EDatabaseTypeName.IBMDB2ZOS.getXmlName(),
        // false, ""))
        // .thenCallRealMethod();
        // ~ConnectionHelper

        when(dbConnection.isContextMode()).thenReturn(false);
        List<Catalog> fillCatalogs = this.dBConnectionFillerImpl.fillCatalogs(dbConnection, dbJDBCMetadata, null, catalogFilter);
        assertTrue(fillCatalogs.size() == 1);
        assertTrue("tbi".equals(fillCatalogs.get(0).getName()));
        List<Schema> schemas = CatalogHelper.getSchemas(fillCatalogs.get(0));
        assertTrue(schemas.size() == 1);
        assertTrue("dbo".equals(schemas.get(0).getName()));

    }

    /**
     * Test filling catalogs for odbc teredata .
     * 
     * @throws SQLException
     */
    @Test
    public void testFillCatalogs_OdbcTeradata() throws SQLException {
        DatabaseMetaData dbJDBCMetadata = mock(DatabaseMetaData.class);
        when(dbJDBCMetadata.getDatabaseProductName()).thenReturn("teradata"); //$NON-NLS-1$
        stub(method(ConnectionUtils.class, "isOdbcTeradata", DatabaseMetaData.class)).toReturn(true); //$NON-NLS-1$
        List<String> catalogFilter = new ArrayList<String>();
        DatabaseConnection dbConnection = mock(DatabaseConnection.class);
        when(dbConnection.isContextMode()).thenReturn(false);
        List<Catalog> fillCatalogs = this.dBConnectionFillerImpl.fillCatalogs(dbConnection, dbJDBCMetadata, catalogFilter);
        assertTrue(fillCatalogs.isEmpty());

    }

    /**
     * 
     * test filling tables with parent catalog.
     * 
     * @throws SQLException
     */
    @Test
    public void testFillTables_withCatalog() throws SQLException {

        DatabaseMetaData dbmd = mock(DatabaseMetaData.class);
        Catalog packge = mock(Catalog.class);
        String[] tableType = { TableType.TABLE.toString() };
        this.initializeForFillTables(packge, dbmd, tableType, false);
        List<TdTable> result = this.dBConnectionFillerImpl.fillTables(packge, dbmd, new ArrayList<String>(), "", tableType);//$NON-NLS-1$
        assertNotNull(result);
        assertTrue(result.size() == 2);

    }

    /**
     * 
     * test filling tables with parent schema.
     * 
     * @throws SQLException
     */
    @Test
    public void testFillTables_withSchema() throws SQLException {
        String[] tableType = { TableType.TABLE.toString() };
        DatabaseMetaData dbmd = mock(DatabaseMetaData.class);
        Schema packge = mock(Schema.class);
        this.initializeForFillTables(packge, dbmd, tableType, true);
        PowerMockito.mockStatic(ExtractMetaDataUtils.class);
        ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
        Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);

        List<TdTable> result = this.dBConnectionFillerImpl.fillTables(packge, dbmd, new ArrayList<String>(), "", tableType);//$NON-NLS-1$
        assertNotNull(result);
        assertTrue(result.size() == 1);

    }

    private void initializeForFillTables(orgomg.cwm.objectmodel.core.Package pack, DatabaseMetaData dbmd, String[] tableType,
            boolean isOracle) throws SQLException {
        when(pack.getName()).thenReturn("tdqPackage");//$NON-NLS-1$
        PowerMockito.mockStatic(PackageHelper.class);
        when(PackageHelper.getParentPackage(pack)).thenReturn(pack);
        when(PackageHelper.getCatalogOrSchema(pack)).thenReturn(pack);
        Connection con = mock(Connection.class);
        PowerMockito.mockStatic(MetadataConnectionUtils.class);
        when(MetadataConnectionUtils.isAS400(pack)).thenReturn(false);
        when(MetadataConnectionUtils.isOracle(con)).thenReturn(isOracle);
        when(MetadataConnectionUtils.isSybase(dbmd)).thenReturn(false);
        when(MetadataConnectionUtils.isOracle8i(con)).thenReturn(false);
        when(MetadataConnectionUtils.isOracleJDBC(con)).thenReturn(isOracle);
        PowerMockito.mockStatic(ConnectionHelper.class);
        when(ConnectionHelper.getConnection(pack)).thenReturn(con);

        List<String> filterNames = new ArrayList<String>();
        filterNames.add("Table1");//$NON-NLS-1$
        if (isOracle) {
            java.sql.Connection sqlConn = mock(java.sql.Connection.class);
            when(dbmd.getConnection()).thenReturn(sqlConn);
            Statement stme = mock(Statement.class);
            ResultSet rsTables = mock(ResultSet.class);
            when(sqlConn.createStatement()).thenReturn(stme);
            when(stme.executeQuery(TableInfoParameters.ORACLE_10G_RECBIN_SQL)).thenReturn(rsTables);
            stub(method(ExtractMetaDataFromDataBase.class, "getTableNamesFromQuery")).toReturn(filterNames);//$NON-NLS-1$
            stub(method(ExtensionImplementationProvider.class, "getInstanceV2", IExtensionPointLimiter.class)).toReturn(//$NON-NLS-1$
                    new ArrayList<IConfigurationElement>());

        }

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getString(GetTable.TABLE_NAME.name())).thenReturn("Table1").thenReturn("Table2");//$NON-NLS-1$ //$NON-NLS-2$
        when(rs.getString(GetTable.TABLE_TYPE.name())).thenReturn("Table");//$NON-NLS-1$
        when(rs.getString(GetTable.REMARKS.name())).thenReturn("");//$NON-NLS-1$
        when(dbmd.getTables("tdqPackage", "tdqPackage", "", tableType)).thenReturn(rs);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        when(dbmd.getTables("tdqPackage", null, "", tableType)).thenReturn(rs);//$NON-NLS-1$//$NON-NLS-2$
        stub(method(StringUtils.class, "isBlank")).toReturn(false);//$NON-NLS-1$
        ProxyRepositoryFactory proxFactory = mock(ProxyRepositoryFactory.class);
        when(proxFactory.getNextId()).thenReturn("abcd1").thenReturn("abcd2");//$NON-NLS-1$//$NON-NLS-2$
        stub(method(ProxyRepositoryFactory.class, "getInstance")).toReturn(proxFactory);//$NON-NLS-1$
    }

    /**
     * 
     * test TDQ-6569, it should be empty String even
     * dbMetadata.getDatabaseProductName()/dbMetadata.getDatabaseProductName() is null .
     * 
     * @throws Exception
     */
    @Test
    public void testfillUIConnParams_DB2ZOS() throws Exception {
        java.sql.Connection sqlConnection = mock(java.sql.Connection.class);
        IMetadataConnection metadataBean = mock(IMetadataConnection.class);
        DatabaseConnection connection = mock(DatabaseConnection.class);
        EList<TaggedValue> taggedValues = new BasicEList<TaggedValue>();
        when(connection.getTaggedValue()).thenReturn(taggedValues);
        when(connection.getDatabaseType()).thenReturn("IBMDB2ZOS"); //$NON-NLS-1$
        when(connection.getDriverClass()).thenReturn(EDatabase4DriverClassName.IBMDB2ZOS.getDriverClass());
        when(connection.isContextMode()).thenReturn(Boolean.FALSE);

        TypedReturnCode<java.sql.Connection> rc = new TypedReturnCode<java.sql.Connection>();
        rc.setOk(true);
        rc.setObject(sqlConnection);
        PowerMockito.mockStatic(MetadataConnectionUtils.class);
        when(MetadataConnectionUtils.checkConnection(metadataBean)).thenReturn(rc);
        when(MetadataConnectionUtils.createConnection(metadataBean)).thenReturn(rc);
        when(MetadataConnectionUtils.createConnection(metadataBean, true)).thenReturn(rc);
        when(MetadataConnectionUtils.createConnection(metadataBean, false)).thenReturn(rc);
        when(MetadataConnectionUtils.isDerbyRelatedDb(anyString(), anyString())).thenReturn(false);
        DatabaseMetaData dbMetadata = mock(DatabaseMetaData.class);
        when(dbMetadata.getDatabaseProductName()).thenReturn(null);
        when(dbMetadata.getDatabaseProductVersion()).thenReturn(null);
        PowerMockito.mockStatic(ExtractMetaDataUtils.class);
        ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
        Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);
        when(extract.getDatabaseMetaData(sqlConnection, connection, false)).thenReturn(dbMetadata);
        when(extract.getDatabaseMetaData(null, connection, false)).thenReturn(dbMetadata);
        PowerMockito.mockStatic(ConnectionUtils.class);
        ReturnCode ret = new ReturnCode();
        ret.setOk(true);
        when(ConnectionUtils.closeConnection(sqlConnection)).thenReturn(ret);
        DBConnectionFillerImpl dBConnectionFillerImp_mock = PowerMockito.spy(dBConnectionFillerImpl);
        PowerMockito.doNothing().when(dBConnectionFillerImp_mock, "fillMetadataParams", metadataBean, connection); //$NON-NLS-1$
        dBConnectionFillerImp_mock.fillUIConnParams(metadataBean, connection);
        String producetName = TaggedValueHelper.getValueString(TaggedValueHelper.DB_PRODUCT_NAME, connection);
        String version = TaggedValueHelper.getValueString(TaggedValueHelper.DB_PRODUCT_VERSION, connection);
        assertNotNull(producetName);
        assertNotNull(version);
        assertEquals(producetName, PluginConstant.EMPTY_STRING);
        assertEquals(version, PluginConstant.EMPTY_STRING);
    }
}
