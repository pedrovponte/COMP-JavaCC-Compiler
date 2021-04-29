
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.ArrayList;
import java.util.List;

public class SethiUllman {

    public  static List<Integer> registersAvailable;
    private  static SymbolTableImp symbolTable;
    private  static String currentMethod;
    private  static String requiredType;

    public static void initialize(SymbolTableImp symbolTable, String currentMethod) {

        SethiUllman.symbolTable = symbolTable;
        SethiUllman.currentMethod = currentMethod;

        registersAvailable = new ArrayList<>();

        for (int i = 1; i <= 100; i++)
            registersAvailable.add(i);

    }

    public static String run(JmmNode node) {
        requiredType = "";
        firstStep(node);
        return secondStep(node);
    }

    public  String run(JmmNode node, String typeRequired) {
        requiredType = typeRequired;

        return run(node);
    }

    private static void firstStep(JmmNode node) {

        if (isTerminal(node)) {
            fillTerminalValue(node);
            return;
        }

        for (JmmNode child : node.getChildren())
            firstStep(child);

        fillNonTerminalValue(node);
    }

    private static String secondStep(JmmNode node) {
        StringBuilder code = new StringBuilder();

        if (!canDismember(node)) {
            code.append(fillNonDismember(node));
            return code.toString();
        }

        if (node.getKind().equals("MethodCall")) {

        }

        if (node.getKind().equals("NewArray")) {

        }

        for (JmmNode child : node.getChildren())
            code.append(codeDismember(child));


        if (!node.getParent().getKind().equals("Assignment") && !node.getParent().getKind().equals("Condition"))
            code.append(dismemberHelper(node));
        else
            code.append(dismemberHelper(node));

        return code.toString();
    }

    private static String fillNonDismember(JmmNode node) {
        String typePrefix = "";
        String typeSuffix = "";
        boolean isClassField = false;


        if(node.getKind().equals("Integer")) typeSuffix = ".i32";
       else if(node.getKind().equals("Integer")) typeSuffix = ".bool";
       else  if(node.getKind().equals("Integer")){

                Symbol variable = null;

                if (symbolTable.getMethodVarsInitStr(currentMethod).contains(node.get("name"))) {
                    for (Symbol key : symbolTable.getMethodVarsInit(currentMethod).keySet()) {
                        if (key.getName().equals(node.get("name"))) {
                            variable = key;
                        }

                    }
                }

          /*      if (variable == null) {
                    for (int i = 0; i < symbolTable.getMethodsHashmap().get(currentMethod).getParameters().size(); i++) {
                        Symbol iterator = symbolTable.getMethodsHashmap().get(currentMethod).getParameters().get(i);
                        if (iterator.getName().equals(node.get("value"))) {
                            typePrefix = "$" + i + ".";
                            variable = iterator;
                            break;
                        }
                    }
                }

                if (variable == null) {
                    if (symbolTable.getClassFields().containsKey(node.get("value"))) {
                        variable = symbolTable.getClassFields().get(node.get("value"));
                        isClassField = true;
                    }
                }

                if (variable == null) {
                    if (symbolTable.getImportedClasses().contains(node.get("value"))) {
                        node.put("result", node.get("value"));
                        node.put("typeSuffix", requiredType);
                        return "";
                    }
                }
                if (variable == null)
                    return "";

                if (variable.getType().getName().equals("int"))
                    if (!variable.getType().isArray())
                        typeSuffix = ".i32";
                    else
                        typeSuffix = ".array.i32";
                else if (variable.getType().getName().equals("boolean"))
                    typeSuffix = ".bool";
                else
                    typeSuffix = "." + variable.getType().getName();

            }*/
            }
            if (isClassField && !node.getParent().getKind().equals("Assignment")) {
                int registerUsed = registersAvailable.remove(0);

                node.put("result", "t" + registerUsed);
                node.put("typePrefix", typePrefix);
                node.put("typeSuffix", typeSuffix);

                return node.get("result") + typeSuffix + " :=" + typeSuffix + " getfield(this, " + node.get("value") + typeSuffix + ")" + typeSuffix + ";" + "\n";

            } else {
                node.put("result", node.get("value"));
                node.put("typePrefix", typePrefix);
                node.put("typeSuffix", typeSuffix);
            }
            return "";
        }

    private static boolean canDismember(JmmNode node) {
        return !node.getKind().equals("Identifier") && !node.getKind().equals("This") && !node.getKind().equals("Boolean") && !node.getKind().equals("Integer");

    }

    private static boolean isTerminal(JmmNode node) {
        return node.getKind().equals("Identifier") || node.getKind().equals("This") || node.getKind().equals("Boolean") || node.getKind().equals("Integer") || node.getKind().equals("NewObject") || node.getKind().equals("MethodCall");
    }

    private static void fillTerminalValue(JmmNode node) {

        if(node.getKind().equals("Identifier") || node.getKind().equals("This") || node.getKind().equals("Boolean") || node.getKind().equals("Integer") || node.getKind().equals("MethodCall") ){
            node.put("registers","0");
        }
        else if (node.getKind().equals("NewObject")){
            node.put("registers", "1");
        }
        else{
            System.err.println("Not implemented yet");
        }
    }

    private static void fillNonTerminalValue(JmmNode node) {

        if (node.getNumChildren() == 0) {
            node.put("registers", String.valueOf(0));
            return;
        }

        int leftChildValue = Integer.parseInt(node.getChildren().get(0).get("registers"));

        //For Unary
        if (node.getNumChildren() == 1 && !node.getKind().equals("Not")) {
            node.put("registers", String.valueOf(leftChildValue));
            return;
        }
        //Not is no longer Unary
        else if (node.getKind().equals("Not")) {
            node.put("registers", String.valueOf(leftChildValue + 1));
            return;
        }

        int rightChildValue = Integer.parseInt(node.getChildren().get(1).get("registers"));

        if (leftChildValue == rightChildValue)
            node.put("registers", String.valueOf(leftChildValue + 1));
        else
            node.put("registers", String.valueOf(Math.max(leftChildValue, rightChildValue)));

    }

    ///Step 1 helpers - Step 2 helpers
    ///----------------------------------------///
    private static String codeDismember(JmmNode node) {
        StringBuilder code = new StringBuilder();

        if (isTerminal(node) && node.getAttributes().contains("value")) {
            code.append(fillNonDismember(node));
            return code.toString();
        }

        if (node.getKind().equals("MethodCall")) {

        }

        if (node.getKind().equals("NewArray")) {

        }

        if (node.getNumChildren() == 1) {
            code.append(codeDismember(node.getChildren().get(0)));
        } else if (node.getNumChildren() == 2) {
            if (Integer.parseInt(node.getChildren().get(0).get("registers")) >= Integer.parseInt(node.getChildren().get(1).get("registers"))) {
                code.append(codeDismember(node.getChildren().get(0)));
                code.append(codeDismember(node.getChildren().get(1)));
            } else {
                code.append(codeDismember(node.getChildren().get(1)));
                code.append(codeDismember(node.getChildren().get(0)));
            }
        }

        if (Integer.parseInt(node.get("registers")) >= 1)
            code.append(dismemberHelper(node));

        return code.toString();
    }


    private String dismemberLength(JmmNode node) {
        StringBuilder code = new StringBuilder();
        int registerUsed = registersAvailable.remove(0);
        node.put("result", "t" + registerUsed + ".i32");

        code.append(SethiUllman.run(node.getChildren().get(0)));

        code.append("t").append(registerUsed).append(".i32").append(" :=");
        code.append(".i32 ").append("arraylength(");

        if (node.getChildren().get(0).getAttributes().contains("typePrefix"))
            code.append(node.getChildren().get(0).get("typePrefix"));

        code.append(node.getChildren().get(0).get("result"));

        if (node.getChildren().get(0).getAttributes().contains("typeSuffix"))
            code.append(node.getChildren().get(0).get("typeSuffix"));

        code.append(")").append(".i32;");

        code.append("\n");

        return code.toString();
    }




    private  String dismemberNewArray(JmmNode node) {
        StringBuilder code = new StringBuilder();

        code.append(SethiUllman.run(node.getChildren().get(0)));

        node.put("result", "new(array," + node.getChildren().get(0).get("result") + ")");
        node.put("typeSuffix", ".array.i32");

        return code.toString();
    }

    private static String dismemberHelper(JmmNode node) {
        StringBuilder code = new StringBuilder();
        int registerUsed = registersAvailable.remove(0);

            if(node.getKind().equals("Less")){
                node.put("result", "t" + registerUsed);
                node.put("typeSuffix", ".bool");
                code.append("t").append(registerUsed).append(".bool").append(" :=").append(".bool ");

                if (node.getChildren().get(0).getAttributes().contains("typePrefix"))
                    code.append(node.getChildren().get(0).get("typePrefix"));

                code.append(node.getChildren().get(0).get("result"));

                if (node.getChildren().get(0).getAttributes().contains("typeSuffix"))
                    code.append(node.getChildren().get(0).get("typeSuffix"));

                code.append(" <").append(".i32 ");

                if (node.getChildren().get(1).getAttributes().contains("typePrefix"))
                    code.append(node.getChildren().get(1).get("typePrefix"));

                code.append(node.getChildren().get(1).get("result"));

                if (node.getChildren().get(1).getAttributes().contains("typeSuffix"))
                    code.append(node.getChildren().get(1).get("typeSuffix"));
            }
        if(node.getKind().equals("And")){
                node.put("result", "t" + registerUsed);
                node.put("typeSuffix", ".bool");
                code.append("t").append(registerUsed).append(".bool").append(" :=").append(".bool ");
                if (node.getChildren().get(0).getAttributes().contains("typePrefix"))
                    code.append(node.getChildren().get(0).get("typePrefix"));

                code.append(node.getChildren().get(0).get("result"));

                if (node.getChildren().get(0).getAttributes().contains("typeSuffix"))
                    code.append(node.getChildren().get(0).get("typeSuffix"));


                code.append("&&").append(".i32 ");

                if (node.getChildren().get(1).getAttributes().contains("typePrefix"))
                    code.append(node.getChildren().get(1).get("typePrefix"));

                code.append(node.getChildren().get(1).get("result"));

                if (node.getChildren().get(1).getAttributes().contains("typeSuffix"))
                    code.append(node.getChildren().get(1).get("typeSuffix"));
            }
        if(node.getKind().equals("Not")){
                node.put("result", "t" + registerUsed);

                node.put("typeSuffix", ".bool");
                code.append("t").append(registerUsed).append(".bool").append(" :=").append(".bool ");

                if (node.getChildren().get(0).getAttributes().contains("typePrefix"))
                    code.append(node.getChildren().get(0).get("typePrefix"));

                code.append(node.getChildren().get(0).get("result"));

                if (node.getChildren().get(0).getAttributes().contains("typeSuffix"))
                    code.append(node.getChildren().get(0).get("typeSuffix"));

                code.append(" !.bool ");

                if (node.getChildren().get(0).getAttributes().contains("typePrefix"))
                    code.append(node.getChildren().get(0).get("typePrefix"));

                code.append(node.getChildren().get(0).get("result"));

                if (node.getChildren().get(0).getAttributes().contains("typeSuffix"))
                    code.append(node.getChildren().get(0).get("typeSuffix"));
            }
        if (node.getKind().equals("AdditiveExpression") || node.getKind().equals("SubtractiveExpression") || node.getKind().equals("MultiplicativeExpression") || node.getKind().equals("DivisionExpression")) {
                node.put("result", "t" + registerUsed);
                node.put("typeSuffix", ".i32");
                code.append("t").append(registerUsed).append(".i32").append(" :=").append(".i32 ");

                if (node.getChildren().get(0).getAttributes().contains("typePrefix"))
                    code.append(node.getChildren().get(0).get("typePrefix"));

                code.append(node.getChildren().get(0).get("result"));

                if (node.getChildren().get(0).getAttributes().contains("typeSuffix"))
                    code.append(node.getChildren().get(0).get("typeSuffix"));


                code.append("+").append(".i32 ");

                if (node.getChildren().get(1).getAttributes().contains("typePrefix"))
                    code.append(node.getChildren().get(1).get("typePrefix"));

                code.append(node.getChildren().get(1).get("result"));

                if (node.getChildren().get(1).getAttributes().contains("typeSuffix"))
                    code.append(node.getChildren().get(1).get("typeSuffix"));
            }

            if(node.getKind().equals("NewObject")){
                node.put("result", "t" + registerUsed);
                node.put("typeSuffix", "." + node.get("value"));
                code.append("t").append(registerUsed).append(".").append(node.get("value")).append(" :=.");
                code.append(node.get("value")).append(" new(").append(node.get("value")).append(").").append(node.get("value")).append(";");
                code.append("\n");
                code.append("invokespecial(").append("t").append(registerUsed).append(".").append(node.get("value")).append(",\"<init>\").V");
            }

        code.append(";");
        code.append("\n");
        return code.toString();
    }
}
