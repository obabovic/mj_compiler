package utils;

import JFlex.sym;
import java_cup.runtime.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

import java.lang.*;
import java.util.EnumMap;
import java.util.Map;
import org.apache.log4j.Logger;
import rs.etf.pp1.mj.runtime.Code;



public class ParserActionImplementer {	
    public Integer printCallCount = 0;
    private Boolean mainIsDefined = false;
    public Map<SymbolOccurence, Integer> mapOfEnumerations = new EnumMap<SymbolOccurence, Integer>(SymbolOccurence.class);
    public Scope globalScope;
    
    public Obj currentProgram;
    public Scope currentScope;
    public boolean isInForLoop;
    public Struct currentVarType;
    public Struct currentConstType;
    
    public Obj currentMethod;
    public String currentMethodName;
    public Struct currentMethodType;
    public int currentMethodNameLine;
    public Boolean currentMethodIsStatic;
    public Boolean currentMethodHasReturn;
    
    public String currentClassName;
    public Struct currentClassParent;
    public Obj currentDesignatorArray;
    public Obj currentClass;
    public boolean currentIfStatementConditionState;
    
    public static final int NUMBER = 25;
    public static final int CHAR = 23;
    public static final int BOOL = 24;
    public static final int ASSIGN_PLUS = 100 + Code.add;
    public static final int ASSIGN_MINUS = 100 + Code.sub;
    public static final int ASSIGN_MUL = 100 + Code.mul;
    public static final int ASSIGN_DIV = 100 + Code.div;
    public static final int ASSIGN_REM = 100 + Code.rem;
    
    
    public static final Struct boolType = new Struct(Struct.Bool);
    public static final Struct stringType = new Struct(Struct.Array, Tab.charType);
    public static final Struct intArrayType = new Struct(Struct.Array, Tab.intType);
    
    public Logger log = Logger.getLogger(getClass());
    
    public void reportInfo(String msg, int line) {
        System.out.println(msg + " " + line);
        log.info(msg.toString());
    }
    
    public void reportInfo(String msg) {
        System.out.println(msg);
        log.info(msg.toString());
    }
    
    public void reportError(String message, Object info) {
      System.err.print(message);
      System.err.flush();
      if (info instanceof Symbol)
            System.err.println("Error! Line " + ((Symbol)info).left);
      else System.err.println("");
    }
    
    public void reportError(String message) {
      System.err.println(message);
      System.err.flush();
    }
    
    public void unrecoveredSyntaxError(Symbol curToken) throws java.lang.Exception {
        reportFatalError("Error! Parsing has to be stopped.", curToken);
    }
    
    public void reportFatalError(String message, Object info) throws java.lang.Exception {
      reportError(message, info);
    }
    
    
    
    public void programStart(String progName) {
        reportInfo("Program named \""+progName+"\" STARTED.");
        Tab.init();
        Tab.currentScope().addToLocals(new Obj(Obj.Type, "bool", boolType));
        Tab.currentScope().addToLocals(new Obj(Obj.Type, "string", stringType));
        Tab.currentScope().addToLocals(new Obj(Obj.Type, "intArray", intArrayType));
        
        
        globalScope = Tab.currentScope();
        currentProgram = Tab.insert(Obj.Prog, progName, Tab.noType);
        Tab.openScope();
        currentScope = Tab.currentScope();
    }
    
    public void programEnd() {
        if(!mainIsDefined) {
            String tmp = "Error! Main function has not been found."; 
            reportError(tmp);
            log.info(tmp);
        }
        presentSymbolOccurences();
        
        Code.dataSize = Tab.currentScope().getnVars();
        
        Tab.chainLocalSymbols(currentProgram);
        Tab.closeScope();
    }
    
    public void presentSymbolOccurences() {
        System.out.println("\n\n//-------- Presenting number of occurences for each item --------\\\\");
        for (Map.Entry<SymbolOccurence, Integer> entry : mapOfEnumerations.entrySet())
        {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println("//---------------------------------------------------------------\\\\\n\n");
    }
    
    public void increment(SymbolOccurence item) {
        mapOfEnumerations.putIfAbsent(item, 0);
        int tmp = mapOfEnumerations.get(item);
        mapOfEnumerations.put(item, tmp+1);
    }
    
    public Struct resolveType(String typeName, int line) {
        Struct res = null;
        
        Obj obj = Tab.find(typeName);
        if((obj == Tab.noObj) || (obj.getKind() != Obj.Type)) 
            reportError("Error! Undefined type \"" + typeName + "\" on line ", line);
        res = obj.getType();
        
        return res;
    }
    
    public Struct compareTypes(Struct type1, Struct type2, int line) {
        Struct res = null;
        
        if(type1.getKind() == Obj.Con)
            reportError("Error! Left part of equation is a constant on line "+line);
        else if(!type1.assignableTo(type2)) 
            reportError("Error! Types are incompatible on line "+line);
        else {
            res = type1;
        }
        
        return res;
    }
    
    public Obj checkIfInt(Obj item, int line) {
        Obj res = Tab.noObj;
        
        if(item.getType() != Tab.intType) {
            reportError("Error! Type is not int. Line ", line);
        } else {
            res = item;
        }
        
        return res;
    }
    
    public void setDesignatorArrayExtension(String ident, int line) {
        Obj temp = Tab.find(ident);
        if(temp.equals(Tab.noObj)) {
            reportError("Error! undefined identificator named \""+ident+"\" on line ", line);
        }
        if(temp.getType().getKind() == Struct.Array) {
            //if the left side identificator is an array
            currentDesignatorArray = temp;
        }
    }
    
    public Obj resolveIdentificator(String ident, int line) {
        Obj res = null;
        
        Obj temp = Tab.find(ident);
        if(temp.equals(Tab.noObj)) {
            res = Tab.noObj;
            reportError("Error! undefined identificator named \""+ident+"\" on line ", line);
        } else {
            res = temp;
            if (temp.getKind() == Obj.Con) 						
                reportInfo("Constant named \"" + ident + "\" has been detected on line ", line);
            else if (temp.getKind() == Obj.Var)
                if (temp.getLevel() == 0) 
                    reportInfo("Global variable named \"" + ident + "\" has been detected on line ", line);
                else 
                    reportInfo("Local variable named \"" + ident + "\" has been detected on line ", line);
        }
        
        return res;
    }
    
    public void addConst(String constName, Object constValue, int line) {
        if(Tab.currentScope().findSymbol(constName) != null) {
            reportError("Error! Constant named \"" + constName + "\" has already been declared. Line " , line);
        } else {
            int address = 0;
            int constKind = currentConstType.getKind();
            
            if(((constKind==Obj.Con)&&(!((constValue instanceof Integer)||(constValue instanceof Boolean))))||((constKind==2)&&(!(constValue instanceof Character)))) {
                reportError("Error! Constant named \"" + constName + "\" has different type and value. Line " , line);
            } else {
                if(constValue instanceof Integer) {
                    address = (Integer) constValue;
                } else if (constValue instanceof Character) {
                    address = (Character) constValue;
                } else if (constValue instanceof Boolean) {
                    address = ((Boolean)constValue) == null ? 0 : 1;
                }
                Tab.insert(Obj.Con, constName, currentConstType).setAdr(address);
                increment(SymbolOccurence.GLOBAL_CONST_DEFINITIONS);
            }
        }
    }
    
    public void addVar(String varName, int line, SymbolOrigin origin, Boolean isArray) {
        if(Tab.currentScope().findSymbol(varName) != null)
                reportError("Error! Variable \"" + varName + "\" has already been declared. Line " , line);
        else {
            switch(origin) {
                case GLOBAL:
                    if(isArray) {
                        increment(SymbolOccurence.GLOBAL_ARRAY_DEFINITIONS);
                    } else {
                        increment(SymbolOccurence.GLOBAL_VAR_DEFINITIONS);
                    }
                    break;
                case LOCAL:
                    if((currentMethod!= null)&&("main".equals(currentMethod.getName()))) {
                        increment(SymbolOccurence.MAIN_VAR_DEFINITIONS);
                    }
                    break;
                case UNIMPORTANT:
                    break;
                default:
                    reportError("Error! Origin of variable \"" + varName + "\" is undefined. Line " , line);
                    return;
            }
            Obj temp;
            if(isArray) {
                temp = Tab.insert(Obj.Var, varName, new Struct (Struct.Array, currentVarType));
            } else {
                temp = Tab.insert(Obj.Var, varName, currentVarType);   
            }
        }
    }
    
    public void addMethod() {
        String methodName = currentMethodName;
        Struct methodType = currentMethodType;
        int line = currentMethodNameLine;
        
        currentMethodHasReturn = false;
        
        if(Tab.currentScope().findSymbol(methodName) != null)
            reportError("Error! Method \"" + methodName + "\" has already been declared. Line " , line);
        else {
            if(currentMethodType == null)
                currentMethodType = Tab.noType;
            
            currentMethod = Tab.insert(Obj.Meth, currentMethodName, currentMethodType);
            Tab.openScope();
        }
    }
    
    public void methodStart() {
        currentMethod.setAdr(Code.pc);
        if("main".equals(currentMethodName)) {
            mainIsDefined = true;
            Code.mainPc = currentMethod.getAdr();
        }
        Code.put(Code.enter);
        Code.put(currentMethod.getLevel());
        Code.put(Tab.currentScope().getnVars());
    }
    
    public void methodEnd() {
        if((currentMethod.getType() != Tab.noType) && (currentMethodHasReturn == false))
            reportError("Error! Non void method has no return.");
        
        Code.put(Code.exit);
        Code.put(Code.return_);
        
        Tab.chainLocalSymbols(currentMethod);
        Tab.closeScope();
        
        currentMethod = null;
        currentMethodHasReturn = false;
        
    }
    
    public void markReturn() {
        if(currentMethod!=null) {
            currentMethodHasReturn = true;
        }
    }
    
    public void classStart(String className, int line) {
        currentClassName = className;
        if(Tab.currentScope().findSymbol(currentClassName) != null)
            reportError("Error! Class \"" + currentClassName + "\" has already been declared. Line " , line);
        else {
            Struct type = new Struct(Struct.Class);
            currentClass = Tab.insert(Obj.Type, currentClassName, type);
            Tab.openScope(); 
        }
    }
    
    public void classEnd() {
        Tab.chainLocalSymbols(currentClass);
        Tab.closeScope();
        currentClass = null;
    }
    
    public void forLoopStart() {
        isInForLoop = true;
    }
    
    public void forLoopEnd() {
        isInForLoop = false;
    }
    
    public void statementCheckIfOutcome() {
//      TODO: implement code generation for if-else     
    }
    
    public void statementCheckIfCondition(Struct condition, int line) {
        if(condition != Tab.intType) {
            reportError("Error! Condition is not of kind bool. Line ", line);
        } else {
            
        }
    }
    
    public void statementCheckForCondition(Struct condition, int line) {
        if(condition.getKind() != Struct.Bool) {
            reportError("Error! Condition is not of kind bool. Line ", line);
        } else {
            
        }
    }
    
    public void statementCheckPrint(Struct expr, Integer number, int line) {
        Struct typeToCheck = (expr.getKind()==Struct.Array)?expr.getElemType():expr;
        
        if(((typeToCheck != Tab.charType) && (typeToCheck != Tab.intType)&&(typeToCheck.getKind() != Struct.Bool)))
                reportError("Error! Expression is not of type int, char or bool on line ", line);
        else {
            Code.loadConst(number);
            if(typeToCheck == Tab.intType) {
                Code.put(Code.print);
            } else if (typeToCheck == Tab.charType) {
                Code.put(Code.bprint);
            }
        }
    }
    
    public void statementCheckRead(Obj designator, int line) {
        if(designator == Tab.noObj) {
               reportError("Error! Designator is no object type on line ", line);
        } else {
            int tmp = designator.getKind();
            if((tmp != Obj.Var)&&(tmp != Obj.Fld)&&(tmp != Obj.Elem)) {
                reportError("Error! Designator is not of kind var, array element or class field on line ", line);
            } else if((designator.getType() != Tab.intType)&&(designator.getType() != Tab.charType)&&(designator.getType().getKind() != Struct.Bool)) {
                reportError("Error! Designator is not of type int, char or bool on line ", line);
            } else {
                if(designator.getType() == Tab.intType || designator.getType().getKind() == Struct.Bool) {
                    Code.put(Code.read);
                    Code.store(designator);
                } else {
                    Code.put(Code.bread);
                    Code.store(designator);
                }
            }
       }
    }
    
    public void statementCheckContinue(int line) {
        if(!isInForLoop) {
            reportError("Error! Break statement is not in for loop on line ", line);
        }
    }
    
    public void statementCheckBreak(int line) {
        if(!isInForLoop) {
            reportError("Error! Break statement is not in for loop on line ", line);
        }
    }
    
    public Obj designatorExtensionResolveArray(int line) {
        Obj res = null;
        
        if(!(currentDesignatorArray.getType().getKind() == Struct.Array)) {
            reportError("Error! Sentence is not of kind array on line ", line);
        } else {
            Code.load(currentDesignatorArray);
            res = new Obj(Obj.Var,"",new Struct(Struct.Array, currentDesignatorArray.getType().getElemType()));
            currentDesignatorArray = res;
        }
        
        return res;
    }
    
    public Obj designatorInc(Obj des, int line) {
        Obj res = null;
        
        if(des == Tab.noObj) {
            reportError("Error! Designator is no object type on line ", line);
        } else {
            int tmp = des.getKind();
            if((tmp != Obj.Var)&&(tmp != Obj.Fld)&&(tmp != Obj.Elem)) {
                reportError("Error! Designator is not of kind var or class field on line ", line);
            } else if(des.getType() != Tab.intType) {
                reportError("Error! Designator is not of type int on line ", line);
            } else {
                if (des.getKind() == Obj.Elem)
                {
                    Code.put(Code.dup2);
                }
                Code.load(des);
                Code.loadConst(1);
                Code.put(Code.add);
                Code.store(des);
                res=des;
            }
        }
        
        return res;
    }
    
    public Obj designatorDec(Obj des, int line) {
        Obj res = null;
        
        if(des == Tab.noObj) {
            reportError("Error! Designator is no object type on line ", line);
        } else {
            int tmp = des.getKind();
            if((tmp != Obj.Var)&&(tmp != Obj.Fld)&&(tmp != Obj.Elem)) {
                reportError("Error! Designator is not of kind var or class field on line ", line);
            } else if(des.getType() != Tab.intType) {
                reportError("Error! Designator is not of type int on line ", line);
            } else {
                if (des.getKind() == Obj.Elem)
                {
                    Code.put(Code.dup2);
                }
                Code.load(des);
                Code.loadConst(1);
                Code.put(Code.sub);
                Code.store(des);
                res=des;
            }
        }
        
        return res;
    }
    
    public Struct designatorCheckAssign(Obj des, Integer op, Struct expr, int line) {
        Struct res = null;
        boolean errorDetected = false;
        
        if(des.getKind() == Obj.Con) {
            reportError("Error! Left part of equation is a constant on line "+line);
            errorDetected = true;
        }
        else if(des.getType().getKind() == Struct.Array) {
            if(expr.getKind() == Struct.Array) {
                if(!(des.getType().getElemType().assignableTo(expr.getElemType()))) {
                    reportError("Error! Types are incompatible on line "+line);
                    errorDetected = true;
                } 
            } else {
                if(!(des.getType().getElemType().assignableTo(expr))) {
                    reportError("Error! Types are incompatible on line "+line);
                    errorDetected = true;
                }
            }
                
        } 
        else if (!(des.getType().assignableTo(expr))) {
            reportError("Error! Types are incompatible on line "+line);
            errorDetected = true;
        }
            
        if (!errorDetected) {
            res = des.getType();
            if(op.intValue() == 0) {
                // ASSIGN
                Code.store(des);
            } else {
                Code.load(des);
                Code.put(getOpCode(op.intValue()));
                Code.store(des);
            }
        }
        return res;
    }
    
    public Struct designatorCallMethod(Obj designator, Obj ActPars, int line) {
        Struct res = null;
        
        if(designator.getKind() != Obj.Meth) {
            reportError("Error! Designator is not of kind Method. Line ", line);
        } else {
            reportInfo("Method of name \"" + designator.getName() + "\" has been detected on line ", line);
            
            res = designator.getType();
            
            int destinationAddress = designator.getAdr() - Code.pc;
            Code.put(Code.call);
            Code.put2(destinationAddress);
            
            if(designator.getType() != Tab.noType) {
                Code.put(Code.pop);
            }
        }
        
        return res;
    }
    
    public Obj termListAddOp(Obj term, Integer operation, Obj termList, int line) {
        Obj res = null;
        
        if((term == Tab.noObj)||(termList == Tab.noObj)) {
            reportError("Error! Term is not of any type. Line ", line);
        } else if((term.getType() != Tab.intType)||(termList.getType() != Tab.intType)) {
            reportError("Error! Term is not of type int. Line ", line);
            res = Tab.noObj;
        } else {
            res = new Obj(Struct.Int,"",Tab.intType);
            Code.put(getOpCode(operation.intValue()));
            if(operation>100) {
                Code.store(term);
                Code.load(term);
            }
        }
        
        return res;
    }
    
    public Obj termMulOp(Obj term, Integer operation, Obj factor, int line) {		
        Obj res = null;

        if((term.getType() == Tab.noType) || (factor.getType() == Tab.noType)) {
            reportError("Error! Operands are not of any type. Line ", line);
        } else {
            if((term.getType() != Tab.intType) || (factor.getType() != Tab.intType)) {
                reportError("Error! Operands are not of type int. Line ", line);
                res = Tab.noObj;   
            }
            else {
                Code.put(getOpCode(operation.intValue()));
                if(operation>100) {
                    Code.store(term);
                    Code.load(term);
                }
                res = term;
            }
        }
        
        return res;
    }
    
    public Obj factorNewDesignator(Obj designator, int line) {
        Obj res = null;
        
        if(designator == Tab.noObj) {
            res = Tab.noObj;
        } else {
            res = designator;
            Code.load(designator);
        }
        
        return res;
    }
    
    public Obj factorNewMethod(Obj designator, int line) {
        Obj res = null;
        
        if(Obj.Meth != designator.getKind()) {
            reportError("Error! Method "+designator.getName()+" is undefined or the designator is not a method at all. Line ", line);
            res = Tab.noObj;
        } else {
            Obj temp = Tab.find(designator.getName());
            if(temp == null) {
                reportError("Error! Method is undefined on line ", line);
            } else if (temp.getType() == Tab.noType) {
                reportError("Error! Method is type of void and used as Rvalue on line ", line);
            } else {
                int destinationAddress = designator.getAdr() - Code.pc;
                Code.put(Code.call);
                Code.put2(destinationAddress);
            }
            res = (temp != null)?temp:Tab.noObj;
        }
        
        return res;
    }
    
    public Obj factorNewNumber(Integer number, int line) {
        Obj res = null;
        
        Obj temp = Tab.insert(Obj.Con, "", Tab.intType);
        temp.setAdr(number.intValue());
        reportInfo("Constant of value \"" + number + "\" has been detected on line ", line);
        Code.load(temp);
        res = temp;
        
        return res;
    }
    
    public Obj factorNewChar(Character ch, int line) {
        Obj res = null;
        
        Obj temp = Tab.insert(Obj.Con, "", Tab.charType);
        temp.setAdr(ch.charValue());
        reportInfo("Constant of value \"" + ch + "\" has been detected on line ", line);
        Code.load(temp);
        res = temp;
        
        return res;
    }
    
    public Obj factorNewBool(Boolean b, int line) {
        Obj res = null;
        
        Obj boolObj = Tab.find("bool");
        Obj temp = Tab.insert(Obj.Con, "", boolObj.getType());
        temp.setAdr((b.booleanValue()==true)?1:0);
        reportInfo("Constant of value \"" + b + "\" has been detected on line ", line);
        Code.load(temp);
        
        res = temp;
        return res;
    }
    
    public Obj factorNewExpr(Struct expr, int line) {
        Obj res = null;
//      TODO: refactor this method        
//      res = expr;  
        return res;
    }
    
    public Obj factorNewArray(Struct type, Struct expr, int line) {
        Obj res = null;
        
        if(!Tab.intType.equals(expr)) {
            reportError("Error! Expression must be of type Integer on line ", line);
        } else {
            res = new Obj(Obj.Elem,"",new Struct(Struct.Array, type));
            Code.put(Code.newarray);
            if (type == Tab.charType) Code.put(0); 
            else Code.put(1);
        }
        
        return res;
    }
    
    public Obj factorNewClass(Struct type, int line) {
        Obj res = null;
        
        Obj temp = new Obj(Struct.Class,"",new Struct(Struct.Class));
        if(!type.assignableTo(temp.getType())) {
            reportError("Error! Element must be of type Class on line ", line);
            res = Tab.noObj;
        } else 
            res = temp;
        
        return res;
    }
    
    //Function getOpCode
    //@Params: opCode - code of a given operation
    //@Return: changed opCode according to the type of opcode (ADD or ASSIGN_PLUS)
    public Integer getOpCode(Integer opCode) {
        Integer res;
        
        res = (opCode.intValue()>100)?opCode.intValue()-100:opCode.intValue();
        
        return res;
    }
    
    //LEVEL 2
    public void onGlobalMethodCalled() {
    
    }
    
    public void onArrayItemCalled() {
    
    }
    
    public void onMethodFormalArgumentCalled() {
    
    }
    
    //LEVEL 3
    public void onInnerClassObjectCreated() {
    
    }
    
    public void onInnerclassFieldCalled() {
    
    }
    
    public void onInnerclassMethodCalled() {
    
    }
}