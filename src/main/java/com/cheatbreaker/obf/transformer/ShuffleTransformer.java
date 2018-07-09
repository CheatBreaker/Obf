package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;

public class ShuffleTransformer extends Transformer {

    public ShuffleTransformer(Obf obf) {
        super(obf);
    }

    @Override
    public void visit(ClassNode classNode) {
        Collections.shuffle(classNode.fields, random);
        Collections.shuffle(classNode.methods, random);
    }
}
