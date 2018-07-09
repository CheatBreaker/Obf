package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import com.cheatbreaker.obf.utils.AsmUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StringTransformer extends Transformer {

    private static final int PARTITION_BITS = 10;
    private static final int PARTITION_SIZE = 1 << PARTITION_BITS;
    private static final int PARTITION_MASK = PARTITION_SIZE - 1;
    private List<String> strings = new ArrayList<>();

    public StringTransformer(Obf obf) {
        super(obf);
    }

    @Override
    public void visit(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for (Iterator<AbstractInsnNode> iter = method.instructions.iterator(); iter.hasNext(); ) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldc = (LdcInsnNode) insn;
                    if (ldc.cst instanceof String) {
                        String string = (String) ldc.cst;
                        int id = strings.indexOf(string);
                        if (id == -1) {
                            id = strings.size();
                            strings.add(string);
                        }
                        int index = id & PARTITION_MASK;
                        int classId = id >> PARTITION_BITS;
                        int mask = (short) random.nextInt();
                        int a = (short) random.nextInt() & mask | index;
                        int b = (short) random.nextInt() & ~mask | index;
                        method.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.GETSTATIC, "generated/Strings" + classId, "strings", "[Ljava/lang/String;"));
                        method.instructions.insertBefore(insn, AsmUtils.pushInt(a));
                        method.instructions.insertBefore(insn, AsmUtils.pushInt(b));
                        method.instructions.insertBefore(insn, new InsnNode(Opcodes.IAND));
                        method.instructions.insertBefore(insn, new InsnNode(Opcodes.AALOAD));
                        iter.remove();
                    }
                }
            }
        }
    }

    @Override
    public void after() {
        for (int classId = 0; classId <= strings.size() >> PARTITION_BITS; classId++) {
            ClassNode classNode = new ClassNode();
            classNode.version = Opcodes.V1_8;
            classNode.access = Opcodes.ACC_PUBLIC;
            classNode.name = "generated/Strings" + classId;
            classNode.superName = "java/lang/Object";
            classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "strings", "[Ljava/lang/String;", null, null));
            MethodNode clinit = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            classNode.methods.add(clinit);
            int start = classId << PARTITION_BITS;
            int end = Math.min(start + PARTITION_SIZE, strings.size());
            clinit.instructions.add(AsmUtils.pushInt(end - start));
            clinit.instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "Ljava/lang/String;"));
            for (int id = start; id < end; id++) {
                clinit.instructions.add(new InsnNode(Opcodes.DUP));
                clinit.instructions.add(AsmUtils.pushInt(id & PARTITION_MASK));
                clinit.instructions.add(new LdcInsnNode(strings.get(id)));
                clinit.instructions.add(new InsnNode(Opcodes.AASTORE));
            }
            clinit.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, "strings", "[Ljava/lang/String;"));
            clinit.instructions.add(new InsnNode(Opcodes.RETURN));
            obf.addNewClass(classNode);
        }
    }
}
