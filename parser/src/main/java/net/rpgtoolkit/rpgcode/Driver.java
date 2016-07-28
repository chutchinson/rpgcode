package net.rpgtoolkit.rpgcode;

import net.rpgtoolkit.blade.ir.CompilationUnit;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Driver {

    public static class ProgramFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File file, String s) {
            return s.endsWith(".prg");
        }

    }

    public static class MonitorParserListener implements ParserListener {

        private boolean success = true;
        private int errors = 0;

        @Override
        public void error(ParseError error) {
            success = false;
            errors++;
        }

    }

    public static String readFile(File file) {

        try {
            final byte[] data = Files.readAllBytes(Paths.get(file.toURI()));
            final ByteBuffer buffer = ByteBuffer.wrap(data);
            final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
            final CharBuffer decoded = decoder.decode(buffer);
            return decoded.toString();
        }
        catch (IOException ex) {
            return "";
        }

    }

    public static void parseDirectory(String path) {

        final File dir = new File(path);
        final FilenameFilter filter = new ProgramFilenameFilter();

        if (!dir.isDirectory())
            return;

        System.out.println(
                String.format("parsing files in \"%s\"", dir.getAbsolutePath()));

        long start = System.currentTimeMillis();
        int count = 0;
        int total = 0;

        for (final File file : dir.listFiles(filter)) {
            total++;
            if (parseFile(file))
                count++;
            else
                System.err.println(String.format("failed: %s", file.getName()));
        }

        int failed = total - count;
        long duration = System.currentTimeMillis() - start;

        float coverage = (count / (float) total) * 100;

        System.out.println("done");
        System.out.println(String.format("coverage: %.02f%% (%d/%d)", coverage, count, total));
        System.out.println(String.format("failed: %.02f%% (%d)", failed / (float) total * 100, failed));
        System.out.println(String.format("parsed in %s ms", duration));

    }

    public static boolean parseFile(File file) {

        final String input = readFile(file);
        final Lexer lexer = new Lexer(input);
        final Parser parser = new Parser(lexer);
        final MonitorParserListener listener = new MonitorParserListener();

        try {
            final CompilationUnit unit = parser.parse();
            if (unit != null)
                return true;
        }
        catch (Exception ex) {
            System.err.println(
                    String.format("exception: %s", ex.getMessage()));
            return false;
        }

        parser.removeListener(listener);

        return listener.success;

    }


    public static void main(String[] args) {

        final String path = "C:\\Users\\chris\\Downloads\\rpgtoolkit321\\RPGToolkit 3.2.1\\Toolkit3\\game\\TheWizardsTower\\Prg";

        parseDirectory(path);

        System.out.println("done");

    }

}
