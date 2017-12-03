package org.apache.zeppelin.haskell;

import javax.script.ScriptException;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    @Test
    public void testHasMain() throws ScriptException {
        assertFalse(Parser.hasMain(""));
        assertFalse(Parser.hasMain("zz_main="));
        assertFalse(Parser.hasMain("z_maintain="));
        assertFalse(Parser.hasMain("z_main"));
        assertTrue(Parser.hasMain("z_main="));
        assertTrue(Parser.hasMain("z_main ="));
        assertTrue(Parser.hasMain("\nz_main\n="));
    }

    @Test
    public void testHasDisplay() throws ScriptException {
        assertFalse(Parser.hasDisplay(""));
        assertFalse(Parser.hasDisplay("zz_display="));
        assertFalse(Parser.hasDisplay("z_displayy="));
        assertFalse(Parser.hasDisplay("z_display"));
        assertTrue(Parser.hasDisplay("z_display="));
        assertTrue(Parser.hasDisplay("z_display ="));
        assertTrue(Parser.hasDisplay("\nz_display\n="));
    }
}