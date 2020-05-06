package org.lepovirta.jmxhealthproxy;

import org.junit.Assert;
import org.junit.Test;

public class ExpectationEvalTest {

    @Test
    public void testEq() {
        Assert.assertTrue(new ExpectationEval("foobar").test("foobar"));
        Assert.assertFalse(new ExpectationEval("foobar").test("fooba"));
        Assert.assertTrue(new ExpectationEval("=asdf").test("asdf"));
        Assert.assertFalse(new ExpectationEval("=asdf").test("asdfg"));
    }

    @Test
    public void testRegEx() {
        Assert.assertTrue(new ExpectationEval("~foo.*").test("foobar"));
        Assert.assertFalse(new ExpectationEval("~foo.*").test("fot"));
        Assert.assertTrue(new ExpectationEval("~first.*second").test("first_second"));
    }

    @Test
    public void testLte() {
        Assert.assertTrue(new ExpectationEval("<=5").test("4"));
        Assert.assertTrue(new ExpectationEval("<=5").test("5"));
        Assert.assertFalse(new ExpectationEval("<=5").test("6"));
    }

    @Test
    public void testLt() {
        Assert.assertTrue(new ExpectationEval("<5").test("4"));
        Assert.assertFalse(new ExpectationEval("<5").test("5"));
        Assert.assertFalse(new ExpectationEval("<5").test("6"));
    }

    @Test
    public void testGte() {
        Assert.assertFalse(new ExpectationEval(">=5").test("4"));
        Assert.assertTrue(new ExpectationEval(">=5").test("5"));
        Assert.assertTrue(new ExpectationEval(">=5").test("6"));
    }

    @Test
    public void testGt() {
        Assert.assertFalse(new ExpectationEval(">5").test("4"));
        Assert.assertFalse(new ExpectationEval(">5").test("5"));
        Assert.assertTrue(new ExpectationEval(">5").test("6"));
    }
}
