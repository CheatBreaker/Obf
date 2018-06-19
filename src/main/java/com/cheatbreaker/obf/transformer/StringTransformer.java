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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StringTransformer extends Transformer {

    int currentId = 0;
    private Map<Integer, String> idToString = new HashMap<>();
    private Map<String, Integer> stringToId = new HashMap<>();

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
                        int id;
                        if (stringToId.containsKey(string)) {
                            id = stringToId.get(string);
                        } else {
                            id = currentId++;
                            idToString.put(id, string);
                            stringToId.put(string, id);
                        }
                        int mask = random.nextInt();
                        int a = random.nextInt() & mask | id;
                        int b = random.nextInt() & ~mask | id;
                        method.instructions.insertBefore(insn, AsmUtils.pushInt(a));
                        method.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.GETSTATIC, "generated/Strings", "strings", "[Ljava/lang/String;"));
                        method.instructions.insertBefore(insn, new InsnNode(Opcodes.SWAP));
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
        ClassNode classNode = new ClassNode();
        classNode.version = Opcodes.V1_8;
        classNode.access = Opcodes.ACC_PUBLIC;
        classNode.name = "generated/Strings";
        classNode.superName = "java/lang/Object";
        FieldNode strings = new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "strings", "[Ljava/lang/String;", null, null);
        classNode.fields.add(strings);
        MethodNode clinit = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        classNode.methods.add(clinit);
        List<Map.Entry<Integer, String>> entries = new ArrayList<>(idToString.entrySet());
        Collections.shuffle(entries, random);
        clinit.instructions.add(AsmUtils.pushInt(entries.size()));
        clinit.instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "Ljava/lang/String;"));
        for (Map.Entry<Integer, String> entry : entries) {
            clinit.instructions.add(new InsnNode(Opcodes.DUP));
            clinit.instructions.add(AsmUtils.pushInt(entry.getKey()));
            clinit.instructions.add(new LdcInsnNode(entry.getValue()));
            clinit.instructions.add(new InsnNode(Opcodes.AASTORE));
        }
        clinit.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, "generated/Strings", "strings", "[Ljava/lang/String;"));
        clinit.instructions.add(new InsnNode(Opcodes.RETURN));
        obf.addNewClass(classNode);
    }
}
