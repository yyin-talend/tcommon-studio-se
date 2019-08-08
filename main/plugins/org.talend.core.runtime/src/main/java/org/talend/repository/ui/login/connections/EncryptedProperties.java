// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.login.connections;

import java.util.Properties;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.daikon.crypto.EncodingUtils;
import org.talend.daikon.crypto.KeySources;
import org.talend.daikon.security.CryptoHelper;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class EncryptedProperties extends Properties {

    private CryptoHelper crypto;

    public EncryptedProperties() {
        String key = System.getProperty("properties.encryption.key");
        if (key == null) {
            try {
                byte[] byteKey = KeySources.file("properties.encryption.key").getKey();
                key = new String(byteKey, EncodingUtils.ENCODING);
            } catch (Exception ex) {
                ExceptionHandler.process(ex);
            }
        }
        crypto = new CryptoHelper(key);
    }

    public String getProperty(String key) {
        try {
            return crypto.decrypt(super.getProperty(key));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't decrypt property");
        }
    }

    public synchronized Object setProperty(String key, String value) {
        try {
            return super.setProperty(key, crypto.encrypt(value));
        } catch (Exception e) {
            throw new RuntimeException("Couldn't encrypt property");
        }
    }

}
