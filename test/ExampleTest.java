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
}
