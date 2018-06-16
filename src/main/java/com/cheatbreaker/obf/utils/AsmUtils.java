package com.cheatbreaker.obf.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class AsmUtils {
    public static AbstractInsnNode pushInt(int i) {
        if (i >= 0 && i <= 5) {
            return new InsnNode(Opcodes.ICONST_0 + i);
        }
        if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
            return new IntInsnNode(Opcodes.BIPUSH, i);
        }
        if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
            return new IntInsnNode(Opcodes.SIPUSH, i);
        }
        return new LdcInsnNode(i);
    }
}
