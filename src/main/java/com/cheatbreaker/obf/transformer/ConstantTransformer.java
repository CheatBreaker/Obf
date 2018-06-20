package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import com.cheatbreaker.obf.utils.AsmUtils;
import com.cheatbreaker.obf.utils.RandomUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

public class ConstantTransformer extends Transformer {

    public ConstantTransformer(Obf obf) {
        super(obf);
    }

    @Override
    public void visit(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (AsmUtils.codeSize(method) > 0x4000) {
                System.out.println(classNode.name + "." + method.name + method.desc + " is too large, skipping");
                continue;
            }
            for (Iterator<AbstractInsnNode> iter = method.instructions.iterator(); iter.hasNext(); ) {
                AbstractInsnNode insn = iter.next();
                if (AsmUtils.isPushInt(insn)) {
                    int value = AsmUtils.getPushedInt(insn);
                    method.instructions.insertBefore(insn, makeInt(value));
                    iter.remove();
                } else if (AsmUtils.isPushLong(insn)) {
                    long value = AsmUtils.getPushedLong(insn);
                    method.instructions.insertBefore(insn, makeLong(value));
                    iter.remove();
                } else if (insn.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldc = (LdcInsnNode) insn;
                    if (ldc.cst instanceof Float) {
                        method.instructions.insertBefore(insn, makeFloat((float) ldc.cst));
                        iter.remove();
                    } else if (ldc.cst instanceof Double) {
                        method.instructions.insertBefore(insn, makeDouble((double) ldc.cst));
                        iter.remove();
                    }
                }
            }
        }
    }

    private InsnList makeInt(int value) {
        InsnList instructions = new InsnList();
        int mask = random.nextInt();
        int a = random.nextInt() & mask | value;
        int b = random.nextInt() & ~mask | value;
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            // try to use bipush/sipush for lower values
            a = (short) a;
            b = (short) b;
        }
        instructions.add(AsmUtils.pushInt(a));
        instructions.add(AsmUtils.pushInt(b));
        instructions.add(new InsnNode(Opcodes.IAND));
        return instructions;
    }

    private InsnList makeLong(long value) {
        InsnList instructions = new InsnList();
        long mask = random.nextLong();
        long a = random.nextInt() & mask | value;
        long b = random.nextInt() & ~mask | value;
        instructions.add(AsmUtils.pushLong(a));
        instructions.add(AsmUtils.pushLong(b));
        instructions.add(new InsnNode(Opcodes.LAND));
        return instructions;
    }

    private InsnList makeFloat(float value) {
        InsnList instructions = new InsnList();
        for (int retry = 0; retry < 10; retry++) {
            float multiplier = (float) (random.nextInt(99) + 1) / (float) (random.nextInt(99) + 1);
            if (value / multiplier * multiplier == value) {
                RandomUtils.swap(random, new LdcInsnNode(value / multiplier), new LdcInsnNode(multiplier)).forEach(instructions::add);
                instructions.add(new InsnNode(Opcodes.FMUL));
                return instructions;
            }
        }
        instructions.add(new LdcInsnNode(value));
        return instructions;
    }

    private InsnList makeDouble(double value) {
        InsnList instructions = new InsnList();
        for (int retry = 0; retry < 10; retry++) {
            double multiplier = (double) (random.nextInt(99) + 1) / (double) (random.nextInt(99) + 1);
            if (value / multiplier * multiplier == value) {
                RandomUtils.swap(random, new LdcInsnNode(value / multiplier), new LdcInsnNode(multiplier)).forEach(instructions::add);
                instructions.add(new InsnNode(Opcodes.DMUL));
                return instructions;
            }
        }
        instructions.add(new LdcInsnNode(value));
        return instructions;
    }
}
