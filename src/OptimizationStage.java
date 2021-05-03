import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class OptimizationStage implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        if (TestUtils.getNumReports(semanticsResult.getReports(), ReportType.ERROR) > 0) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started OLLIR generation but there are errors from previous stage");
            return new OllirResult(semanticsResult, null, Arrays.asList(errorReport));
        }

        if (semanticsResult.getRootNode() == null) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started OLLIR generation but AST root node is null");
            return new OllirResult(semanticsResult, null, Arrays.asList(errorReport));
        }

        if (semanticsResult.getSymbolTable() == null) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started OLLIR generation but Symbol Table is null");
            return new OllirResult(semanticsResult, null, Arrays.asList(errorReport));
        }

        SymbolTable table = semanticsResult.getSymbolTable();
        JmmNode node= semanticsResult.getRootNode();

        var emitter = new OllirEmitter(table);
        emitter.visit(node);
        // Convert the AST to a String containing the equivalent OLLIR code
        String ollirCode = emitter.getMethodCode(); // Convert node ...

        System.out.println("OLLIR CODE: \n\n " + ollirCode + "\n\n");


        // More reports from this stage
        List<Report> reports = new ArrayList<>();

        return new OllirResult(semanticsResult, ollirCode, reports);
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return ollirResult;
    }

}
