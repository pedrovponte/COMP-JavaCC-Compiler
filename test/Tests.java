import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.specs.util.SpecsIo;

import java.util.ArrayList;


public class Tests {

    @Test
    public void testHelloWorld() {
        String jmmCode = SpecsIo.getResource("fixtures/public/HelloWorld.jmm");
        //TestUtils.parse(jmmCode);
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        TestUtils.noErrors(res.getReports());
    }

    @Test
    public void testFindMaximum() {
        String jmmCode = SpecsIo.getResource("fixtures/public/FindMaximum.jmm");
        //TestUtils.parse(jmmCode);
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        TestUtils.noErrors(res.getReports());
    }

    @Test
    public void testLazysort() {
        String jmmCode = SpecsIo.getResource("fixtures/public/Lazysort.jmm");
        JmmParserResult res = TestUtils.parse(jmmCode);
        TestUtils.noErrors(res.getReports());
        /*JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        TestUtils.noErrors(res.getReports());*/
    }

    @Test
    public void testLife() {
        String jmmCode = SpecsIo.getResource("fixtures/public/Life.jmm");
        //TestUtils.parse(jmmCode);
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        TestUtils.noErrors(res.getReports());
    }

    @Test
    public void testMonteCarloPi() {
        String jmmCode = SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm");
        //TestUtils.parse(jmmCode);
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        TestUtils.noErrors(res.getReports());
    }

    @Test
    public void testQuickSort() {
        String jmmCode = SpecsIo.getResource("fixtures/public/QuickSort.jmm");
        JmmParserResult res = TestUtils.parse(jmmCode);
        TestUtils.noErrors(res.getReports());
        /*JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        TestUtils.noErrors(res.getReports());*/
    }

    @Test
    public void testSimple() {
        String jmmCode = SpecsIo.getResource("fixtures/public/Simple.jmm");
        //TestUtils.parse(jmmCode);
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        TestUtils.noErrors(res.getReports());
    }

    @Test
    public void testTicTacToe() {
        String jmmCode = SpecsIo.getResource("fixtures/public/TicTacToe.jmm");
        //TestUtils.parse(jmmCode);
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        TestUtils.noErrors(res.getReports());
    }

    @Test
    public void testWhileAndIf() {
        String jmmCode = SpecsIo.getResource("fixtures/public/WhileAndIF.jmm");
        //TestUtils.parse(jmmCode);
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        TestUtils.noErrors(res.getReports());
    }

    /*Fail semantic*/

    @Test
    public void testFail_ArrIndexNotInt() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/arr_index_not_int.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_ArrSizeNotInt() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/arr_size_not_int.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_BadArguments() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/badArguments.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_BinopIncomp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/binop_incomp.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_FuncNotFound() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/funcNotFound.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_SimpleLength() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/simple_length.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_VarExpIncomp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/var_exp_incomp.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_VarLitIncomp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/var_lit_incomp.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_VarUndef() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/var_undef.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_VarNotInit() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/varNotInit.jmm");
        JmmSemanticsResult res = TestUtils.analyse(TestUtils.parse(jmmCode));
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }


    /*Fail syntactical*/

    @Test
    public void testFail_BlowUp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntactical/BlowUp.jmm");
        var res = TestUtils.parse(jmmCode);
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());

    }

    @Test
    public void testFail_CompleteWhileTest() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntactical/CompleteWhileTest.jmm");
        var res = TestUtils.parse(jmmCode);
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_LengthError() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntactical/LengthError.jmm");
        var res = TestUtils.parse(jmmCode);
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_MissingRightPar() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntactical/MissingRightPar.jmm");
        var res = TestUtils.parse(jmmCode);
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_MultipleSequential() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntactical/MultipleSequential.jmm");
        var res = TestUtils.parse(jmmCode);
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }

    @Test
    public void testFail_NestedLoop() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntactical/NestedLoop.jmm");
        var res = TestUtils.parse(jmmCode);
        System.out.println("Report: " + res.getReports());
        TestUtils.mustFail(res.getReports());
    }
}
