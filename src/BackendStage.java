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
    StringBuilder finalJasmin = new StringBuilder();
    Integer numberLimit = 0;
    Integer currentNumber = 2;
    Integer lthOperation = 1;
    Boolean nonAssign = true;

    public void setNumberLimit() {
        if(this.currentNumber > this.numberLimit)
            this.numberLimit = this.currentNumber;
        this.currentNumber = 0;
    }

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
                else if (elementType == ElementType.STRING)
                {
                    jasmin.append("[Ljava/lang/String;");
                }
                break;
            case OBJECTREF:
                ClassType classType = (ClassType) type;
                jasmin.append(classType.getName());
                break;
            case CLASS:
                ClassType classType1 = (ClassType) type;
                jasmin.append(classType1.getName());
                //jasmin.append("");
                break;
            case THIS:
                //jasmin.append(" java/lang/Object");
                jasmin.append("this");
                break;
            case STRING:
                jasmin.append("Ljava/lang/String;");
                break;
            case VOID:
                jasmin.append("V");
                break;
        }
    }

    public void LiteralValues(LiteralElement literalElement){
        switch (literalElement.getLiteral()){
            case("-1"):
                jasmin.append("\ticonst_m1 \n");
                break;
            case ("0"):
                jasmin.append("\ticonst_0 \n");
                break;
            case ("1"):
                jasmin.append("\ticonst_1 \n");
                break;
            case ("2"):
                jasmin.append("\ticonst_2 \n");
                break;
            case("3"):
                jasmin.append("\ticonst_3 \n");
                break;
            case("4"):
                jasmin.append("\ticonst_4 \n");
                break;
            case ("5"):
                jasmin.append("\ticonst_5 \n");
                break;
            default:
                if (Integer.parseInt(literalElement.getLiteral())>=-128 && Integer.parseInt(literalElement.getLiteral())<=127)
                    jasmin.append("\tbipush " + literalElement.getLiteral() + "\n");
                else if (Integer.parseInt(literalElement.getLiteral())>=-32768 && Integer.parseInt(literalElement.getLiteral())<=32767)
                    jasmin.append("\tsipush " + literalElement.getLiteral() + "\n");
                else if (Integer.parseInt(literalElement.getLiteral())>=-2147483648 && Integer.parseInt(literalElement.getLiteral())<=2147483647)
                    jasmin.append("\tldc " + literalElement.getLiteral() + "\n");
                break;
        }
    }

    public void GetInstructions(Instruction inst, Method method){
        if (!method.getLabels(inst).isEmpty()){
            for (var label: method.getLabels(inst))
            {
                jasmin.append(label + ":\n");
            }
        }
        switch (inst.getInstType()) {
            case ASSIGN:
                nonAssign=false;
                AssignInstruction assignInstruction = (AssignInstruction) inst;

                Instruction rhs = assignInstruction.getRhs();

                Operand dest = (Operand) assignInstruction.getDest();

                if (dest instanceof ArrayOperand){
                    ArrayOperand destArray = (ArrayOperand) assignInstruction.getDest();

                    if (OllirAccesser.getVarTable(method).get(destArray.getName()).getVirtualReg()<4)
                        jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(destArray.getName()).getVirtualReg() + "\n");
                    else
                        jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(destArray.getName()).getVirtualReg() + "\n");
                    currentNumber++;

                    for (var index: destArray.getIndexOperands())
                    {
                        if (index.isLiteral())
                        {
                            LiteralElement literalElement = (LiteralElement) index;
                            LiteralValues(literalElement);
                        }
                        else{
                            Operand newOperand = (Operand) index;

                            if (newOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || newOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                            {
                                if (OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg()<4)
                                    jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                else
                                    jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                            }
                            else
                            {
                                if (OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg()<4)
                                    jasmin.append("\tiload_" + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                else
                                    jasmin.append("\tiload " + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                            }
                        }
                        currentNumber++;
                    }

                }

                GetInstructions(rhs,method);

                Descriptor descriptor = OllirAccesser.getVarTable(method).get(dest.getName());

                if (dest.getType().getTypeOfElement()==ElementType.OBJECTREF || dest.getType().getTypeOfElement()==ElementType.ARRAYREF){
                    if (descriptor.getVirtualReg()<4)
                        jasmin.append("\tastore_" + descriptor.getVirtualReg() + "\n");
                    else
                        jasmin.append("\tastore " + descriptor.getVirtualReg() + "\n");
                }
                else if (dest instanceof ArrayOperand){
                    jasmin.append("\tiastore\n");
                }
                else{
                    if (descriptor.getVirtualReg()<4)
                        jasmin.append("\tistore_" + descriptor.getVirtualReg() + "\n");
                    else
                        jasmin.append("\tistore " + descriptor.getVirtualReg() + "\n");
                }

                currentNumber++;
                setNumberLimit();

                break;
            case CALL:
                CallInstruction callInstruction = (CallInstruction) inst;

                CallType callType = callInstruction.getInvocationType();

                switch (callType){
                    case arraylength:
                        Operand arrayLengthFirst = (Operand) callInstruction.getFirstArg();

                        if (OllirAccesser.getVarTable(method).get(arrayLengthFirst.getName()).getVirtualReg()<4)
                            jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(arrayLengthFirst.getName()).getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(arrayLengthFirst.getName()).getVirtualReg() + "\n");
                        currentNumber++;
                        jasmin.append("\tarraylength\n");
                        break;

                    case NEW:
                        CallInstruction operandNew = (CallInstruction) inst;

                        Operand newFirst = (Operand) callInstruction.getFirstArg();

                        if (newFirst.getType() instanceof ArrayType)
                        {
                            for (Element operand : callInstruction.getListOfOperands())
                            {
                                if (operand.isLiteral())
                                {
                                    LiteralElement literalElement = (LiteralElement) operand;
                                    LiteralValues(literalElement);
                                }
                                else{
                                    Operand newOperand = (Operand) operand;

                                    if (newOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || newOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                                    {
                                        if (OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg()<4)
                                            jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                        else
                                            jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                    }
                                    else
                                    {
                                        if (OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg()<4)
                                            jasmin.append("\tiload_" + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                        else
                                            jasmin.append("\tiload " + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                    }
                                }
                                currentNumber++;
                            }
                            /*if ((operandNew.getReturnType()).getTypeOfElement()==ElementType.INT32)
                            {*/
                            jasmin.append("\tnewarray int");
                            currentNumber++;
                            jasmin.append("\n");
                            //}
                            /*else if(callInstruction.getListOfOperands().size() > 1)
                            {
                                jasmin.append("\tmultianewarray [");
                                addType(callInstruction.getListOfOperands().get(0).getType());
                                jasmin.append(" "+callInstruction.getListOfOperands().size()+"\n");
                            }
                            else if (operandNew.getReturnType().getTypeOfElement()==ElementType.ARRAYREF){
                                //var classTypeNewArray = (ClassType) operandNew.getReturnType();

                                Operand newReturn = (Operand) callInstruction.getFirstArg();

                                jasmin.append("\tanewarray ");
                                jasmin.append(newReturn.getName());
                                jasmin.append("\n");
                            }*/
                        }
                        else {
                            ClassType classTypeNew = (ClassType) callInstruction.getReturnType();

                            jasmin.append("\tnew " + classTypeNew.getName() + "\n");
                            jasmin.append("\tdup\n");
                            currentNumber+=2;

                            jasmin.append("\tinvokespecial " + classTypeNew.getName() + ".<init>()V\n");
                            //numberLimit++;
                        }
                        break;

                    case invokevirtual:

                        Operand callField1 = (Operand) callInstruction.getFirstArg();

                        if (OllirAccesser.getVarTable(method).get(callField1.getName()).getVirtualReg()<4)
                            jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(callField1.getName()).getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(callField1.getName()).getVirtualReg() + "\n");

                        currentNumber++;

                        ArrayList<Element> elements = callInstruction.getListOfOperands();

                        for (Element element : elements){
                            if (element.isLiteral()){
                                LiteralElement literalElement = (LiteralElement) element;
                                LiteralValues(literalElement);
                            }
                            else {
                                Operand returnOperand = (Operand) element;

                                Descriptor elementDescriptor = OllirAccesser.getVarTable(method).get(returnOperand.getName());

                                //jasmin.append("\tiload_" + returnDescriptor.getVirtualReg() + "\n");
                                if (element.getType().getTypeOfElement()==ElementType.OBJECTREF || element.getType().getTypeOfElement()==ElementType.ARRAYREF)
                                {
                                    if (elementDescriptor.getVirtualReg()<4)
                                        jasmin.append("\taload_" + elementDescriptor.getVirtualReg() + "\n");
                                    else
                                        jasmin.append("\taload " + elementDescriptor.getVirtualReg() + "\n");
                                }
                                else
                                {
                                    if (elementDescriptor.getVirtualReg()<4)
                                        jasmin.append("\tiload_" + elementDescriptor.getVirtualReg() + "\n");
                                    else
                                        jasmin.append("\tiload " + elementDescriptor.getVirtualReg() + "\n");
                                }

                            }
                            currentNumber++;
                        }
                        LiteralElement callField2 = (LiteralElement) callInstruction.getSecondArg();
                        ClassType classTypeVirtual = (ClassType) callInstruction.getFirstArg().getType();
                        jasmin.append("\tinvokevirtual " + classTypeVirtual.getName() + "." + callField2.getLiteral().substring( 1, callField2.getLiteral().length() - 1 ) + "(");

                        //Boolean first = false;
                        for (Element element : elements){
                            /*if (first == true)
                            {
                                jasmin.append(" ");
                            }*/
                            addType(element.getType());
                            //first = true;
                        }
                        jasmin.append(")");

                        addType(((CallInstruction) inst).getReturnType());
                        if (((CallInstruction) inst).getReturnType().getTypeOfElement()!=ElementType.VOID)
                        {
                            if (nonAssign)
                                jasmin.append("\tpop\n");
                        }
                        jasmin.append("\n");

                        break;
                    //case invokeinterface:
                    //break;
                    case invokespecial:
                        //Operand callFieldSpecial = (Operand) callInstruction.getFirstArg();

                        /*if (OllirAccesser.getVarTable(method).get(callFieldSpecial.getName()).getVirtualReg()<4)
                            jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(callFieldSpecial.getName()).getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(callFieldSpecial.getName()).getVirtualReg() + "\n");
                        ArrayList<Element> elementsSpecial = callInstruction.getListOfOperands();

                        for (Element element : elementsSpecial){
                            if (element.isLiteral()){
                                LiteralElement literalElement = (LiteralElement) element;
                                LiteralValues(literalElement);
                            }
                            else {
                                Operand returnOperand = (Operand) element;

                                Descriptor elementDescriptor = OllirAccesser.getVarTable(method).get(returnOperand.getName());

                                //jasmin.append("\tiload_" + returnDescriptor.getVirtualReg() + "\n");
                                if (element.getType().getTypeOfElement()==ElementType.OBJECTREF || element.getType().getTypeOfElement()==ElementType.ARRAYREF)
                                {
                                    if (elementDescriptor.getVirtualReg()<4)
                                        jasmin.append("\taload_" + elementDescriptor.getVirtualReg() + "\n");
                                    else
                                        jasmin.append("\taload " + elementDescriptor.getVirtualReg() + "\n");
                                }
                                else
                                {
                                    if (elementDescriptor.getVirtualReg()<4)
                                        jasmin.append("\tiload_" + elementDescriptor.getVirtualReg() + "\n");
                                    else
                                        jasmin.append("\tiload " + elementDescriptor.getVirtualReg() + "\n");
                                }
                            }
                        }
                        ClassType classTypeVirtual1 = (ClassType) callInstruction.getFirstArg().getType();
                        jasmin.append("\tinvokespecial " + classTypeVirtual1.getName() + ".");
                        LiteralElement callField2Special = (LiteralElement) callInstruction.getSecondArg();
                        jasmin.append( callField2Special.getLiteral().substring( 1, callField2Special.getLiteral().length() - 1 ) + "()");
                        addType(((CallInstruction) inst).getReturnType());
                        jasmin.append("\n");*/
                        return;

                    case invokestatic:
                        Operand callField1Static = (Operand) callInstruction.getFirstArg();

                        //jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(callField1Static.getName()).getVirtualReg() + "\n");

                        ArrayList<Element> elementsStatic = callInstruction.getListOfOperands();

                        for (Element element : elementsStatic){
                            if (element.isLiteral()){
                                LiteralElement literalElement = (LiteralElement) element;
                                LiteralValues(literalElement);
                            }
                            else {
                                Operand returnOperand = (Operand) element;

                                Descriptor elementDescriptor = OllirAccesser.getVarTable(method).get(returnOperand.getName());

                                //jasmin.append("\tiload_" + returnDescriptor.getVirtualReg() + "\n");
                                if (element.getType().getTypeOfElement()==ElementType.OBJECTREF || element.getType().getTypeOfElement()==ElementType.ARRAYREF)
                                {
                                    if (elementDescriptor.getVirtualReg()<4)
                                        jasmin.append("\taload_" + elementDescriptor.getVirtualReg() + "\n");
                                    else
                                        jasmin.append("\taload " + elementDescriptor.getVirtualReg() + "\n");
                                }
                                else if (element instanceof ArrayOperand){
                                    if (elementDescriptor.getVirtualReg()<4)
                                        jasmin.append("\taload_" + elementDescriptor.getVirtualReg() + "\n");
                                    else
                                        jasmin.append("\taload " + elementDescriptor.getVirtualReg() + "\n");
                                }
                                else
                                {
                                    if (elementDescriptor.getVirtualReg()<4)
                                        jasmin.append("\tiload_" + elementDescriptor.getVirtualReg() + "\n");
                                    else
                                        jasmin.append("\tiload " + elementDescriptor.getVirtualReg() + "\n");
                                }
                            }
                            currentNumber++;
                        }
                        LiteralElement callField2Static = (LiteralElement) callInstruction.getSecondArg();
                        jasmin.append("\tinvokestatic " + callField1Static.getName() + "." + callField2Static.getLiteral().substring( 1, callField2Static.getLiteral().length() - 1 ) + "(");
                        currentNumber++;
                        //Boolean first = false;
                        for (Element element : elementsStatic){
                            /*if (first == true)
                            {
                                jasmin.append(" ");
                            }*/
                            addType(element.getType());
                            //first = true;
                        }
                        jasmin.append(")");

                        addType(((CallInstruction) inst).getReturnType());
                        if (((CallInstruction) inst).getReturnType().getTypeOfElement()!=ElementType.VOID)
                        {
                            if (nonAssign)
                                jasmin.append("\tpop\n");
                        }
                        jasmin.append("\n");
                        break;

                    case ldc:
                        break;
                }
                setNumberLimit();
                nonAssign=true;
                break;
            case RETURN:
                ReturnInstruction returnInstruction = (ReturnInstruction) inst;
                ElementType returnValue;

                if (!returnInstruction.hasReturnValue())
                {
                    jasmin.append("\treturn\n");
                    return;
                }

                returnValue = returnInstruction.getOperand().getType().getTypeOfElement();

                Element operand = returnInstruction.getOperand();
                if (operand.isLiteral()){
                    LiteralElement literalElement = (LiteralElement) operand;
                    LiteralValues(literalElement);
                }
                else {
                    Operand returnOperand = (Operand) operand;

                    Descriptor returnDescriptor = OllirAccesser.getVarTable(method).get(returnOperand.getName());

                    //jasmin.append("\tiload_" + returnDescriptor.getVirtualReg() + "\n");
                    if (operand.getType().getTypeOfElement()==ElementType.OBJECTREF || operand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                    {
                        if (returnDescriptor.getVirtualReg()<4)
                            jasmin.append("\taload_" + returnDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + returnDescriptor.getVirtualReg() + "\n");
                    }
                    else
                    {
                        if (returnDescriptor.getVirtualReg()<4)
                            jasmin.append("\tiload_" + returnDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\tiload " + returnDescriptor.getVirtualReg() + "\n");
                    }
                }
                currentNumber++;

                switch (returnValue){
                    case INT32:
                        jasmin.append("\tireturn\n");
                        break;
                    case BOOLEAN:
                        jasmin.append("\tireturn\n");
                        break;
                    case OBJECTREF:
                        jasmin.append("\tareturn\n");
                        break;
                    case ARRAYREF:
                        jasmin.append("\tareturn\n");
                        break;
                }
                nonAssign=true;
                break;
            case PUTFIELD:
                PutFieldInstruction putFieldInstruction = (PutFieldInstruction) inst;

                if (putFieldInstruction.getFirstOperand().isLiteral()){
                    LiteralElement literalElement = (LiteralElement) putFieldInstruction.getFirstOperand();
                    LiteralValues(literalElement);
                }
                else {
                    Operand firstOperand = (Operand) putFieldInstruction.getFirstOperand();

                    Descriptor firstDescriptor = OllirAccesser.getVarTable(method).get(firstOperand.getName());

                    if (firstOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || firstOperand.getType().getTypeOfElement()==ElementType.THIS || firstOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                    {
                        if (firstDescriptor.getVirtualReg()<4)
                            jasmin.append("\taload_" + firstDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + firstDescriptor.getVirtualReg() + "\n");
                    }
                    else
                    {
                        if (firstDescriptor.getVirtualReg()<4)
                            jasmin.append("\tiload_" + firstDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\tiload " + firstDescriptor.getVirtualReg() + "\n");
                    }
                }

                currentNumber++;

                if (putFieldInstruction.getThirdOperand().isLiteral()){
                    LiteralElement literalElement = (LiteralElement) putFieldInstruction.getThirdOperand();
                    LiteralValues(literalElement);
                }
                else {
                    Operand thirdOperand = (Operand) putFieldInstruction.getThirdOperand();

                    Descriptor thirdDescriptor = OllirAccesser.getVarTable(method).get(thirdOperand.getName());

                    if (thirdOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || thirdOperand.getType().getTypeOfElement()==ElementType.THIS || thirdOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                    {
                        if (thirdDescriptor.getVirtualReg()<4)
                            jasmin.append("\taload_" + thirdDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + thirdDescriptor.getVirtualReg() + "\n");
                    }
                    else
                    {
                        if (thirdDescriptor.getVirtualReg()<4)
                            jasmin.append("\tiload_" + thirdDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\tiload " + thirdDescriptor.getVirtualReg() + "\n");
                    }
                }

                currentNumber++;

                jasmin.append("\tputfield ");

                Operand operand1 = (Operand) putFieldInstruction.getFirstOperand();
                Operand operand2 = (Operand) putFieldInstruction.getSecondOperand();

                /*if(operand1.getName()=="this"){
                    jasmin.append(operand1.getName() + "/");
                    jasmin.append(operand2.getName() + " ");
                    addType(putFieldInstruction.getSecondOperand().getType());
                    jasmin.append("\n");
                }
                else{*/
                ClassType class1 = (ClassType) operand1.getType();
                jasmin.append(class1.getName() + "/");
                jasmin.append(operand2.getName() + " ");
                addType(putFieldInstruction.getSecondOperand().getType());
                jasmin.append("\n");
                //}
                setNumberLimit();
                nonAssign=true;
                break;
            case GETFIELD:
                GetFieldInstruction getFieldInstruction = (GetFieldInstruction) inst;

                Operand field1 = (Operand) getFieldInstruction.getFirstOperand();
                Operand field2 = (Operand) getFieldInstruction.getSecondOperand();

                if (OllirAccesser.getVarTable(method).get(field1.getName()).getVirtualReg()<4)
                    jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(field1.getName()).getVirtualReg() + "\n");
                else
                    jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(field1.getName()).getVirtualReg() + "\n");

                currentNumber++;

                jasmin.append("\tgetfield ");

                ClassType classType2 = (ClassType) field1.getType();
                jasmin.append(classType2.getName() + "/");
                jasmin.append(field2.getName()+" ");

                addType(getFieldInstruction.getSecondOperand().getType());
                jasmin.append("\n");
                setNumberLimit();
                nonAssign=true;
                break;
            case UNARYOPER:
                UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) inst;

                OperationType unaryOperationType = unaryOpInstruction.getUnaryOperation().getOpType();

                if (unaryOperationType == OperationType.NOT || unaryOperationType == OperationType.NOTB)
                {
                    if (unaryOpInstruction.getRightOperand().isLiteral()){
                        LiteralElement literalElement = (LiteralElement) unaryOpInstruction.getRightOperand();
                        LiteralValues(literalElement);
                    }
                    else {
                        Operand rightOperand = (Operand) unaryOpInstruction.getRightOperand();

                        Descriptor binaryDescriptor = OllirAccesser.getVarTable(method).get(rightOperand.getName());

                        //jasmin.append("\tiload_" + binaryDescriptor.getVirtualReg() + "\n");

                        if (rightOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || rightOperand.getType().getTypeOfElement()==ElementType.THIS || rightOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                        {
                            if (binaryDescriptor.getVirtualReg()<4)
                                jasmin.append("\taload_" + binaryDescriptor.getVirtualReg() + "\n");
                            else
                                jasmin.append("\taload " + binaryDescriptor.getVirtualReg() + "\n");
                        }
                        else
                        {
                            if (binaryDescriptor.getVirtualReg()<4)
                                jasmin.append("\tiload_" + binaryDescriptor.getVirtualReg() + "\n");
                            else
                                jasmin.append("\tiload " + binaryDescriptor.getVirtualReg() + "\n");
                        }
                    }
                    currentNumber++;
                    jasmin.append("\ticonst_1\n");
                    currentNumber++;
                    jasmin.append("\tixor\n");
                    if(this.currentNumber > this.numberLimit)
                        this.numberLimit = this.currentNumber;
                    currentNumber--;
                }
                nonAssign=true;
                break;
            case BINARYOPER:
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) inst;

                OperationType operationType = binaryOpInstruction.getUnaryOperation().getOpType();

                if (binaryOpInstruction.getLeftOperand().isLiteral()){
                    LiteralElement literalElement = (LiteralElement) binaryOpInstruction.getLeftOperand();
                    LiteralValues(literalElement);
                }
                else {
                    Operand leftOperand = (Operand) binaryOpInstruction.getLeftOperand();

                    Descriptor binaryDescriptor = OllirAccesser.getVarTable(method).get(leftOperand.getName());

                    //jasmin.append("\tiload_" + binaryDescriptor.getVirtualReg() + "\n");
                    if (leftOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || leftOperand.getType().getTypeOfElement()==ElementType.ARRAYREF) {
                        if (binaryDescriptor.getVirtualReg() < 4)
                            jasmin.append("\taload_" + binaryDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + binaryDescriptor.getVirtualReg() + "\n");
                    }
                    else {
                        if (binaryDescriptor.getVirtualReg() < 4)
                            jasmin.append("\tiload_" + binaryDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\tiload " + binaryDescriptor.getVirtualReg() + "\n");
                    }
                }
                currentNumber++;

                //if (binaryOpInstruction.getRightOperand().getType() instanceof )
                if (binaryOpInstruction.getRightOperand().isLiteral()){
                    LiteralElement literalElement = (LiteralElement) binaryOpInstruction.getRightOperand();
                    LiteralValues(literalElement);
                }
                else {
                    Operand rightOperand = (Operand) binaryOpInstruction.getRightOperand();

                    Descriptor binaryDescriptor = OllirAccesser.getVarTable(method).get(rightOperand.getName());

                    //jasmin.append("\tiload_" + binaryDescriptor.getVirtualReg() + "\n");
                    if (rightOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || rightOperand.getType().getTypeOfElement()==ElementType.THIS || rightOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                    {
                        if (binaryDescriptor.getVirtualReg()<4)
                            jasmin.append("\taload_" + binaryDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + binaryDescriptor.getVirtualReg() + "\n");
                        currentNumber++;
                    }
                    else if (rightOperand instanceof ArrayOperand){

                        ArrayOperand refArrayRigth = (ArrayOperand) rightOperand;

                        for (var index: refArrayRigth.getIndexOperands())
                        {
                            if (index.isLiteral())
                            {
                                LiteralElement literalElement = (LiteralElement) index;
                                LiteralValues(literalElement);
                            }
                            else{
                                Operand newOperand = (Operand) index;

                                if (OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg()<4)
                                    jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                else
                                    jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                            }
                            currentNumber++;
                        }

                        if (refArrayRigth.getType().getTypeOfElement()==ElementType.OBJECTREF || refArrayRigth.getType().getTypeOfElement()==ElementType.ARRAYREF)
                        {
                            if (OllirAccesser.getVarTable(method).get(refArrayRigth.getName()).getVirtualReg()<4)
                                jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(refArrayRigth.getName()).getVirtualReg() + "\n");
                            else
                                jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(refArrayRigth.getName()).getVirtualReg() + "\n");
                        }
                        else
                        {
                            if (OllirAccesser.getVarTable(method).get(refArrayRigth.getName()).getVirtualReg()<4)
                                jasmin.append("\tiload_" + OllirAccesser.getVarTable(method).get(refArrayRigth.getName()).getVirtualReg() + "\n");
                            else
                                jasmin.append("\tiload " + OllirAccesser.getVarTable(method).get(refArrayRigth.getName()).getVirtualReg() + "\n");
                        }
                        currentNumber++;
                    }
                    else
                    {
                        if (rightOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || rightOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                        {
                            if (OllirAccesser.getVarTable(method).get(rightOperand.getName()).getVirtualReg()<4)
                                jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(rightOperand.getName()).getVirtualReg() + "\n");
                            else
                                jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(rightOperand.getName()).getVirtualReg() + "\n");
                        }
                        else
                        {
                            if (OllirAccesser.getVarTable(method).get(rightOperand.getName()).getVirtualReg()<4)
                                jasmin.append("\tiload_" + OllirAccesser.getVarTable(method).get(rightOperand.getName()).getVirtualReg() + "\n");
                            else
                                jasmin.append("\tiload " + OllirAccesser.getVarTable(method).get(rightOperand.getName()).getVirtualReg() + "\n");
                        }
                        currentNumber++;
                    }
                }

                switch (operationType) {
                    case ADD:
                        jasmin.append("\tiadd\n");
                        break;
                    case SUB:
                        jasmin.append("\tisub\n");
                        break;
                    case DIV:
                        jasmin.append("\tidiv\n");
                        break;
                    case MUL:
                        jasmin.append("\timul\n");
                        break;
                    case AND:
                        jasmin.append("\tiand\n");
                        break;
                    case ANDB:
                        jasmin.append("\tiand\n");
                        /*
                    case LTH:
                        jasmin.append("\tif_icmplt LT_True_" + lthOperation + "\n");
                        jasmin.append("\ticonst_0\n");
                        jasmin.append("\tgoto LT_END_" + lthOperation + "\n" );
                        jasmin.append("LT_True_" + lthOperation + ":\n");
                        jasmin.append("\ticonst_1\n");
                        currentNumber++;
                        jasmin.append("LT_End_" + lthOperation + ":\n");
                        lthOperation++;
                        break;*/
                }
                nonAssign=true;
                break;
            case NOPER:
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) inst;

                if (singleOpInstruction.getSingleOperand().isLiteral()){
                    LiteralElement literalElement = (LiteralElement) singleOpInstruction.getSingleOperand();
                    LiteralValues(literalElement);
                }
                else {
                    Operand singleOperand = (Operand) singleOpInstruction.getSingleOperand();

                    Descriptor SingleDescriptor = OllirAccesser.getVarTable(method).get(singleOperand.getName());

                    //jasmin.append("\tiload_" + binaryDescriptor.getVirtualReg() + "\n");
                    if (singleOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || singleOperand.getType().getTypeOfElement()==ElementType.THIS || singleOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                    {
                        if (SingleDescriptor.getVirtualReg()<4)
                            jasmin.append("\taload_" + SingleDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + SingleDescriptor.getVirtualReg() + "\n");
                        currentNumber++;
                    }
                    else if (singleOperand instanceof ArrayOperand){

                        ArrayOperand refArray = (ArrayOperand) singleOperand;

                        if (OllirAccesser.getVarTable(method).get(refArray.getName()).getVirtualReg()<4)
                            jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(refArray.getName()).getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(refArray.getName()).getVirtualReg() + "\n");
                        currentNumber++;
                        for (var index: refArray.getIndexOperands())
                        {
                            if (index.isLiteral())
                            {
                                LiteralElement literalElement = (LiteralElement) index;
                                LiteralValues(literalElement);
                            }
                            else{
                                Operand newOperand = (Operand) index;

                                if (newOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || newOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                                {
                                    if (OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg()<4)
                                        jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                    else
                                        jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                }
                                else
                                {
                                    if (OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg()<4)
                                        jasmin.append("\tiload_" + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                    else
                                        jasmin.append("\tiload " + OllirAccesser.getVarTable(method).get(newOperand.getName()).getVirtualReg() + "\n");
                                }
                            }
                            currentNumber++;
                        }
                        currentNumber++;
                        jasmin.append("\tiaload\n");
                    }
                    else
                    {
                        if (singleOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || singleOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                        {
                            if (OllirAccesser.getVarTable(method).get(singleOperand.getName()).getVirtualReg()<4)
                                jasmin.append("\taload_" + OllirAccesser.getVarTable(method).get(singleOperand.getName()).getVirtualReg() + "\n");
                            else
                                jasmin.append("\taload " + OllirAccesser.getVarTable(method).get(singleOperand.getName()).getVirtualReg() + "\n");
                        }
                        else
                        {
                            if (OllirAccesser.getVarTable(method).get(singleOperand.getName()).getVirtualReg()<4)
                                jasmin.append("\tiload_" + OllirAccesser.getVarTable(method).get(singleOperand.getName()).getVirtualReg() + "\n");
                            else
                                jasmin.append("\tiload " + OllirAccesser.getVarTable(method).get(singleOperand.getName()).getVirtualReg() + "\n");
                        }
                        currentNumber++;
                    }
                }
                nonAssign=true;
                break;
            case BRANCH:
                CondBranchInstruction branchInstruction = (CondBranchInstruction) inst;

                //jasmin.append("IF");

                if (branchInstruction.getLeftOperand().isLiteral()){
                    LiteralElement literalElement = (LiteralElement) branchInstruction.getLeftOperand();
                    LiteralValues(literalElement);
                }
                else {
                    Operand leftOperand = (Operand) branchInstruction.getLeftOperand();

                    Descriptor leftDescriptor = OllirAccesser.getVarTable(method).get(leftOperand.getName());

                    if (leftOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || leftOperand.getType().getTypeOfElement()==ElementType.THIS || leftOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                    {
                        if (leftDescriptor.getVirtualReg()<4)
                            jasmin.append("\taload_" + leftDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + leftDescriptor.getVirtualReg() + "\n");
                    }
                    else
                    {
                        if (leftDescriptor.getVirtualReg()<4)
                            jasmin.append("\tiload_" + leftDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\tiload " + leftDescriptor.getVirtualReg() + "\n");
                    }
                }
                currentNumber++;

                if (branchInstruction.getRightOperand().isLiteral()){
                    LiteralElement literalElement = (LiteralElement) branchInstruction.getRightOperand();
                    LiteralValues(literalElement);
                }
                else {
                    Operand rigthOperand = (Operand) branchInstruction.getRightOperand();

                    Descriptor rigthDescriptor = OllirAccesser.getVarTable(method).get(rigthOperand.getName());

                    if (rigthOperand.getType().getTypeOfElement()==ElementType.OBJECTREF || rigthOperand.getType().getTypeOfElement()==ElementType.THIS || rigthOperand.getType().getTypeOfElement()==ElementType.ARRAYREF)
                    {
                        if (rigthDescriptor.getVirtualReg()<4)
                            jasmin.append("\taload_" + rigthDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\taload " + rigthDescriptor.getVirtualReg() + "\n");
                    }
                    else
                    {
                        if (rigthDescriptor.getVirtualReg()<4)
                            jasmin.append("\tiload_" + rigthDescriptor.getVirtualReg() + "\n");
                        else
                            jasmin.append("\tiload " + rigthDescriptor.getVirtualReg() + "\n");
                    }
                }
                currentNumber++;

                OperationType operationType1 = branchInstruction.getCondOperation().getOpType();

                switch (operationType1){
                    case EQ:
                        jasmin.append("\tif_icmpeq ");
                        break;
                    case GTE:
                        jasmin.append("\tif_icmpge ");
                        break;
                    case NEQ:
                        jasmin.append("\tif_icmpne ");
                        break;
                    case GTH:
                        jasmin.append("\tif_icmpgt ");
                        break;
                    case LTH:
                        jasmin.append("\tif_icmplt ");
                        break;
                    case LTE:
                        jasmin.append("\tif_icmple ");
                        break;
                    case ANDB:
                        jasmin.append("\tif_icmpeq ");
                        break;
                }
                jasmin.append(branchInstruction.getLabel() + "\n");
                nonAssign=true;
                break;

            case GOTO:
                GotoInstruction gotoInstruction = (GotoInstruction) inst;

                jasmin.append("\tgoto " + gotoInstruction.getLabel() + "\n");
                nonAssign=true;
                break;
        }
    }

    public void MethodOperations(Method method){
        for (var inst: method.getInstructions()) {
            GetInstructions(inst, method);
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
            //addAccessModifier(method.getMethodAccessModifier());
            jasmin.append("public <init>()");
            addType(method.getReturnType());
            jasmin.append("\n");
            int locals = method.getVarTable().size()+1;
            jasmin.append("\t.limit stack 2\n" + "\t.limit locals " + locals + "\n");
            //jasmin.append("\n\taload_0\n");
            //GetInstructions(method.getInstr(0), method);
            jasmin.append("\taload_0\n");
            jasmin.append("\tinvokespecial java/lang/Object.<init>()V\n");
            //jasmin.append("\tinvokenonvirtual java/lang/Object/<init>()V\n");
            jasmin.append("\treturn\n");
            jasmin.append(".end method\n");
            finalJasmin.append(jasmin);
            jasmin = new StringBuilder();
            return;
        }

        jasmin.append(".method");
        //System.out.println(method.getMethodAccessModifier());

        addAccessModifier(method.getMethodAccessModifier());

        if (method.isStaticMethod())
            jasmin.append("static ");
        else if (method.isFinalMethod())
            jasmin.append("final ");

        jasmin.append(method.getMethodName());

        if(method.getParams().size()>0){
            jasmin.append("(");
            for (var param: method.getParams()){
                //if (param.)
                addType(param.getType());
                //jasmin.append(";");
            }
            // Remove last ;
            // jasmin.deleteCharAt(jasmin.toString().length()-1);
            jasmin.append(")");
        }
        else
            jasmin.append("()");

        addType(method.getReturnType());

        finalJasmin.append(jasmin);
        jasmin = new StringBuilder();

        MethodOperations(method);

        /*if (method.getReturnType().getTypeOfElement()==ElementType.VOID)
            jasmin.append("\treturn\n");*/

        jasmin.append(".end method\n");

        int locals = method.getVarTable().size()+1;

        finalJasmin.append("\n\t.limit stack 1" + numberLimit + "\n" + "\t.limit locals " + locals + "\n");

        //jasmin.append(".limits " + numberLimit);
        finalJasmin.append(jasmin);
        jasmin = new StringBuilder();

        numberLimit = 0;
        currentNumber = 0;

    }

    public void AddClassFields(ClassUnit ollirClass){
        for (Field field: ollirClass.getFields()){
            jasmin.append(".field");

            addAccessModifier(field.getFieldAccessModifier());

            if (field.isStaticField())
                jasmin.append("static");
            else if (field.isFinalField())
                jasmin.append("final");

            jasmin.append("'"+field.getFieldName()+"'"+" ");
            addType(field.getFieldType());
            jasmin.append("\n");
        }
        jasmin.append("\n");
    }

    public void AddClass(ClassUnit ollirClass){
        jasmin.append(".class ");
        addAccessModifier(ollirClass.getClassAccessModifier());
        jasmin.append(ollirClass.getClassName() + "\n");
        jasmin.append(".super java/lang/Object\n\n");
    }

    public void GetImports(ArrayList<String> imports){
        for (String oneImport : imports){
            jasmin.append("[ "+ oneImport.replace('.', '/') + ";\n");
        }
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();

        ArrayList<String> imports = ollirClass.getImports();
        GetImports(imports);

        AddClass(ollirClass);

        if (ollirClass.getNumFields()>0)
            AddClassFields(ollirClass);

        try {

            // Example of what you can do with the OLLIR class
            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
            ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method
            ollirClass.show(); // print to console main information about the input OLLIR

            for (var method: ollirClass.getMethods()){
                methodLine(method);
            }

            System.out.println("\n\nJASMIN CODE:\n\n");
            System.out.println(finalJasmin.toString());

            // Convert the OLLIR to a String containing the equivalent Jasmin code
            String jasminCode = finalJasmin.toString(); // Convert node ...

            // More reports from this stage
            List<Report> reports = new ArrayList<>();

            return new JasminResult(ollirResult, jasminCode, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(ollirClass.getClassName(), null,
                    Arrays.asList(Report.newError(Stage.GENERATION, -1, -1, "Exception during Jasmin generation", e)));
        }

    }

}
