package verify.run;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Examin {
    public static void main(String[] args) throws IOException, InterruptedException {
        Set<String> files = getAllFiles(new File("E:\\Repositories\\heu-search\\examin\\org"), ".java");
        Set<String> jars = getAllFiles(new File("H:\\"), ".jar");

                compile(jars.toArray(new String[jars.size()]),
                files.toArray(new String[files.size()]),
                "E:\\Repositories\\heu-search\\examin\\out");
    }

    public static void compile(String[] jars, String[] files, String dest) throws IOException, InterruptedException {
        com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
        String classpath = ".";
        for (String jar : jars) {
            classpath += ";" + jar;
        }
        for (String file : files) {
            String[] cpargs = new String[] {"-cp", classpath, "-d", dest, file};
            int status = javac.compile(cpargs);

            if(status!=0){
                System.out.println("fail");
            }
            else {
                System.out.println("success");
            }
        }
    }

    public static Set<String> getAllFiles(File root, String postfix) {
        if (!root.exists()) {
            throw new RuntimeException("parameter shouldn't be null");
        }
        if (!root.isDirectory()) {
            Set<String> result = new HashSet<>();
            if (root.getPath().endsWith(postfix)) {
                result.add(root.getPath());
            }
            return result;
        }
        else {
            Set<String> result = new HashSet<>();
            File[] children = root.listFiles();
            if (children != null) {
                for (File child : children) {
                    result.addAll(getAllFiles(child, postfix));
                }
            }
            return result;
        }
    }
}
