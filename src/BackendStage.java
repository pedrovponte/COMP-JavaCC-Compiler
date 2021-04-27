import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class BackendStage implements JasminBackend {
    StringBuilder jasmin = new StringBuilder();

    public void addType(Type type){
        switch (type.getTypeOfElement()){
            case INT32:
                jasmin.append("I");
                break;
            case BOOLEAN:
                jasmin.append("Z");
                break;
            case ARRAYREF:
                ArrayType arrayType = (ArrayType) type;
                ElementType elementType =  arrayType.getTypeOfElements();
                if (elementType == ElementType.BOOLEAN)
                {
                    jasmin.append("[Z");
                }
                else if (elementType == ElementType.INT32)
                {
                    jasmin.append("[I");
                }
                break;
            case OBJECTREF:
                //jasmin.append("[LMyClass;");
                break;
            case CLASS:
                ClassType classType = (ClassType) type;
                jasmin.append("[" + classType.getName() + ";");
                //jasmin.append("");
                break;
            case THIS:
                jasmin.append(" java/lang/Object");
                break;
            case STRING:
                jasmin.append("Ljava/lang/String;");
                break;
            case VOID:
                jasmin.append("V");
                break;
        }
    }

    public void MethodOperations(Method method){
        for (var inst: method.getInstructions()) {
            switch (inst.getInstType()) {
                case ASSIGN:
                    AssignInstruction assignInstruction = (AssignInstruction) inst;
                    jasmin.append("\n");
                    Operand dest = (Operand) assignInstruction.getDest();
                    //jasmin.append("\taload_"+dest.getParamId()+"\n");

                    Instruction rhs = assignInstruction.getRhs();
                    //rhs.show();

                    if (rhs instanceof SingleOpInstruction){
                        jasmin.append("\taload_"+dest.getParamId()+"\n");
                        jasmin.append("\tbipush ");
                        SingleOpInstruction singleOperand = ((SingleOpInstruction) rhs);
                        if (singleOperand.getSingleOperand().isLiteral()) {
                            jasmin.append(((LiteralElement) singleOperand.getSingleOperand()).getLiteral());
                        } else {
                            Operand o1 = (Operand)singleOperand.getSingleOperand();
                            System.out.println("\t" + singleOperand.getInstType() + " Operand: " + o1.getName() + " " + o1.getType());
                        }
                    }
                    if (rhs instanceof BinaryOpInstruction){
                        OperationType operation = ((UnaryOpInstruction) rhs).getUnaryOperation().getOpType();
                        switch (operation){
                            case ADD:
                                break;
                            case SUB:
                                break;
                            case DIV:
                                break;
                            case MUL:
                                break;
                        }
                    }

                    //System.out.println(dest.getParamId());

                    break;
                case CALL:
                    break;
                case GOTO:
                    break;
                case BRANCH:
                    break;
                case RETURN:
                    break;
                case PUTFIELD:
                    break;
                case GETFIELD:
                    break;
                case UNARYOPER:
                    break;
                case BINARYOPER:
                    break;
                case NOPER:
                    break;
            }
        }
    }

    public void addAccessModifier(AccessModifiers modifier){
        switch (modifier){
            case PUBLIC:
                jasmin.append(" public ");
                break;
            case PRIVATE:
                jasmin.append(" private ");
                break;
            case PROTECTED:
                jasmin.append(" protected ");
                break;
            case DEFAULT:
                //jasmin.append(" default ");
                break;
        }
    }

    public void methodLine(Method method)
    {
        // Deals with Construct
        if (method.isConstructMethod()){
            jasmin.append(".method ");
            addAccessModifier(method.getMethodAccessModifier());
            jasmin.append(method.getMethodName() + "()");
            addType(method.getReturnType());
            jasmin.append("\n\taload_0\n");
            jasmin.append("\tinvokenonvirtual java/lang/Object/<init>()V\n");
            jasmin.append("\treturn\n");
            return;
        }

        jasmin.append(".method");
        //System.out.println(method.getMethodAccessModifier());

        addAccessModifier(method.getMethodAccessModifier());

        if (method.isStaticMethod())
            jasmin.append("static");
        else if (method.isFinalMethod())
            jasmin.append("final");

        jasmin.append(method.getMethodName());

        if(method.getParams().size()>0){
            jasmin.append("(");
            for (var param: method.getParams()){
                //if (param.)
                addType(param.getType());
                jasmin.append(";");
            }
            // Remove last ;
            // jasmin.deleteCharAt(jasmin.toString().length()-1);
            jasmin.append(")");
        }

        addType(method.getReturnType());

        MethodOperations(method);

        jasmin.append("\n.end method\n");

    }

    public void AddClassFields(ClassUnit ollirClass){
        for (Field field: ollirClass.getFields()){
            jasmin.append(".field");

            addAccessModifier(field.getFieldAccessModifier());

            if (field.isStaticField())
                jasmin.append("static");
            else if (field.isFinalField())
                jasmin.append("final");

            jasmin.append(field.getFieldName()+" ");
            addType(field.getFieldType());
            jasmin.append("\n");
        }
    }

    public void addClass(ClassUnit ollirClass){
        jasmin.append(".class ");
        addAccessModifier(ollirClass.getClassAccessModifier());
        jasmin.append(ollirClass.getClassName() + "\n");
        jasmin.append(".super java/lang/Object\n\n");
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();

        addClass(ollirClass);

        if (ollirClass.getNumFields()>0)
            AddClassFields(ollirClass);

        try {

            // Example of what you can do with the OLLIR class
            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
            ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method
            //ollirClass.show(); // print to console main information about the input OLLIR

            for (var method: ollirClass.getMethods()){
                methodLine(method);
            }

            System.out.println("\nJASMIN");
            System.out.println(jasmin.toString());

            // Convert the OLLIR to a String containing the equivalent Jasmin code
            String jasminCode = ""; // Convert node ...

            // More reports from this stage
            List<Report> reports = new ArrayList<>();

            return new JasminResult(ollirResult, jasminCode, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(ollirClass.getClassName(), null,
                    Arrays.asList(Report.newError(Stage.GENERATION, -1, -1, "Exception during Jasmin generation", e)));
        }

    }

}
