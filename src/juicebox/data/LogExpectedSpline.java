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

import javastraw.reader.basics.Chromosome;
import javastraw.reader.block.ContactRecord;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.NormalizationType;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LogExpectedSpline {

    private static final int minValsInFinalBins = 100;
    private final PolynomialSplineFunction function;
    private final float maxX;

    public LogExpectedSpline(MatrixZoomData zd, NormalizationType norm, Chromosome chrom, int res) {
        int maxBin = (int) (chrom.getLength() / res + 1);
        int[] maxDist = new int[1];
        function = fitDataToFunction(zd, norm, maxBin, maxDist);
        maxX = logp1i(Math.min(maxBin, maxDist[0]));
    }

    public static double logp1(double x) {
        return Math.log(1 + x);
    }

    public static int getDist(ContactRecord record) {
        return Math.abs(record.getBinX() - record.getBinY());
    }

    public static Iterator<ContactRecord> getIterator(MatrixZoomData zd, NormalizationType norm) {
        if (norm.getLabel().equalsIgnoreCase("none")) {
            return zd.getDirectIterator();
        } else {
            return zd.getNormalizedIterator(norm);
        }
    }

    private PolynomialSplineFunction fitDataToFunction(MatrixZoomData zd, NormalizationType norm, int maxBin, int[] maxDist) {

        List<double[]> points = getAverageInEachBin(zd, norm, maxBin, maxDist);
        double[] x = new double[points.size()];
        double[] y = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            x[i] = points.get(i)[0];
            y[i] = points.get(i)[1];
        }
        points.clear();

        SplineInterpolator interpolator = new SplineInterpolator();
        return interpolator.interpolate(x, y);
    }

    private List<double[]> getAverageInEachBin(MatrixZoomData zd, NormalizationType norm, int maxBin, int[] maxDist) {

        double[] initExpected = new double[maxBin];
        long[] countsPerBin = new long[maxBin];
        populateWithCounts(zd, norm, initExpected, countsPerBin, maxBin, maxDist);

        int maxDistToUse = Math.min(maxBin, maxDist[0]);

        List<double[]> currentPoints = collapseToSetOfPoints(initExpected, countsPerBin, maxDistToUse);
        List<double[]> finalPoints = new ArrayList<>(currentPoints.size());
        initExpected = null;
        countsPerBin = null;

        for (double[] current : currentPoints) {
            if (current[3] > 0 && current[1] > 0) {
                double[] finalPoint = new double[2];
                finalPoint[0] = current[2] / current[3];
                finalPoint[1] = current[0] / current[1];
                finalPoints.add(finalPoint);
            }
        }
        currentPoints.clear();

        return finalPoints;
    }

    private List<double[]> collapseToSetOfPoints(double[] initExpected, long[] countsPerBin, int maxBin) {
        List<double[]> points = new LinkedList<>();
        double[] latest = new double[4]; // vals, counts, distances, num_distances
        int k = 0;
        int numToGroup = 1;
        //System.out.println(Arrays.toString(initExpected));
        //System.out.println(Arrays.toString(countsPerBin));
        while (k < initExpected.length) {
            latest[0] += initExpected[k];
            latest[1] += countsPerBin[k];
            latest[2] += logp1(k);
            latest[3]++;

            if (latest[3] == numToGroup) {
                points.add(latest);
                latest = new double[4];
                if (points.size() % 10 == 0) {
                    numToGroup *= 5;
                }
            }
            k++;
        }
        if (latest[3] > 10 && latest[1] > minValsInFinalBins) {
            points.add(latest);
        }
        latest = new double[4];
        for (k = (int) (.75 * maxBin); k < maxBin; k++) {
            latest[0] += initExpected[k];
            latest[1] += countsPerBin[k];
        }
        latest[2] = logp1(maxBin) + 1;
        latest[3] = 1;
        points.add(latest);
        return points;
    }

    private void populateWithCounts(MatrixZoomData zd, NormalizationType norm, double[] initExpected,
                                    long[] countsPerBin, int maxBin, int[] maxDist) {
        Iterator<ContactRecord> records = getIterator(zd, norm);
        while (records.hasNext()) {
            ContactRecord record = records.next();
            int dist = getDist(record);
            if (dist < maxBin) {
                maxDist[0] = Math.max(maxDist[0], dist);
                initExpected[dist] += logp1(record.getCounts());
                countsPerBin[dist]++;
            }
        }
    }

    public double getExpectedFromUncompressedBin(int dist0) {
        double dist = Math.max(0, logp1(dist0));
        dist = Math.min(dist, maxX);
        return Math.expm1(function.value(dist));
    }

    public int logp1i(int x) {
        return (int) Math.log(1 + x);
    }
}
