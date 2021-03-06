import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class AnalysisStage implements JmmAnalysis {

    SymbolTableImp symbolTable;

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {

        if (TestUtils.getNumReports(parserResult.getReports(), ReportType.ERROR) > 0) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but there are errors from previous stage");
            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errorReport));
        }

        if (parserResult.getRootNode() == null) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but AST root node is null");
            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errorReport));
        }

        JmmNode node = parserResult.getRootNode();

        this.symbolTable = new SymbolTableImp();

        System.out.println("\n\nImport Visitor\n");
        ImportVisitor visitorImport = new ImportVisitor("Import", this.symbolTable);
        System.out.println(visitorImport.visit(node, ""));

        System.out.println("\n\nClass Visitor\n");
        ClassVisitor visitorClass = new ClassVisitor("Class", this.symbolTable);
        System.out.println(visitorClass.visit(node, ""));

        List<Report> reports = new ArrayList<>();

        SemanticVisitor lengthVisitor = new SemanticVisitor(this.symbolTable);
        lengthVisitor.visit(node, reports);

        return new JmmSemanticsResult(parserResult, symbolTable, reports);

        /*System.out.println("Dump tree with Visitor where you control tree traversal");
        ExampleVisitor visitor = new ExampleVisitor("Identifier", "name");
        System.out.println(visitor.visit(node, ""));

        System.out.println("Dump tree with Visitor that automatically performs preorder tree traversal");
        var preOrderVisitor = new ExamplePreorderVisitor("Identifier", "id");
        System.out.println(preOrderVisitor.visit(node, ""));*/

        /*System.out.println(
                "Create histogram of node kinds with Visitor that automatically performs postorder tree traversal");
        var postOrderVisitor = new ExamplePostorderVisitor();
        var kindCount = new HashMap<String, Integer>();
        postOrderVisitor.visit(node, kindCount);
        System.out.println("Kinds count: " + kindCount + "\n");*/

        /*System.out.println(
                "Print variables name and line, and their corresponding parent with Visitor that automatically performs preorder tree traversal");
        var varPrinter = new ExamplePrintVariables("Variable", "name", "line");
        varPrinter.visit(node, null);*/

        // System.out.println("NODE: " + node.getClass());
    }

    public SymbolTableImp getSymbolTable() {
        return symbolTable;
    }
}