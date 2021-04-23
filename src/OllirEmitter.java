import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
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
        System.out.println("NODE: \n\n");
        System.out.println("String: "  +node.toString());
        System.out.println("Type: " + node.getClass().getComponentType());
        System.out.println("Kind: " + node.getKind());
        System.out.println("Class: " + node.getClass());
        System.out.println("Attributes: " + node.getAttributes());
        System.out.println("Children: " + node.getChildren());


        switch (node.getKind()) {
            case "Class":
                this.generateClass(node);

        }

        return defaultVisit(node,"");
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



    private void generateClass(JmmNode classNode) {
        this.localVars = 0;
        stringCode.append(symbolTable.getClassName() + " { \n");
        //stringCode.append( ".construct public " + symbolTable.getClassName() + "().V \n");

        this.generateClassVariables(classNode);
        this.generateConstructor();
        this.generateMethods(classNode);
    }

    private void generateConstructor() {
        stringCode.append("\n\t.construct "+symbolTable.getClassName()+ "().V {\n");
        stringCode.append("\t\tinvokespecial(this, '<init>').V; \n");
        stringCode.append("\t}\n");
    }

    private void generateClassVariables(JmmNode node) {
        for (int i = 0; i < node.getNumChildren(); i++) {
            JmmNode child =  node.getChildren().get(i);
           // stringCode.append(node.toString() + "\n");
           // if (child instanceof ASTVarDeclaration) {
             //   generateGlobalVar((ASTVarDeclaration) child);
            //}
        }
    }

    private void generateMethods(JmmNode node ) {
        for (int i = 0; i < node.getNumChildren(); i++) {
            JmmNode child =  node.getChildren().get(i);
            if (child.getKind().equals("Identifier")) {
                generateMethod(child, symbolTable.getActualMethods().get(i));
            }
        }

    }


    private void generateMethod(JmmNode no, Symbol method ) {
        this.generateMethodHeader(no,method);
        this.generateMethodBody(no, method);
        stringCode.append("\t }");

    }

    private void generateMethodHeader(JmmNode methodNode, Symbol method) {
        StringBuilder methodArgs = new StringBuilder();
        StringBuilder methodType = new StringBuilder();

        for (int i = 0; i < methodNode.getNumChildren(); i++) {
            JmmNode child =  methodNode.getChildren().get(i);
            if (child.getKind()=="Identifier" && child.toString()=="args") {
                methodArgs.append("");
                this.nParams++;
            }
        }

        stringCode.append("\t.method public " + method.getName() + "(" + methodArgs.toString() + ")." + this.getType(method.getType())+ " { \n");
    }


    private void generateMethodBody(JmmNode method, Symbol symbolMethod) {
        for (int i = 0; i < method.getNumChildren(); i++) {

            JmmNode node = method.getChildren().get(i);

            //Already processed
            //retirar se for argumento ou declaração de variaveis e return

            //If is not any of the others, it is a statement
            generateStatement(node,  symbolMethod, false);

        }
    }

    private void generateStatement(JmmNode node, Symbol symbolMethod, boolean insideIfOrWhile) {
        if (node.getKind().equals("TwoPartExpression")) {


        } else if (node.getKind().equals("TwoPartExpression")) {


        } else if (node.getKind().equals("While")) {


        } else if (node.getKind().equals("If")) {

        }
        else if (node.getKind().equals("StatementBlock")) {
            for (int i = 0; i < node.getNumChildren(); i++)
                generateStatement((SimpleNode) node.getChildren().get(i), symbolMethod, insideIfOrWhile);
        }

        //If it is not any of the others it is an expression
        generateExpression(node,  symbolMethod);
    }


    private void generateExpression(JmmNode node, Symbol symbolMethod) {

    }


   /* private void generateGlobalVar(ASTVarDeclaration var) {

        if (var.jjtGetChild(0) instanceof ASTType) {
            ASTType nodeType = (ASTType) var.jjtGetChild(0);
            printWriterFile.println(".field private " + var.name + " " + getType(nodeType));
        }
    }*/

    private String getType(Type nodeType) {

        /*if (nodeType.isArray)
            return "[I";*/

        switch (nodeType.getName()) {
            case "int":
                return "i32";
            case "String":
                return "Ljava/lang/String";
            case "boolean":
                return "Z";
            case "void":
                return "V";
        }

        return "L" + nodeType.getName() + ";";
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


    @Override
    public void setDefaultVisit(BiFunction method) {

    }

    @Override
    public void addVisit(String kind, BiFunction method) {

    }
}
