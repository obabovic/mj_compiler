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
    GLOBAL_METHOD_CALLS,
    
    MAIN_VAR_DEFINITIONS,
    ARRAY_ITEM_CALLS,
    METHOD_FORMAL_ARGUMENT_DECLARATIONS,
    BLOCK_STATEMENTS,
    
    INNERCLASS_CREATIONS,
    INNERCLASS_FIELD_CALLS,
    INNERCLASS_METHOD_DEFINITIONS,
    INNERCLASS_METHOD_CALLS
}