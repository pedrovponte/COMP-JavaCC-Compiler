import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

//import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmVisitor;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.examples.ExamplePostorderVisitor;
import pt.up.fe.specs.util.SpecsCheck;

public class visitorExp extends PostorderJmmVisitor<Boolean, String> {

    private StringBuilder stringCode;

    public visitorExp(String varNodeKind) {
        addVisit(varNodeKind, this::printId); // Method reference
    }

    private String printId(JmmNode node, Boolean dummy) {
        /*int registerUsed = 1;
        JmmNode node1 = node.getChildren().get(0);
        JmmNode node2 = node.getChildren().get(1);

        if(node1.getKind().equals("Identifier")){
            String type = getNodeType(node1);
            stringCode.append(node1.get("name") + "." + getType(type) + " " + op + ".i32 " );
        }
        else{
           // generateExpression(node1, methodname);
        }
        if(node2.getKind().equals("Identifier")){
            String type = getNodeType(node2);
            stringCode.append(node2.get("name") + "." + getType(type) + ";\n" )  ;
        }
        else{
            aux.append("t"+registerUsed);
            stringCode.append("\n\t\tt").append(registerUsed).append(".i32").append(" :=").append(".i32 ");
          //  generateExpression(node2, methodname);
        }*/
        return "";


    }



}
