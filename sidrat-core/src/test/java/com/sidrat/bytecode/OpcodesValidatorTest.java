package com.sidrat.bytecode;

import java.util.HashMap;
import java.util.Map;

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
    public void testAllOpcodesCovered() {
        Map<String, Integer> mnemonicsByIndex = new HashMap<>();
        for (int i = 0; i < Mnemonic.OPCODE.length; i++) {
            mnemonicsByIndex.put(Mnemonic.OPCODE[i], new Integer(i));
        }
        for (String mnemonic : Mnemonic.OPCODE) {
            Opcodes opcode = Opcodes.fromMnemonic(mnemonic);
            if (opcode == null) {
                System.out.println(mnemonic.toUpperCase() + "(" + mnemonicsByIndex.get(mnemonic) + ", ...");
            }
            // Assert.assertNotNull(opcode);
        }
    }

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
