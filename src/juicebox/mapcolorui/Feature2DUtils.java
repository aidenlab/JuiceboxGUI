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

import javastraw.feature2D.Feature2D;
import juicebox.HiCGlobals;
import juicebox.assembly.AssemblyHeatmapHandler;
import juicebox.assembly.Scaffold;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Feature2DUtils {
    private static final String[] categories = new String[]{"observed", "coordinate", "enriched", "expected", "fdr"};

    public static String generate(Feature2D f) {

        NumberFormat formatter = NumberFormat.getInstance();

        String scaledStart1 = formatter.format(f.getStart1() * HiCGlobals.hicMapScale + 1);
        String scaledStart2 = formatter.format(f.getStart2() * HiCGlobals.hicMapScale + 1);
        String scaledEnd1 = formatter.format(f.getEnd1() * HiCGlobals.hicMapScale);
        String scaledEnd2 = formatter.format(f.getEnd2() * HiCGlobals.hicMapScale);

        if (f.getFeatureType() == Feature2D.FeatureType.SCAFFOLD) {
            Scaffold scaffold = AssemblyHeatmapHandler.getSuperAdapter().getAssemblyStateTracker()
                    .getAssemblyHandler().getScaffoldFromFeature(f);

            scaledStart1 = formatter.format(scaffold.getCurrentStart() + 1);
            scaledStart2 = formatter.format(scaffold.getCurrentStart() + 1);
            scaledEnd1 = formatter.format(scaffold.getCurrentEnd());
            scaledEnd2 = formatter.format(scaffold.getCurrentEnd());
        }

        StringBuilder txt = new StringBuilder();
        txt.append("<span style='color:red; font-family: arial; font-size: 12pt;'>");
        txt.append(f.getFeatureName());
        txt.append("</span><br>");

        txt.append("<span style='font-family: arial; font-size: 12pt;color:" + HiCGlobals.topChromosomeColor + ";'>");
        txt.append(f.getChr1()).append(":").append(scaledStart1);
        if (f.getWidth1() > 1) {
            txt.append("-").append(scaledEnd1);
        }

        txt.append("</span><br>");

        txt.append("<span style='font-family: arial; font-size: 12pt;color:" + HiCGlobals.leftChromosomeColor + ";'>");
        txt.append(f.getChr2()).append(":").append(scaledStart2);
        if (f.getWidth2() > 1) {
            txt.append("-").append(scaledEnd2);
        }
        txt.append("</span>");
        DecimalFormat df = new DecimalFormat("#.##");

        if (HiCGlobals.allowSpacingBetweenFeatureText) {
            // organize attributes into categories. +1 is for the leftover category if no keywords present
            List<List<Map.Entry<String, String>>> sortedFeatureAttributes = new ArrayList<>();
            for (int i = 0; i < categories.length + 1; i++) {
                sortedFeatureAttributes.add(new ArrayList<>());
            }

            // sorting the entries, also filtering out f1-f5 flags
            for (Map.Entry<String, String> entry : f.getAttributes().entrySet()) {
                String tmpKey = entry.getKey();
                boolean categoryHasBeenAssigned = false;
                for (int i = 0; i < categories.length; i++) {
                    if (tmpKey.contains(categories[i])) {
                        sortedFeatureAttributes.get(i).add(entry);
                        categoryHasBeenAssigned = true;
                        break;
                    }
                }
                if (!categoryHasBeenAssigned) {
                    sortedFeatureAttributes.get(categories.length).add(entry);
                }
            }

            // append to tooltip text, but now each category is spaced apart
            for (List<Map.Entry<String, String>> attributeCategory : sortedFeatureAttributes) {
                if (attributeCategory.isEmpty()) {
                    continue;
                }
                //sort attributes before printing
                Comparator<Map.Entry<String, String>> cmp = new Comparator<Map.Entry<String, String>>() {
                    @Override
                    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                        return o1.getKey().compareToIgnoreCase(o2.getKey());
                    }
                };
                attributeCategory.sort(cmp);
                for (Map.Entry<String, String> entry : attributeCategory) {
                    String tmpKey = entry.getKey();
                    txt.append("<br>");
                    txt.append("<span style='font-family: arial; font-size: 12pt;'>");
                    txt.append(tmpKey);
                    txt.append(" = <b>");
                    try {
                        txt.append(df.format(Double.valueOf(entry.getValue())));
                    } catch (Exception e) {
                        txt.append(entry.getValue()); // for text i.e. non-decimals
                    }
                    txt.append("</b>");
                    txt.append("</span>");
                }
                txt.append("<br>"); // the extra spacing between categories
            }
        } else {
            // simple text dump for plotting, no spacing or rearranging by category
            for (Map.Entry<String, String> entry : f.getAttributes().entrySet()) {
                String tmpKey = entry.getKey();
                if (!(tmpKey.equals("f1") || tmpKey.equals("f2") || tmpKey.equals("f3") || tmpKey.equals("f4") || tmpKey.equals("f5"))) {
                    txt.append("<br>");
                    txt.append("<span style='font-family: arial; font-size: 12pt;'>");
                    txt.append(tmpKey);
                    txt.append(" = <b>");
                    //System.out.println(entry.getValue());
                    try {
                        txt.append(df.format(Double.valueOf(entry.getValue())));
                    } catch (Exception e) {
                        txt.append(entry.getValue());
                    }
                    txt.append("</b>");
                    txt.append("</span>");
                }
            }
        }
        return txt.toString();
    }

    public static boolean containsPoint(Feature2D feature, Point point) {
        return containsPoint(feature, point.x, point.y);
    }

    public static boolean containsPoint(Feature2D feature, float x, float y) {
        return feature.getStart1() <= x && x <= feature.getEnd1()
                && feature.getStart2() <= y && y <= feature.getEnd2();
    }
}
