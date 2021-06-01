
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.jasmin.JasminUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
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
		    Parser myParser = new Parser(new ByteArrayInputStream(jmmCode.getBytes()));
    		SimpleNode root = myParser.Program(); // returns reference to root node

    		root.dump(""); // prints the tree on the screen

			FileOutputStream jsonFile = new FileOutputStream("AST.json");

			JmmParserResult parserResult = new JmmParserResult(root, myParser.getSyntacticErrors());
			jsonFile.write(parserResult.toJson().getBytes());

			jsonFile.close();

			return parserResult;
		} catch(Exception e) {
			ArrayList<Report> reports = new ArrayList<Report>();
			e.printStackTrace();
			if (e instanceof ParseException) {
				reports.add(new Report(ReportType.ERROR, Stage.SYNTATIC, ((ParseException) e).currentToken.beginLine, "Detected generic error: " + e.getMessage()));
			}
			else {
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
		String filename = args[0].split("\\.jmm")[0];

		try {
			Main main = new Main();
			JmmParserResult parserResult = main.parse(SpecsIo.read(args[0]));

			AnalysisStage analysisStage = new AnalysisStage();
			JmmSemanticsResult semanticsResult = analysisStage.semanticAnalysis(parserResult);

			FileOutputStream fileSymbolTable = new FileOutputStream( filename + ".symbols.txt");
			fileSymbolTable.write(semanticsResult.getSymbolTable().print().getBytes());
			fileSymbolTable.close();

			OptimizationStage optimizationStage = new OptimizationStage();
			OllirResult ollirResult = optimizationStage.toOllir(semanticsResult);

			FileOutputStream jsonOllirCode = new FileOutputStream( filename + ".ollir");
			jsonOllirCode.write(ollirResult.getOllirCode().getBytes());
			jsonOllirCode.close();

			BackendStage backendStage = new BackendStage();
			JasminResult jasminResult = backendStage.toJasmin(ollirResult);

			FileOutputStream fileJasminCode = new FileOutputStream( filename + ".j");
			fileJasminCode.write(jasminResult.getJasminCode().getBytes());
			fileJasminCode.close();

			File jasminFile = new File(filename + ".j");
			File outputDir = new File("comp2021-5e");

			JasminUtils.assemble(jasminFile, outputDir);
		}
		catch (Exception e) {
			e.getMessage();
		}
	}
}