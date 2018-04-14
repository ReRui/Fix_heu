package fix;

import fix.entity.ImportPath;
import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Test {
    static  int tettt = 0;
    static String dirPath = ImportPath.examplesRootPath + "/examples/" + ImportPath.projectName;
    public static void main(String[] args) {

        /*File src = new File(path);
        File dest = new File("C:\\Users\\lhr\\Desktop\\a");
        try {
            copyFolder(src,dest);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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
//        useASTChangeLine(ImportPath.examplesRootPath + "\\examples\\" + ImportPath.projectName + "\\MyLinkedList.java");
        /*UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(48, 50, "D:\\Patch\\examples\\critical\\Critical.java");
        System.out.println(lockLine.getFirstLoc());
        System.out.println(lockLine.getLastLoc());*/
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
    public static void useASTChangeLine(String filePath) {

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
            public boolean visit(SynchronizedStatement node) {
                System.out.println(node);
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
