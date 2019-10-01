/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2019 Broad Institute, Aiden Lab
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

package juicebox.tools.clt;

import juicebox.windowui.NormalizationHandler;
import juicebox.windowui.NormalizationType;

import java.util.List;

/**
 * Command Line Parser for Juicer commands (hiccups, arrowhead, apa)
 * @author Muhammad Shamim
 */
public class CommandLineParserForJuicer extends CommandLineParser {

    // used flags
    // wmnxcrplafdptkqbvuhgjyz

    // available flags
    // oes

    // General
    private final Option matrixSizeOption = addIntegerOption('m', "matrix_window_width");
    private final Option multipleChromosomesOption = addStringOption('c', "chromosomes");
    private final Option multipleResolutionsOption = addStringOption('r', "resolutions");
    private final Option normalizationTypeOption = addStringOption('k', "normalization");
    private final Option bypassMinimumMapCountCheckOption = addBooleanOption('b', "ignore_sparsity");
    private final Option legacyOutputOption = addBooleanOption('g', "legacy");
    private final Option threadNumOption = addIntegerOption('z', "threads");

    // APA
    private final Option apaWindowOption = addIntegerOption('w', "window");
    private final Option apaMinValOption = addDoubleOption('n', "min_dist");
    private final Option apaMaxValOption = addDoubleOption('x', "max_dist");
    private final Option multipleCornerRegionDimensionsOption = addStringOption('q', "corner_width");
    private final Option includeInterChromosomalOption = addBooleanOption('e', "include_inter_chr");
    private final Option apaSaveAllData = addBooleanOption('u', "all_data");

    // HICCUPS
    private final Option fdrOption = addStringOption('f', "fdr_thresholds");
    private final Option windowOption = addStringOption('i', "window_width");
    private final Option peakOption = addStringOption('p', "peak_width");
    private final Option clusterRadiusOption = addStringOption('d', "centroid_radii");
    private final Option thresholdOption = addStringOption('t', "postprocessing_thresholds");
    private final Option cpuVersionHiCCUPSOption = addBooleanOption('j', "cpu");
    private final Option restrictSearchRegionsOption = addBooleanOption('y', "restrict");

    // previously for AFA
    private final Option relativeLocationOption = addStringOption('l', "location_type");
    private final Option multipleAttributesOption = addStringOption('a', "attributes");

    // for GRIND
    private final Option useObservedOverExpectedOption = addBooleanOption("observed-over-expected");
    private final Option useDenseLabelsOption = addBooleanOption("dense-labels");
    private final Option useWholeGenome = addBooleanOption("whole-genome");
    private final Option useLoopOption = addBooleanOption("loops");
    private final Option useDomainOption = addBooleanOption("domains");
    private final Option useStripeOption = addBooleanOption("stripes");
    private final Option useDistortionOption = addBooleanOption("distort");
    private final Option cornerOffBy = addIntegerOption("off-from-diagonal");
    private final Option stride = addIntegerOption("stride");
    private final Option useIgnoreDirectionOrientationOption = addBooleanOption("ignore-feature-orientation");
    private final Option useOnlyMakePositiveExamplesOption = addBooleanOption("only-make-positives");
    private final Option generateImageFormatPicturesOption = addStringOption("img");


    public CommandLineParserForJuicer() {
    }

    public static boolean isJuicerCommand(String cmd) {
        return cmd.equals("hiccups") || cmd.equals("apa") || cmd.equals("arrowhead") || cmd.equals("motifs")
                || cmd.equals("cluster") || cmd.equals("compare") || cmd.equals("loop_domains") ||
                cmd.equals("hiccupsdiff") || cmd.equals("ab_compdiff") || cmd.equals("genes")
                || cmd.equals("apa_vs_distance") || cmd.equals("drink") || cmd.equals("shuffle") || cmd.equals("grind");
    }

    public int getGrindDataSliceOption() {
        Object opt = getOptionValue(useLoopOption);
        if (opt != null) return 1;
        opt = getOptionValue(useDomainOption);
        if (opt != null) return 2;
        opt = getOptionValue(useStripeOption);
        if (opt != null) return 3;
        opt = getOptionValue(useDistortionOption);
        if (opt != null) return 4;
        return 0;
    }

    public boolean getBypassMinimumMapCountCheckOption() {
        return optionToBoolean(bypassMinimumMapCountCheckOption);
    }

    // for GRIND
    public boolean getUseObservedOverExpectedOption() {
        return optionToBoolean(useObservedOverExpectedOption);
    }


    public boolean getUseWholeGenome() {
        return optionToBoolean(useWholeGenome);
    }

    public boolean getDenseLabelsOption() {
        return optionToBoolean(useDenseLabelsOption);
    }

    public boolean getUseIgnoreDirectionOrientationOption() {
        return optionToBoolean(useIgnoreDirectionOrientationOption);
    }

    public boolean getUseOnlyMakePositiveExamplesOption() {
        return optionToBoolean(useOnlyMakePositiveExamplesOption);
    }

    public boolean getLegacyOutputOption() {
        return optionToBoolean(legacyOutputOption);
    }

    public boolean getIncludeInterChromosomal() {
        return optionToBoolean(includeInterChromosomalOption);
    }


    public boolean getAPASaveAllData() {
        return optionToBoolean(apaSaveAllData);
    }

    /**
     * String flags
     */

    public String getRelativeLocationOption() {
        return optionToString(relativeLocationOption);
    }

    public NormalizationType getNormalizationTypeOption(NormalizationHandler normalizationHandler) {
        return retrieveNormalization(optionToString(normalizationTypeOption), normalizationHandler);
    }

    public NormalizationType[] getBothNormalizationTypeOption(NormalizationHandler normHandler1,
                                                              NormalizationHandler normHandler2) {
        NormalizationType[] normalizationTypes = new NormalizationType[2];
        String normStrings = optionToString(normalizationTypeOption);
        if (normStrings != null) {
            String[] bothNorms = normStrings.split(",");
            if (bothNorms.length > 2 || bothNorms.length < 1) {
                System.err.println("Invalid norm syntax: " + normStrings);
                return null;
            } else if (bothNorms.length == 2) {
                normalizationTypes[0] = retrieveNormalization(bothNorms[0], normHandler1);
                normalizationTypes[1] = retrieveNormalization(bothNorms[1], normHandler2);
            } else if (bothNorms.length == 1) {
                normalizationTypes[0] = retrieveNormalization(bothNorms[0], normHandler1);
                normalizationTypes[1] = retrieveNormalization(bothNorms[0], normHandler2);
            }
            return normalizationTypes;
        }
        return null;
    }

    private NormalizationType retrieveNormalization(String norm, NormalizationHandler normalizationHandler) {
        if (norm == null || norm.length() < 1)
            return null;

        try {
            return normalizationHandler.getNormTypeFromString(norm);
        } catch (IllegalArgumentException error) {
            System.err.println("Normalization must be one of \"NONE\", \"VC\", \"VC_SQRT\", \"KR\", \"GW_KR\", \"GW_VC\", \"INTER_KR\", or \"INTER_VC\".");
            System.exit(7);
        }
        return null;
    }

    /**
     * int flags
     */
    public int getAPAWindowSizeOption() {
        return optionToInt(apaWindowOption);
    }

    public int getCornerOffBy() {
        return optionToInt(cornerOffBy);
    }

    public int getStride() {
        return optionToInt(stride);
    }

    public int getMatrixSizeOption() {
        return optionToInt(matrixSizeOption);
    }

    public int getNumThreads() {
        return optionToInt(threadNumOption);
    }

    /**
     * double flags
     */

    public double getAPAMinVal() {
        return optionToDouble(apaMinValOption);
    }

    public double getAPAMaxVal() {
        return optionToDouble(apaMaxValOption);
    }

    /**
     * String Set flags
     */

    List<String> getChromosomeListOption() {
        return optionToStringList(multipleChromosomesOption);
    }

    public List<String> getMultipleResolutionOptions() {
        return optionToStringList(multipleResolutionsOption);
    }

    public List<String> getAPACornerRegionDimensionOptions() {
        return optionToStringList(multipleCornerRegionDimensionsOption);
    }

    public List<String> getAttributeOption() {
        return optionToStringList(multipleAttributesOption);
    }

    public List<String> getFDROptions() {
        return optionToStringList(fdrOption);
    }

    public List<String> getPeakOptions() {
        return optionToStringList(peakOption);
    }

    public List<String> getWindowOptions() {
        return optionToStringList(windowOption);
    }

    public List<String> getClusterRadiusOptions() {
        return optionToStringList(clusterRadiusOption);
    }

    public List<String> getThresholdOptions() {
        return optionToStringList(thresholdOption);
    }

    public boolean getCPUVersionOfHiCCUPSOptions() {
        Object opt = getOptionValue(cpuVersionHiCCUPSOption);
        return opt != null;
    }

    public boolean restrictSearchRegionsOptions() {
        return optionToBoolean(restrictSearchRegionsOption);
    }

    public String getGenerateImageFormatPicturesOption() {
        return optionToString(generateImageFormatPicturesOption);
    }
}