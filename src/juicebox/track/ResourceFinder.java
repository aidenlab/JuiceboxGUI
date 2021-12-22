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

package juicebox.track;

import javastraw.reader.Dataset;
import org.broad.igv.util.FileUtils;
import org.broad.igv.util.ResourceLocator;

public class ResourceFinder {
    public static ResourceLocator getSubcompartments(Dataset dataset) {
        ResourceLocator locator = null;

        String path = dataset.getPath();
        //Special case for combined maps:
        if (path == null) {
            return null;
        }

        if (path.contains("gm12878/in-situ/combined")) {
            path = path.substring(0, path.lastIndexOf('.'));
            if (path.lastIndexOf("_30") > -1) {
                path = path.substring(0, path.lastIndexOf("_30"));
            }

            String location = path + "_subcompartments.bed";
            locator = new ResourceLocator(location);

            locator.setName("Subcompartments");
        }
        return locator;
    }


    public static ResourceLocator getPeaks(Dataset dataset) {

        String path = dataset.getPath();

        //Special case for combined maps:
        if (path == null) {
            return null;
        }

        path = path.substring(0, path.lastIndexOf('.'));


        if (path.lastIndexOf("_30") > -1) {
            path = path.substring(0, path.lastIndexOf("_30"));
        }

        String location = path + "_peaks.txt";

        if (FileUtils.resourceExists(location)) {
            return new ResourceLocator(location);
        } else {
            location = path + "_loops.txt";
            if (FileUtils.resourceExists(location)) {
                return new ResourceLocator(location);
            } else {
                return null;
            }
        }

    }

    public static ResourceLocator getBlocks(Dataset dataset) {

        String path = dataset.getPath();

        //Special case for combined maps:
        if (path == null) {
            return null;
        }

        path = path.substring(0, path.lastIndexOf('.'));

        if (path.lastIndexOf("_30") > -1) {
            path = path.substring(0, path.lastIndexOf("_30"));
        }

        String location = path + "_blocks.txt";

        if (FileUtils.resourceExists(location)) {
            return new ResourceLocator(location);
        } else {
            location = path + "_domains.txt";
            if (FileUtils.resourceExists(location)) {
                return new ResourceLocator(location);
            } else {
                return null;
            }

        }
    }

    public static ResourceLocator getSuperLoops(Dataset dataset) {
        ResourceLocator locator = null;

        String path = dataset.getPath();
        //Special case for combined maps:
        if (path == null) {
            return null;
        }

        if (path.contains("gm12878/in-situ/combined")) {
            path = path.substring(0, path.lastIndexOf('.'));

            if (path.lastIndexOf("_30") > -1) {
                path = path.substring(0, path.lastIndexOf("_30"));
            }

            String location = path + "_chrX_superloop_list.txt";
            locator = new ResourceLocator(location);

            locator.setName("ChrX super loops");
        }
        return locator;
    }

    public static ResourceLocator[] get2DResources(Dataset dataset) {
        return new ResourceLocator[]{getPeaks(dataset),
                getBlocks(dataset),
                getSuperLoops(dataset)};
    }

    public static String[] get2DResourceNames() {
        return new String[]{"Peaks", "Contact Domains", "ChrX Super Loops"};
    }
}
