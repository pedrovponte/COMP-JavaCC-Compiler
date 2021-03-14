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

    @Test
    public void testFail_arrindexnotint() {
        String jmmCode = SpecsIo.getResource("fixtures/public/arr_index_not_int.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_arrsizenotint() {
        String jmmCode = SpecsIo.getResource("fixtures/public/arr_size_not_int.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_badArguments() {
        String jmmCode = SpecsIo.getResource("fixtures/public/badArguments.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_binopincomp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/bino_incomp.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_funcNotFound() {
        String jmmCode = SpecsIo.getResource("fixtures/public/funcNotFound.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_simpleLength() {
        String jmmCode = SpecsIo.getResource("fixtures/public/simple_length.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_varexpincomp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/var_exp_incomp.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_litincomp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/var_lit_incomp.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_varundef() {
        String jmmCode = SpecsIo.getResource("fixtures/public/var_undef.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_varNotInit() {
        String jmmCode = SpecsIo.getResource("fixtures/public/varNotInit.jmm");
        TestUtils.parse(jmmCode);
    }

    /*Fail syntactical*/


    @Test
    public void testFail_BlowUp() {
        String jmmCode = SpecsIo.getResource("fixtures/public/BlowUp.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_CompleteWhileTest() {
        String jmmCode = SpecsIo.getResource("fixtures/public/CompleteWhileTest.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_MissingRightPar() {
        String jmmCode = SpecsIo.getResource("fixtures/public/MissingRightPar.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_MultipleSequential() {
        String jmmCode = SpecsIo.getResource("fixtures/public/MultipleSequential.jmm");
        TestUtils.parse(jmmCode);
    }

    @Test
    public void testFail_NestedLoop() {
        String jmmCode = SpecsIo.getResource("fixtures/public/NestedLoop.jmm");
        TestUtils.parse(jmmCode);
    }

}
