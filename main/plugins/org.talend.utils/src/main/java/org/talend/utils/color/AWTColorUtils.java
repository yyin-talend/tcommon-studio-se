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
package org.talend.utils.color;

import java.awt.Color;

/**
 * @author scorreia
 * 
 * Utility class to handle colors.
 */
public final class AWTColorUtils {
    private static final Color[] COLORS = initializeColors();

    /**
     * Method "getColor".
     * 
     * @param idx a positive index
     * @return a color among a list of predefined colors (White is not returned).
     */
    public static Color getColor(int idx) {
        assert idx >= 0;
        idx = idx % COLORS.length;
        return COLORS[idx];
    }

    
    private static Color[] initializeColors() {
        Color[] origColors = { new Color(150, 220, 25) /*Talend Color */ , Color.blue, Color.red, Color.magenta, Color.green, Color.yellow,
                Color.cyan, Color.black, Color.darkGray, Color.gray, Color.lightGray, Color.orange, Color.pink };
        Color[] enhancedColors = new Color[origColors.length * 3];
        for (int i = 0; i < origColors.length; i++) {
            enhancedColors[i] = origColors[i];
            enhancedColors[i + origColors.length] = origColors[i].brighter();
            enhancedColors[i + 2 * origColors.length] = origColors[i].darker();
        }
        return enhancedColors;
    }

    private AWTColorUtils() {
    }
}
