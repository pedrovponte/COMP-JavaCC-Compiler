import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

// ----- Type verification -----
// operaçoes com o mesmo tipo de operandos (int + boolean -> erro) (done)
// nao e possivel utilizar arrays diretamente para operaçoes aritmeticas (array1 + array2) (done)
// verificar se um array access e de facto feito sobre um array (1[10] nao e permitido) (done)
// verificar se o indice do array access é um inteiro (e.g. a[true] não é permitido) (done)
// verificar se valor do assignee é igual ao do assigned (a_int = b_boolean não é permitido!)
// verificar se operação booleana (&&, < ou !) é efetuada só com booleanos
// verificar se conditional expressions (if e while) resulta num booleano

// ----- Method verification -----
// verificar se o "target" do método existe, e se este contém o método (e.g. a.foo, ver se 'a' existe e se tem um método 'foo')
// caso seja do tipo da classe declarada (e.g. a usar o this), se não existir declaração na própria classe: se não tiver extends retorna erro, se tiver extends assumir que é da classe super.
//	caso o método não seja da classe declarada, isto é uma classe importada, assumir como existente e assumir tipos esperados. (e.g. a = Foo.b(), se a é um inteiro, e Foo é uma classe importada, assumir que o método b é estático (pois estamos a aceder a uma método diretamente da classe), que não tem argumentos e que retorna um inteiro)
//	verificar se o número de argumentos na invocação é igual ao número de parâmetros da declaração
//	verificar se o tipo dos parâmetros coincide com o tipo dos argumentos


public class SemanticVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    private SymbolTableImp symbolTable;
    public SemanticVisitor(SymbolTableImp symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("Dot", this::visitDot);
        addVisit("InsideArray", this::visitInsideArray);
        addVisit("AdditiveExpression", this::visitArithmetic);
        addVisit("MultiplicativeExpression", this::visitArithmetic);
        addVisit("SubtractiveExpression", this::visitArithmetic);
        addVisit("DivisionExpression", this::visitArithmetic);
    }

    private Boolean visitDot(JmmNode node, List<Report> reports) {
        String methodName = null;
        if(node.getChildren().get(1).getKind().equals("Length")) {
            methodName = getNodeMethod(node);
            if(node.getChildren().get(0).getKind().equals("This")) {
                System.out.println("ANCESTOR: " + (node.getAncestor("Method").isPresent() || node.getAncestor("Main").isPresent()));
                if ((symbolTable.getSuper() == null || symbolTable.getSuper().isEmpty()) && !symbolTable.getMethods().contains(methodName)) { // falta o caso de o metodo nao tar declarado
                    System.out.println("METHOD NOT DECLARED OR EXTENDED");
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Builtin \"length\" does not exist over simple types."));
                }
            }
            else if(!node.getChildren().get(0).getKind().equals("Identifier")) {
                System.out.println("NOT IDENTIFIER");
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Builtin \"length\" does not exist over simple types."));
            }
            else if(node.getChildren().get(0).getKind().equals("Identifier") && (!symbolTable.getImports().contains(node.getChildren().get(0).get("name")) && !symbolTable.getLocalVariables(methodName).contains(node.getChildren().get(0).get("name")) || !symbolTable.getParameters(methodName).contains(node.getChildren().get(0).get("name")))) {
                System.out.println("NOT DECLARED");
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Variable " + node.getChildren().get(0).get("name") + " not declared."));
            }
        }
        return true;
    }

    private Boolean visitInsideArray(JmmNode node, List<Report> reports) {
        System.out.println("INSIDE ARRAY: " + node.getChildren().get(0).getKind());
        if(!node.getChildren().get(0).getKind().equals("int") && !node.getChildren().get(0).getKind().equals("AdditiveExpression") && !node.getChildren().get(0).getKind().equals("SubtractiveExpression") && !node.getChildren().get(0).getKind().equals("MultiplicativeExpression") && !node.getChildren().get(0).getKind().equals("DivisionExpression")) {
            System.out.println("NOT INT");
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Array indices must be integer"));
        }
        return true;
    }

    // caso tenha this antes?
    private Boolean visitArithmetic(JmmNode node, List<Report> reports) {
        String methodName = null;
        System.out.println("ARITHMETIC: " + node.getChildren().get(0).getKind());
        methodName = getNodeMethod(node);
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        List<Symbol> allVariables = new ArrayList<>();
        List<String> allNames = new ArrayList<>();
        allVariables.addAll(symbolTable.getParameters(methodName));
        allVariables.addAll(symbolTable.getLocalVariables(methodName));
        allVariables.addAll(symbolTable.getFields());

        for(int i = 0; i < allVariables.size(); i++) {
            allNames.add(allVariables.get(i).getName());
        }

        if(firstChild.getKind().equals("Identifier")) {
            if(allNames.contains(firstChild.get("name"))) {
                int idx = allNames.indexOf(firstChild.get("name"));
                if(!allVariables.get(idx).getType().equals("int")) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Binary operations can only be applied to Integer type variables"));
                }
            }
        }

        else if(secondChild.getKind().equals("Identifier")) {
            if(allNames.contains(secondChild.get("name"))) {
                int idx = allNames.indexOf(secondChild.get("name"));
                if(!allVariables.get(idx).getType().equals("int")) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Binary operations can only be applied to Integer type variables"));
                }
            }
        }

        else if(!firstChild.getKind().equals("int") && !firstChild.getKind().equals("SubtractiveExpression") && !firstChild.getKind().equals("MultiplicativeExpression") && !firstChild.getKind().equals("DivisionExpression")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Binary operations can only be applied to Integer type variables"));
        }
        else if(!secondChild.getKind().equals("int") && !secondChild.getKind().equals("SubtractiveExpression") && !secondChild.getKind().equals("MultiplicativeExpression") && !secondChild.getKind().equals("DivisionExpression")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Binary operations can only be applied to Integer type variables"));
        }

        return true;
    }

    public String getNodeMethod(JmmNode node) {
        String methodName = null;
        if(node.getAncestor("Method").isPresent()) {
            if(node.getChildren().get(0).getKind().equals("Identifier")) {
                methodName = node.getChildren().get(1).get("name");
            }
            else {
                methodName = node.getChildren().get(1).get("name");
            }
        }
        else {
            methodName = "main";
        }
        return methodName;
    }
}
