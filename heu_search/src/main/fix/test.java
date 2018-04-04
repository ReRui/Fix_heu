package fix;

import fix.analyzefile.UseASTAnalysisClass;
import fix.entity.ImportPath;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import org.eclipse.jdt.core.dom.*;
import p_heu.entity.filter.Filter;
import p_heu.listener.SequenceProduceListener;
import p_heu.run.GenerateClass;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;

public class Test {
    static  int tettt = 0;

    public static void main(String[] args) {

        String path = "D:\\Patch\\exportExamples\\org\\apache\\log4j";
        /*File src = new File(path);
        File dest = new File("C:\\Users\\lhr\\Desktop\\a");
        try {
            copyFolder(src,dest);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        File root = new File(path);
        List<File> files = new ArrayList<File>();
        listFiles(files, root);
        List<String> list = new ArrayList<String>();
        for(File f : files)
            list.add(f.toString());
//        System.out.println(list);
       try {
            GenerateClass.compile(list, "C:\\Users\\lhr\\Desktop\\a");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*for (int i = 0; i < 5; ++i) {
            String[] str = new String[]{
                    "+classpath=" + "D:\\Patch\\out\\production\\Patch",
                    "+search.class=p_heu.search.SingleExecutionSearch",
                    ImportPath.projectName + "." + ImportPath.mainClassName
            };
            Config config = new Config(str);
            JPF jpf = new JPF(config);
            SequenceProduceListener listener = new SequenceProduceListener();

            *//*Filter filter = Filter.createFilePathFilter();
            listener.setPositionFilter(filter);
*//*
            jpf.addListener(listener);
            jpf.run();
        }*/
//        useASTChangeLine(50, 51, ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName + "\\MergeSort.java");
        /*UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(48, 50, "D:\\Patch\\examples\\critical\\Critical.java");
        System.out.println(lockLine.getFirstLoc());
        System.out.println(lockLine.getLastLoc());*/

        String result = "";
        String read = "            if ( a2.turn2 != 0)";
//        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^.*?\\W?((\\w+\\.)?" + "turn2" + ").*$");
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^.*?((\\w+\\.)?" + "turn2" + ").*$");
        Matcher m = p.matcher(read);
        if (m.matches()) {
            result = m.group(1);
            int indexTemp = result.indexOf('.');
            if (indexTemp == -1)
                result = "this";
            else
                result = result.substring(0, indexTemp);
        } else {
            result = "this";
        }
        System.out.println(result);
    }

    private static void copyFolder(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // 递归复制
                copyFolder(srcFile, destFile);
            }
        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }
    }

    static void listFiles(List<File> files, File dir){
        File[] listFiles = dir.listFiles();
        for(File f: listFiles){
            if(f.isFile()){
                files.add(f);
            }else if(f.isDirectory()){
                listFiles(files, f);
            }
        }
    }


    //利用AST来改变加锁位置
    public static void useASTChangeLine(int firstLoc, int lastLoc, String filePath) {

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        /*char[] fileContents = getFileContents(new File(filePath));
        for (char c : fileContents)
            System.out.print(c);
*/
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {

            @Override
            public void endVisit(TypeDeclaration node) {
                System.out.println(node);
                super.endVisit(node);
            }

            @Override
            public void endVisit(MethodDeclaration node) {
                System.out.println(node);
                super.endVisit(node);
            }

            /*@Override
            public boolean visit(SimpleName node) {
                System.out.println(node + "," + cu.getLineNumber(node.getStartPosition()));
                return super.visit(node);
            }
*/
            @Override
            public boolean visit(InfixExpression node) {
               /* ASTNode parent = node.getParent();
                int start = cu.getLineNumber(parent.getStartPosition());
                int end = cu.getLineNumber(parent.getStartPosition() + parent.getLength());
                System.out.println(start);
                System.out.println(end);
                System.out.println(parent);
                if (firstLoc >= start && lastLoc <= end) {//加锁区域在括号的里面
                    System.out.println("yes");
                }*/

                return super.visit(node);
            }
        });
    }

    //chanage file content to buffer array
    public static char[] getFileContents(File file) {
        // char array to store the file contents in
        char[] contents = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                // append the content and the lost new line.
                sb.append(line + "\n");
            }
            contents = new char[sb.length()];
            sb.getChars(0, sb.length() - 1, contents, 0);

            assert (contents.length > 0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return contents;
    }
}
