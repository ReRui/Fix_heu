package fix;

import fix.analyzefile.UseASTAnalysisClass;
import fix.entity.ImportPath;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import org.eclipse.jdt.core.dom.*;
import p_heu.entity.filter.Filter;
import p_heu.listener.SequenceProduceListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;

public class Test {
    public static void main(String[] args) {
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
//        useASTChangeLine(50,51,"D:\\Patch\\examples\\critical\\Critical.java");
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
            if(indexTemp == -1)
                result = "this";
            else
                result = result.substring(0, indexTemp);
        } else {
            result = "this";
        }
        System.out.println(result);
    }


    //����AST���ı����λ��
    public static void useASTChangeLine(int firstLoc, int lastLoc, String filePath) {

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(getFileContents(new File(filePath)));
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {

            @Override
            public boolean visit(InfixExpression node) {
                ASTNode parent = node.getParent();
                int start = cu.getLineNumber(parent.getStartPosition());
                int end = cu.getLineNumber(parent.getStartPosition() + parent.getLength());
                System.out.println(start);
                System.out.println(end);
                System.out.println(parent);
                if (firstLoc >= start && lastLoc <= end) {//�������������ŵ�����
                    System.out.println("yes");
                }

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
