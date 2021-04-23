import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

//import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmVisitor;

public class OllirEmitter implements JmmVisitor {

    private PrintWriter printWriterFile;
    private  SymbolTable symbolTable;
    private int loopCounter;
    private int localVars;
    private int nParams;
    private int maxStack;
    private int totalStack;
    private StringBuilder methodCode;
    private StringBuilder bodyCode;


    public OllirEmitter(SymbolTable table) {
        symbolTable = table;
        this.loopCounter = 0;
        this.localVars = 0;
        this.nParams = 0;
        this.maxStack = 0;
        this.totalStack = 0;
        this.methodCode = new StringBuilder();
        this.bodyCode = new StringBuilder();
    }

    public String getMethodCode() {
        return methodCode.toString();
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
        methodCode.append( ".construct public " + classNode.toString() + "().V");
    }



    /*
    public void run(JmmNode node) {
        System.out.println("MethodCode: " + methodCode);
        String result = null;
        for (int i = 0; i < node.getNumChildren(); i++) {
            System.out.println("NODE \n");
            methodCode.append("A");
           if (node.getChildren().get(i) instanceof ASTClassDeclaration) {
               ASTClassDeclaration classNode = (ASTClassDeclaration) node.getChildren().get(i);
               System.out.println("Declaration \n");
           }
            if (node.getChildren().get(i) instanceof ASTBlockStatement) {
                ASTClassDeclaration classNode = (ASTClassDeclaration) node.getChildren().get(i);
                System.out.println("Statement \n");
            }
            if (node.getChildren().get(i) instanceof ASTExpression) {
                ASTClassDeclaration classNode = (ASTClassDeclaration) node.getChildren().get(i);
                System.out.println("2 \n");
            }
            if (node.getChildren().get(i) instanceof ASTArgs) {
                ASTClassDeclaration classNode = (ASTClassDeclaration) node.getChildren().get(i);
                System.out.println("3 \n");
            }
            if (node.getChildren().get(i) instanceof ASTVarDeclaration) {
                ASTClassDeclaration classNode = (ASTClassDeclaration) node.getChildren().get(i);
                System.out.println("4 \n");
            }
            if (node.getChildren().get(i) instanceof ASTequal) {

                ASTClassDeclaration classNode = (ASTClassDeclaration) node.getChildren().get(i);
                System.out.println("5 \n");
            }
            if (node.getChildren().get(i) instanceof ASTIf) {
                ASTClassDeclaration classNode = (ASTClassDeclaration) node.getChildren().get(i);
                System.out.println("6 \n");
            }

            this.run(node.getChildren().get(i));
        }

    }


    public String getMethodCode() {
        return methodCode.toString();
    }
    */


    @Override
    public void setDefaultVisit(BiFunction method) {

    }

    @Override
    public void addVisit(String kind, BiFunction method) {

    }
}
