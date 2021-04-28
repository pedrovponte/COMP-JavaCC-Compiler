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

public class OllirEmitter implements JmmVisitor {

    private PrintWriter printWriterFile;
    private  SymbolTableImp symbolTable;
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


    public OllirEmitter(SymbolTable table) {
        symbolTable = (SymbolTableImp)table;
        this.loopCounter = 0;
        this.localVars = 0;
        this.nParams = 0;
        this.maxStack = 0;
        this.totalStack = 0;
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

        return defaultVisit(node,"");
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
        for(int i = 0; i < children.size(); i++) {
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
            JmmNode child =  node.getChildren().get(i);
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

        if(typeNode.getKind().equals("Identifier")) {
            type = typeNode.get("name");
        }
        else {
            type = typeNode.getKind();
        }

        name = nameNode.get("name");

        stringCode.append("\n\t.field private " + name + "." + getType(type) + ";\n");

    }

    private void generateMain(JmmNode node) {
        this.localVariables.clear();
        this.localVariablesNames.clear();
        this.methodParameters.clear();
        this.methodParametersNames.clear();
        this.methodName = "main";
        generateMainMethodHeader(node);
        generateMainMethodBody(node);
        stringCode.append("\t}\n");
    }

    private void generateMainMethodHeader(JmmNode node) {
        StringBuilder methodArgs = new StringBuilder();
        StringBuilder methodType = new StringBuilder();

        this.methodParameters = symbolTable.getParameters(methodName);

        for(int i = 0; i < this.methodParameters.size(); i++) {
            this.methodParametersNames.add(this.methodParameters.get(i).getName());
        }


        if(symbolTable.getLocalVariables(this.methodName) != null) {
            this.localVariables = symbolTable.getLocalVariables(this.methodName);
        }

        if(this.localVariables != null) {
            for(int i = 0; i < this.localVariables.size(); i++) {
                this.localVariablesNames.add(this.localVariables.get(i).getName());
            }
        }


        for(int i = 0; i < this.methodParameters.size(); i++) {
            if(i > 0) {
                methodArgs.append(", ");
            }
            String type = this.methodParameters.get(i).getType().getName();
            if(this.methodParameters.get(i).getType().isArray()) {
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
                    generateStatement(child, "main");
                    break;
            }

            //Already processed
            //retirar se for argumento ou declaração de variaveis e return

            //If is not any of the others, it is a statement
            //generateStatement(node, false);

        }
    }

    private void generateMethod(JmmNode node) {
        this.localVariablesNames.clear();
        this.localVariables.clear();
        this.methodParameters.clear();
        this.methodParametersNames.clear();
        generateMethodHeader(node);
        generateMethodBody(node, this.methodName);
        stringCode.append("\t}\n");
    }

    private void generateMethodHeader(JmmNode methodNode) {
        StringBuilder methodArgs = new StringBuilder();
        StringBuilder methodType = new StringBuilder();

        this.methodName = methodNode.getChildren().get(1).get("name");

        this.methodParameters = symbolTable.getParameters(this.methodName);

        if(methodParameters != null) {
            for(int i = 0; i < this.methodParameters.size(); i++) {
                this.methodParametersNames.add(this.methodParameters.get(i).getName());
            }
        }

        this.localVariables = symbolTable.getLocalVariables(this.methodName);

        if(localVariables != null) {
            for(int i = 0; i < this.localVariables.size(); i++) {
                this.localVariablesNames.add(this.localVariables.get(i).getName());
            }
        }


        if(this.methodParameters != null) {
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
        if(type.isArray()) {
            typeS += "[]";
        }

        methodType.append("." + getType(typeS));

        stringCode.append("\n\t.method public " + this.methodName + "(" + methodArgs + ")" + methodType + " {\n");
    }

    private void generateMethodBody(JmmNode node, String methodName) {
        for (int i = 0; i < node.getNumChildren(); i++) {

            JmmNode child = node.getChildren().get(i);

            //Already processed
            //retirar se for argumento ou declaração de variaveis e return

            //If is not any of the others, it is a statement

            switch (child.getKind()) {
                case "Statement":
                    generateStatement(child, methodName);
                    break;
                case "Return":
                    generateReturn(child, methodName);
                    break;
            }
        }
    }

    private void generateStatement(JmmNode node, String methodName) {
        for (int i = 0; i < node.getNumChildren(); i++) {
            JmmNode child = node.getChildren().get(i);
            String type = null;

            if (child.getKind().equals("Assign")) {
                JmmNode first = child.getChildren().get(0);
                JmmNode second = child.getChildren().get(1);

                if (first.getKind().equals("Identifier")) {
                    type = getNodeType(first);

                    if(this.methodParametersNames.contains(first.get("name"))) {
                        int idx = methodParametersNames.indexOf(first.get("name"));
                        stringCode.append("\t\t" + "$" + idx + "." + first.get("name") + "." + getType(type) + " :=." + getType(type) + " ");
                    }
                    else {
                        stringCode.append("\t\t" + first.get("name") + "." + getType(type) + " :=." + getType(type) + " ");
                    }
                }
                else {
                    generateExpression(first, methodName);
                    stringCode.append(" := ");
                }

                if (second.getKind().equals("Identifier")) {
                    type = getNodeType(second);

                    if(this.methodParametersNames.contains(first.get("name"))) {
                        int idx = methodParametersNames.indexOf(first.get("name"));
                        stringCode.append("$" + idx + "." + second.get("name") + "." + getType(type) + ";\n");
                    }
                    else {
                        stringCode.append(second.get("name") + "." + getType(type) + ";\n");
                    }
                }
                else if(second.getKind().equals("int")) {
                    type = "int";

                    stringCode.append(second.get("value") + "." + getType(type) + ";\n");
                }
                else {
                    generateExpression(second, methodName);
                }
            }
            else if(child.getKind().equals("TwoPartExpression")) { //InsideArray or DotExpression
                generateTwoPartExpression(child);
            }
        }
    }


    private void generateExpression(JmmNode node, String methodName) {
        if (node.getKind().equals("AdditiveExpression") || node.getKind().equals("SubtractiveExpression") || node.getKind().equals("MultiplicativeExpression") || node.getKind().equals("DivisionExpression")){
            JmmNode first = node.getChildren().get(0);
            JmmNode second = node.getChildren().get(1);
            addExp(first,second,node.get("operation"), methodName);
        }
    }

    private void addExp(JmmNode node1, JmmNode node2, String op, String methodname){
        if(node1.getKind().equals("Identifier")){
            String type = getNodeType(node1);

            if(this.methodParametersNames.contains(node1.get("name"))) {
                int idx = methodParametersNames.indexOf(node1.get("name"));
                stringCode.append("$" + idx + "." + node1.get("name") + "." + getType(type) + " " + op + ".i32 ");
            }
            else {
                stringCode.append(node1.get("name") + "." + getType(type) + " " + op + ".i32 ");
            }
        }
        else if(node1.getKind().equals("int")) {
            String type = "int";

            stringCode.append(node1.get("value") + "." + getType(type) + " " + op + ".i32 ");
        }
        else{
            generateExpression(node1, methodname);
        }
        if(node2.getKind().equals("Identifier")){
            String type = getNodeType(node2);

            if(this.methodParametersNames.contains(node2.get("name"))) {
                int idx = methodParametersNames.indexOf(node2.get("name"));
                stringCode.append("$" + idx + "." + node2.get("name") + "." + getType(type) + ";\n" );
            }
            else {
                stringCode.append(node2.get("name") + "." + getType(type) + ";\n" );
            }
        }
        else if(node2.getKind().equals("int")) {
            String type = "int";

            stringCode.append(node2.get("value") + "." + getType(type) + ";\n");
        }
        else{
            generateExpression(node2, methodname);
        }
    }

    public void generateTwoPartExpression(JmmNode node) { //InsideArray or DotExpression
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        switch (secondChild.getKind()) {
            case "DotExpression":
                generateDotExpression(firstChild, secondChild);
                break;
            case "InsideArray":
                break;
        }
    }

    private void generateDotExpression(JmmNode firstChild, JmmNode secondChild) { //Length or MethodCall
        JmmNode child = secondChild.getChildren().get(0);

        switch (child.getKind()) {
            case "MethodCall":
                generateMethodCall(firstChild, child);
                break;
            case "Length":
                break;
        }
    }

    private void generateMethodCall(JmmNode firstChild, JmmNode methodNode) {
        JmmNode methodNameNode = methodNode.getChildren().get(0);
        String methodName = methodNameNode.get("name");
        String methodType = "void";

        if(!this.methods.contains(methodName)) {
            stringCode.append("\t\tinvokestatic(");
        }
        else {
            stringCode.append("\t\tinvokespecial(");
            Type mType = symbolTable.getReturnType(methodName);
            methodType = mType.getName();
            if(mType.isArray()) {
                methodType += "[]";
            }
        }

        if(firstChild.getKind().equals("Identifier")) {
            stringCode.append(firstChild.get("name") + ", \"" + methodName + "\"");
        }
        else if(firstChild.getKind().equals("This")) {

        }
        else if(firstChild.getKind().equals("New")) {

        }

        if(methodNode.getNumChildren() == 1) {
            stringCode.append(")." + getType(methodType) + ";\n"); //sera sempre .V aqui?
        }
        else {
            for(int i = 1; i < methodNode.getNumChildren(); i++) {
                JmmNode child = methodNode.getChildren().get(i);

                if(child.getKind().equals("Identifier")) {
                    String type = getNodeType(child);

                    if(this.methodParametersNames.contains(child.get("name"))) {
                        int idx = methodParametersNames.indexOf(child.get("name"));
                        stringCode.append(", " + "$" + idx + "." + child.get("name") + "." + getType(type));
                    }
                    else {
                        stringCode.append(", " + child.get("name") + "." + getType(type));
                    }
                }
                else if(child.getKind().equals("int") || child.getKind().equals("int[]")) {
                    String type = child.getKind();
                    stringCode.append(", " + child.get("value") + "." + getType(type));
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
                    stringCode.append(", " + value + "." + getType(type));
                }
                else if(child.getKind().equals("TwoPartExpression")) {
                    generateTwoPartExpression(child);
                }
            }
            stringCode.append(")." + getType(methodType) + ";\n");
        }
    }

    private void generateReturn(JmmNode node, String methodName) {
        String returnType = symbolTable.getReturnType(this.methodName).getName();
        if(symbolTable.getReturnType(this.methodName).isArray()) {
            returnType += "[]";
        }

        JmmNode returnVarNode = node.getChildren().get(0);
        String varKind = returnVarNode.getKind();
        String var = null;

        if(varKind.equals("Identifier")) {
            var = returnVarNode.get("name");

            List<Symbol> vars = new ArrayList<>();

            if(symbolTable.getLocalVariables(this.methodName) != null) {
                for(int i = 0; i < symbolTable.getLocalVariables(this.methodName).size(); i++) {
                    vars.add(symbolTable.getLocalVariables(this.methodName).get(i));
                }
            }

            Boolean found = false;

            for(int i = 0; i < vars.size(); i++) {
                if(vars.get(i).getName().equals(var)) {
                    varKind = vars.get(i).getType().getName();
                    if(vars.get(i).getType().isArray()) {
                        varKind += "[]";
                    }
                    found = true;
                }
            }

            if(!found) {
                vars = symbolTable.getFields();

                for(int i = 0; i < vars.size(); i++) {
                    if(vars.get(i).getName().equals(var)) {
                        varKind = vars.get(i).getType().getName();
                        if(vars.get(i).getType().isArray()) {
                            varKind += "[]";
                        }
                    }
                }

                String type = getType(varKind);
                //String tempVar = "t1";
                stringCode.append("\t\tt1" + "." + type + " :=." + type + " getfield(o1, " + var + "." + type + ")." + type + ";\n");
                var = "t1";
            }

        }
        else {
            var = returnVarNode.get("value");
        }

        stringCode.append("\t\tret." + getType(returnType) + " " + var + "." + getType(varKind) + ";\n");
    }


    private String getType(Type nodeType) {

        /*if (nodeType.isArray)
            return "[I";*/

        switch (nodeType.getName()) {
            case "int":
                return "i32";
            case "String":
                return "String";
            case "boolean":
                return "bool";
            case "void":
                return "V";
        }

        return "L" + nodeType.getName() + ";";
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
            if(this.globalVariables.get(idx).getType().isArray()) {
                type += "[]";
            }
        }
        return type;
    }

   /* private String getSymbolType(Type symbolType) {

        if (symbolType == Type.INT_ARRAY)
            return "[I";

        switch (symbolType) {
            case INT:
                return "I";
            case STRING:
                return "Ljava/lang/String";
            case BOOLEAN:
                return "Z";
            case VOID:
                return "V";
        }
        return "";
    }*/

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

    @Override
    public void setDefaultVisit(BiFunction method) {

    }

    @Override
    public void addVisit(String kind, BiFunction method) {

    }

}
