import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// ----- Type verification -----
// operaçoes com o mesmo tipo de operandos (int + boolean -> erro) (done)
// nao e possivel utilizar arrays diretamente para operaçoes aritmeticas (array1 + array2) (done)
// verificar se um array access e de facto feito sobre um array (1[10] nao e permitido) (done)
// verificar se o indice do array access é um inteiro (e.g. a[true] não é permitido) (done)
// verificar se valor do assignee é igual ao do assigned (a_int = b_boolean não é permitido!) (done)
// verificar se operação booleana (&&, < ou !) é efetuada só com booleanos (done)
// verificar se conditional expressions (if e while) resulta num booleano (done)

// ----- Method verification -----
// verificar se o "target" do método existe, e se este contém o método (e.g. a.foo, ver se 'a' existe e se tem um método 'foo') (done)
// caso seja do tipo da classe declarada (e.g. a usar o this), se não existir declaração na própria classe: se não tiver extends retorna erro, se tiver extends assumir que é da classe super. (done)
//	caso o método não seja da classe declarada, isto é uma classe importada, assumir como existente e assumir tipos esperados. (e.g. a = Foo.b(), se a é um inteiro, e Foo é uma classe importada, assumir que o método b é estático (pois estamos a aceder a uma método diretamente da classe), que não tem argumentos e que retorna um inteiro) (done)
//	verificar se o número de argumentos na invocação é igual ao número de parâmetros da declaração (done)
//	verificar se o tipo dos parâmetros coincide com o tipo dos argumentos (done)

// ver a parte dos . fora do length
// arr_size_not_int (como distinguir o a do int[] do a do string[])
// import methods used (io.println) deve-se verificar se o import e feito?

public class SemanticVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    private SymbolTableImp symbolTable;
    public SemanticVisitor(SymbolTableImp symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("TwoPartExpression", this::visitDot);
        addVisit("InsideArray", this::visitInsideArray);
        addVisit("AdditiveExpression", this::visitArithmetic);
        addVisit("MultiplicativeExpression", this::visitArithmetic);
        addVisit("SubtractiveExpression", this::visitArithmetic);
        addVisit("DivisionExpression", this::visitArithmetic);
        addVisit("Assign", this::visitAssign);
        addVisit("If", this::visitConditional);
        addVisit("While", this::visitConditional);
        addVisit("Less", this::visitArithmetic);
        addVisit("And", this::visitBoolean);
        addVisit("Not", this::visitNot);
    }

    private Boolean visitDot(JmmNode node, List<Report> reports) {
        String methodName = getNodeMethod(node);
        if(node.getChildren().get(1).getKind().equals("DotExpression")) {
            JmmNode dot = node.getChildren().get(1);
            if(dot.getChildren().get(0).getKind().equals("Length")) {
                // caso antes do .length nao seja um identifier, adicionar ao report
                // caso seja um identifier, se nao estiver declarada a variavel ou se nao for array, adicionar ao report

                if(!node.getChildren().get(0).getKind().equals("Identifier")) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Builtin \"length\" does not exist over simple types."));
                }
                else if(node.getChildren().get(0).getKind().equals("Identifier") && (checkIdentifiers(node.getChildren().get(0), methodName) == null)) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Variable " + node.getChildren().get(0).get("name") + " not declared."));
                }
            }
            else if(dot.getChildren().get(0).getKind().equals("MethodCall")) {
                JmmNode methodNode = dot.getChildren().get(0);
                String methodCallName = methodNode.getChildren().get(0).get("name");
                int argsSize = methodNode.getNumChildren() - 1;
                List<Symbol> methodArgs = symbolTable.getParameters(methodCallName);

                Boolean importMethod = false;
                if(node.getChildren().get(0).getKind().equals("Identifier") && (symbolTable.getImports() != null && symbolTable.getImports().contains(node.getChildren().get(0).get("name")))) {
                    importMethod = true;
                }

                if(node.getChildren().get(0).getKind().equals("This")) {
                    System.out.println("ANCESTOR: " + (node.getAncestor("Method").isPresent() || node.getAncestor("Main").isPresent()));
                    if ((symbolTable.getSuper() == null || symbolTable.getSuper().isEmpty()) && !symbolTable.getMethods().contains(methodName)) { // falta o caso de o metodo nao tar declarado
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Builtin \"length\" does not exist over simple types."));
                    }
                }
                else if(!node.getChildren().get(0).getKind().equals("Identifier")) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Builtin \"length\" does not exist over simple types."));
                }
                else if(node.getChildren().get(0).getKind().equals("Identifier") && (checkIdentifiers(node.getChildren().get(0), methodName) == null && !(symbolTable.getImports() != null && symbolTable.getImports().contains(node.getChildren().get(0).get("name"))))) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Variable " + node.getChildren().get(0).get("name") + " not declared."));
                }

                if(!symbolTable.getMethods().contains(methodCallName) && !importMethod) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(methodNode.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Method '" + methodCallName + "' not declared in class"));
                }
                else if(methodArgs != null && methodArgs.size() != argsSize) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(methodNode.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Wrong parameters number for method '" + methodCallName + "' call. Provided " + argsSize + " arguments, but " + methodArgs.size() + " is / are needed."));
                }
                else if(methodArgs != null && methodArgs.size() == argsSize) {
                    for(int i = 0; i < methodArgs.size(); i++) {
                        Symbol identifierSymbol = checkIdentifiers(methodNode.getChildren().get(i+1), methodName);
                        String identifierType = null;
                        if(identifierSymbol != null) {
                            identifierType = identifierSymbol.getType().getName();
                        }
                        System.out.println(methodNode.getChildren().get(i+1).getKind());
                        if(!methodArgs.get(i).getType().getName().equals(methodNode.getChildren().get(i+1).getKind()) && !methodArgs.get(i).getType().getName().equals(identifierType)) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(methodNode.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Wrong type parameter for method '" + methodCallName + "' call."));
                        }
                    }
                }
            }
        }
        return true;
    }

    private Boolean visitInsideArray(JmmNode node, List<Report> reports) {
        if(!node.getChildren().get(0).getKind().equals("int") && !node.getChildren().get(0).getKind().equals("AdditiveExpression") && !node.getChildren().get(0).getKind().equals("SubtractiveExpression") && !node.getChildren().get(0).getKind().equals("MultiplicativeExpression") && !node.getChildren().get(0).getKind().equals("DivisionExpression")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Array indices must be integer"));
        }
        return true;
    }


    private Boolean visitArithmetic(JmmNode node, List<Report> reports) {
        String methodName = getNodeMethod(node);
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        HashMap<Symbol, Boolean> classVars = symbolTable.getClassVarsInit();
        HashMap<Symbol, Boolean> methodVars = symbolTable.getMethodVarsInit(methodName);

        Symbol identifierSymbolF = checkIdentifiers(firstChild, methodName);
        String identifierTypeF = null;
        Boolean varInitializedF = false;

        if(identifierSymbolF != null) {
            identifierTypeF = identifierSymbolF.getType().getName();
            if(classVars.containsKey(identifierSymbolF)) {
                varInitializedF = classVars.get(identifierSymbolF);
            }
            else if(methodVars.containsKey(identifierSymbolF)) {
                varInitializedF = methodVars.get(identifierSymbolF);
            }
        }

        Symbol identifierSymbolS = checkIdentifiers(secondChild, methodName);
        String identifierTypeS = null;
        Boolean varInitializedS = false;

        if(identifierSymbolS != null) {
            identifierTypeS = identifierSymbolS.getType().getName();
            if(classVars.containsKey(identifierSymbolS)) {
                varInitializedS = classVars.get(identifierSymbolS);
            }
            else if(methodVars.containsKey(identifierSymbolS)) {
                varInitializedS = methodVars.get(identifierSymbolS);
            }
        }


        if(firstChild.getKind().equals("Identifier") && identifierSymbolF == null) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("line")), "Variable not declared"));
        }
        else if(secondChild.getKind().equals("Identifier") && identifierSymbolS == null) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("line")), "Variable not declared"));
        }
        else if(identifierTypeF != null && varInitializedF == false) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("col")), "Variable not initialized"));
        }
        else if(identifierTypeS != null && varInitializedS == false) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("col")), "Variable not initialized"));
        }
        else if(!(identifierTypeF != null && identifierTypeF.equals("int")) && !firstChild.getKind().equals("int") && !firstChild.getKind().equals("AdditiveExpression") && !firstChild.getKind().equals("SubtractiveExpression") && !firstChild.getKind().equals("MultiplicativeExpression") && !firstChild.getKind().equals("DivisionExpression")){
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("line")), "Binary operations can only be applied to Integer type variables"));
        }
        else if(!(identifierTypeS != null && identifierTypeS.equals("int")) && !secondChild.getKind().equals("int") && !secondChild.getKind().equals("AdditiveExpression") && !secondChild.getKind().equals("SubtractiveExpression") && !secondChild.getKind().equals("MultiplicativeExpression") && !secondChild.getKind().equals("DivisionExpression")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("col")), "Binary operations can only be applied to Integer type variables"));
        }

        return true;
    }

    private Boolean visitAssign(JmmNode node, List<Report> reports) {
        String methodName = getNodeMethod(node);
        // types: int, int[], boolean, Identifier
        // left -> identifier (kind int); right -> additive, subtractive, multiplicative, division, identifier -> int, int
        // left -> identifier (kind boolean); right -> boolean, identifier (kind boolean), less, and, not

        System.out.println("Assign: " + node);

        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        HashMap<Symbol, Boolean> classVars = symbolTable.getClassVarsInit();
        HashMap<Symbol, Boolean> methodVars = symbolTable.getMethodVarsInit(methodName);

        Symbol identifierSymbolF = checkIdentifiers(firstChild, methodName);
        String identifierTypeF = null;

        if(identifierSymbolF != null) {
            identifierTypeF = identifierSymbolF.getType().getName();
            if(identifierSymbolF.getType().isArray()) {
                identifierTypeF = identifierTypeF + "[]";
            }
        }

        Symbol identifierSymbolS = checkIdentifiers(secondChild, methodName);
        String identifierTypeS = null;
        Boolean varInitializedS = false;

        if(identifierSymbolS != null) {
            identifierTypeS = identifierSymbolS.getType().getName();
            if(classVars.containsKey(identifierSymbolS)) {
                varInitializedS = classVars.get(identifierSymbolS);
            }
            else if(methodVars.containsKey(identifierSymbolS)) {
                varInitializedS = methodVars.get(identifierSymbolS);
            }
        }

        if(firstChild.getKind().equals("Identifier") && identifierSymbolF == null) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("line")), "Variable not declared"));
        }
        else if(secondChild.getKind().equals("Identifier") && identifierSymbolS == null) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("line")), "Variable not declared"));
        }
        else if(identifierTypeF == null || (!identifierTypeF.equals("int") && !identifierTypeF.equals("boolean") && !identifierTypeF.equals("int[]"))) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("col")), "Assignee must be a previous declared variable"));
        }
        else if(identifierTypeS != null && varInitializedS == false) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("col")), "Variable not initialized"));
        }
        else if(identifierTypeF.equals("int")) {
            if(!(identifierTypeS != null && identifierTypeS.equals("int")) && !secondChild.getKind().equals("int") && !secondChild.getKind().equals("AdditiveExpression") && !secondChild.getKind().equals("SubtractiveExpression") && !secondChild.getKind().equals("MultiplicativeExpression") && !secondChild.getKind().equals("DivisionExpression") && !secondChild.getKind().equals("TwoPartExpression")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("col")), "Assigned must have int type"));
            }
            else {
                if(classVars.containsKey(identifierSymbolF)) {
                    symbolTable.changeInitClass(identifierSymbolF);
                }
                else if(methodVars.containsKey(identifierSymbolF)) {
                    symbolTable.changeInitMethod(methodName, identifierSymbolF);
                }
            }
        }
        else if(identifierTypeF.equals("boolean")) {
            if(!(identifierTypeS != null && identifierTypeS.equals("boolean")) && !secondChild.getKind().equals("boolean") && !secondChild.getKind().equals("Less") && !secondChild.getKind().equals("And") && !secondChild.getKind().equals("Not")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("col")), "Assigned must have boolean type"));
            }
            else {
                if(classVars.containsKey(identifierSymbolF)) {
                    symbolTable.changeInitClass(identifierSymbolF);
                }
                else if(methodVars.containsKey(identifierSymbolF)) {
                    symbolTable.changeInitMethod(methodName, identifierSymbolF);
                }
            }
        }
        else if(identifierTypeF.equals("int[]")) {
            if(!secondChild.getKind().equals("New")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("col")), "Assigned must start with new"));
            }
            else if(!secondChild.getChildren().get(0).getKind().equals("Array")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("col")), "Assigned must have array type"));
            }
        }

        return true;
    }

    private Boolean visitConditional(JmmNode node, List<Report> reports) {
        if(!node.getChildren().get(0).getKind().equals("boolean") && !node.getChildren().get(0).getKind().equals("And") && !node.getChildren().get(0).getKind().equals("Less") && !node.getChildren().get(0).getKind().equals("Not")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.getChildren().get(0).get("line")), Integer.parseInt(node.getChildren().get(0).get("col")), "Conditional expressions (If and While) have to result in a boolean type"));
        }

        return true;
    }

    // less, and, not serao aqui?
    private Boolean visitBoolean(JmmNode node, List<Report> reports) {
        System.out.println("Boolean: " + node.getKind());

        String methodName = getNodeMethod(node);
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        Symbol identifierSymbolF = checkIdentifiers(firstChild, methodName);
        String identifierTypeF = null;
        if(identifierSymbolF != null) {
            identifierTypeF = identifierSymbolF.getType().getName();
        }

        Symbol identifierSymbolS = checkIdentifiers(secondChild, methodName);
        String identifierTypeS = null;
        if(identifierSymbolS != null) {
            identifierTypeS = identifierSymbolS.getType().getName();
        }

        if(firstChild.getKind().equals("Identifier") && identifierSymbolF == null) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("line")), "Variable not declared"));
        }
        else if(secondChild.getKind().equals("Identifier") && identifierSymbolF == null) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("line")), "Variable not declared"));
        }
        else if(!identifierTypeF.equals("boolean") && !firstChild.getKind().equals("boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("line")), "Boolean operator ('&&') can only be applied to boolean type variables"));
        }
        else if(!identifierTypeS.equals("boolean") && !secondChild.getKind().equals("boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(secondChild.get("line")), Integer.parseInt(secondChild.get("col")), "Boolean operator ('&&') can only be applied to boolean type variables"));
        }

        return true;
    }

    private Boolean visitNot(JmmNode node, List<Report> reports) {
        System.out.println("Not: " + node.getKind());

        String methodName = getNodeMethod(node);
        JmmNode firstChild = node.getChildren().get(0);

        Symbol identifierSymbolF = checkIdentifiers(firstChild, methodName);
        String identifierTypeF = null;
        if(identifierSymbolF != null) {
            identifierTypeF = identifierSymbolF.getType().getName();
        }

        if(firstChild.getKind().equals("Identifier") && identifierSymbolF == null) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("line")), "Variable not declared"));
        }
        else if(!identifierTypeF.equals("boolean") && !firstChild.getKind().equals("boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(firstChild.get("line")), Integer.parseInt(firstChild.get("line")), "Not operator ('!') can only be applied to boolean type variables"));
        }

        return true;
    }

    public String getNodeMethod(JmmNode node) {
        String methodName = null;
        System.out.println("NODE: " + node);
        if(node.getAncestor("Method").isPresent()) {
            JmmNode methodNode = node.getAncestor("Method").get();
            methodName = methodNode.getChildren().get(1).get("name");
            System.out.println("METHOD NAME: " + methodName);
        }
        else {
            methodName = "main";
        }
        return methodName;
    }

    public Symbol checkIdentifiers(JmmNode node, String methodName) {
        List<Symbol> allVariables = new ArrayList<>();
        List<String> allNames = new ArrayList<>();
        if(symbolTable.getParameters(methodName) != null && !symbolTable.getParameters(methodName).isEmpty()) {
            allVariables.addAll(symbolTable.getParameters(methodName));
        }
        if(symbolTable.getLocalVariables(methodName) != null && !symbolTable.getLocalVariables(methodName).isEmpty()) {
            allVariables.addAll(symbolTable.getLocalVariables(methodName));
        }

        if(symbolTable.getFields() != null && !symbolTable.getFields().isEmpty()) {
            allVariables.addAll(symbolTable.getFields());
        }

        for(int i = 0; i < allVariables.size(); i++) {
            allNames.add(allVariables.get(i).getName());
        }

        Symbol identifierType = null;
        if(node.getKind().equals("Identifier")) {
            if(allNames.contains(node.get("name"))) {
                int idx = allNames.indexOf(node.get("name"));
                identifierType = allVariables.get(idx);
            }
        }
        return identifierType;
    }
}
