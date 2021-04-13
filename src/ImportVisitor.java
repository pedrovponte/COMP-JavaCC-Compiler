import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.List;
import java.util.stream.Collectors;

public class ImportVisitor extends AJmmVisitor<String, String> {
    private SymbolTableImp symbolTable;
    public ImportVisitor(String identifierType, SymbolTableImp symbolTable) {
        this.symbolTable = symbolTable;
        addVisit(identifierType, this::dealWithIdentifier); // Method reference
        setDefaultVisit(this::defaultVisit); // Method reference
    }

    public String dealWithIdentifier(JmmNode node, String space) {
        List<JmmNode> children = node.getChildren();
        for(int i = 0; i < children.size(); i++) {
            if(children.get(i).getKind().equals("Identifier")) {
                symbolTable.addImport(children.get(i).get("name"));
                return space + "IMPORTNAME: " + children.get(i).get("name") + "\n";
            }
        }
        return defaultVisit(node, space);
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
}
