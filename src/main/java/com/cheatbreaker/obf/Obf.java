package com.cheatbreaker.obf;

import com.cheatbreaker.obf.transformer.AccessTransformer;
import com.cheatbreaker.obf.transformer.ConstantTransformer;
import com.cheatbreaker.obf.transformer.JunkFieldTransformer;
import com.cheatbreaker.obf.transformer.ShuffleTransformer;
import com.cheatbreaker.obf.transformer.StringTransformer;
import com.cheatbreaker.obf.transformer.Transformer;
import com.cheatbreaker.obf.utils.StreamUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Obf {

    private final Random random;
    private final List<ClassNode> classes = new ArrayList<>();
    private final List<Transformer> transformers = new ArrayList<>();
    private final List<ClassNode> newClasses = new ArrayList<>();

    public Obf(File inputFile, File outputFile) throws IOException {
        random = new Random();

        transformers.add(new ConstantTransformer(this));
        transformers.add(new StringTransformer(this));
        transformers.add(new JunkFieldTransformer(this));
        transformers.add(new AccessTransformer(this));
        transformers.add(new ShuffleTransformer(this));

        JarFile inputJar = new JarFile(inputFile);

        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(outputFile))) {

            // read all classes into this.classes and copy all resources to output jar
            System.out.println("Reading jar...");
            for (Enumeration<JarEntry> iter = inputJar.entries(); iter.hasMoreElements(); ) {
                JarEntry entry = iter.nextElement();
                try (InputStream in = inputJar.getInputStream(entry)) {
                    if (entry.getName().endsWith(".class")) {
                        ClassReader reader = new ClassReader(in);
                        ClassNode classNode = new ClassNode();
                        reader.accept(classNode, 0);
                        classes.add(classNode);
                    } else {
                        out.putNextEntry(new JarEntry(entry.getName()));
                        StreamUtils.copy(in, out);
                    }
                }
            }

            // shuffle the entries in case the order in the output jar gives away information
            Collections.shuffle(classes, random);

            System.out.println("Transforming classes...");
            for (Transformer transformer : transformers) {
                System.out.println("Running " + transformer.getClass().getSimpleName() + "...");
                classes.forEach(transformer::visit);
            }
            for (Transformer transformer : transformers) {
                transformer.after();
            }

            System.out.println("Writing classes...");
            for (ClassNode classNode : classes) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                out.putNextEntry(new JarEntry(classNode.name + ".class"));
                out.write(writer.toByteArray());
            }

            System.out.println("Writing generated classes...");
            for (ClassNode classNode : newClasses) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                classNode.accept(writer);
                out.putNextEntry(new JarEntry(classNode.name + ".class"));
                out.write(writer.toByteArray());
            }
        }
    }

    public Random getRandom() {
        return random;
    }

    public List<ClassNode> getClasses() {
        return classes;
    }

    public void addNewClass(ClassNode classNode) {
        newClasses.add(classNode);
    }
}
