
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.StringReader;

public class Main implements JmmParser {
	public JmmParserResult parse(String jmmCode) {

		try {
		    Parser myParser = new Parser(new StringReader(jmmCode));
    		SimpleNode root = myParser.Program(); // returns reference to root node
            	
    		root.dump(""); // prints the tree on the screen
    		return new JmmParserResult(root, myParser.getSyntacticErrors());
		} catch(ParseException e) {
			throw new RuntimeException("Error while parsing", e);
		}

	}

	// java jmm [-r=<num>] [-o] <input_file.jmm> ou java –jar jmm.jar [-r=<num>] [-o] <input_file.jmm>
    public static void main(String[] args) {
		/*String jmmCode = args[1];
		Main main = new Main();
		main.parse(jmmCode);*/

		System.out.println("Executing with args: " + Arrays.toString(args));
		if(args[0].contains("fail")) {
			throw new RuntimeException("It's supposed to fail");
		}

		// root.toJson();
	}
}