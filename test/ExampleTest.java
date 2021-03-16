import static org.junit.Assert.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;
import java.io.StringReader;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;


public class ExampleTest {

    /*@Test
    public void testExpression() {		
		assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
	}*/

    @Test
    public void testHelloWorld() {
        String jmmCode = SpecsIo.getResource("fixtures/public/HelloWorld.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFindMaximum() {
        String jmmCode = SpecsIo.getResource("fixtures/public/FindMaximum.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testLazysort() {
        String jmmCode = SpecsIo.getResource("fixtures/public/Lazysort.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testLife() {
        String jmmCode = SpecsIo.getResource("fixtures/public/Life.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testMonteCarloPi() {
        String jmmCode = SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testQuickSort() {
        String jmmCode = SpecsIo.getResource("fixtures/public/QuickSort.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testSimple() {
        String jmmCode = SpecsIo.getResource("fixtures/public/Simple.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testTicTacToe() {
        String jmmCode = SpecsIo.getResource("fixtures/public/TicTacToe.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testWhileAndIf() {
        String jmmCode = SpecsIo.getResource("fixtures/public/WhileAndIf.jmm");
        TestUtils.parse(jmmCode);
    }

    /*Fail semantic*/

    /*@Test
    public void testFail_ArrIndexNotInt() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/arr_index_not_int.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_ArrSizeNotInt() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/arr_size_not_int.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_BadArguments() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/badArguments.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_BinopIncomp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/bino_incomp.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_FuncNotFound() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/funcNotFound.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_SimpleLength() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/simple_length.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_VarExpIncomp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/var_exp_incomp.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_LitIncomp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/var_lit_incomp.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_VarUndef() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/var_undef.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_VarNotInit() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/semantic/varNotInit.jmm");
        TestUtils.parse(jmmCode);
    }*/

    /*Fail syntactical*/

    @Test
    public void testFail_BlowUp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntatical/BlowUp.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_CompleteWhileTest() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntatical/CompleteWhileTest.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_LengthError() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntatical/CompleteLengthError.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_MissingRightPar() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntatical/MissingRightPar.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_MultipleSequential() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntatical/MultipleSequential.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_NestedLoop() {
        String jmmCode = SpecsIo.getResource("fixtures/public/fail/syntatical/NestedLoop.jmm");
        TestUtils.parse(jmmCode);
    }

}
