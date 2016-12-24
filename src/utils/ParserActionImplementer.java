package utils;

import JFlex.sym;
import java_cup.runtime.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

import java.lang.*;
import java.util.EnumMap;
import java.util.Map;
import org.apache.log4j.Logger;



public class ParserActionImplementer {	
    public Integer printCallCount = 0;
    private Boolean mainIsDefined = false;
    public Map<SymbolOccurence, Integer> mapOfEnumerations = new EnumMap<SymbolOccurence, Integer>(SymbolOccurence.class);
    public Scope globalScope;
    
    public Obj currentProgram;
    public Scope currentScope;
    public Struct currentVarType;
    public Struct currentConstType;
    
    public Obj currentMethod;
    public String currentMethodName;
    public Struct currentMethodType;
    public Boolean currentMethodIsStatic;
    
    
    
    public String currentClassName;
    public Struct currentClassParent;
    
    public static final int NUMBER = 25;
    public static final int CHAR = 23;
    public static final int BOOL = 24;
    
    public static final Struct intType = new Struct(NUMBER, Tab.intType);
    public static final Struct charType = new Struct(CHAR, Tab.charType);
    public static final Struct boolType = new Struct(BOOL);
    
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
    
    public void startProgram(String progName) {
        reportInfo("Program named \""+progName+"\" STARTED.");
        Tab.insert(Obj.Type, "int", intType);
        Tab.insert(Obj.Type, "char", charType);
        Tab.insert(Obj.Type, "bool", boolType);
        
        
        globalScope = Tab.currentScope();
        currentProgram = Tab.insert(Obj.Prog, progName, Tab.noType);
        Tab.openScope();
        currentScope = Tab.currentScope();
    }
    
    public void endProgram() {
        if(!mainIsDefined) {
            String tmp = "Error! Main function has not been found."; 
            reportError(tmp);
            log.info(tmp);
        }
        presentSymbolOccurences();
//        Tab.chainLocalSymbols(currentProgram);
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
    
    public void addConst(String constName, Object constValue, int line) {
        if(Tab.currentScope().findSymbol(constName) != null) {
            reportError("Error! Constant named \"" + constName + "\" has already been declared. Line " , line);
        } else {
            int address = 0;
            int constKind = currentConstType.getKind();
            
            if(((constKind==1)&&(!((constValue instanceof Integer)||(constValue instanceof Boolean))))||((constKind==2)&&(!(constValue instanceof Character)))) {
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
    
    public void addVar(String varName, int line, SymbolOrigin origin) {
        if(Tab.currentScope().findSymbol(varName) != null)
                reportError("Error! Variable \"" + varName + "\" has already been declared. Line " , line);
        else {
            switch(origin) {
                case GLOBAL:
                    increment(SymbolOccurence.GLOBAL_VAR_DEFINITIONS);
                    break;
                case INNERCLASS:
                    increment(SymbolOccurence.INNERCLASS_FIELD_DECLARATIONS);
                    break;
                case MAIN:
                    increment(SymbolOccurence.MAIN_VAR_DEFINITIONS);
                    break;
                default:
                    reportError("Error! Origin of variable \"" + varName + "\" is undefined. Line " , line);
                    return;
            }
            Obj temp = Tab.insert(Obj.Var, varName, currentVarType);   
        }
    }
    
    public void addArray(String arrayName, int line, SymbolOrigin origin) {
        if(Tab.currentScope().findSymbol(arrayName) != null)
            reportError("Error! Array \"" + arrayName + "\" has already been declared. Line " , line);
        else {
            switch(origin) {
                case GLOBAL:
                    increment(SymbolOccurence.GLOBAL_ARRAY_DEFINITIONS);
                    break;
                case INNERCLASS:
                    increment(SymbolOccurence.INNERCLASS_FIELD_DECLARATIONS);
                    break;
                default:
                    reportError("Error! Origin of array \"" + arrayName + "\" is undefined. Line " , line);
                    return;
            }
            Obj temp = Tab.insert(Obj.Var, arrayName, new Struct (Struct.Array, currentVarType));
        }
    }
    
    public void addMethod(String methodName, Struct methodType, int line) {
        currentMethodName = methodName;
        currentMethodType = methodType;
        
        if(Tab.currentScope().findSymbol(methodName) != null)
            reportError("Error! Method \"" + methodName + "\" has already been declared. Line " , line);
        else {
            if("main".equals(methodName)) {
                mainIsDefined = true;
            }
            
            if(currentMethodType == null)
                currentMethodType = Tab.noType;
            
            currentMethod = Tab.insert(Obj.Meth, currentMethodName, currentMethodType);
            increment(SymbolOccurence.ALL_METHOD_DEFINITIONS);
            Tab.openScope();
        }
    }
    
    public void methodStart() {
    
    }
    
    public void methodEnd() {
    
    }
}