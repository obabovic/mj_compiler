package utils;

import java_cup.runtime.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

import java.lang.*;

public class ParserActionImplementer {	
	

	public void report_error(String msg, int line) {

	}

	public void report_info(String msg, int line) {

	}

	public void report_error(String msg) {

	}

	public void report_info(String msg) {

	}

	public void startProgram(String name) {

	}

	public void endProgram() {

	}

	public Struct findType(String typeName, int line) {
		return null ;
	}

	public void newConst(String name, int line, Object value) {

	}

	public void newVar(String name, int line, boolean isGlobal) {

	}

	public void newArray(String name, int line, boolean isGlobal) {

	}

	public void funcType (Struct type, String name, int line) {

	}

	public void endFunc() {

	}

	public void funcStart() {

	}

	public void newEqualExpression(Obj designator, Struct expression, int line) {

	}

	public Obj getObj(String ident) {
		Obj res = null;
		return res;
	}

	public void detection(String ident, int line) {
            
	}

	public void newArray(String ident, int line) {

	}

	public void checkInt(Struct type, int line) {

	}

	public Struct checkMinus(Struct term, int line) {
		Struct res = null;
		return res;
	}

	public Struct addOp(Struct first, Struct second, Integer operation, int line) {
		Struct res = null;
		return res;
	}

	public Struct mulOp(Struct first, Struct second, Integer operation, int line) {
		Struct res = null;
		return res;
	}

	public Struct newDesFactor (Obj designator) {
		Struct res = null;
		return res;
	}

	public Struct newFuncFactor(Obj designator, int line, int v) {
		Struct res = null;
		return res;
	}

	public Struct newExprFactor(Struct expr) {
		Struct res = null;
		return res;
	}

	public Struct newArrFactor(Struct first, Struct second, int line) {
		Struct res = null;
		return res;
	}

	public void increment (Obj designator, int line) {

	}

	public void decrement (Obj designator, int line) {

	}

	public void returnMatched(Struct type, int line) {

	}

	public void readMatched(Obj designator, int line) {

	}

	public void printMatched (Struct type, Integer length, int line) {

	}

	public void newFunction(Obj designator, int line) {

	}
}