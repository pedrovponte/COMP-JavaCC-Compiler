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


    public OllirEmitter(SymbolTable table) {
        symbolTable = (SymbolTableImp)table;
        this.loopCounter = 0;
        this.localVars = 0;
        this.nParams = 0;
        this.maxStack = 0;
        this.totalStack = 0;
        this.stringCode = new StringBuilder();
        this.bodyCode = new StringBuilder();
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
                this.generateClass(node);
        }

        return defaultVisit(node,"");
    }

    private void generateClass(JmmNode classNode) {
        this.localVars = 0;
        stringCode.append(symbolTable.getClassName() + "{\n");
        //stringCode.append( ".construct public " + symbolTable.getClassName() + "().V \n");

        this.generateClassVariables(classNode);
        this.generateConstructor();

        List<JmmNode> children = classNode.getChildren();
        for(int i = 0; i < children.size(); i++) {
            JmmNode child = children.get(i);

            switch (child.getKind()) {
                case "Main":
                    this.generateMain(child);
                    break;
                case "Method":
                    this.generateMethod(child);
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

    private void generateMain(JmmNode node) {
        this.generateMainMethodHeader(node);
        this.generateMainMethodBody(node);
        stringCode.append("\t}\n");
    }

    private void generateMainMethodHeader(JmmNode node) {
        StringBuilder methodArgs = new StringBuilder();
        StringBuilder methodType = new StringBuilder();

        List<Symbol> args = symbolTable.getParameters("main");

        for(int i = 0; i < args.size(); i++) {
            if(i > 0) {
                methodArgs.append(", ");
            }
            String type = args.get(i).getType().getName();
            if(args.get(i).getType().isArray()) {
                type += "[]";
            }
            methodArgs.append(args.get(i).getName() + "." + getType(type));
            this.nParams++;
        }

        methodType.append(".V");

        stringCode.append("\n\t.method public static main (" + methodArgs + ")" + methodType + " {\n");
    }

    public void generateMainMethodBody(JmmNode node) {
        for (int i = 0; i < node.getNumChildren(); i++) {

            JmmNode child = node.getChildren().get(i);

            //Already processed
            //retirar se for argumento ou declaração de variaveis e return

            //If is not any of the others, it is a statement
            generateStatement(node, false);

        }
    }

    private void generateMethod(JmmNode node) {
        this.generateMethodHeader(node);
        this.generateMethodBody(node);
        stringCode.append("\t}\n");

    }

    private void generateMethodHeader(JmmNode methodNode) {
        StringBuilder methodArgs = new StringBuilder();
        StringBuilder methodType = new StringBuilder();

        System.out.println("NOOOOOOOOOOODE: " + methodNode);
        String methodName = methodNode.getChildren().get(1).get("name");
        List<Symbol> args = symbolTable.getParameters(methodName);

        if(args != null) {
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    methodArgs.append(", ");
                }
                String type = args.get(i).getType().getName();
                if (args.get(i).getType().isArray()) {
                    type += "[]";
                }
                methodArgs.append(args.get(i).getName() + "." + getType(type));
                this.nParams++;
            }
        }

        Type type = symbolTable.getReturnType(methodName);
        String typeS = type.getName();
        if(type.isArray()) {
            typeS += "[]";
        }

        methodType.append("." + getType(typeS));

        stringCode.append("\n\t.method public " + methodName + "(" + methodArgs + ")" + methodType + " {\n");

//        for (int i = 0; i < methodNode.getNumChildren(); i++) {
//            JmmNode child =  methodNode.getChildren().get(i);
//            if (child.getKind().equals("Identifier") && child.toString().equals("args")) {
//                methodArgs.append("");
//                this.nParams++;
//            }
//        }
//
//
//
//        stringCode.append("\n\t.method public " + method.getName() + "(" + methodArgs.toString() + ")." + this.getType(method.getType())+ " { \n");

    }

    private void generateMethodBody(JmmNode method) {
        for (int i = 0; i < method.getNumChildren(); i++) {

            JmmNode node = method.getChildren().get(i);
            //Already processed
            //retirar se for argumento ou declaração de variaveis e return

           if(node.getKind().equals("Statement")){
                 generateStatement(node, false);
           }


        }
    }

    private void generateStatement(JmmNode n, boolean insideIfOrWhile) {
       for (int i = 0; i < n.getNumChildren(); i++) {
           JmmNode node = n.getChildren().get(i);
          
           if (node.getKind().equals("Assign")) {
               JmmNode first = node.getChildren().get(0);
             //  JmmNode second = node.getChildren().get(1);

               if (first.getKind().equals("Identifier")) {
                   stringCode.append(first.get("name") + " := ");
               } else {
                   generateExpression(first);
                   stringCode.append(" := ");
               }
              /* if (second.getKind().equals("Identifier")) {
                   stringCode.append(second.get("name") + ";\n" );
               } else {
                   generateExpression(second);
               }         */
           }
       }

    }


    private void generateExpression(JmmNode node) {
        if (node.getKind()=="AdditiveExpression"){
            JmmNode first=node.getChildren().get(0);
            JmmNode second= node.getChildren().get(1);
            addExp(first,second,node.get("operator"));
        }
        else if(node.getKind()=="TwoPartExpression"){

        }
    }

    private void addExp(JmmNode node1, JmmNode node2, String op){
        if(node1.getKind()=="Identifier"){
            stringCode.append(node1.get("name") + op )  ;
        }
        else{
            generateExpression(node1);
        }
        if(node2.getKind().equals("Identifier")){
             stringCode.append(node2.get("name") + ";\n" )  ;
        }
        else{ generateExpression(node2);}


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
