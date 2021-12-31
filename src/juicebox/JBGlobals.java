/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2021 Broad Institute, Aiden Lab, Rice University, Baylor College of Medicine
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package juicebox;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * @author Muhammad Shamim
 * @since 11/25/14
 */
public class JBGlobals {

    public static final String versionNum = "3.02.02";
    public static final String juiceboxTitle = "[Juicebox " + versionNum + "] Hi-C Map ";
    public static final Color RULER_LINE_COLOR = new Color(0, 0, 230, 100);
    public static final Color DARKULA_RULER_LINE_COLOR = new Color(200, 200, 250, 100);
    public static final String topChromosomeColor = "#0000FF";
    public static final String leftChromosomeColor = "#009900";
    public static final Color backgroundColor = new Color(204, 204, 204);
    public static final String BACKUP_FILE_STEM = "unsaved_hic_annotations_backup_";
    public static File stateFile;
    public static File xmlSavedStatesFile;
    public static final boolean allowSpacingBetweenFeatureText = true;
    public static final ArrayList<String> savedStatesList = new ArrayList<>();
    public static final int minVersion = 6;
    public static final int bufferSize = 2097152;
    public static final String defaultPropertiesURL = "http://hicfiles.tc4ga.com/juicebox.properties";
    public static final Color diffGrayColor = new Color(238, 238, 238);
    public static int MAX_PEARSON_ZOOM = 50000;
    public static int MAX_EIGENVECTOR_ZOOM = 100000;
    public static double hicMapScale = 1;
    public static boolean useCache = true;
    public static boolean guiIsCurrentlyActive = false;
    public static boolean printVerboseComments = false;
    public static boolean slideshowEnabled = false;
    public static boolean splitModeEnabled = false;
    public static boolean translationInProgress = false;
    public static boolean displayTiles = false;
    public static boolean isDarkulaModeEnabled = false;
    public static boolean phasing = false;
    public static boolean noSortInPhasing = false;
    public static boolean wasLinkedBeforeMousePress = false;
    public static final boolean isDevAssemblyToolsAllowedPublic = true;
    public static boolean HACK_COLORSCALE = false;
    public static boolean HACK_COLORSCALE_EQUAL = false;
    public static boolean HACK_COLORSCALE_LINEAR = false;

    public static Font font(int size, boolean isBold) {
        if (isBold)
            return new Font("Arial", Font.BOLD, size);
        return new Font("Arial", Font.PLAIN, size);
    }

    public enum menuType {MAP, LOCATION, STATE}
}
