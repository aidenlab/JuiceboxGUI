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

package juicebox.windowui;

import javastraw.reader.Dataset;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class StatisticsParser {

    public static String parse(String oldStats, Dataset ds) {
        Map<String, String> statsMap = new HashMap<>();
        StringTokenizer lines = new StringTokenizer(oldStats, "\n");
        DecimalFormat decimalFormat = new DecimalFormat("0.00%");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        while (lines.hasMoreTokens()) {
            String current = lines.nextToken();
            StringTokenizer colon = new StringTokenizer(current, ":");
            if (colon.countTokens() != 2) {
                System.err.println("Incorrect form in original statistics attribute. Offending line:\n" + current);
            } else { // Appears to be correct format, convert files as appropriate
                String label = colon.nextToken();
                String value = colon.nextToken();
                statsMap.put(label, value);
            }
        }
        String newStats = "";
        int sequenced = -1;
        int unique = -1;

        newStats += "<table><tr><th colspan=2>Experiment Information</th></tr>\n" +
                "        <tr> <td> Experiment #:</td> <td>";
        String filename = ds.getPath();
        boolean mapq30 = filename.lastIndexOf("_30") > 0;
        String[] parts = filename.split("/");
        newStats += parts[parts.length - 2];
        newStats += "</td></tr>";
        // newStats += "<tr> <td> Restriction Enzyme:</td><td>";
        // newStats += ds.getRestrictionEnzyme() + "</td></tr>";
        if (statsMap.containsKey("Experiment description")) {
            String value = statsMap.get("Experiment description").trim();
            if (!value.isEmpty())
                newStats += "<tr><td>Experiment Description:</td><td>" + value + "</td></tr>";
        }
        if (ds.getSoftware() != null) {
            newStats += "<tr> <td> Software: </td><td>" + ds.getSoftware() + "</td></tr>";
        }
        if (ds.getHiCFileScalingFactor() != null) {
            newStats += "<tr> <td> File Scaling: </td><td>" + ds.getHiCFileScalingFactor() + "</td></tr>";
        }

        newStats += "<tr><th colspan=2>Alignment Information</th></tr>\n" +
                "        <tr> <td> Reference Genome:</td>";
        newStats += "<td>" + ds.getGenomeId() + "</td></tr>";
        newStats += "<tr> <td> MAPQ Threshold: </td><td>";
        if (mapq30) newStats += "30";
        else newStats += "1";
        newStats += "</td></tr>";



      /*  <table>
        <tr>
        <th colspan=2>Experiment Information</th></tr>
        <tr> <td> Experiment #:</td> <td>HIC034</td></tr>
        <tr> <td> Cell Type: </td><td>GM12878</td></tr>
        <tr> <td> Protocol: </td><td>dilution</td></tr>
        <tr> <td> Restriction Enzyme:</td><td>HindIII</td></tr>
        <tr> <td> Crosslinking: </td><td>1% FA, 10min, RT</td></tr>
        <tr> <td> Biotin Base: </td><td>bio-dCTP</td></tr>
        <tr> <td> Ligation Volume: </td><td>8ml</td></tr>
        <tr></tr>
        <tr><th colspan=2>Alignment Information</th></tr>
        <tr> <td> Reference Genome:</td><td>hg19</td></tr>
        <tr> <td> MAPQ Threshold: </td><td>1</td></tr>
        <tr></tr>
        <tr><th colspan=2>Sequencing Information</th></tr>
        <tr> <td> Instrument:  </td> <td>HiSeq 2000</td></tr>
        <tr> <td> Read 1 Length:  </td> <td>101</td></tr>
        <tr> <td> Read 2 Length:  </td> <td>101</td></tr>
        </table>
         */

        newStats += "</table><table>";
        if (statsMap.containsKey("Total") || statsMap.containsKey("Sequenced Read Pairs")) {
            newStats += "<tr><th colspan=2>Sequencing</th></tr>";
            newStats += "<tr><td>Sequenced Reads:</td>";
            String value = "";
            try {
                if (statsMap.containsKey("Total")) value = statsMap.get("Total").trim();
                else value = statsMap.get("Sequenced Read Pairs").trim();
                sequenced = numberFormat.parse(value).intValue();
            } catch (ParseException error) {
                sequenced = -1;
            }
            newStats += "<td>" + value + "</td></tr>";
            // TODO: add in Total Bases
        }
        if (statsMap.containsKey(" Regular") || statsMap.containsKey(" Normal Paired")) {
            newStats += "<tr></tr>";
            newStats += "<tr><th colspan=2>Alignment (% Sequenced Reads)</th></tr>";
            newStats += "<tr><td>Normal Paired:</td>";
            newStats += "<td>";
            if (statsMap.containsKey(" Regular")) newStats += statsMap.get(" Regular");
            else newStats += statsMap.get(" Normal Paired");
            newStats += "</td></tr>";
        }
        if (statsMap.containsKey(" Normal chimeric") || statsMap.containsKey(" Chimeric Paired")) {
            newStats += "<tr><td>Chimeric Paired:</td>";
            newStats += "<td>";
            if (statsMap.containsKey(" Normal chimeric")) newStats += statsMap.get(" Normal chimeric");
            else newStats += statsMap.get(" Chimeric Paired");
            newStats += "</td></tr>";
        }
        if (statsMap.containsKey(" Abnormal chimeric") || statsMap.containsKey(" Chimeric Ambiguous")) {
            newStats += "<tr><td>Chimeric Ambiguous:</td>";
            newStats += "<td>";
            if (statsMap.containsKey(" Abnormal chimeric")) newStats += statsMap.get(" Abnormal chimeric");
            else newStats += statsMap.get(" Chimeric Ambiguous");
            newStats += "</td></tr>";
        }
        if (statsMap.containsKey(" Unmapped")) {
            newStats += "<tr><td>Unmapped:</td>";
            newStats += "<td>" + statsMap.get(" Unmapped") + "</td></tr>";
        }
        newStats += "<tr></tr>";
        newStats += "<tr><th colspan=2>Duplication and Complexity (% Sequenced Reads)</td></tr>";
        if (statsMap.containsKey(" Total alignable reads") || statsMap.containsKey("Alignable (Normal+Chimeric Paired)")) {
            newStats += "<tr><td>Alignable (Normal+Chimeric Paired):</td>";
            newStats += "<td>";
            if (statsMap.containsKey(" Total alignable reads")) newStats += statsMap.get(" Total alignable reads");
            else newStats += statsMap.get("Alignable (Normal+Chimeric Paired)");
            newStats += "</td></tr>";
        }
        if (statsMap.containsKey("Total reads after duplication removal")) {
            newStats += "<tr><td>Unique Reads:</td>";
            String value = statsMap.get("Total reads after duplication removal");
            try {
                unique = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                unique = -1;
            }

            newStats += "<td>" + value;

            if (sequenced != -1) {
                newStats += " (" + decimalFormat.format(unique / (float) sequenced) + ")";
            }
            newStats += "</td></tr>";
        } else if (statsMap.containsKey("Unique Reads")) {
            newStats += "<tr><td>Unique Reads:</td>";
            String value = statsMap.get("Unique Reads");
            newStats += "<td>" + value + "</td></tr>";
            if (value.indexOf('(') >= 0) {
                value = value.substring(0, value.indexOf('('));
            }

            try {
                unique = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                unique = -1;
            }
        }
        if (statsMap.containsKey("Duplicate reads")) {
            newStats += "<tr><td>PCR Duplicates:</td>";
            String value = statsMap.get("Duplicate reads");
            newStats += "<td>" + value;
            int num;
            try {
                num = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) + ")";
            }
            newStats += "</td></tr>";
        } else if (statsMap.containsKey("PCR Duplicates")) {
            newStats += "<tr><td>PCR Duplicates:</td>";
            newStats += "<td>" + statsMap.get("PCR Duplicates") + "</td></tr>";
        }
        if (statsMap.containsKey("Optical duplicates")) {
            newStats += "<tr><td>Optical Duplicates:</td>";
            String value = statsMap.get("Optical duplicates");
            int num;
            try {
                num = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1 && num != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) + ")";
            }
            newStats += "</td></tr>";
        } else if (statsMap.containsKey("Optical Duplicates")) {
            newStats += "<tr><td>Optical Duplicates:</td>";
            newStats += "<td>" + statsMap.get("Optical Duplicates") + "</td></tr>";
        }
        if (statsMap.containsKey("Library complexity (new)") || statsMap.containsKey("Library Complexity Estimate")) {
            newStats += "<tr><td><b>Library Complexity Estimate:</b></td>";
            newStats += "<td><b>";
            if (statsMap.containsKey("Library complexity (new)")) newStats += statsMap.get("Library complexity (new)");
            else newStats += statsMap.get("Library Complexity Estimate");
            newStats += "</b></td></tr>";
        }
        newStats += "<tr></tr>";
        newStats += "<tr><th colspan=2>Analysis of Unique Reads (% Sequenced Reads / % Unique Reads)</td></tr>";
        if (statsMap.containsKey("Intra-fragment Reads")) {
            newStats += "<tr><td>Intra-fragment Reads:</td>";
            String value = statsMap.get("Intra-fragment Reads");
            if (value.indexOf('(') > 0) value = value.substring(0, value.indexOf('('));
            newStats += "<td>" + value;
            int num;
            try {
                num = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1 && num != -1 && unique != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) +
                        " / " + decimalFormat.format(num / (float) unique) + ")";
            }
            newStats += "</td></tr>";
        }
        if (statsMap.containsKey("Non-uniquely Aligning Reads")) {
            newStats += "<tr><td>Below MAPQ Threshold:</td>";
            String value = statsMap.get("Non-uniquely Aligning Reads");
            newStats += "<td>" + value.trim();
            int num;
            try {
                num = numberFormat.parse(value).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1 && num != -1 && unique != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) +
                        " / " + decimalFormat.format(num / (float) unique) + ")";
            }
            newStats += "</td></tr>";
        } else if (statsMap.containsKey("Below MAPQ Threshold")) {
            newStats += "<tr><td>Below MAPQ Threshold:</td>";
            newStats += "<td>" + statsMap.get("Below MAPQ Threshold") + "</td></tr>";
        }
        if (statsMap.containsKey("Total reads in current file")) {
            newStats += "<tr><td><b>Hi-C Contacts:</b></td>";
            String value = statsMap.get("Total reads in current file");
            newStats += "<td><b>" + value.trim();
            int num;
            try {
                num = numberFormat.parse(value).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1 && num != -1 && unique != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) +
                        " / " + decimalFormat.format(num / (float) unique) + ")";
            }
            newStats += "</b></td></tr>";
            // Error checking
            if (statsMap.containsKey("HiC Contacts")) {
                int num2;
                try {
                    num2 = numberFormat.parse(statsMap.get("HiC Contacts").trim()).intValue();
                } catch (ParseException error) {
                    num2 = -1;
                }
                if (num != num2) {
                    System.err.println("Check files -- \"HiC Contacts\" should be the same as \"Total reads in current file\"");
                }
            }
        } else if (statsMap.containsKey("Hi-C Contacts")) {
            newStats += "<tr><td><b>Hi-C Contacts:</b></td>";
            newStats += "<td><b>" + statsMap.get("Hi-C Contacts") + "</b></td></tr>";

        }
        if (statsMap.containsKey("Ligations") || statsMap.containsKey(" Ligation Motif Present")) {
            newStats += "<tr><td>&nbsp;&nbsp;Ligation Motif Present:</td>";
            String value = statsMap.containsKey("Ligations") ? statsMap.get("Ligations") : statsMap.get(" Ligation Motif Present");
            newStats += "<td>" + value.substring(0, value.indexOf('('));
            int num;
            try {
                num = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1 && num != -1 && unique != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) +
                        " / " + decimalFormat.format(num / (float) unique) + ")";
            }
            newStats += "</td></tr>";
        }
        if (statsMap.containsKey("Five prime") && statsMap.containsKey("Three prime")) {
            newStats += "<tr><td>&nbsp;&nbsp;3' Bias (Long Range):</td>";
            String value = statsMap.get("Five prime");
            value = value.substring(value.indexOf('(') + 1);
            value = value.substring(0, value.indexOf('%'));
            int num1 = Math.round(Float.parseFloat(value));

            value = statsMap.get("Three prime");
            value = value.substring(value.indexOf('(') + 1);
            value = value.substring(0, value.indexOf('%'));
            int num2 = Math.round(Float.parseFloat(value));

            newStats += "<td>" + num2 + "% - " + num1 + "%</td></tr>";
        } else if (statsMap.containsKey(" 3' Bias (Long Range)")) {
            newStats += "<tr><td>&nbsp;&nbsp;3' Bias (Long Range):</td>";
            newStats += "<td>" + statsMap.get(" 3' Bias (Long Range)") + "</td></tr>";
        }
        if (statsMap.containsKey("Inner") && statsMap.containsKey("Outer") &&
                statsMap.containsKey("Left") && statsMap.containsKey("Right")) {
            newStats += "<tr><td>&nbsp;&nbsp;Pair Type % (L-I-O-R):</td>";
            String value = statsMap.get("Left");
            value = value.substring(value.indexOf('(') + 1);
            value = value.substring(0, value.indexOf('%'));
            int num1 = Math.round(Float.parseFloat(value));

            value = statsMap.get("Inner");
            value = value.substring(value.indexOf('(') + 1);
            value = value.substring(0, value.indexOf('%'));
            int num2 = Math.round(Float.parseFloat(value));

            value = statsMap.get("Outer");
            value = value.substring(value.indexOf('(') + 1);
            value = value.substring(0, value.indexOf('%'));
            int num3 = Math.round(Float.parseFloat(value));

            value = statsMap.get("Right");
            value = value.substring(value.indexOf('(') + 1);
            value = value.substring(0, value.indexOf('%'));
            int num4 = Math.round(Float.parseFloat(value));
            newStats += "<td>" + num1 + "% - " + num2 + "% - " + num3 + "% - " + num4 + "%</td></tr>";
        } else if (statsMap.containsKey(" Pair Type %(L-I-O-R)")) {
            newStats += "<tr><td>&nbsp;&nbsp;Pair Type % (L-I-O-R):</td>";
            newStats += "<td>" + statsMap.get(" Pair Type %(L-I-O-R)") + "</td></tr>";
        }
        newStats += "<tr></tr>";
        newStats += "<tr><th colspan=2>Analysis of Hi-C Contacts (% Sequenced Reads / % Unique Reads)</th></tr>";
        if (statsMap.containsKey("Inter")) {
            newStats += "<tr><td>Inter-chromosomal:</td>";
            String value = statsMap.get("Inter");
            newStats += "<td>" + value.substring(0, value.indexOf('('));
            int num;
            try {
                num = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1 && num != -1 && unique != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) +
                        " / " + decimalFormat.format(num / (float) unique) + ")";
            }
            newStats += "</td></tr>";
        } else if (statsMap.containsKey("Inter-chromosomal")) {
            newStats += "<tr><td>Inter-chromosomal:</td>";
            newStats += "<td>" + statsMap.get("Inter-chromosomal") + "</td></tr>";
        }
        if (statsMap.containsKey("Intra")) {
            newStats += "<tr><td>Intra-chromosomal:</td>";
            String value = statsMap.get("Intra");
            newStats += "<td>" + value.substring(0, value.indexOf('('));
            int num;
            try {
                num = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1 && num != -1 && unique != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) +
                        " / " + decimalFormat.format(num / (float) unique) + ")";
            }
            newStats += "</td></tr>";
        } else if (statsMap.containsKey("Intra-chromosomal")) {
            newStats += "<tr><td>Intra-chromosomal:</td>";
            newStats += "<td>" + statsMap.get("Intra-chromosomal") + "</td></tr>";
        }
        if (statsMap.containsKey("Small")) {
            newStats += "<tr><td>&nbsp;&nbsp;Short Range (&lt;20Kb):</td>";
            String value = statsMap.get("Small");
            newStats += "<td>" + value.substring(0, value.indexOf('('));
            int num;
            try {
                num = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1 && num != -1 && unique != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) +
                        " / " + decimalFormat.format(num / (float) unique) + ")";
            }
            newStats += "</td></tr>";
        } else if (statsMap.containsKey("Short Range (<20Kb)")) {
            newStats += "<tr><td>&nbsp;&nbsp;Short Range (&lt;20Kb):</td>";
            newStats += "<td>" + statsMap.get("Short Range (<20Kb)") + "</td></tr>";
        }
        if (statsMap.containsKey("Large")) {
            newStats += "<tr><td><b>&nbsp;&nbsp;Long Range (&gt;20Kb):</b></td>";
            String value = statsMap.get("Large");
            newStats += "<td><b>" + value.substring(0, value.indexOf('('));
            int num;
            try {
                num = numberFormat.parse(value.trim()).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (sequenced != -1 && num != -1 && unique != -1) {
                newStats += " (" + decimalFormat.format(num / (float) sequenced) +
                        " / " + decimalFormat.format(num / (float) unique) + ")";
            }
            newStats += "</b></td></tr>";
        } else if (statsMap.containsKey("Long Range (>20Kb)")) {
            newStats += "<tr><td><b>&nbsp;&nbsp;Long Range (&gt;20Kb):</b></td>";
            newStats += "<td><b>" + statsMap.get("Long Range (>20Kb)") + "</b></td></tr>";
        }
        // Error checking
        if (statsMap.containsKey("Unique Reads")) {
            int num;
            try {
                num = numberFormat.parse(statsMap.get("Unique Reads").trim()).intValue();
            } catch (ParseException error) {
                num = -1;
            }
            if (num != unique) {
                System.err.println("Check files -- \"Unique Reads\" should be the same as \"Total reads after duplication removal\"");
            }
        }

        return newStats;
    }
}
