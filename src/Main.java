
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;

public class Main implements JmmParser {
	public JmmParserResult parse(String jmmCode) {

		try {
			Parser myParser = new Parser(new StringReader(jmmCode));
			SimpleNode root = myParser.Program(); // returns reference to root node

			root.dump(""); // prints the tree on the screen

			return new JmmParserResult(root, new ArrayList<Report>());
		} catch(ParseException e) {
			throw new RuntimeException("Error while parsing", e);
		}

	}

	// java jmm [-r=<num>] [-o] <input_file.jmm> ou java –jar jmm.jar [-r=<num>] [-o] <input_file.jmm>
	// java -jar comp2021-5e.jar test/fixtures/public/HelloWorld.jmm
	// java -cp "./build/classes/java/main/" Main test/fixtures/public/HelloWorld.jmm
    public static void main(String[] args) {
		InputStream in = null;

		try {
			System.out.printf(args[0]);
			in = new FileInputStream(args[0]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		try {
			Parser myParser = new Parser(in);
			SimpleNode root = myParser.Program(); // returns reference to root node

			root.dump(""); // prints the tree on the screen

			FileOutputStream outputStream = new FileOutputStream("AST.json");
			outputStream.write(root.toJson().getBytes(StandardCharsets.UTF_8));
			outputStream.flush();

		} catch(ParseException | FileNotFoundException e) {
			throw new RuntimeException("Error while parsing", e);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}