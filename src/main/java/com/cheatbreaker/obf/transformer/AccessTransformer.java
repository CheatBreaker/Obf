package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class AccessTransformer extends Transformer {
    public AccessTransformer(Obf obf) {
        super(obf);
    }

    @Override
    public void visit(ClassNode classNode) {
        classNode.access &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL);
        classNode.access |= Opcodes.ACC_PUBLIC;
        if ((classNode.access & Opcodes.ACC_INTERFACE) == 0) {
            for (FieldNode field : classNode.fields) {
                field.access &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL);
                field.access |= Opcodes.ACC_PUBLIC;
            }
            for (MethodNode method : classNode.methods) {
                method.access &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL);
                method.access |= Opcodes.ACC_PUBLIC;
            }
        }
    }
}
