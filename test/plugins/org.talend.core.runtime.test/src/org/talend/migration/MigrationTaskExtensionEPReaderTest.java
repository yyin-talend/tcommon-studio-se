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
package org.talend.migration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * created by sgandon on 7 avr. 2014 Detailled comment
 * 
 */
public class MigrationTaskExtensionEPReaderTest {

    /**
     * Test method for {@link org.talend.migration.MigrationTaskExtensionEPReader#getMigrationTaskExtension()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetMigrationTaskExtensions() throws IOException {
        MigrationTaskExtensionEPReader migrationTaskExtensionEPReader = new MigrationTaskExtensionEPReader();
        Map<ERepositoryObjectType, List<ERepositoryObjectType>> migrationTaskExtensions = migrationTaskExtensionEPReader
                .getMigrationTaskExtensions();
        assertFalse(migrationTaskExtensions.isEmpty());
        List<ERepositoryObjectType> listOfExtensions = migrationTaskExtensions.get(ERepositoryObjectType.JOBS);
        assertNotNull(listOfExtensions);
        assertTrue("list of extensions should have one extension", listOfExtensions.size() == 2); //$NON-NLS-1$
        assertEquals(listOfExtensions.get(0), ERepositoryObjectType.getType("JOB_EXT")); //$NON-NLS-1$
        assertEquals(listOfExtensions.get(1), ERepositoryObjectType.getType("JOB_EXT2")); //$NON-NLS-1$
    }

    @Test
    public void testGetMigrationTaskExtensionsWithNoExtension() throws IOException {
        MigrationTaskExtensionEPReader migrationTaskExtensionEPReader = new MigrationTaskExtensionEPReader() {

            /*
             * (non-Javadoc)
             * 
             * @see org.talend.core.utils.RegistryReader#readRegistry()
             */
            @Override
            public void readRegistry() {// do nothing to simulate that no extension is foundd.
            }
        };
        Map<ERepositoryObjectType, List<ERepositoryObjectType>> migrationTaskExtensions = migrationTaskExtensionEPReader
                .getMigrationTaskExtensions();
        assertTrue(migrationTaskExtensions.isEmpty());
    }

    @Test
    public void testGetObjectTypeExtensionsOneType() {
        MigrationTaskExtensionEPReader migrationTaskExtensionEPReader = new MigrationTaskExtensionEPReader();
        Set<ERepositoryObjectType> objectTypeExtensions = migrationTaskExtensionEPReader.getObjectTypeExtensions(Collections
                .singleton(ERepositoryObjectType.JOBS));
        assertEquals(2, objectTypeExtensions.size());
        assertEquals("JOB_EXT2", ((ERepositoryObjectType) objectTypeExtensions.toArray()[0]).getType());
        assertEquals("JOB_EXT", ((ERepositoryObjectType) objectTypeExtensions.toArray()[1]).getType());
        objectTypeExtensions = migrationTaskExtensionEPReader.getObjectTypeExtensions(Collections
                .singleton(ERepositoryObjectType.BUSINESS_PROCESS));
        assertTrue(objectTypeExtensions.isEmpty());
    }

    @Test
    public void testGetObjectTypeExtensions2TypesType() {
        MigrationTaskExtensionEPReader migrationTaskExtensionEPReader = new MigrationTaskExtensionEPReader();
        Set<ERepositoryObjectType> objectTypeExtensions = migrationTaskExtensionEPReader.getObjectTypeExtensions(Arrays.asList(
                ERepositoryObjectType.JOBS, ERepositoryObjectType.CODE));
        assertEquals(3, objectTypeExtensions.size());
        assertEquals("JOB_EXT2", ((ERepositoryObjectType) objectTypeExtensions.toArray()[0]).getType());
        assertEquals("JOB_EXT", ((ERepositoryObjectType) objectTypeExtensions.toArray()[1]).getType());
        assertEquals("CODE_EXT", ((ERepositoryObjectType) objectTypeExtensions.toArray()[2]).getType());
        objectTypeExtensions = migrationTaskExtensionEPReader.getObjectTypeExtensions(Collections
                .singleton(ERepositoryObjectType.BUSINESS_PROCESS));
        assertTrue(objectTypeExtensions.isEmpty());
    }

}
