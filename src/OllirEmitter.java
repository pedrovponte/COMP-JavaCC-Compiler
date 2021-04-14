

    import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

//import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
    import pt.up.fe.comp.jmm.analysis.table.Symbol;
    import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

    public class OllirEmitter {

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


        private PrintWriter createOutputFile(String className) {
            try {
                File dir = new File("./");
                if (!dir.exists())
                    dir.mkdirs();

                File file = new File(dir + "/" + className + ".j");
                if (!file.exists())
                    file.createNewFile();

                return new PrintWriter(file);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return null;
        }

        public void generate(SimpleNode node) {

            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                if (node.jjtGetChild(i) instanceof ASTClassDeclaration) {
                    ASTClassDeclaration classNode = (ASTClassDeclaration) node.jjtGetChild(i);

                    this.printWriterFile = createOutputFile(classNode.getKind());
                    this.generateClass(classNode);
                    this.printWriterFile.close();

                }
            }
        }

        private void generateClass(ASTClassDeclaration classNode) {
            this.localVars = 0;
            this.printWriterFile.println(".class public " + classNode.name);

            if (classNode.ext != null)
                this.printWriterFile.println(".super " + classNode.ext);
            else
                this.printWriterFile.println(".super java/lang/Object");

            Symbol symbolClass = (Symbol) this.symbolTable.get(classNode.name);

            generateClassVariables(classNode);
            generateExtend(classNode);
            generateMethods(classNode, symbolClass);
        }

        private void generateClassVariables(SimpleNode node) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode child = (SimpleNode) node.jjtGetChild(i);
                if (child instanceof ASTVarDeclaration) {
                    generateGlobalVar((ASTVarDeclaration) child);
                }
            }
        }

        private void generateGlobalVar(ASTVarDeclaration var) {

            if (var.jjtGetChild(0) instanceof ASTType) {
                ASTType nodeType = (ASTType) var.jjtGetChild(0);
                printWriterFile.println(".field private " + var.name + " " + getType(nodeType));
            }
        }




    }
