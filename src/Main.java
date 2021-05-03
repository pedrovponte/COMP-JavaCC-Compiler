
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
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

			AnalysisStage analysisStage = new AnalysisStage();
			JmmSemanticsResult semanticsResult = analysisStage.semanticAnalysis(parserResult);

			OptimizationStage optimizationStage = new OptimizationStage();
			OllirResult ollirResult = optimizationStage.toOllir(semanticsResult);


			/*for(int i = 0; i < root.jjtGetNumChildren(); i++) { // Import, Class
				//System.out.println("Child: " + root.jjtGetChild(i).toString());
				if(root.jjtGetChild(i).toString().equals("Import")) {
					SimpleNode node = (SimpleNode) root.jjtGetChild(i);
					SimpleNode val = (SimpleNode) node.jjtGetChild(0);
					System.out.println("Import: " + val.jjtGetValue());
				}
				else if(root.jjtGetChild(i).toString().equals("Class")) {
					SimpleNode node = (SimpleNode) root.jjtGetChild(i);
					SimpleNode val = (SimpleNode) node.jjtGetChild(0);
					System.out.println("Class: " + val.jjtGetValue());
					for(int j = 0; j < node.jjtGetNumChildren(); j++) { // Identifier, Extends, Main, Method, VarDeclaration
						System.out.println("Child: " + node.jjtGetChild(j).toString());
						if(node.jjtGetChild(j).toString().equals("Identifier")) {
							SimpleNode identifier = (SimpleNode) node.jjtGetChild(i);
							SimpleNode vali = (SimpleNode) identifier.jjtGetChild(0);
							System.out.println("Class Name: " + vali.jjtGetValue());
						}
						if(node.jjtGetChild(j).toString().equals("Extends")) {
							SimpleNode extendsNode = (SimpleNode) node.jjtGetChild(j);
							System.out.println("Extends: " + extendsNode.jjtGetChild(0));
						}
//						if(node.jjtGetChild(j).toString().equals("Method")) { // Main or another
//							SimpleNode methodNode = (SimpleNode) node.jjtGetChild(j);
//							System.out.println("Method: " + methodNode.jjtGetChild(0));
//						}
//						if(node.jjtGetChild(j).toString().equals("PrimitiveType")) {
//							System.out.println("Bla");
//						}
//
//						else {
//							System.out.println("Child: " + node.jjtGetChild(j).toString());
//						}
					}
				}
			}*/



			return parserResult;
		} catch(Exception e) {
			ArrayList<Report> reports = new ArrayList<Report>();
			e.printStackTrace();
			if (e instanceof ParseException) {
				reports.add(new Report(ReportType.ERROR, Stage.SYNTATIC, ((ParseException) e).currentToken.beginLine, "Detected generic error: " + e.getMessage()));
			}
			else
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
		Main main = new Main();
		main.parse(SpecsIo.read(args[0]));
	}
}