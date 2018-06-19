package com.cheatbreaker.obf.transformer;

import com.cheatbreaker.obf.Obf;
import org.objectweb.asm.tree.ClassNode;

import java.util.Random;

public abstract class Transformer {

    protected final Obf obf;
    protected final Random random;

    public Transformer(Obf obf) {
        this.obf = obf;
        this.random = obf.getRandom();
    }

    public abstract void visit(ClassNode classNode);

    public void after() {
    }
}
