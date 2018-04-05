package p_heu.run;

import fix.entity.ImportPath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GenerateClass {
    public static void main(String[] args) {

        compileJava(ImportPath.verifyPath + "\\exportExamples\\" + ImportPath.projectName, ImportPath.verifyPath + "\\generateClass");

    }

    public static void compileJava(String dirPath, String desk) {
        //源代码
        //遍历该目录下的所有java文件
        File file = new File(dirPath);
        File[] fileArr = file.listFiles();
        List<String> fileList = new ArrayList<String>();

        for (File f : fileArr) {
            fileList.add(f.toString());
        }
        compile(fileList, desk);

        /*if(dirPath.contains(".java")){
            System.out.println(dirPath + "======");
            dirPath = dirPath.substring(0, dirPath.length() - 5);
            //处理包名有几层的情况
            if(dirPath.contains(".")) {
                dirPath = dirPath.replaceAll("\\.","/");
            }
            dirPath += ".java";
        } else {
            if(dirPath.contains(".")) {
                dirPath = dirPath.replaceAll("\\.","/");
            }
        }

        File src = new File(dirPath);
        File dest = new File(desk);
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // 递归编译
                compileJava(srcFile.toString(), destFile.toString());
            }
        } else {
            List<String> fileList = new ArrayList<String>();
            fileList.add(src.toString());
            compile(fileList,desk);
        }*/

    }


    public static void compile(List<String> files, String dest){
        com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();

//        String[] cpargs = new String[] {"-d", dest, files};
        String[] cpargs = new String[files.size() + 2];
//        String[] cpargs = new String[files.size() + 4];
        cpargs[0] = "-d";
        cpargs[1] = dest;
        /*cpargs[2] = "-classpath";
        cpargs[3] = "D:/Patch/lib/additionalJAR/lib/realLib/javaee.jar;D:/Patch/lib/additionalJAR/lib/realLib/jacontebe-1.0.jar;D:/Patch/lib/additionalJAR/jdmkrt.jar;" +
                "D:/Patch/lib/additionalJAR/lib/realLib/coring-1.4.jar;D:/Patch/lib/additionalJAR/lib/realLib/commons-collections-2.1.jar;" +
                "D:/Patch/lib/additionalJAR/lib/realLib/commons-pool-1.2.jar;D:/Patch/lib/additionalJAR/lib/realLib/mockito-all-1.9.5.jar";*/
        for (int i = 2; i < cpargs.length; ++i) {
//        for (int i = 4; i < cpargs.length; ++i) {
            cpargs[i] = files.get(i - 2);
//            cpargs[i] = files.get(i - 4);
        }
        int status = javac.compile(cpargs);

        //输出
        /*if(status!=0){
            System.out.println("fail");
        }
        else {
            System.out.println("success");
        }*/
    }
}
