/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2022 Broad Institute, Aiden Lab, Rice University, Baylor College of Medicine
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

import javastraw.reader.block.Block;
import javastraw.reader.block.BlockModifier;
import javastraw.reader.block.ContactRecord;
import javastraw.reader.expected.ExpectedValueFunction;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.MatrixType;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;
import juicebox.JBGlobals;
import juicebox.assembly.AssemblyHeatmapHandler;
import juicebox.assembly.AssemblyModifier;
import juicebox.assembly.AssemblyScaffoldHandler;
import juicebox.assembly.Scaffold;
import juicebox.gui.SuperAdapter;
import juicebox.track.HiCFixedGridAxis;
import juicebox.track.HiCGridAxis;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.IOException;
import java.util.*;

import static javastraw.reader.mzd.BlockLoader.actuallyLoadGivenBlocks;

public class GUIMatrixZoomData extends MatrixZoomData {

    private final HiCGridAxis xGridAxis;
    private final HiCGridAxis yGridAxis;

    public GUIMatrixZoomData(MatrixZoomData mzd) {
        super(mzd);

        long correctedBinCount = mzd.getCorrectedBinCount();
        HiCZoom zoom = mzd.getZoom();
        HiCZoom.HiCUnit unit = zoom.getUnit();
        int blockColumnCount = mzd.getBlockColumnCount();

        if (unit == HiCZoom.HiCUnit.BP) {
            this.xGridAxis = new HiCFixedGridAxis(correctedBinCount * blockColumnCount, zoom.getBinSize());
            this.yGridAxis = new HiCFixedGridAxis(correctedBinCount * blockColumnCount, zoom.getBinSize());
        } else {
            System.err.println("Requested " + zoom.getUnit() + " unit; error encountered");
            this.xGridAxis = null;
            this.yGridAxis = null;
        }
    }

    public HiCGridAxis getXGridAxis() {
        return xGridAxis;
    }

    public HiCGridAxis getYGridAxis() {
        return yGridAxis;
    }

    public String getColorScaleKey(MatrixType displayOption, NormalizationType n1, NormalizationType n2) {
        return getKey() + displayOption + "_" + n1 + "_" + n2;
    }

    public String getTileKey(int tileRow, int tileColumn, MatrixType displayOption) {
        return getKey() + "_" + tileRow + "_" + tileColumn + "_ " + displayOption;
    }

    public RealMatrix extractLocalBoundedRegion(int binXStart, int binXEnd, int binYStart, int binYEnd, int matrixWidth,
                                                NormalizationType requestedNormType, boolean fillUnderDiagonal) throws IOException {
        return HiCFileTools.extractLocalBoundedRegion(this, binXStart, binXEnd, binYStart, binYEnd,
                matrixWidth, matrixWidth, requestedNormType, fillUnderDiagonal);
    }

    public List<Block> getNormalizedBlocksOverlapping(int binX1, int binY1, int binX2, int binY2, final NormalizationType no,
                                                      boolean isImportant, boolean fillUnderDiagonal) {
        if (SuperAdapter.assemblyModeCurrentlyActive) {
            return addNormalizedBlocksToListAssembly(binX1, binY1, binX2, binY2, no);
        } else {
            return super.getNormalizedBlocksOverlapping(binX1, binY1, binX2, binY2, no, isImportant, fillUnderDiagonal);
        }
    }

    protected List<Block> addNormalizedBlocksToListAssembly(int binX1, int binY1, int binX2, int binY2,
                                                            final NormalizationType no) {

        final List<Block> blockList = Collections.synchronizedList(new ArrayList<>());

        Set<Integer> blocksToLoad = new HashSet<>();

        // get aggregate scaffold handler
        AssemblyScaffoldHandler aFragHandler = AssemblyHeatmapHandler.getSuperAdapter().getAssemblyStateTracker().getAssemblyHandler();

        final int binSize = getZoom().getBinSize();

        long actualBinSize = binSize;
        if (getChr1().getIndex() == 0 && getChr2().getIndex() == 0) {
            actualBinSize = 1000 * actualBinSize;
        }

        List<Scaffold> xAxisAggregateScaffolds = aFragHandler.getIntersectingAggregateFeatures(
                (long) (actualBinSize * binX1 * JBGlobals.hicMapScale), (long) (actualBinSize * binX2 * JBGlobals.hicMapScale));
        List<Scaffold> yAxisAggregateScaffolds = aFragHandler.getIntersectingAggregateFeatures(
                (long) (actualBinSize * binY1 * JBGlobals.hicMapScale), (long) (actualBinSize * binY2 * JBGlobals.hicMapScale));

        long x1pos, x2pos, y1pos, y2pos;

        for (Scaffold xScaffold : xAxisAggregateScaffolds) {

            if (JBGlobals.phasing && xScaffold.getLength() < (actualBinSize / 2f) * JBGlobals.hicMapScale) {
                continue;
            }

            for (Scaffold yScaffold : yAxisAggregateScaffolds) {

                if (JBGlobals.phasing && yScaffold.getLength() < (actualBinSize / 2f) * JBGlobals.hicMapScale) {
                    continue;
                }

                x1pos = (long) (xScaffold.getOriginalStart() / JBGlobals.hicMapScale);
                x2pos = (long) (xScaffold.getOriginalEnd() / JBGlobals.hicMapScale);
                y1pos = (long) (yScaffold.getOriginalStart() / JBGlobals.hicMapScale);
                y2pos = (long) (yScaffold.getOriginalEnd() / JBGlobals.hicMapScale);

                // have to case long because of thumbnail, maybe fix thumbnail instead

                if (xScaffold.getCurrentStart() < actualBinSize * binX1 * JBGlobals.hicMapScale) {
                    if (!xScaffold.getInvertedVsInitial()) {
                        x1pos = (int) ((xScaffold.getOriginalStart() + actualBinSize * binX1 * JBGlobals.hicMapScale - xScaffold.getCurrentStart()) / JBGlobals.hicMapScale);
                    } else {
                        x2pos = (int) ((xScaffold.getOriginalStart() - actualBinSize * binX1 * JBGlobals.hicMapScale + xScaffold.getCurrentEnd()) / JBGlobals.hicMapScale);
                    }
                }

                if (yScaffold.getCurrentStart() < actualBinSize * binY1 * JBGlobals.hicMapScale) {
                    if (!yScaffold.getInvertedVsInitial()) {
                        y1pos = (int) ((yScaffold.getOriginalStart() + actualBinSize * binY1 * JBGlobals.hicMapScale - yScaffold.getCurrentStart()) / JBGlobals.hicMapScale);
                    } else {
                        y2pos = (int) ((yScaffold.getOriginalStart() - actualBinSize * binY1 * JBGlobals.hicMapScale + yScaffold.getCurrentEnd()) / JBGlobals.hicMapScale);
                    }
                }

                if (xScaffold.getCurrentEnd() > actualBinSize * binX2 * JBGlobals.hicMapScale) {
                    if (!xScaffold.getInvertedVsInitial()) {
                        x2pos = (int) ((xScaffold.getOriginalStart() + actualBinSize * binX2 * JBGlobals.hicMapScale - xScaffold.getCurrentStart()) / JBGlobals.hicMapScale);
                    } else {
                        x1pos = (int) ((xScaffold.getOriginalStart() - actualBinSize * binX2 * JBGlobals.hicMapScale + xScaffold.getCurrentEnd()) / JBGlobals.hicMapScale);
                    }
                }

                if (yScaffold.getCurrentEnd() > actualBinSize * binY2 * JBGlobals.hicMapScale) {
                    if (!yScaffold.getInvertedVsInitial()) {
                        y2pos = (int) ((yScaffold.getOriginalStart() + actualBinSize * binY2 * JBGlobals.hicMapScale - yScaffold.getCurrentStart()) / JBGlobals.hicMapScale);
                    } else {
                        y1pos = (int) ((yScaffold.getOriginalStart() - actualBinSize * binY2 * JBGlobals.hicMapScale + yScaffold.getCurrentEnd()) / JBGlobals.hicMapScale);
                    }
                }

                long[] genomePosition = new long[]{
                        x1pos, x2pos, y1pos, y2pos
                };

                List<Integer> tempBlockNumbers = getBlockNumbersForRegionFromGenomePosition(genomePosition);
                for (int blockNumber : tempBlockNumbers) {
                    if (!blocksToLoad.contains(blockNumber)) {
                        String key = getBlockKey(blockNumber, no);
                        Block b;
                        //temp fix for AllByAll. TODO: trace this!
                        if (JBGlobals.useCache && blockCache.containsKey(key)) {
                            b = blockCache.get(key);
                            blockList.add(b);
                        } else {
                            blocksToLoad.add(blockNumber);
                        }
                    }
                }
            }
        }

        BlockModifier modifier = new AssemblyModifier();
        actuallyLoadGivenBlocks(blockList, blocksToLoad, no, modifier, getKey(),
                chr1, chr2, zoom, blockCache, reader);
        return new ArrayList<>(new HashSet<>(blockList));
    }

    public double[] getEigenvector(ExpectedValueFunction df) {
        return getEigenvector(df, 0);
    }

    public float getObservedValue(int binX, int binY, NormalizationType normalizationType) {

        // Intra stores only lower diagonal
        if (getChr1() == getChr2()) {
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
}
