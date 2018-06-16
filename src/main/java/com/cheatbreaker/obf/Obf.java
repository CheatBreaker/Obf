package com.cheatbreaker.obf;

import com.cheatbreaker.obf.utils.StreamUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Obf {

    private final Random random;

    public Obf(File inputFile, File outputFile) {
        random = new Random();

        try {
            JarFile inputJar = new JarFile(inputFile);

            try (JarOutputStream out = new JarOutputStream(new FileOutputStream(outputFile))) {
                for (Enumeration<JarEntry> iter = inputJar.entries(); iter.hasMoreElements(); ) {
                    JarEntry entry = iter.nextElement();
                    try (InputStream in = inputJar.getInputStream(entry)) {
                        byte[] data;
                        if (entry.getName().endsWith(".class")) {
                            data = processClass(in);
                        } else {
                            data = StreamUtils.readAll(in);
                        }
                        out.putNextEntry(new JarEntry(entry.getName()));
                        out.write(data);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private byte[] processClass(InputStream in) throws IOException {
        ClassReader reader = new ClassReader(in);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        System.out.println(classNode.name);

        for (MethodNode method : classNode.methods) {
            for (Iterator<AbstractInsnNode> iter = method.instructions.iterator(); iter.hasNext(); ) {
                AbstractInsnNode insn = iter.next();
                switch (insn.getOpcode()) {
                    case Opcodes.LDC:
                        LdcInsnNode ldc = (LdcInsnNode) insn;
                        if (ldc.cst instanceof Integer) {
                            obfuscateLdcInt(iter, ldc, method.instructions);
                        } else if (ldc.cst instanceof Long) {
                            obfuscateLdcLong(iter, ldc, method.instructions);
                        } else if (ldc.cst instanceof Double) {
                            obfuscateLdcDouble(iter, ldc, method.instructions);
                        } else if (ldc.cst instanceof Float) {
                            obfuscateLdcFloat(iter, ldc, method.instructions);
                        }
                        break;
                    case Opcodes.SIPUSH:
                        IntInsnNode sipush = (IntInsnNode) insn;
                        obfuscateSipush(iter, sipush, method.instructions);
                        break;
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private void obfuscateLdcInt(Iterator<AbstractInsnNode> iter, LdcInsnNode ldc, InsnList instructions) {
        int cst = (int) ldc.cst;
        int xor = random.nextInt();
        InsnList insns = new InsnList();
        if (random.nextBoolean()) {
            insns.add(new LdcInsnNode(cst ^ xor));
            insns.add(new LdcInsnNode(xor));
        } else {
            insns.add(new LdcInsnNode(xor));
            insns.add(new LdcInsnNode(cst ^ xor));
        }
        insns.add(new InsnNode(Opcodes.IXOR));
        instructions.insertBefore(ldc, insns);
        iter.remove();
    }

    private void obfuscateLdcLong(Iterator<AbstractInsnNode> iter, LdcInsnNode ldc, InsnList instructions) {
        long cst = (long) ldc.cst;
        long xor = random.nextLong();
        InsnList insns = new InsnList();
        if (random.nextBoolean()) {
            insns.add(new LdcInsnNode(cst ^ xor));
            insns.add(new LdcInsnNode(xor));
        } else {
            insns.add(new LdcInsnNode(xor));
            insns.add(new LdcInsnNode(cst ^ xor));
        }
        insns.add(new InsnNode(Opcodes.LXOR));
        instructions.insertBefore(ldc, insns);
        iter.remove();
    }

    private void obfuscateLdcFloat(Iterator<AbstractInsnNode> iter, LdcInsnNode ldc, InsnList instructions) {
        float cst = (float) ldc.cst;
        int icst = (int) cst;
        if (icst == cst) {
            int xor = random.nextInt();
            InsnList insns = new InsnList();
            if (random.nextBoolean()) {
                insns.add(new LdcInsnNode(icst ^ xor));
                insns.add(new LdcInsnNode(xor));
            } else {
                insns.add(new LdcInsnNode(xor));
                insns.add(new LdcInsnNode(icst ^ xor));
            }
            insns.add(new InsnNode(Opcodes.IXOR));
            insns.add(new InsnNode(Opcodes.I2F));
            instructions.insertBefore(ldc, insns);
            iter.remove();
        }
    }

    private void obfuscateLdcDouble(Iterator<AbstractInsnNode> iter, LdcInsnNode ldc, InsnList instructions) {
        double cst = (double) ldc.cst;
        int icst = (int) cst;
        if (icst == cst) {
            int xor = random.nextInt();
            InsnList insns = new InsnList();
            if (random.nextBoolean()) {
                insns.add(new LdcInsnNode(icst ^ xor));
                insns.add(new LdcInsnNode(xor));
            } else {
                insns.add(new LdcInsnNode(xor));
                insns.add(new LdcInsnNode(icst ^ xor));
            }
            insns.add(new InsnNode(Opcodes.IXOR));
            insns.add(new InsnNode(Opcodes.I2D));
            instructions.insertBefore(ldc, insns);
            iter.remove();
        }
    }

    private void obfuscateSipush(Iterator<AbstractInsnNode> iter, IntInsnNode sipush, InsnList instructions) {
        int si = sipush.operand;
        int xor = random.nextInt() & 0xFFFF;
        InsnList insns = new InsnList();
        if (random.nextBoolean()) {
            insns.add(new IntInsnNode(Opcodes.SIPUSH, si ^ xor));
            insns.add(new IntInsnNode(Opcodes.SIPUSH, xor));
        } else {
            insns.add(new IntInsnNode(Opcodes.SIPUSH, xor));
            insns.add(new IntInsnNode(Opcodes.SIPUSH, si ^ xor));
        }
        insns.add(new InsnNode(Opcodes.IXOR));
        instructions.insertBefore(sipush, insns);
        iter.remove();
    }
}
