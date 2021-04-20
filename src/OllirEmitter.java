

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




        public  OllirEmitter(SymbolTable table) {
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

      /*      System.out.println("NODE: \n\n");
            System.out.println("String: "  +node.toString());
            System.out.println("Type: " + node.getClass().getComponentType());
            System.out.println("Kind: " + node.getKind());
            System.out.println("Class: " + node.getClass());
            System.out.println("Attributes: " + node.getAttributes());
            System.out.println("Children: " + node.getChildren());*/


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
            methodCode.append("class " + symbolTable.getClassName() + "{ \n");
            methodCode.append(".construct" + symbolTable.getClassName() + ".V" );
            generateClassVariables(classNode);
           // generateExtend(classNode);
           // generateMethods(classNode, symbolClass);
        }

        private void generateClassVariables(JmmNode node) {
            for (int i = 1; i < node.getChildren().size(); i++) {
                JmmNode child = node.getChildren().get(i);
                methodCode.append(child.toString());
            }
        }
        /*

        private void generateGlobalVar(ASTVarDeclaration var) {

            if (var.jjtGetChild(0) instanceof ASTType) {
                ASTType nodeType = (ASTType) var.jjtGetChild(0);
                printWriterFile.println(".field private " + var.name + " " + getType(nodeType));
            }
        }

        private String getType(ASTType nodeType) {

            if (nodeType.isArray)
                return "[I";

            switch (nodeType.type) {
                case "int":
                    return "I";
                case "String":
                    return "Ljava/lang/String";
                case "boolean":
                    return "Z";
                case "void":
                    return "V";
            }

            return "L" + nodeType.type + ";";
        }
*/







        @Override
        public void setDefaultVisit(BiFunction method) {

        }

        @Override
        public void addVisit(String kind, BiFunction method) {

        }
    }
