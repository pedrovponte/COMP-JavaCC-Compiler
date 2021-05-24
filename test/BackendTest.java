
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.specs.util.SpecsIo;

public class BackendTest {

    @Test
    public void testFindMaximum() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        //assertEquals("30", output.trim());
    }

    @Test
    public void testHelloWorld() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("Hello, World!", output.trim());
    }

    /*@Test
    public void testLazysort() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Lazysort.jmm"));
        TestUtils.noErrors(result.getReports());

        //var output = result.run();
        //assertEquals("30", output.trim());
    }*/

    @Test
    public void testLife() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Life.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        //assertEquals("30", output.trim());
    }

    @Test
    public void testMonteCarloPi() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        //assertEquals("30", output.trim());
    }

    /*@Test
    public void testQuickSort() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/QuickSort.jmm"));
        TestUtils.noErrors(result.getReports());

        //var output = result.run();
        //assertEquals("30", output.trim());
    }*/

    @Test
    public void testSimple() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Simple.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("30", output.trim());
    }

    @Test
    public void testTicTacToe() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        //assertEquals("30", output.trim());
    }

    @Test
    public void testWhileAndIf() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/WhileAndIf.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("10\r\n10\r\n10\r\n10\r\n10\r\n10\r\n10\r\n10\r\n10\r\n10", output.trim());
    }

    @Test
    public void testTeste1() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/customize/teste1_geral.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        //assertEquals("30", output.trim());
    }

    @Test
    public void testTeste2() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/customize/teste2_geralBlockStatements.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        //assertEquals("30", output.trim());
    }

    @Test
    public void testTeste3() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/customize/teste3_geralConditionalsAndArrays.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        //assertEquals("30", output.trim());
    }

    @Test
    public void testTeste5() {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/public/customize/teste7_extraVariableLoading.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("10000", output.trim());
    }

    @Test
    public void jasmin_test() {
        JasminResult result = TestUtils.backend(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/myclass1.ollir")));
    }
}
