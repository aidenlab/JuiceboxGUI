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

import juicebox.JBGlobals;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

/**
 * Created by muhammadsaadshamim on 10/26/15.
 * <p/>
 * Parses the .txt file of motifs created by FIMO
 * <p/>
 * FIMO -
 */
public class GeneFileParser {


    /**
     * http://kamwo.me/java-download-file-from-url-to-temp-directory/
     *
     * @param url
     * @param localFilename
     * @return
     * @throws IOException
     */
    public static String downloadFromUrl(URL url, String localFilename) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;

        String tempDir = System.getProperty("java.io.tmpdir");
        File outputFile = new File(tempDir, localFilename);

        try {
            URLConnection urlConn = url.openConnection();
            is = urlConn.getInputStream();
            fos = new FileOutputStream(outputFile);

            byte[] buffer = new byte[JBGlobals.bufferSize];
            int length;

            // read from source and write into local file
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            return outputFile.getAbsolutePath();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }


    public static String uncompressFromGzip(String compressedFile, String decompressedFile) throws IOException {

        InputStream fileIn = null;
        GZIPInputStream gZIPInputStream = null;
        FileOutputStream fileOutputStream = null;

        String tempDir = System.getProperty("java.io.tmpdir");
        File outputFile = new File(tempDir, decompressedFile);


        try {

            byte[] buffer = new byte[JBGlobals.bufferSize];


            fileIn = new FileInputStream(compressedFile);
            gZIPInputStream = new GZIPInputStream(fileIn);
            fileOutputStream = new FileOutputStream(outputFile);

            int bytes_read;

            while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {

                fileOutputStream.write(buffer, 0, bytes_read);
            }

            gZIPInputStream.close();
            fileOutputStream.close();

            System.out.println("The file was decompressed successfully!");
            return outputFile.getAbsolutePath();
        } finally {
            try {
                if (fileIn != null) {
                    fileIn.close();
                }
                if (gZIPInputStream != null) {
                    gZIPInputStream.close();
                }
            } finally {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        }
    }
}
