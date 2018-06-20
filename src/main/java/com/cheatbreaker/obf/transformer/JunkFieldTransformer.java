package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import com.cheatbreaker.obf.utils.RandomUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class JunkFieldTransformer extends Transformer {
    public JunkFieldTransformer(Obf obf) {
        super(obf);
    }

    @Override
    public void visit(ClassNode classNode) {
        if ((classNode.access & Opcodes.ACC_INTERFACE) != 0) {
            return;
        }

        // add fields of this class type to a few random classes
        for (int i = random.nextInt(3) + 1; i >= 0; i--) {
            ClassNode target = RandomUtils.choice(random, obf.getClasses());
            if ((target.access & Opcodes.ACC_INTERFACE) != 0) {
                continue;
            }
            String name = "__junk" + Math.abs(random.nextLong());
            target.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, name, "L" + classNode.name + ";", null, null));
        }
    }
}
