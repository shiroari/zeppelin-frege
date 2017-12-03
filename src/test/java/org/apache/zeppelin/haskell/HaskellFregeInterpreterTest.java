package org.apache.zeppelin.haskell;

import java.util.Properties;

import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterOutput;
import org.apache.zeppelin.interpreter.InterpreterOutputListener;
import org.apache.zeppelin.interpreter.InterpreterResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HaskellFregeInterpreterTest {

    private InterpreterContext context;
    private InterpreterOutput output;
    private InterpreterOutputListener outputListener;

    private HaskellFregeInterpreter interpreter;

    @Before
    public void setup() throws Exception {
        context = mock(InterpreterContext.class);
        outputListener = mock(InterpreterOutputListener.class);
        output = new InterpreterOutput(outputListener);
        when(context.out()).thenReturn(output);

        Properties props = new Properties();
        interpreter = new HaskellFregeInterpreter(props);
        interpreter.open();
    }

    @After
    public void cleanup() throws Exception {
        output.close();
        interpreter.close();
    }

    @Test
    public void shouldOutputCompilerError() {
        InterpreterResult result = interpreter.interpret("1 = 2", context);
        assertThat(result.toString(), containsString("syntax error"));
        assertEquals(InterpreterResult.Code.ERROR, result.code());
    }

    @Test
    public void shouldEvalExpression() {
        InterpreterResult result = interpreter.interpret("123", context);
        assertEquals("%text 123", result.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, result.code());
    }

    @Test
    public void shouldEvalComplexExpression() {
        InterpreterResult result = interpreter.interpret("show [x*x | x <- [1..10]]", context);
        assertEquals("%text [1, 4, 9, 16, 25, 36, 49, 64, 81, 100]", result.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, result.code());
    }

    @Test
    public void shouldDefineFunction() {
        InterpreterResult defResult = interpreter.interpret("my_func x y = x * y", context);
        assertEquals("", defResult.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, defResult.code());

        InterpreterResult callResult = interpreter.interpret("my_func 2 7", context);
        assertEquals("%text 14", callResult.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, callResult.code());
    }

    @Test
    public void shouldRedefineFunction() {
        InterpreterResult defResult1 = interpreter.interpret("my_func :: Int -> Int -> Int\n"
                + "my_func x y = x * y", context);
        assertEquals("", defResult1.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, defResult1.code());

        InterpreterResult defResult2 = interpreter.interpret("my_func :: Int -> Int -> Int\n"
                + "my_func x y = x + y", context);
        assertEquals("", defResult2.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, defResult2.code());

        InterpreterResult callResult = interpreter.interpret("my_func 2 7", context);
        assertEquals("%text 9", callResult.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, callResult.code());
    }

    @Test
    public void shouldCallDisplayMethod() {
        InterpreterResult result1 = interpreter.interpret("my_func x y = x * y\n"
                + "z_display = my_func 2 7", context);
        assertEquals("%text 14", result1.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, result1.code());
    }

    @Test
    public void shouldCallFakeMainMethod() {
        InterpreterResult result = interpreter.interpret("my_func = 123\n"
                + "z_main = my_func", context);
        assertEquals("%text 123", result.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, result.code());
    }

    @Test
    public void shouldIgnoreComments() {
        InterpreterResult callResult = interpreter.interpret("-- comment comment comment\n"
                + "{- comment comment comment\n"
                + "   comment comment comment -}\n"
                + "z_display = 42", context);
        assertEquals("%text 42", callResult.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, callResult.code());
    }

    @Test
    public void shouldSupportModules() {
        InterpreterResult defResult = interpreter.interpret("module test.MyModule where\n"
                + "hello = 1\n", context);
        assertEquals("", defResult.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, defResult.code());

        InterpreterResult moduleResult = interpreter.interpret("import test.MyModule", context);
        assertEquals("", moduleResult.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, moduleResult.code());

        InterpreterResult callResult = interpreter.interpret("hello", context);
        assertEquals("%text 1", callResult.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, callResult.code());
    }

    @Test
    public void shouldPrintToOutputAndFlush() throws Exception {
        InterpreterResult result = interpreter.interpret("print 1", context);
        assertEquals("", result.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, result.code());
        assertEquals(1, output.toInterpreterResultMessage().size());
        assertEquals("%text 1", output.toInterpreterResultMessage().get(0).toString());
    }

    @Test
    public void shouldPrintToOutput() throws Exception {
        InterpreterResult result = interpreter.interpret("do\n"
                + "print 1\n"
                + "println 2\n"
                + "putStrLn \"3\"", context);
        assertEquals("", result.toString());
        assertEquals(InterpreterResult.Code.SUCCESS, result.code());
        assertEquals(1, output.toInterpreterResultMessage().size());
        assertEquals("%text 12\n3\n", output.toInterpreterResultMessage().get(0).toString());
    }
}
