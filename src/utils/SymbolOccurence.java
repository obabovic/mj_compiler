/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author obabovic
 */
public enum SymbolOccurence {
    GLOBAL_CONST_DEFINITIONS,
    GLOBAL_VAR_DEFINITIONS,
    GLOBAL_ARRAY_DEFINITIONS,
    
    ALL_METHOD_DEFINITIONS,
    
    MAIN_VAR_DEFINITIONS,
    MAIN_METHOD_CALLS,
    
    INNERCLASS_DEFINITIONS,
    INNERCLASS_GLOBAL_AND_STATIC_FUNCTION_DEFINITIONS,
    INNERCLASS_METHOD_DEFINITIONS,
    INNERCLASS_FIELD_DECLARATIONS,
    
    BLOCK_STATEMENTS,
    FORMAL_ARGUMENTS_DECLARATIONS
}