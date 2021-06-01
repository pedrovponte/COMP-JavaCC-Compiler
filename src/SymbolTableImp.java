import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolTableImp implements SymbolTable {
    private List<String> imports;
    private String className;
    private String superName;
    private List<Symbol> fields;
    private List<Symbol> methods;
    private HashMap<String, ArrayList<Symbol>> parameters;
    private HashMap<String, ArrayList<Symbol>> localVariables;
    private HashMap<Symbol, Boolean> classVarsInit;
    private HashMap<String, HashMap<Symbol, Boolean>> methodVarsInit;

    public SymbolTableImp() {
        this.imports = new ArrayList<>();
        this.className = null;
        this.superName = null;
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.parameters = new HashMap<>(); // methodName -> array with parameters
        this.localVariables = new HashMap<>(); // methodName -> array with parameters
        this.classVarsInit = new HashMap<>();
        this.methodVarsInit = new HashMap<>();
    }

    @Override
    public List<String> getImports() {
        return this.imports;
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public String getSuper() {
        return this.superName;
    }

    @Override
    public List<Symbol> getFields() {
        return this.fields;
    }

    @Override
    public List<String> getMethods() {
        List<String> methodsNames = new ArrayList<String>();
        for (int i = 0; i < this.methods.size(); i++) {
            methodsNames.add(this.methods.get(i).getName());
        }
        return methodsNames;
    }

    public List<Symbol> getActualMethods() {
       return methods;
    }

    @Override
    public Type getReturnType(String methodName) {
        for(int i = 0; i < this.methods.size(); i++) {
            if(this.methods.get(i).getName().equals(methodName)) {
                return this.methods.get(i).getType();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getParameters(String methodName) {
        for(String key : this.parameters.keySet()) {
            if(key.equals(methodName)) {
                return this.parameters.get(key);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        for(String key : this.localVariables.keySet()) {
            if(key.equals(methodName)) {
                return this.localVariables.get(key);
            }
        }
        return null;
    }

    public HashMap<Symbol, Boolean> getClassVarsInit() {
        return this.classVarsInit;
    }

    public HashMap<Symbol, Boolean> getMethodVarsInit(String methodName) {
        for(String key : this.methodVarsInit.keySet()) {
            if(key.equals(methodName)) {
                return this.methodVarsInit.get(key);
            }
        }
        return null;
    }

    public List<String> getMethodVarsInitStr(String methodName) {
        List<String> ret = new ArrayList<>();
        for(String key : this.methodVarsInit.keySet()) {
            if(key.equals(methodName)) {
                for(Symbol key2 :this.methodVarsInit.get(key).keySet())
                    ret.add(key2.getName());
            }

        }
        return ret;
    }


    public void addImport(String importName) {
        this.imports.add(importName);
    }

    public void addClassName(String className) {
        this.className = className;
    }

    public void addSuper(String superName) {
        this.superName = superName;
    }

    public void addField(Type type, String name) {
        Symbol symbol = new Symbol(type, name);
        this.fields.add(symbol);
        this.classVarsInit.put(symbol, false);
    }

    public void addMethod(Type type, String name) {
        Symbol symbol = new Symbol(type, name);
        this.methods.add(symbol);
        HashMap<Symbol, Boolean> a = new HashMap<>();
        this.methodVarsInit.put(name, a);
    }

    public void addParameters(String methodName, String name, Type type) {
        Symbol symbol = new Symbol(type, name);
        if(this.parameters.containsKey(methodName)) {
            if(!this.parameters.get(methodName).contains(symbol)) {
                this.parameters.get(methodName).add(symbol);
            }
        }
        else {
            ArrayList<Symbol> a = new ArrayList<Symbol>();
            a.add(symbol);
            this.parameters.put(methodName, a);
        }

        this.methodVarsInit.get(methodName).put(symbol, true);
    }

    public void addLocalVariables(String methodName, String name, Type type) {
        Symbol symbol = new Symbol(type, name);
        if(this.localVariables.containsKey(methodName)) {
            if(!this.localVariables.get(methodName).contains(symbol)) {
                this.localVariables.get(methodName).add(symbol);
            }
        }
        else {
            ArrayList<Symbol> a = new ArrayList<Symbol>();
            a.add(symbol);
            this.localVariables.put(methodName, a);
        }

        this.methodVarsInit.get(methodName).put(symbol, false);
    }

    public void changeInitClass(Symbol var) {
        classVarsInit.replace(var, true);
    }

    public void changeInitMethod(String methodName, Symbol var) {
        methodVarsInit.get(methodName).replace(var, true);
    }


}
