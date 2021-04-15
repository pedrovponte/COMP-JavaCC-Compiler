
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
        // System.out.println("NODE: " + node.getClass());

        SymbolTableImp symbolTable = new SymbolTableImp();

        System.out.println("Import Visitor");
        ImportVisitor visitorImport = new ImportVisitor("Import", symbolTable);
        System.out.println(visitorImport.visit(node, ""));

        System.out.println("Class Visitor");
        ClassVisitor visitorClass = new ClassVisitor("Class", symbolTable);
        System.out.println(visitorClass.visit(node, ""));

//        System.out.println("SYMBOL TABLE IMPORTS: " + symbolTable.getImports());
//        System.out.println("SYMBOL TABLE CLASS: " + symbolTable.getClassName());
//        System.out.println("SYMBOL TABLE SUPER: " + symbolTable.getSuper());
//        System.out.println("SYMBOL TABLE FIELDS: " + symbolTable.getFields());
//        System.out.println("SYMBOL TABLE METHODS: " + symbolTable.getMethods());

//        System.out.println("SYMBOL TABLE PARAMETERS MAIN: " + symbolTable.getParameters("main"));
//        System.out.println("SYMBOL TABLE LOCAL VARS MAIN: " + symbolTable.getLocalVariables("main"));
//        System.out.println("SYMBOL TABLE PARAMETERS QUICKSORT: " + symbolTable.getParameters("quicksort"));
//        System.out.println("SYMBOL TABLE LOCAL VARS QUICKSORT: " + symbolTable.getLocalVariables("quicksort"));
//        System.out.println("SYMBOL TABLE PARAMETERS BELAZY: " + symbolTable.getParameters("beLazy"));
//        System.out.println("SYMBOL TABLE LOCAL VARS BELAZY: " + symbolTable.getLocalVariables("beLazy"));

        List<Report> reports = new ArrayList<>();
        SemanticVisitor lengthVisitor = new SemanticVisitor(symbolTable);
        lengthVisitor.visit(node, reports);


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

        // No Symbol Table being calculated yet
        return new JmmSemanticsResult(parserResult, symbolTable, reports);

    }

}