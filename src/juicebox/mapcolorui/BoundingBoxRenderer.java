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

package juicebox.mapcolorui;

import juicebox.JBGlobals;
import juicebox.data.GUIMatrixZoomData;

import java.awt.*;

public class BoundingBoxRenderer {

    private final HeatmapPanel parent;
    private long[] chromosomeBoundaries;

    public BoundingBoxRenderer(HeatmapPanel heatmapPanel) {
        parent = heatmapPanel;
    }

    public void drawAllByAllGrid(Graphics2D g, GUIMatrixZoomData zd, boolean showGridLines,
                                 double binOriginX, double binOriginY, double scaleFactor) {
        if (JBGlobals.isDarkulaModeEnabled) {
            g.setColor(Color.LIGHT_GRAY);
        } else {
            g.setColor(Color.DARK_GRAY);
        }

        long maxDimension = chromosomeBoundaries[chromosomeBoundaries.length - 1];
        int maxHeight = getGridLineHeightLimit(zd, maxDimension, scaleFactor);
        int maxWidth = getGridLineWidthLimit(zd, maxDimension, scaleFactor);

        g.drawLine(0, 0, 0, maxHeight);
        g.drawLine(0, 0, maxWidth, 0);
        g.drawLine(maxWidth, 0, maxWidth, maxHeight);
        g.drawLine(0, maxHeight, maxWidth, maxHeight);

        // Draw grid lines only if option is selected
        if (showGridLines) {
            for (long bound : chromosomeBoundaries) {
                // vertical lines
                int xBin = zd.getXGridAxis().getBinNumberForGenomicPosition(bound);
                int x = (int) ((xBin - binOriginX) * scaleFactor);
                g.drawLine(x, 0, x, maxHeight);

                // horizontal lines
                int yBin = zd.getYGridAxis().getBinNumberForGenomicPosition(bound);
                int y = (int) ((yBin - binOriginY) * scaleFactor);
                g.drawLine(0, y, maxWidth, y);
            }
        }

        //Cover gray background for the empty parts of the matrix:
        if (JBGlobals.isDarkulaModeEnabled) {
            g.setColor(Color.darkGray);
        } else {
            g.setColor(Color.white);
        }

        int pHeight = parent.getHeight();
        int pWidth = parent.getWidth();
        g.fillRect(maxHeight, 0, pHeight, pWidth);
        g.fillRect(0, maxWidth, pHeight, pWidth);
        g.fillRect(maxHeight, maxWidth, pHeight, pWidth);
    }

    private int getGridLineWidthLimit(GUIMatrixZoomData zd, long maxPosition, double scaleFactor) {
        if (parent.getWidth() < 50 || scaleFactor < 1e-10) {
            return 0;
        }
        int xBin = zd.getXGridAxis().getBinNumberForGenomicPosition(maxPosition);
        return (int) (xBin * scaleFactor);
    }

    private int getGridLineHeightLimit(GUIMatrixZoomData zd, long maxPosition, double scaleFactor) {
        if (parent.getHeight() < 50 || scaleFactor < 1e-10) {
            return 0;
        }
        int yBin = zd.getYGridAxis().getBinNumberForGenomicPosition(maxPosition);
        return (int) (yBin * scaleFactor);
    }

    public void setChromosomeBoundaries(long[] chromosomeBoundaries) {
        this.chromosomeBoundaries = chromosomeBoundaries;
    }
}
