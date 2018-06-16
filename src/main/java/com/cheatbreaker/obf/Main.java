package com.cheatbreaker.obf;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("input").withRequiredArg().required().ofType(File.class);
        parser.accepts("output").withRequiredArg().required().ofType(File.class);

        OptionSet options;

        try {
            options = parser.parse(args);
        } catch (OptionException ex) {
            System.out.println("Usage: obf --input <inputjar> --output <outputjar>");
            System.out.println(ex.getMessage());
            System.exit(1);
            return;
        }

        File inputFile = (File) options.valueOf("input");
        File outputFile = (File) options.valueOf("output");
        new Obf(inputFile, outputFile);
    }
}
