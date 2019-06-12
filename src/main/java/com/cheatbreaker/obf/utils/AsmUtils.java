/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2018 CheatBreaker, LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cheatbreaker.obf.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class AsmUtils {
    public static boolean isPushInt(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        return (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5)
                || op == Opcodes.BIPUSH
                || op == Opcodes.SIPUSH
                || (op == Opcodes.LDC && ((LdcInsnNode) insn).cst instanceof Integer);
    }

    public static int getPushedInt(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5) {
            return op - Opcodes.ICONST_0;
        }
        if (op == Opcodes.BIPUSH || op == Opcodes.SIPUSH) {
            return ((IntInsnNode) insn).operand;
        }
        if (op == Opcodes.LDC) {
            Object cst = ((LdcInsnNode) insn).cst;
            if (cst instanceof Integer) {
                return (int) cst;
            }
        }
        throw new IllegalArgumentException("insn is not a push int instruction");
    }

    public static AbstractInsnNode pushInt(int value) {
        if (value >= -1 && value <= 5) {
            return new InsnNode(Opcodes.ICONST_0 + value);
        }
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return new IntInsnNode(Opcodes.BIPUSH, value);
        }
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return new IntInsnNode(Opcodes.SIPUSH, value);
        }
        return new LdcInsnNode(value);
    }

    public static boolean isPushLong(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        return op == Opcodes.LCONST_0
                || op == Opcodes.LCONST_1
                || (op == Opcodes.LDC && ((LdcInsnNode) insn).cst instanceof Long);
    }

    public static long getPushedLong(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op == Opcodes.LCONST_0) {
            return 0;
        }
        if (op == Opcodes.LCONST_1) {
            return 1;
        }
        if (op == Opcodes.LDC) {
            Object cst = ((LdcInsnNode) insn).cst;
            if (cst instanceof Long) {
                return (long) cst;
            }
        }
        throw new IllegalArgumentException("insn is not a push long instruction");
    }

    public static AbstractInsnNode pushLong(long value) {
        if (value == 0) {
            return new InsnNode(Opcodes.LCONST_0);
        }
        if (value == 1) {
            return new InsnNode(Opcodes.LCONST_1);
        }
        return new LdcInsnNode(value);
    }

    public static int codeSize(MethodNode methodNode) {
        CodeSizeEvaluator evaluator = new CodeSizeEvaluator(null);
        methodNode.accept(evaluator);
        return evaluator.getMaxSize();
    }
}
