package com.sidrat.bytecode;

import org.junit.Assert;
import org.junit.Test;

import javassist.bytecode.Mnemonic;

/**
 * Test for obvious errors in the Opcodes enum to protect against regressions.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class OpcodesValidatorTest {

    @Test
    public void testOpcodesMatch() {
        for (Opcodes opcode : Opcodes.values()) {
            if (opcode == Opcodes.BREAKPOINT)
                continue;
            int code = opcode.getCode();
            String mnemonic = Mnemonic.OPCODE[code].toUpperCase();
            Assert.assertEquals(mnemonic, opcode.name());
        }
    }
}
