package fix;

import fix.analyzefile.CheckWhetherLocked;
import fix.entity.ImportPath;
import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test {


    public static void main(String[] args) {
//        useASTChangeLine(444, 448, 439, 451, "D:/Patch/examples/stringbuffer/StringBuffer.java");
//        System.out.println(CheckWhetherLocked.check("wrongLock/WrongLock.java:30", "value", ImportPath.examplesRootPath + "/out/production/Patch", "D:\\Patch\\examples\\wrongLock\\WrongLock.java"));
        System.out.println(CheckWhetherLocked.check("linkedlist/MyLinkedList.java:30", "_next", ImportPath.examplesRootPath + "/out/production/Patch", "D:\\Patch\\examples\\wrongLock\\WrongLock.java"));
    }


    static void listFiles(List<File> files, File dir) {
        File[] listFiles = dir.listFiles();
        for (File f : listFiles) {
            if (f.isFile()) {
                files.add(f);
            } else if (f.isDirectory()) {
                listFiles(files, f);
            }
        }
    }


    public static void useASTChangeLine(int lockStart, int locEnd, int functionStart, int functionEnd, String filePath) {


    }

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
            public boolean visit(TypeDeclaration node) {
                /*System.out.println(node);
                System.out.println(cu.getLineNumber(node.getStartPosition()));
                System.out.println(cu.getLineNumber(node.getStartPosition() + node.getLength()));*/
                return super.visit(node);
            }


            @Override
            public boolean visit(MemberValuePair node) {
                System.out.println(node);
                return super.visit(node);
            }
/* @Override
            public boolean visit(TryStatement node) {
                System.out.println(node + "===========>");
                return super.visit(node);
            }

            @Override
            public boolean visit(TypeDeclarationStatement node) {
                System.out.println(node + "===========>");
                return super.visit(node);
            }

            @Override
            public boolean visit(ClassInstanceCreation node) {
                System.out.println(node + "ClassInstanceCreation");
                return super.visit(node);
            }*/

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
