package utils;

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
    public Scope currentScope;
    public Obj currentProgram;
    public Scope universeScope;
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
            System.err.println("Error : on line " + ((Symbol)info).left);
      else System.err.println("");
    }
    
    public void reportError(String message) {
      System.err.println(message);
      System.err.flush();
    }
    
    public void unrecoveredSyntaxError(Symbol curToken) throws java.lang.Exception {
        reportFatalError("Error : parsing has to be stopped", curToken);
    }
    
    public void reportFatalError(String message, Object info) throws java.lang.Exception {
      reportError(message, info);
    }
    
    public void startProgram(String progName) {
        reportInfo("Program named \""+progName+"\" STARTED.");
    }
    
    public void endProgram() {
        if(!mainIsDefined) {
            String tmp = "Error: Main function not found."; 
            reportError(tmp);
            log.info(tmp);
        }	
//        Tab.chainLocalSymbols(currentProgram);
        Tab.closeScope();
    }
    
    public void increment(SymbolOccurence item) {
        mapOfEnumerations.putIfAbsent(item, 0);
        int tmp = mapOfEnumerations.get(item);
        mapOfEnumerations.put(item, tmp+1);
    }
}