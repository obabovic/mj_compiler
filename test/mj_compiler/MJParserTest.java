package mj_compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import utils.Log4JUtils;
import rs.etf.pp1.symboltable.Tab;


public class MJParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(MJParserTest.class);
                String inputTests[] = {"lexical analysis", "syntax analysis", "semantics analysis", "code generator"};
                String testLevel[] = {"A","B","C"};
                String testType[] = {"_correct.mj","_wrong.mj"};
		String inputProgram = "test/"+inputTests[1]+"/"+testLevel[0]+testType[1];
                
		Reader br = null;
		try {
			File sourceCode = new File(inputProgram);
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        
//	        log.info("Print calls = " + p.printCallCount);
	        
	        Tab.dump();
	        
//	        if (!p.errorDetected) {
//	        	log.info("Parsiranje uspesno zavrseno!");
//	        }
//	        else {
//	        	log.error("Parsiranje NIJE uspesno zavrseno!");
//	        }
	        
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
