import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassVisitor extends AJmmVisitor<String, String> {
    private SymbolTableImp symbolTable;

    public ClassVisitor(String identifierType, SymbolTableImp symbolTable) {
        this.symbolTable = symbolTable;
        addVisit(identifierType, this::dealWithClass); // Method reference
        setDefaultVisit(this::defaultVisit); // Method reference
    }


    public String dealWithClass(JmmNode node, String space) {
        List<JmmNode> children = node.getChildren();
        for(int i = 0; i < children.size(); i++) {
            JmmNode child = children.get(i);
            if(child.getKind().equals("Identifier")) {
                symbolTable.addClassName(child.get("name"));
            }
            if(child.getKind().equals("Extends")) {
                symbolTable.addSuper(child.getChildren().get(0).get("name"));

            }
            if(child.getKind().equals("Main")) {
                symbolTable.addMethod(new Type("void", false), "main");
                symbolTable.addParameters("main", child.getChildren().get(0).get("name"), new Type("String", true));

                for(int j = 1; j < child.getNumChildren(); j++) {
                    if(child.getChildren().get(j).getKind().equals("LocalVariableDeclaration")) {
                        JmmNode localVar = child.getChildren().get(j);

                        if(localVar.getChildren().get(0).getKind().equals("Identifier")) {
                            symbolTable.addLocalVariables("main", localVar.getChildren().get(1).get("name"), new Type(localVar.getChildren().get(0).get("name"), false));
                        }
                        else {
                            String nameVar = localVar.getChildren().get(0).getKind();
                            if(nameVar.equals("int[]")) {
                                symbolTable.addLocalVariables("main", localVar.getChildren().get(1).get("name"), new Type("int", true));
                            }
                            else {
                                symbolTable.addLocalVariables("main", localVar.getChildren().get(1).get("name"), new Type(nameVar, false));
                            }
                        }
                    }
                }
            }
            if(child.getKind().equals("VarDeclaration")) {
                if(child.getChildren().get(0).getKind().equals("Identifier")) {
                    symbolTable.addField(new Type(child.getChildren().get(0).get("name"), false), child.getChildren().get(1).get("name"));
                }
                else {
                    String name = child.getChildren().get(0).getKind();
                    if(name.equals("int[]")) {
                        symbolTable.addField(new Type("int", true), child.getChildren().get(1).get("name"));
                    }
                    else {
                        symbolTable.addField(new Type(name, false), child.getChildren().get(1).get("name"));
                    }
                }
            }
            if(child.getKind().equals("Method")) {
                if(child.getChildren().get(0).getKind().equals("Identifier")) {
                    symbolTable.addMethod(new Type(child.getChildren().get(0).get("name"), false), child.getChildren().get(1).get("name"));
                }
                else {
                    String name = child.getChildren().get(0).getKind();
                    if(name.equals("int[]")) {
                        symbolTable.addMethod(new Type("int", true), child.getChildren().get(1).get("name"));
                    }
                    else {
                        symbolTable.addMethod(new Type(name, false), child.getChildren().get(1).get("name"));
                    }
                }
                for(int j = 2; j < child.getNumChildren(); j++) {
                    if(child.getChildren().get(j).getKind().equals("Args")) {
                        JmmNode args = child.getChildren().get(j);

                        for(int k = 0; k < args.getNumChildren(); k++) {
                            JmmNode varDecl = args.getChildren().get(k);

                            if(varDecl.getChildren().get(0).getKind().equals("Identifier")) {
                                symbolTable.addParameters(child.getChildren().get(1).get("name"), varDecl.getChildren().get(1).get("name"), new Type(varDecl.getChildren().get(0).get("name"), false));
                            }
                            else {
                                String namePar = varDecl.getChildren().get(0).getKind();
                                if(namePar.equals("int[]")) {
                                    symbolTable.addParameters(child.getChildren().get(1).get("name"), varDecl.getChildren().get(1).get("name"), new Type("int", true));
                                }
                                else {
                                    symbolTable.addParameters(child.getChildren().get(1).get("name"), varDecl.getChildren().get(1).get("name"), new Type(namePar, false));
                                }
                            }
                        }

                    }
                    if(child.getChildren().get(j).getKind().equals("LocalVariableDeclaration")) {
                        JmmNode localVar = child.getChildren().get(j);

                        if(localVar.getChildren().get(0).getKind().equals("Identifier")) {
                            symbolTable.addLocalVariables(child.getChildren().get(1).get("name"), localVar.getChildren().get(1).get("name"), new Type(localVar.getChildren().get(0).get("name"), false));
                        }
                        else {
                            String nameVar = localVar.getChildren().get(0).getKind();
                            if(nameVar.equals("int[]")) {
                                symbolTable.addLocalVariables(child.getChildren().get(1).get("name"), localVar.getChildren().get(1).get("name"), new Type("int", true));
                            }
                            else {
                                symbolTable.addLocalVariables(child.getChildren().get(1).get("name"), localVar.getChildren().get(1).get("name"), new Type(nameVar, false));
                            }
                        }
                    }
                }
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
