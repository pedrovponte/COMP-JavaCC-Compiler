
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;

public class Main implements JmmParser {
	public JmmParserResult parse(String jmmCode) {

		try {
		    Parser myParser = new Parser(new FileInputStream(jmmCode));
    		SimpleNode root = myParser.Program(); // returns reference to root node

    		root.dump(""); // prints the tree on the screen

			FileOutputStream jsonFile = new FileOutputStream("AST.json");

			JmmParserResult parserResult = new JmmParserResult(root, myParser.getSyntacticErrors());
			jsonFile.write(parserResult.toJson().getBytes());

			jsonFile.close();

			return parserResult;
		} catch(Exception e) {
			ArrayList<Report> reports = new ArrayList<Report>();
			if (e instanceof ParseException) {
				reports.add(new Report(ReportType.ERROR, Stage.SYNTATIC, ((ParseException) e).currentToken.beginLine, "Detected generic error: " + e.getMessage()));
			}
			else if (e instanceof RuntimeException)
			{
				reports.add(new Report(ReportType.ERROR, Stage.SYNTATIC, -1, "Detected generic error: " + e.getMessage()));
			}
			return new JmmParserResult(null, reports);
		}

	}

	// java jmm [-r=<num>] [-o] <input_file.jmm> ou java –jar jmm.jar [-r=<num>] [-o] <input_file.jmm>
	// java -jar comp2021-5e.jar test/fixtures/public/HelloWorld.jmm
	// java -cp "./build/classes/java/main/" Main test/fixtures/public/HelloWorld.jmm
	// .\comp2021-5e.bat Main test/fixtures/public/WhileAndIF.jmm
    public static void main(String[] args) {
		InputStream in = null;

		try {
			System.out.printf(args[0]);
			in = new FileInputStream(args[0]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		Main main = new Main();
		main.parse(args[0]);
	}
}