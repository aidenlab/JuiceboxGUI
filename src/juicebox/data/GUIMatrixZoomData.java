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

package juicebox.data;

import javastraw.matrices.BasicMatrix;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.block.Block;
import javastraw.reader.block.ContactRecord;
import javastraw.reader.expected.ExpectedValueFunction;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.MatrixType;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;
import juicebox.track.HiCFixedGridAxis;
import juicebox.track.HiCGridAxis;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIMatrixZoomData {

    private final HiCGridAxis xGridAxis;
    private final HiCGridAxis yGridAxis;
    private final MatrixZoomData zd;
    private final Map<NormalizationType, BasicMatrix> duplicatePearsonsMap;

    public GUIMatrixZoomData(MatrixZoomData matrixZoomData) {
        this.zd = matrixZoomData;

        long correctedBinCount = matrixZoomData.getCorrectedBinCount();
        HiCZoom zoom = matrixZoomData.getZoom();
        HiCZoom.HiCUnit unit = zoom.getUnit();
        int blockColumnCount = zd.getBlockColumnCount();

        if (unit == HiCZoom.HiCUnit.BP) {
            this.xGridAxis = new HiCFixedGridAxis(correctedBinCount * blockColumnCount, zoom.getBinSize());
            this.yGridAxis = new HiCFixedGridAxis(correctedBinCount * blockColumnCount, zoom.getBinSize());
        } else {
            System.err.println("Requested " + zoom.getUnit() + " unit; error encountered");
            this.xGridAxis = null;
            this.yGridAxis = null;
        }

        duplicatePearsonsMap = new HashMap<>();
    }

    public HiCGridAxis getXGridAxis() {
        return xGridAxis;
    }

    public HiCGridAxis getYGridAxis() {
        return yGridAxis;
    }

    public Chromosome getChr1() {
        return zd.getChr1();
    }

    public int getChr1Idx() {
        return zd.getChr1Idx();
    }

    public Chromosome getChr2() {
        return zd.getChr2();
    }

    public int getChr2Idx() {
        return zd.getChr2Idx();
    }


    public HiCZoom getZoom() {
        return zd.getZoom();
    }

    public BasicMatrix getPearsons(ExpectedValueFunction expectedValues) {
        BasicMatrix matrix = zd.getPearsons(expectedValues);
        duplicatePearsonsMap.put(expectedValues.getNormalizationType(), matrix);
        return matrix;
    }

    public String getColorScaleKey(MatrixType displayOption, NormalizationType n1, NormalizationType n2) {
        return zd.getKey() + displayOption + "_" + n1 + "_" + n2;
    }

    public String getTileKey(int tileRow, int tileColumn, MatrixType displayOption) {
        return zd.getKey() + "_" + tileRow + "_" + tileColumn + "_ " + displayOption;
    }

    public RealMatrix extractLocalBoundedRegion(int binXStart, int binXEnd, int binYStart, int binYEnd, int matrixWidth,
                                                NormalizationType requestedNormType, boolean fillUnderDiagonal) throws IOException {
        return HiCFileTools.extractLocalBoundedRegion(zd, binXStart, binXEnd, binYStart, binYEnd,
                matrixWidth, matrixWidth, requestedNormType, fillUnderDiagonal);
    }

    public void dump(PrintWriter printWriter, NormalizationType obsNormalizationType, MatrixType matrixType,
                     long[] currentRegionWindowGenomicPositions, ExpectedValueFunction df) throws IOException {
        zd.dump(printWriter, null, obsNormalizationType, matrixType,
                true, currentRegionWindowGenomicPositions, df, false);
    }

    public int getBinSize() {
        return zd.getBinSize();
    }

    public double getAverageCount() {
        return zd.getAverageCount();
    }

    public String getNormLessBlockKey(Block b) {
        return zd.getNormLessBlockKey(b);
    }

    public List<Block> getNormalizedBlocksOverlapping(int x, int y, int maxX, int maxY, NormalizationType normType, boolean isImportant, boolean fillUnderDiagonal) {
        return zd.getNormalizedBlocksOverlapping(x, y, maxX, maxY, normType, isImportant, fillUnderDiagonal);
    }

    public double[] getEigenvector(ExpectedValueFunction df) {
        return zd.getEigenvector(df, 0);
    }

    public float getObservedValue(int binX, int binY, NormalizationType normalizationType) {

        // Intra stores only lower diagonal
        if (zd.getChr1() == zd.getChr2()) {
            if (binX > binY) {
                int tmp = binX;
                //noinspection SuspiciousNameCombination
                binX = binY;
                binY = tmp;
            }
        }

        List<Block> blocks = getNormalizedBlocksOverlapping(binX, binY, binX, binY, normalizationType, false, false);
        if (blocks == null) return 0;
        for (Block b : blocks) {
            for (ContactRecord rec : b.getContactRecords()) {
                if (rec.getBinX() == binX && rec.getBinY() == binY) {
                    return rec.getCounts();
                }
            }
        }
        // No record found for this bin
        return 0;
    }

    public float getPearsonValue(int binX, int binY, NormalizationType type) {
        BasicMatrix pearsons = duplicatePearsonsMap.get(type);
        if (pearsons != null) {
            return pearsons.getEntry(binX, binY);
        } else {
            return 0;
        }
    }

    public void clearCache() {
        duplicatePearsonsMap.clear();
    }
}
