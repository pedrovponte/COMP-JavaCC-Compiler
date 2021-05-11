import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

//import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmVisitor;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

public class OllirEmitter implements JmmVisitor {

    private PrintWriter printWriterFile;
    private SymbolTableImp symbolTable;
    private int loopCounter;
    private int localVars;
    private int nParams;
    private int maxStack;
    private int totalStack;
    private StringBuilder stringCode;
    private StringBuilder bodyCode;
    private List<Symbol> localVariables;
    private List<String> localVariablesNames;
    private List<Symbol> methodParameters;
    private List<String> methodParametersNames;
    private String methodName;
    private List<String> methods;
    private List<Symbol> globalVariables;
    private List<String> globalVariablesNames;
    private List<Symbol> tempRegisters;
    private List<Symbol> objects;
    private int tempVarsCount;
    private int objectsCount;
    private StringBuilder aux1;
    private StringBuilder aux2;
    private StringBuilder auxGeral;
    private String fieldType;
    private Boolean isField;
    private Boolean needPar;
    private Boolean insideWhile;

    public OllirEmitter(SymbolTable table) {
        this.symbolTable = (SymbolTableImp) table;
        this.loopCounter = 0;
        this.localVars = 0;
        this.nParams = 0;
        this.maxStack = 0;
        this.totalStack = 0;
        this.aux1 = new StringBuilder();
        this.aux2 = new StringBuilder();
        this.auxGeral = new StringBuilder();
        this.stringCode = new StringBuilder();
        this.bodyCode = new StringBuilder();
        this.localVariables = new ArrayList<>();
        this.localVariablesNames = new ArrayList<>();
        this.methodParameters = new ArrayList<>();
        this.methodParametersNames = new ArrayList<>();
        this.methodName = null;
        this.methods = new ArrayList<>();
        this.globalVariables = new ArrayList<>();
        this.globalVariablesNames = new ArrayList<>();
        this.tempRegisters = new ArrayList<>();
        this.tempVarsCount = 1;
        this.objects = new ArrayList<>();
        this.objectsCount = 1;
        this.fieldType = null;
        this.isField = false;
        this.needPar = false;
        this.insideWhile=false;
    }


    public String getMethodCode() {
        return stringCode.toString();
    }

    @Override
    public Object visit(JmmNode node, Object data) {
        /*System.out.println("\n\nNODE: ");
        System.out.println("String: "  +node.toString());
        System.out.println("Type: " + node.getClass().getComponentType());
        System.out.println("Kind: " + node.getKind());
        System.out.println("Class: " + node.getClass());
        System.out.println("Attributes: " + node.getAttributes());
        System.out.println("Children: " + node.getChildren());             */

        switch (node.getKind()) {
            case "Class":
                generateClass(node);
        }

        return defaultVisit(node, "");
    }

    private void generateClass(JmmNode classNode) {
        this.localVars = 0;
        this.methods = symbolTable.getMethods();
        this.globalVariables = symbolTable.getFields();

        if(this.globalVariables != null) {
            for(int i = 0; i < this.globalVariables.size(); i++) {
                this.globalVariablesNames.add(this.globalVariables.get(i).getName());
            }
        }

        stringCode.append(symbolTable.getClassName() + "{\n");
        //stringCode.append( ".construct public " + symbolTable.getClassName() + "().V \n");

        generateClassVariables(classNode);
        generateConstructor();

        List<JmmNode> children = classNode.getChildren();
        for (int i = 0; i < children.size(); i++) {
            JmmNode child = children.get(i);

            switch (child.getKind()) {
                case "Main":
                    generateMain(child);
                    break;
                case "Method":
                    generateMethod(child);
                    break;
            }
        }
        stringCode.append("}");
    }

    private void generateConstructor() {
        stringCode.append("\n\t.construct " + symbolTable.getClassName() + "().V {\n");
        stringCode.append("\t\tinvokespecial(this, \"<init>\").V;");
        stringCode.append("\n\t}\n");
    }

    private void generateClassVariables(JmmNode node) {
        for (int i = 0; i < node.getNumChildren(); i++) {
            JmmNode child = node.getChildren().get(i);
            // stringCode.append(node.toString() + "\n");
            if (child.getKind().equals("VarDeclaration")) {
                generateGlobalVar(child);
            }
        }
    }

    private void generateGlobalVar(JmmNode node) {
        JmmNode typeNode = node.getChildren().get(0);
        JmmNode nameNode = node.getChildren().get(1);
        String type = null;
        String name = null;

        if (typeNode.getKind().equals("Identifier")) {
            type = typeNode.get("name");
        } else {
            type = typeNode.getKind();
        }

        name = nameNode.get("name");

        stringCode.append("\n\t.field private " + name + "." + getType(type) + ";\n");

    }

    private void generateMain(JmmNode node) {
        if(this.localVariables != null) {
            this.localVariables.clear();
            this.localVariablesNames.clear();
        }

        if(this.methodParameters != null) {
            this.methodParameters.clear();
            this.methodParametersNames.clear();
        }

        this.methodName = "main";
        this.tempVarsCount = 1;
        this.tempRegisters.clear();
        this.objects.clear();
        this.objectsCount = 1;
        generateMainMethodHeader(node);
        generateMainMethodBody(node);
        stringCode.append("\t}\n");
    }

    private void generateMainMethodHeader(JmmNode node) {
        StringBuilder methodArgs = new StringBuilder();
        StringBuilder methodType = new StringBuilder();

        this.methodParameters = symbolTable.getParameters(this.methodName);

        for (int i = 0; i < this.methodParameters.size(); i++) {
            this.methodParametersNames.add(this.methodParameters.get(i).getName());
        }


        if (symbolTable.getLocalVariables(this.methodName) != null) {
            this.localVariables = symbolTable.getLocalVariables(this.methodName);
        }

        if (this.localVariables != null) {
            for (int i = 0; i < this.localVariables.size(); i++) {
                this.localVariablesNames.add(this.localVariables.get(i).getName());
            }
        }


        for (int i = 0; i < this.methodParameters.size(); i++) {
            if (i > 0) {
                methodArgs.append(", ");
            }
            String type = this.methodParameters.get(i).getType().getName();
            if (this.methodParameters.get(i).getType().isArray()) {
                type += "[]";
            }
            methodArgs.append(this.methodParametersNames.get(i) + "." + getType(type));
            this.nParams++;
        }

        methodType.append(".V");

        stringCode.append("\n\t.method public static main (" + methodArgs + ")" + methodType + " {\n");
    }

    public void generateMainMethodBody(JmmNode node) {
        for (int i = 1; i < node.getNumChildren(); i++) {
            JmmNode child = node.getChildren().get(i);

            switch (child.getKind()) {
                case "Statement":
                    generateStatement(child);
                    break;
            }

            //Already processed
            //retirar se for argumento ou declaração de variaveis e return

            //If is not any of the others, it is a statement
            //generateStatement(node, false);

        }
    }

    private void generateMethod(JmmNode node) {
        if(localVariablesNames != null) {
            this.localVariablesNames.clear();
            this.localVariables.clear();
        }

        if(methodParameters != null) {
            this.methodParameters.clear();
            this.methodParametersNames.clear();
        }

        this.tempVarsCount = 1;
        this.tempRegisters.clear();
        this.objects.clear();
        this.objectsCount = 1;
        generateMethodHeader(node);
        generateMethodBody(node);
        stringCode.append("\t}\n");
    }

    private void generateMethodHeader(JmmNode methodNode) {
        StringBuilder methodArgs = new StringBuilder();
        StringBuilder methodType = new StringBuilder();

        this.methodName = methodNode.getChildren().get(1).get("name");

        this.methodParameters = symbolTable.getParameters(this.methodName);

        if (methodParameters != null) {
            for (int i = 0; i < this.methodParameters.size(); i++) {
                this.methodParametersNames.add(this.methodParameters.get(i).getName());
            }
        }

        if(symbolTable.getLocalVariables(this.methodName) != null) {
            this.localVariables = symbolTable.getLocalVariables(this.methodName);
        }

        if (localVariables != null) {
            for (int i = 0; i < this.localVariables.size(); i++) {
                this.localVariablesNames.add(this.localVariables.get(i).getName());
            }
        }


        if (this.methodParameters != null) {
            for (int i = 0; i < this.methodParameters.size(); i++) {
                if (i > 0) {
                    methodArgs.append(", ");
                }
                String type = this.methodParameters.get(i).getType().getName();
                if (this.methodParameters.get(i).getType().isArray()) {
                    type += "[]";
                }
                methodArgs.append(this.methodParameters.get(i).getName() + "." + getType(type));
                this.nParams++;
            }
        }

        Type type = symbolTable.getReturnType(this.methodName);
        String typeS = type.getName();
        if (type.isArray()) {
            typeS += "[]";
        }

        methodType.append("." + getType(typeS));

        stringCode.append("\n\t.method public " + this.methodName + "(" + methodArgs + ")" + methodType + " {\n");
    }

    private void generateMethodBody(JmmNode node) {
        for (int i = 0; i < node.getNumChildren(); i++) {

            JmmNode child = node.getChildren().get(i);

            //Already processed
            //retirar se for argumento ou declaração de variaveis e return

            //If is not any of the others, it is a statement

            switch (child.getKind()) {
                case "Statement":
                    generateStatement(child);
                    break;
                case "Return":
                    generateReturn(child);
                    break;
            }
        }
    }

    private void generateStatement(JmmNode node) {
        this.auxGeral = new StringBuilder();
        this.needPar = false;
        for (int i = 0; i < node.getNumChildren(); i++) {
            JmmNode child = node.getChildren().get(i);
            StringBuilder stringBuilder = new StringBuilder();
            String type = null;
            if (child.getKind().equals("Assign")) {
                JmmNode first = child.getChildren().get(0);
                JmmNode second = child.getChildren().get(1);

                if (first.getKind().equals("Identifier")) {
                    type = getNodeType(first);
                    Symbol sf = null;

                    if(this.isField) {
                        if(checkIfObject(type)) {
                            Symbol o = addObject(this.fieldType, type.contains("[]"));
                            stringBuilder.append("\t\tputfield(" + o.getName() + "." + getType(type) + ", " + first.get("name") + "." + getType(type) + ", ");
                            this.auxGeral.append("\t\tputfield(" + o.getName() + "." + getType(type) + ", " + first.get("name") + "." + getType(type) + ", ");
                        }
                        else {
                            stringBuilder.append("\t\tputfield(this" + ", " + first.get("name") + "." + getType(type) + ", ");
                            this.auxGeral.append("\t\tputfield(this" + ", " + first.get("name") + "." + getType(type) + ", ");
                        }
                        this.isField = false;
                        needPar = true;
                    }
                    else {
                        if(this.methodParametersNames.contains(first.get("name"))) {
                            int idx = this.methodParametersNames.indexOf(first.get("name")) + 1;
                            stringBuilder.append("\t\t$" + idx + "." + first.get("name") + "." + getType(type) + " :=." + getType(type) + " ");
                            this.auxGeral.append("\t\t$" + idx + "." + first.get("name") + "." + getType(type) + " :=." + getType(type) + " ");
                        }
                        else {
                            stringBuilder.append("\t\t" + first.get("name") + "." + getType(type) + " :=." + getType(type) + " ");
                            this.auxGeral.append("\t\t" + first.get("name") + "." + getType(type) + " :=." + getType(type) + " ");
                        }
                    }

                    if (second.getKind().equals("Identifier")){
                        String t = getNodeType(second);
                        Symbol s = null;

                        if(this.isField) {
                            s = addTempVar(this.fieldType, t.contains("[]"));

                            if(checkIfObject(t)) {
                                Symbol o = addObject(this.fieldType, t.contains("[]"));
                                stringCode.append("\t\t" + s.getName() + "." + getType(t) + " :=." + getType(t) + " getfield(" + o.getName() + "." + getType(t) + ", " + second.get("name") + "." + getType(t) + ")." + getType(t) + ";\n");
                            }
                            else {
                                stringCode.append("\t\t" + s.getName() + "." + getType(t) + " :=." + getType(t) + " getfield(this" + ", " + second.get("name") + "." + getType(t) + ")." + getType(t) + ";\n");
                            }
                            this.isField = false;
                            stringCode.append(stringBuilder);
                            stringCode.append(s.getName() + "." + getType(t));
                            if(needPar) {
                                stringCode.append(").V;\n");
                            }
                            else {
                                stringCode.append(";\n");
                            }
                        }
                        else {
                            if(this.methodParametersNames.contains(second.get("name"))) {
                                int idx = this.methodParametersNames.indexOf(second.get("name")) + 1;
                                stringCode.append(stringBuilder);
                                stringCode.append("$" + idx + "." + second.get("name") + "." + getType(t));
                                if(needPar) {
                                    stringCode.append(").V;\n");
                                }
                                else {
                                    stringCode.append(";\n");
                                }
                            }
                            else {
                                stringCode.append(stringBuilder);
                                stringCode.append(second.get("name") + "." + getType(t));
                                if(needPar) {
                                    stringCode.append(").V;\n");
                                }
                                else {
                                    stringCode.append(";\n");
                                }
                            }
                        }
                    }
                    else if (second.getKind().equals("int") || second.getKind().equals("int[]")) {
                        if(this.insideWhile) {
                            stringCode.append("\t");
                        }
                        stringCode.append(stringBuilder);
                        stringCode.append(second.get("value") + "." + getType(type));
                        if(needPar) {
                            stringCode.append(").V;\n");
                        }
                        else {
                            stringCode.append(";\n");
                        }
                    }
                    else if(second.getKind().equals("boolean")) {
                        if(this.insideWhile) {
                            stringCode.append("\t");
                        }
                        stringCode.append(stringBuilder);
                        String boolT;
                        if(second.get("value").equals("true")) {
                            boolT = "1";
                        }
                        else {
                            boolT = "0";
                        }
                        stringCode.append(boolT + "." + getType(type));
                        if(needPar) {
                            stringCode.append(").V;\n");
                        }
                        else {
                            stringCode.append(";\n");
                        }
                    }
                    else if(second.getKind().equals("New")) {
                        if(needPar) {
                            Symbol sNew = addTempVar(getType(type), false);
                            stringCode.append("\t\t" + sNew.getName() + "." + getType(type) + " :=." + getType(type) + " new(" + getType(type) + ")." + getType(type) + ";\n");
                            stringCode.append(stringBuilder);
                            stringCode.append(sNew.getName() + "." + getType(type));
                            stringCode.append(").V;\n");
                            //stringCode.append("\t\tinvokespecial(" + first.get("name") + "." + getType(type) + ", \"<init>\").V;\n");
                        }
                        else {
                            stringCode.append("\t\t" + first.get("name") + "." + getType(type) + " :=." + getType(type) + " ");
                            stringCode.append("new(" + getType(type) + ")." + getType(type) + ";\n");
                            //stringCode.append("\t\tinvokespecial(" + first.get("name") + "." + getType(type) + ", \"<init>\").V;\n");
                        }

                    }
                    else if(second.getKind().equals("TwoPartExpression")) {
                        generateTwoPartExpression(second);
                        stringCode.append(stringBuilder);
                        String t = this.tempRegisters.get(this.tempRegisters.size() - 1).getType().getName();
                        if(this.tempRegisters.get(this.tempRegisters.size() - 1).getType().isArray()) {
                            t += "[]";
                        }
                        stringCode.append(this.tempRegisters.get(this.tempRegisters.size() - 1).getName() + "." + getType(t));
                        if(needPar) {
                            stringCode.append(").V;\n");
                        }
                        else {
                            stringCode.append(";\n");
                        }
                    }
                    else {
                        stringCode.append(generateExpression(second));
                        /*Symbol sExp = addTempVar(type.split("\\[")[0], type.contains("[]"));
                        stringCode.append(stringBuilder + sExp.getName() + "." + getType(type));*/
                        if(needPar) {
                            Symbol sExp = addTempVar(type.split("\\[")[0], type.contains("[]"));
                            stringCode.append(stringBuilder + sExp.getName() + "." + getType(type));
                            stringCode.append(").V;\n");
                        }
                        else {
                            stringCode.append(";\n");
                        }
                    }
                }
            } 
            else if (child.getKind().equals("TwoPartExpression")) { //InsideArray or DotExpression
                generateTwoPartExpression(child);
            }
            else if (child.getKind().equals("While")) {
                generateWhileStatement(child);
            }
            else if (child.getKind().equals("If")) {
                generateIf(child);
            }
            else if(child.getKind().equals("IfBody")) {
                this.insideWhile = true;
                generateIfBody(child);
                this.insideWhile = false;
            }
            else if(child.getKind().equals("ElseBody")) {
                this.insideWhile = true;
                generateElseBody(child);
                this.insideWhile = false;
            }
            else if(child.getKind().equals("Statement")) {
                System.out.println("Inside Statement");
                generateStatement(child);
            }
        }
    }

    private String newAuxiliarVar(String type, JmmNode node){
        String value;
        value = generateExpression(node);
        Symbol s = addTempVar(type, false);
        if(!insideWhile){
            return s.getName() + "." + getType(type) + " :=." + getType(type) + " " + value + "." + getType(type) + ";\n";
        }else{
            return "\t" + s.getName() + "." + getType(type) + " :=." + getType(type) + " " + value + "." + getType(type) + ";\n";
        }

    }

    private String generateExpression(JmmNode node) {
        System.out.println("KIND EXPRESSION");
        StringBuilder sb = new StringBuilder();
        StringBuilder st = new StringBuilder();
        String leftValue ="";
        String rightValue ="";
        switch (node.getKind()) {
            case "int":
            case "int[]":
                sb.append(node.get("value") + "." + getType(node.getKind()));
                return sb.toString();
            case "boolean":
                StringBuilder stb = new StringBuilder();
                String boolT;
                if(node.get("value").equals("true")) {
                    boolT = "1";
                }
                else {
                    boolT = "0";
                }
                stb.append(boolT + "." + getType(node.getKind()));
                return stb.toString();
            case "Identifier": {
                String t = getNodeType(node);
                Symbol s = null;
                StringBuilder a = new StringBuilder();

                if(this.isField) {
                    s = addTempVar(this.fieldType, t.contains("[]"));

                    if(checkIfObject(t)) {
                        Symbol o = addObject(this.fieldType, t.contains("[]"));
                        stringCode.append("\t\t" + s.getName() + "." + getType(t) + " :=." + getType(t) + " getfield(" + o.getName() + "." + getType(t) + ", " + node.get("name") + "." + getType(t) + ")." + getType(t) + ";\n");
                    }
                    else {
                        stringCode.append("\t\t" + s.getName() + "." + getType(t) + " :=." + getType(t) + " getfield(this" + ", " + node.get("name") + "." + getType(t) + ")." + getType(t) + ";\n");
                    }
                    this.isField = false;
                    a.append(s.getName() + "." + getType(t));
                }
                else {
                    if(this.methodParametersNames.contains(node.get("name"))) {
                        int idx = this.methodParametersNames.indexOf(node.get("name")) + 1;
                        a.append("$" + idx + "." + node.get("name") + "." + getType(t));
                    }
                    else {
                        a.append(node.get("name") + "." + getType(t));
                    }
                }
                return a.toString();
            }
            case "AdditiveExpression":
            case "SubtractiveExpression":
            case "MultiplicativeExpression":
            case "DivisionExpression": {
                JmmNode left = node.getChildren().get(0);
                if(left.getNumChildren()>0 ){
                    st.append(newAuxiliarVar("i32", left));
                    leftValue = this.tempRegisters.get(this.tempRegisters.size() - 1).getName() + ".i32" ;
                }
                else{
                    leftValue = generateExpression(left);
                }
                JmmNode right = node.getChildren().get(1);
                if(right.getNumChildren()>0 ){
                    st.append(newAuxiliarVar("i32", right));
                    rightValue= this.tempRegisters.get(this.tempRegisters.size() - 1).getName() + ".i32" ;
                }
                else{
                    rightValue = generateExpression(right);
                }

                if(insideWhile){
                    stringCode.append("\t");
                }
                if(node.getParent().getKind().equals("Assign") && !needPar) {
                    stringCode.append(this.auxGeral + leftValue + " " + node.get("operation") + ".i32 " + rightValue);
                }
                else {
                    //st.append(leftValue + " " + node.get("operation") + ".i32 " + rightValue + ";\n");
                    stringCode.append("\t\tt" + tempVarsCount + ".i32" + " :=.i32 " + leftValue + " " + node.get("operation") + ".i32 " + rightValue + ";\n");
                }


                //return st.toString() ;
                break;
            }
            case "Less": {
                JmmNode left = node.getChildren().get(0);
                if(left.getNumChildren()>0 ){
                    st.append(newAuxiliarVar("boolean", left));
                    leftValue = this.tempRegisters.get(this.tempRegisters.size() - 1).getName() + ".bool" ;
                }
                else{
                    leftValue = generateExpression(left);
                }
                JmmNode right = node.getChildren().get(1);
                if(right.getNumChildren()>0 ){
                    st.append(newAuxiliarVar("boolean", right));
                    rightValue= this.tempRegisters.get(this.tempRegisters.size() - 1).getName() + ".bool" ;
                }
                else{
                    rightValue = generateExpression(right);
                }

                if(insideWhile){
                    stringCode.append("\t");
                }
                if((node.getParent().getKind().equals("Assign") && !needPar)) {
                    stringCode.append(this.auxGeral + leftValue + " <" + ".bool " + rightValue);
                }
                else if(node.getParent().getKind().equals("If")) {
                    stringCode.append(this.auxGeral + leftValue + " >=" + ".bool " + rightValue);
                }
                else {
                    //st.append(leftValue + " " + node.get("operation") + ".i32 " + rightValue + ";\n");
                    stringCode.append("\t\tt" +tempVarsCount + ".bool" + " :=.bool " + leftValue + " <" + ".bool " + rightValue + ";\n");
                }

                //return st.toString() ;
                break;
            }
            case "And": {
                JmmNode left = node.getChildren().get(0);
                if (left.getNumChildren() > 0) {
                    st.append(newAuxiliarVar("boolean", left));
                    leftValue = this.tempRegisters.get(this.tempRegisters.size() - 1).getName() + ".bool";
                } else {
                    leftValue = generateExpression(left);
                }
                JmmNode right = node.getChildren().get(1);
                if (right.getNumChildren() > 0) {
                    st.append(newAuxiliarVar("boolean", right));
                    rightValue = this.tempRegisters.get(this.tempRegisters.size() - 1).getName() + ".bool";
                } else {
                    rightValue = generateExpression(right);
                }

                if(insideWhile){
                    stringCode.append("\t");
                }
                if(node.getParent().getKind().equals("Assign") && !needPar) {
                    stringCode.append(this.auxGeral + leftValue + " " + " &&" + ".bool " + rightValue);
                }
                else {
                    //st.append(leftValue + " " + node.get("operation") + ".i32 " + rightValue + ";\n");
                    stringCode.append("\t\tt" + tempVarsCount + ".bool" + " :=.bool " + leftValue + " &&" + ".bool " + rightValue + ";\n");
                }

                //return st.toString() ;
                break;
            }
            case "Not": {
                JmmNode left = node.getChildren().get(0);
                if (left.getNumChildren() > 0) {
                    st.append(newAuxiliarVar("boolean", left));
                    leftValue = this.tempRegisters.get(this.tempRegisters.size() - 1).getName() + ".bool";
                } else {
                    leftValue = generateExpression(left);
                }

                if(insideWhile){
                    stringCode.append("\t");
                }
                if(node.getParent().getKind().equals("Assign") && !needPar) {
                    stringCode.append(this.auxGeral + leftValue + " !" + ".bool " + leftValue);
                }
                else {
                    //st.append(leftValue + " " + node.get("operation") + ".i32 " + rightValue + ";\n");
                    stringCode.append("\t\tt" + tempVarsCount + ".bool" + " :=.bool " + leftValue + " !" + ".bool " + leftValue + ";\n");
                }

                //return st.toString() ;
                break;
            }
            case "TwoPartExpression":
                generateTwoPartExpression(node);
                break;
        }
        return "";
    }

    public void generateTwoPartExpression(JmmNode node) { //InsideArray or DotExpression
        TwoPartExpressionVisitor twoPartExpressionVisitor = new TwoPartExpressionVisitor(this.methods, this.symbolTable, this.methodName, this.methodParametersNames, this.globalVariables, this.globalVariablesNames, this.methodParameters, this.localVariables, this.localVariablesNames, this.tempRegisters, this.tempVarsCount, this.objects, this.objectsCount);
        twoPartExpressionVisitor.visit(node, stringCode);
    }

    public void generateIf(JmmNode node) {
        JmmNode child = node.getChildren().get(0);

        stringCode.append("\t\tif (");
        if(child.getKind().equals("Less") || child.getKind().equals("And")) {
            stringCode.append(generateExpression(child));
        }
        stringCode.append(") goto else;\n");
    }

    public void generateIfBody(JmmNode node) {
        for(int i = 0; i < node.getNumChildren(); i++) {
            System.out.println("KIND IF: " + node.getChildren().get(i));
            generateStatement(node.getChildren().get(i));
        }
        stringCode.append("\t\t\tgoto endif;\n");
    }

    public void generateElseBody(JmmNode node) {
        stringCode.append("\t\telse:\n");
        for(int i = 0; i < node.getNumChildren(); i++) {
            System.out.println("KIND ELSE: " + node.getChildren().get(i));
            generateStatement(node.getChildren().get(i));
        }
    }

    public void generateWhileStatement(JmmNode node){
        this.insideWhile=true;

        JmmNode condition = node.getChildren().get(0);
        JmmNode body = node.getChildren().get(1);

        stringCode.append("\t\tLoop: \n");
        stringCode.append(generateExpression(condition));
        stringCode.append("\t\t\tif(");
        int i = this.tempVarsCount;
        stringCode.append("t" + i);
        stringCode.append(") goto Body; \n");
        stringCode.append("\t\t\tgoto EndLoop;\n");
        stringCode.append("\t\tBody: \n");
        for(int j = 0; j<body.getChildren().size(); j++){
            generateStatement(body.getChildren().get(j));
        }
        stringCode.append("\t\t\tgoto Loop;\n");
        stringCode.append("\t\tEndLoop: \n");
        stringCode.append("\t\t\tgoto Then; \n");
        stringCode.append("\t\tThen: \n");
        stringCode.append("\t\t\tgoto End; \n");
        stringCode.append("\t\tEnd:");

        this.insideWhile=false;

    }

    private void generateReturn(JmmNode node) {
        String returnType = symbolTable.getReturnType(this.methodName).getName();
        if(symbolTable.getReturnType(this.methodName).isArray()) {
            returnType += "[]";
        }

        JmmNode returnVarNode = node.getChildren().get(0);
        String varKind = returnVarNode.getKind();
        String var = null;

        if(varKind.equals("Identifier")) {
            var = returnVarNode.get("name");

            String t = getNodeType(returnVarNode);
            Symbol s = null;

            if(this.isField) {
                s = addTempVar(this.fieldType, t.contains("[]"));

                if(checkIfObject(t)) {
                    Symbol o = addObject(this.fieldType, t.contains("[]"));
                    stringCode.append("\t\t" + s.getName() + "." + getType(t) + " :=." + getType(t) + " getfield(" + o.getName() + "." + getType(t) + ", " + returnVarNode.get("name") + "." + getType(t) + ")." + getType(t) + ";\n");
                }
                else {
                    stringCode.append("\t\t" + s.getName() + "." + getType(t) + " :=." + getType(t) + " getfield(this" + ", " + returnVarNode.get("name") + "." + getType(t) + ")." + getType(t) + ";\n");
                }
                this.isField = false;
                var = s.getName();
                varKind = t;
            }
            else {
                if(this.methodParametersNames.contains(returnVarNode.get("name"))) {
                    int idx = this.methodParametersNames.indexOf(returnVarNode.get("name")) + 1;
                    var = "$" + idx + "." + returnVarNode.get("name");
                    varKind = this.methodParameters.get(idx - 1).getType().getName();
                    if(this.methodParameters.get(idx - 1).getType().isArray()) {
                        varKind += "[]";
                    }
                }
                else {
                    var = returnVarNode.get("name");
                    varKind = t;
                }
            }
        }
        else if(varKind.equals("TwoPartExpression")) {
            generateTwoPartExpression(returnVarNode);
            varKind = this.tempRegisters.get(this.tempRegisters.size() - 1).getType().getName(); // estará sempre certo ao por aqui 2???
            if(this.tempRegisters.get(this.tempRegisters.size() - 1).getType().isArray()) {
                varKind += "[]";
            }
            var = this.tempRegisters.get(this.tempRegisters.size() - 1).getName();
        }
        else if(varKind.equals("AdditiveExpression") || varKind.equals("SubtractiveExpression") || varKind.equals("MultiplicativeExpression") || varKind.equals("DivisionExpression")) {
            // falta a variavel antes e acrescentar as outras possibilidades de nos
            stringCode.append(generateExpression(returnVarNode));
            Symbol sExp = addTempVar("int", false);
            var = sExp.getName();
            varKind = "int";
        }
        else if(varKind.equals("And") || varKind.equals("Not") || varKind.equals("Less")) {
            stringCode.append(generateExpression(returnVarNode));
            Symbol sExp = addTempVar("boolean", false);
            var = sExp.getName();
            varKind = "boolean";
        }
        else {
            var = returnVarNode.get("value");
        }
        stringCode.append("\n\t\tret." + getType(returnType) + " " + var + "." + getType(varKind) + ";\n");
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
        else {
            int idx = this.globalVariablesNames.indexOf(node.get("name"));
            type = this.globalVariables.get(idx).getType().getName();
            this.fieldType = type;
            if(this.globalVariables.get(idx).getType().isArray()) {
                type += "[]";
            }
            this.isField = true;
        }
        return type;
    }

    private String defaultVisit(JmmNode node, String space) {
        String content = space + node.getKind();
        String attrs = node.getAttributes()
                .stream()
                .filter(a -> !a.equals("line"))
                .map(a -> a + "=" + node.get(a))
                .collect(Collectors.joining(", ", "[", "]"));

        content += ((attrs.length() > 2) ? attrs : "") + "\n";
        for (JmmNode child : node.getChildren()) {
            content += visit(child, space + " ");
        }
        return content;
    }

    private Symbol addTempVar(String type, Boolean isArray) {
        Symbol s = new Symbol(new Type(type, isArray), "t" + this.tempVarsCount);
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

    @Override
    public void setDefaultVisit(BiFunction method) {

    }

    @Override
    public void addVisit(String kind, BiFunction method) {

    }

}
