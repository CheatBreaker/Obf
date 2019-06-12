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
