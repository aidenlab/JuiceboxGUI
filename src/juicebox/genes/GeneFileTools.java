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

package juicebox.genes;

import javastraw.reader.basics.Chromosome;
import javastraw.reader.basics.ChromosomeHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by muhammadsaadshamim on 8/3/16.
 */
public class GeneFileTools {


    public static BufferedReader getStreamToGeneFile(String genomeID) {
        String path = extractProperGeneFilePath(genomeID);
        try {
            return new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            System.err.println("Unable to read from " + path);
            System.exit(56);
        }
        return null;
    }

    private static String extractProperGeneFilePath(String genomeID) {
        if (genomeID.equals("hg19") || genomeID.equals("hg38") || genomeID.equals("mm9") || genomeID.equals("mm10")) {
            String newURL = "http://hicfiles.s3.amazonaws.com/internal/" + genomeID + "_refGene.txt";
            try {
                return GeneFileParser.downloadFromUrl(new URL(newURL), "genes");
            } catch (IOException e) {
                System.err.println("Unable to download file from online; attempting to use direct file path");
            }
        } else {
            try {
                return GeneFileParser.uncompressFromGzip(genomeID, "genes");
            } catch (IOException e) {
                System.err.println("Unable to unzip file; attempting to use direct file path");
            }
        }
        return genomeID;
    }

    public static Map<String, GeneLocation> getLocationMap(BufferedReader reader, ChromosomeHandler handler) throws IOException {
        Map<String, GeneLocation> geneLocationHashMap = new HashMap<>();

        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
            String[] values = nextLine.split("\\s+");
            if (values.length == 4 || values.length == 16) {  // 16 is refGene official format

                // transcript start; for 4 column format, just position-1
                int txStart = (values.length == 4) ? Integer.parseInt(values[3].trim()) - 1 : Integer.parseInt(values[4].trim());
                // transcript end; for 4 column format, just position+1
                //int txEnd = (values.length==4) ? Integer.valueOf(values[3].trim())+1 : Integer.valueOf(values[5].trim());
                String name = values[1].trim();
                String name2 = (values.length==4) ? values[0].trim() : values[12].trim();
                Chromosome chr = handler.getChromosomeFromName(values[2]);
                GeneLocation location = new GeneLocation(chr, txStart);
                geneLocationHashMap.put(name2.toLowerCase(), location);
                geneLocationHashMap.put(name.trim().toLowerCase(), location);
            }
        }
        return geneLocationHashMap;
    }
}
