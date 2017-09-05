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
package org.talend.utils.files;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.talend.utils.string.StringUtilities;
import org.talend.utils.sugars.ReturnCode;

/**
 * DOC stephane class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 * 
 */
public final class FileUtils {

    private FileUtils() {
    }
    
    public static synchronized void replaceInFile(String path, String oldString, String newString) throws IOException,
            URISyntaxException {
        File file = new File(path);
        File tmpFile = new File(path + ".tmp");//$NON-NLS-1$

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;

            fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);
        dis = new DataInputStream(bis);


        OutputStream tempOutputStream = new FileOutputStream(tmpFile);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(tempOutputStream, "UTF8"));

        String line;
        int len = 0;
        String newLine;
        byte[] buf2 = new byte[1024];

        while (((len = dis.read(buf2))) != -1) {
            line = new String(buf2, 0, len);
            newLine = line.replace(oldString, newString);
            newLine = new String((newLine).getBytes(), "UTF8");//$NON-NLS-1$//$NON-NLS-2$
            bufferedWriter.write(newLine);
            bufferedWriter.flush();
        }

        bufferedWriter.close();
        dis.close();

        file.delete();
        tmpFile.renameTo(file);
    }

    /**
     * Method "checkBracketsInFile" checks whether the parentheses are well balanced on each line of the given file.
     * 
     * @param path the path of the file to check
     * @return true when all lines contain well balanced parentheses.
     * @throws IOException
     * @throws URISyntaxException
     */
    public static synchronized List<ReturnCode> checkBracketsInFile(String path) throws IOException,
            URISyntaxException {
        List<ReturnCode> returncodes = new ArrayList<ReturnCode>();
        File file = new File(path);
        BufferedReader in = new BufferedReader(new FileReader(file));

        String line;
        int lineNb = 0;

        while ((line = in.readLine()) != null) {
            ReturnCode checkBlocks = StringUtilities.checkBalancedParenthesis(line, '(', ')');
            lineNb++;
            if (!checkBlocks.isOk()) {
                String errorMsg = "Line " + lineNb + ": " + checkBlocks.getMessage();
                returncodes.add(new ReturnCode(errorMsg, false));
            }
        }

        in.close();
        return returncodes;
    }

}
