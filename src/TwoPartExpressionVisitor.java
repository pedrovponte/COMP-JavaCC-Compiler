import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmVisitor;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

import java.util.List;
import java.util.Map;

public class TwoPartExpressionVisitor extends PostorderJmmVisitor<StringBuilder, Boolean> {

    private List<String> methods;
    private SymbolTableImp symbolTable;
    private String methodName;
    private List<String> methodParametersNames;
    private String methodType;
    private List<Symbol> globalVariables;
    private List<String> globalVariablesNames;
    private List<Symbol> methodParameters;
    private List<Symbol> localVariables;
    private List<String> localVariablesNames;
    private boolean firstMultLines;
    private List<Symbol> tempRegisters;
    private int tempVarsCount;
    private Symbol lastSymbol;
    private Boolean isField;
    private String fieldType;
    private List<Symbol> objects;
    private int objectsCount;
    private Boolean hasAssign;

    public TwoPartExpressionVisitor(List<String> methods, SymbolTableImp symbolTable, String methodName, List<String> methodParametersNames, List<Symbol> globalVariables, List<String> globalVariablesNames, List<Symbol> methodParameters, List<Symbol> localVariables, List<String> localVariablesNames, List<Symbol> tempRegisters, int tempVarsCount, List<Symbol> objects, int objectsCount) {
        this.methods = methods;
        this.symbolTable = symbolTable;
        this.globalVariables = globalVariables;
        this.globalVariablesNames = globalVariablesNames;
        this.methodName = methodName;
        this.methodParameters = methodParameters;
        this.methodParametersNames = methodParametersNames;
        this.localVariables = localVariables;
        this.localVariablesNames = localVariablesNames;
        this.methodType = "void";
        this.firstMultLines = false;
        this.tempRegisters = tempRegisters;
        this.tempVarsCount = tempVarsCount;
        this.lastSymbol = null;
        this.isField = false;
        this.fieldType = null;
        this.objects = objects;
        this.objectsCount = objectsCount;
        this.hasAssign = false;
        addVisit("TwoPartExpression", this::visitTwoPartExpression);
    }

    public Boolean visitTwoPartExpression(JmmNode node, StringBuilder stringBuilder) {
        JmmNode firstChild = node.getChildren().get(0); // Identifier, This, New
        JmmNode secondChild = node.getChildren().get(1); // InsideArray, DotExpression
//        stringBuilder.append("\t\tTwo Part Expression: line = " + node.get("line") + ", col = " + node.get("col") + ";\n");
//        stringBuilder.append("\t\t\t First Child: " + firstChild + ";\n");
//        stringBuilder.append("\t\t\t Second Child: " + secondChild + ";\n");
        StringBuilder firstChildString = new StringBuilder();

        switch (firstChild.getKind()) {
            case "Identifier":
                generateIdentifier(firstChild, firstChildString);
                break;

            case "This":
                generateThis(firstChildString);
                break;

            case "New":
                generateNew(firstChild, firstChildString);
                break;
        }

        switch (secondChild.getKind()) {
            case "InsideArray":
                break;

            case "DotExpression": // MethodCall or Length
                if(secondChild.getChildren().get(0).getKind().equals("MethodCall")) {
                    generateMethodCall(secondChild.getChildren().get(0), stringBuilder, firstChildString);
                }

                break;
        }

        return true;
    }

    public void generateIdentifier(JmmNode node, StringBuilder stringBuilder) {
        stringBuilder.append(node.get("name"));
        String type = getNodeType(node);
        if(type != null) {
            stringBuilder.append("." + getType(type));
        }
    }

    public void generateThis(StringBuilder stringBuilder) {
        stringBuilder.append("this");
    }

    public void generateNew(JmmNode node, StringBuilder stringBuilder) {
//            aux1.Fac :=.Fac new(Fac).Fac;
//            invokespecial(aux1.Fac,"<init>").V;
//            aux2.i32 :=.i32 invokevirtual(aux1.Fac,"compFac",10.i32).i32;
//            invokestatic(io, "println", aux2.i32).V;

        JmmNode child = node.getChildren().get(0);
        if(child.getKind().equals("ClassCall")) {
            String className = child.getChildren().get(0).get("name");
            Symbol s = addTempVar(className, false);
            stringBuilder.append("\t\t" + s.getName() + "." + className + " :=." + className + " new(" + className + ")." + className + ";\n");
            stringBuilder.append("\t\tinvokespecial(" + s.getName() + "." + className + ", \"<init>\").V;\n");
            this.lastSymbol = s;
        }
        this.firstMultLines = true;
    }

    public void generateMethodCall(JmmNode node, StringBuilder stringBuilder, StringBuilder firstChildBuilder) {
        StringBuilder temp = new StringBuilder();
        JmmNode callMethodNameNode = node.getChildren().get(0);
        String callMethodName = callMethodNameNode.get("name");
        Boolean isArray = false;
        String callType = "void";
        this.methodType = "void";
        Boolean hasLines = false;

        if(symbolTable.getMethods().contains(callMethodName)) {
            this.methodType = symbolTable.getReturnType(callMethodName).getName();
            callType = symbolTable.getReturnType(callMethodName).getName();
            if(symbolTable.getReturnType(callMethodName).isArray()) {
                this.methodType += "[]";
                isArray = true;
            }
        }

        if(this.firstMultLines) {
            stringBuilder.append(firstChildBuilder);
            Symbol s = addTempVar(callType, isArray);
            temp.append("\t\t" + s.getName() + "." + getType(methodType) + " :=." + getType(methodType) + " invokevirtual(" + lastSymbol.getName() + "." + lastSymbol.getType().getName() + ", \"" + callMethodName + "\"");
            this.lastSymbol = s;
            this.firstMultLines = false;
            hasLines = true;
        }
        else {
            if(!this.methods.contains(callMethodName)) {
                temp.append("\t\tinvokestatic(");
            }
            else {
                Type mType = symbolTable.getReturnType(callMethodName);
                this.methodType = mType.getName();
                if(mType.isArray()) {
                    this.methodType += "[]";
                }
                temp.append(("invokevirtual("));
                /*if(!mType.equals("void")) {
                    Symbol s = addTempVar(mType.getName(), mType.isArray());
                    temp.append("\t\t" + s.getName() + "." + getType(this.methodType) + " :=." + getType(this.methodType) + " invokespecial(");
                }
                else {
                    temp.append("\t\tinvokespecial(");
                }*/
            }
            temp.append(firstChildBuilder + ", \"" + callMethodName + "\"");
        }

        if(node.getNumChildren() != 1) {
            for(int i = 1; i < node.getNumChildren(); i++) {
                JmmNode child = node.getChildren().get(i);

                if(child.getKind().equals("Identifier")) {
                    String type = getNodeType(child);
                    Symbol s = null;

                    if(this.isField) {
                        s = addTempVar(this.fieldType, type.contains("[]"));

                        if(checkIfObject(type)) {
                            Symbol o = addObject(this.fieldType, type.contains("[]"));
                            stringBuilder.append("\t\t" + s.getName() + "." + getType(type) + " :=." + getType(type) + " getfield(" + o.getName() + "." + getType(type) + ", " + child.get("name") + "." + getType(type) + ")." + getType(type) + ";\n");
                        }
                        else {
                            stringBuilder.append("\t\t" + s.getName() + "." + getType(type) + " :=." + getType(type) + " getfield(this" + ", " + child.get("name") + "." + getType(type) + ")." + getType(type) + ";\n");
                        }
                        this.isField = false;
                        temp.append(", " + s.getName() + "." + getType(type));
                    }
                    else {
                        if(this.methodParametersNames.contains(child.get("name"))) {
                            int idx = this.methodParametersNames.indexOf(child.get("name")) + 1;
                            temp.append(", " + "$" + idx + "." + child.get("name") + "." + getType(type));
                        }
                        else {
                            temp.append(", " + child.get("name") + "." + getType(type));
                        }
                    }
                }
                else if(child.getKind().equals("int") || child.getKind().equals("int[]")) {
                    String type = child.getKind();
                    temp.append(", " + child.get("value") + "." + getType(type));
                }
                else if(child.getKind().equals("boolean")) {
                    String type = "boolean";
                    String value;
                    if(child.get("value").equals("true")) {
                        value = "1";
                    }
                    else {
                        value = "0";
                    }
                    temp.append(", " + value + "." + getType(type));
                }
                else if(child.getKind().equals("TwoPartExpression")) {
                    String exprType = this.lastSymbol.getType().getName();
                    if(this.lastSymbol.getType().isArray()) {
                        exprType += "[]";
                    }
                    temp.append(", " + this.lastSymbol.getName() + "." + getType(exprType));
                }
            }


        }

        if(!hasLines && this.methods.contains(callMethodName)) {
            if (!this.methodType.equals("void")) {
                Symbol s = addTempVar(this.methodType.split("\\[")[0], this.methodType.contains("[]"));
                StringBuilder sbFirst = new StringBuilder();
                sbFirst.append("\t\t" + s.getName() + "." + getType(this.methodType) + " :=." + getType(this.methodType) + " ");
                stringBuilder.append(sbFirst);
                stringBuilder.append(temp);
            } else {
                temp.append("\t\t");
                stringBuilder.append(temp);
            }
        }
        else {
            stringBuilder.append(temp);
        }

        stringBuilder.append(")." + getType(this.methodType) + ";\n");

        System.out.println("\n\n");
    }

    private String getType(String type) {
        switch(type) {
            case "int":
                return "i32";
            case "boolean":
                return "bool";
            case "void":
                return "V";
            case "int[]":
                return "array.i32";
            case "String[]":
                return "array.String";
            case "String":
                return "String";
            default:
                return type;
        }
    }

    public String getNodeType(JmmNode node) {
        String type;
        if(this.methodParametersNames.contains(node.get("name"))) {
            int idx = this.methodParametersNames.indexOf(node.get("name"));
            type = this.methodParameters.get(idx).getType().getName();
            if(this.methodParameters.get(idx).getType().isArray()) {
                type += "[]";
            }
        }
        else if(this.localVariablesNames.contains(node.get("name"))) {
            int idx = this.localVariablesNames.indexOf(node.get("name"));
            type = this.localVariables.get(idx).getType().getName();
            if(this.localVariables.get(idx).getType().isArray()) {
                type += "[]";
            }
        }
        else if(this.globalVariablesNames.contains(node.get("name"))) {
            int idx = this.globalVariablesNames.indexOf(node.get("name"));
            type = this.globalVariables.get(idx).getType().getName();
            this.fieldType = type;
            if(this.globalVariables.get(idx).getType().isArray()) {
                type += "[]";
            }
            this.isField = true;
        }
        else {
            return null;
        }
        return type;
    }

    private Symbol addTempVar(String type, Boolean isArray) {
        Symbol s = new Symbol(new Type("int", false), "t"+this.tempVarsCount);
        this.tempRegisters.add(s);
        this.tempVarsCount++;
        return s;
    }

    private Symbol addObject(String type, Boolean isArray) {
        Symbol s = new Symbol(new Type(type, isArray), "o"+this.objectsCount);
        this.objects.add(s);
        this.objectsCount++;
        return s;
    }

    private Boolean checkIfObject(String type) {
        if(!(type.equals("int") && type.equals("int[]") && type.equals("boolean") && type.equals("String[]") && type.equals("String"))) {
            return false;
        }
        return true;
    }
}
