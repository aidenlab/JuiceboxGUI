/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2017 Broad Institute, Aiden Lab
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
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package juicebox.mapcolorui;

import juicebox.HiC;
import juicebox.HiCGlobals;
import juicebox.MainWindow;
import juicebox.assembly.AssemblyOperationExecutor;
import juicebox.gui.SuperAdapter;
import juicebox.track.feature.Feature2D;
import juicebox.track.feature.Feature2DGuiContainer;
import juicebox.windowui.HiCZoom;
import org.broad.igv.feature.Chromosome;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created by muhammadsaadshamim on 8/9/17.
 */
public class HeatmapClickListener extends MouseAdapter implements ActionListener {
    private static final int clickDelay = 500;
    public SuperAdapter superAdapter;
    public HeatmapPanel.PromptedAssemblyAction promptedAssemblyAction;
    private Timer clickTimer;
    private MouseEvent lastMouseEvent;

    public HeatmapClickListener(SuperAdapter superAdapter) {
        this(clickDelay);
        this.superAdapter = superAdapter;
    }

    public HeatmapClickListener(int delay) {
        clickTimer = new Timer(delay, this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(lastMouseEvent.getClickCount());
        clickTimer.stop();
        singleClick(lastMouseEvent, this.promptedAssemblyAction);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        promptedAssemblyAction = superAdapter.getHeatmapPanel().getPromptedAssemblyAction();
        if (superAdapter.getHiC() == null) return;
        safeMouseClicked(e);
    }

    private void unsafeMouseClickSubActionA(final MouseEvent eF) {
        HiC hic = superAdapter.getHiC();
        int[] chromosomeBoundaries = superAdapter.getHeatmapPanel().getChromosomeBoundaries();

        double binX = hic.getXContext().getBinOrigin() + (eF.getX() / hic.getScaleFactor());
        double binY = hic.getYContext().getBinOrigin() + (eF.getY() / hic.getScaleFactor());

        Chromosome xChrom = null;
        Chromosome yChrom = null;

        try {
            int xGenome = hic.getZd().getXGridAxis().getGenomicMid(binX);
            int yGenome = hic.getZd().getYGridAxis().getGenomicMid(binY);
            for (int i = 0; i < chromosomeBoundaries.length; i++) {
                if (xChrom == null && chromosomeBoundaries[i] > xGenome) {
                    xChrom = hic.getChromosomeHandler().getChromosomeFromIndex(i + 1);
                }
                if (yChrom == null && chromosomeBoundaries[i] > yGenome) {
                    yChrom = hic.getChromosomeHandler().getChromosomeFromIndex(i + 1);
                }
            }
        } catch (Exception ex) {
            // do nothing, leave chromosomes null
        }
        if (xChrom != null && yChrom != null) {
            superAdapter.unsafeSetSelectedChromosomes(xChrom, yChrom);
        }

        //Only if zoom is changed All->Chr:
        superAdapter.updateThumbnail();
    }

    private void unsafeMouseClickSubActionB(double centerBinX, double centerBinY, HiCZoom newZoom) {
        HiC hic = superAdapter.getHiC();

        try {
            final String chrXName = hic.getXContext().getChromosome().toString();
            final String chrYName = hic.getYContext().getChromosome().toString();

            final int xGenome = hic.getZd().getXGridAxis().getGenomicMid(centerBinX);
            final int yGenome = hic.getZd().getYGridAxis().getGenomicMid(centerBinY);

            hic.unsafeActuallySetZoomAndLocation(chrXName, chrYName, newZoom, xGenome, yGenome, -1, false,
                    HiC.ZoomCallType.STANDARD, true, hic.isResolutionLocked() ? 1 : 0, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void safeMouseClicked(final MouseEvent eF) {
        HiC hic = superAdapter.getHiC();

        if (!eF.isPopupTrigger() && eF.getButton() == MouseEvent.BUTTON1 && !eF.isControlDown()) {

            try {
                hic.getZd();
            } catch (Exception e) {
                return;
            }

            if (eF.getClickCount() > 0) {
                lastMouseEvent = eF;
                if (clickTimer.isRunning()) {
                    clickTimer.stop();
                    doubleClick(lastMouseEvent);
                } else {
                    clickTimer.restart();
                }
            }
        }
    }

    private void singleClick(final MouseEvent lastMouseEvent, HeatmapPanel.PromptedAssemblyAction promptedAssemblyAction) {
        HiC hic = superAdapter.getHiC();
        MainWindow mainWindow = superAdapter.getMainWindow();
        HeatmapPanel heatmapPanel = superAdapter.getHeatmapPanel();

        if (hic.isWholeGenome()) {
            Runnable runnable = new Runnable() {
                public void run() {
                    unsafeMouseClickSubActionA(lastMouseEvent);
                }
            };
            mainWindow.executeLongRunningTask(runnable, "Mouse Click Set Chr");
        } else {
            List<Feature2D> selectedFeatures = heatmapPanel.getSelectedFeatures();
            Feature2DGuiContainer currentUpstreamFeature = heatmapPanel.getCurrentUpstreamFeature();
            Feature2DGuiContainer currentDownstreamFeature = heatmapPanel.getCurrentDownstreamFeature();
            switch (promptedAssemblyAction) {
                case REGROUP:
                    AssemblyOperationExecutor.toggleGroup(superAdapter, currentUpstreamFeature.getFeature2D(), currentDownstreamFeature.getFeature2D());
                    heatmapPanel.repaint();
                    mouseMoved(lastMouseEvent);
                    break;
                case PASTE:
                    AssemblyOperationExecutor.moveSelection(superAdapter, selectedFeatures, currentUpstreamFeature.getFeature2D());
                    heatmapPanel.removeSelection(); //TODO fix this so that highlight moves with translated selection
                    heatmapPanel.repaint();
//                        mouseMoved(lastMouseEvent);
                    break;
                case INVERT:
                    AssemblyOperationExecutor.invertSelection(superAdapter, selectedFeatures);
                    heatmapPanel.removeSelection(); //TODO fix this so that highlight moves with translated selection
                    heatmapPanel.repaint();
//                        mouseMoved(lastMouseEvent);
                    break;
                case ANNOTATE:
                    Feature2D debrisFeature = generateDebrisFeature(lastMouseEvent);
                    heatmapPanel.setDebrisFeauture(debrisFeature);
                    int chr1Idx = hic.getXContext().getChromosome().getIndex();
                    int chr2Idx = hic.getYContext().getChromosome().getIndex();
                    if (debrisFeature != null) {
                        superAdapter.getEditLayer().getAnnotationLayer().getFeatureHandler().getFeatureList().checkAndRemoveFeature(chr1Idx, chr2Idx, debrisFeature);
                    }
                    superAdapter.getEditLayer().getAnnotationLayer().add(chr1Idx, chr2Idx, debrisFeature);
                    HiCGlobals.splitModeEnabled = true;
                    superAdapter.setActiveLayerHandler(superAdapter.getEditLayer());
                    restoreDefaultVariables();
                    heatmapPanel.repaint();
                    break;
                default:
                    break;
            }
        }

        if (HiCGlobals.printVerboseComments) {
            try {
                superAdapter.getAssemblyStateTracker().getAssemblyHandler().printAssembly();
            } catch (Exception e) {
                System.err.println("Unable to print assembly state");
            }
        }
    }

    private void doubleClick(MouseEvent lastMouseEvent) {
        HiC hic = superAdapter.getHiC();
        MainWindow mainWindow = superAdapter.getMainWindow();

        // Double click, zoom and center on click location
        try {
            final HiCZoom currentZoom = hic.getZd().getZoom();
            final HiCZoom nextPotentialZoom = hic.getDataset().getNextZoom(currentZoom, !lastMouseEvent.isAltDown());
            final HiCZoom newZoom = hic.isResolutionLocked() ||
                    hic.isPearsonEdgeCaseEncountered(nextPotentialZoom) ? currentZoom : nextPotentialZoom;

            // If newZoom == currentZoom adjust scale factor (no change in resolution)
            final double centerBinX = hic.getXContext().getBinOrigin() + (lastMouseEvent.getX() / hic.getScaleFactor());
            final double centerBinY = hic.getYContext().getBinOrigin() + (lastMouseEvent.getY() / hic.getScaleFactor());

            // perform superzoom / normal zoom / reverse-superzoom
            if (newZoom.equals(currentZoom)) {
                double mult = lastMouseEvent.isAltDown() ? 0.5 : 2.0;
                // if newScaleFactor > 1.0, performs superzoom
                // if newScaleFactor = 1.0, performs normal zoom
                // if newScaleFactor < 1.0, performs reverse superzoom
                double newScaleFactor = Math.max(0.0, hic.getScaleFactor() * mult);

                String chrXName = hic.getXContext().getChromosome().getName();
                String chrYName = hic.getYContext().getChromosome().getName();

                int genomeX = Math.max(0, (int) (centerBinX) * newZoom.getBinSize());
                int genomeY = Math.max(0, (int) (centerBinY) * newZoom.getBinSize());

                hic.unsafeActuallySetZoomAndLocation(chrXName, chrYName, newZoom, genomeX, genomeY,
                        newScaleFactor, true, HiC.ZoomCallType.STANDARD, true, hic.isResolutionLocked() ? 1 : 0, true);

            } else {
                Runnable runnable = new Runnable() {
                    public void run() {
                        unsafeMouseClickSubActionB(centerBinX, centerBinY, newZoom);
                    }
                };
                mainWindow.executeLongRunningTask(runnable, "Mouse Click Zoom");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Feature2D generateDebrisFeature(final MouseEvent eF) {
        HiC hic = superAdapter.getHiC();
        HeatmapPanel heatmapPanel = superAdapter.getHeatmapPanel();
        final double scaleFactor = hic.getScaleFactor();
        double binOriginX = hic.getXContext().getBinOrigin();
        double binOriginY = hic.getYContext().getBinOrigin();
        Rectangle annotateRectangle = new Rectangle(eF.getX(), (int) (eF.getX() + (binOriginX - binOriginY) * scaleFactor), heatmapPanel.RESIZE_SNAP, heatmapPanel.RESIZE_SNAP);
        superAdapter.getEditLayer().updateSelectionRegion(annotateRectangle);
        return superAdapter.getEditLayer().generateFeature(hic);
    }

    private void restoreDefaultVariables() {
        superAdapter.getHiC().setCursorPoint(null);
        superAdapter.getHeatmapPanel().setCursor(Cursor.getDefaultCursor());
        superAdapter.getHeatmapPanel().repaint();
        superAdapter.repaintTrackPanels();
    }
}