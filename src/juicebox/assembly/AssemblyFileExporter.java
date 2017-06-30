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

package juicebox.assembly;

import java.util.List;
/**
 * Created by nathanielmusial on 6/29/17.
 */
public class AssemblyFileExporter {

    private AssemblyHandler assemblyHandler;
    private String cpropsFilePath;
    private String asmFilePath;
    private List<ContigProperty> contigProperties;
    private List<List<Integer>> scaffoldProperties;

    public AssemblyFileExporter(AssemblyHandler assemblyHandler, String cpropsFilePath, String asmFilePath) {
        this.assemblyHandler = assemblyHandler;
        this.cpropsFilePath = cpropsFilePath;
        this.asmFilePath = asmFilePath;
        this.contigProperties = assemblyHandler.getContigProperties();
        this.scaffoldProperties = assemblyHandler.getScaffoldProperties();
    }

    public void exportContigsAndScaffolds() {
        exportContigs();
        exportScaffolds();
    }

    private void exportContigs() {
        for (ContigProperty contigProperty : contigProperties) {
            System.out.println(contigProperty.toString());
        }
    }

    private void exportScaffolds() {

        for (List<Integer> row : scaffoldProperties) {
            for (Integer contigIndex : row) {
                System.out.print(contigIndex + " ");
            }
            System.out.println();
        }
    }
}
