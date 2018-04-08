package soot;

import analysis.Tools;
import soot.structure.entity.CommonCaller;
import soot.structure.entity.Method;
import soot.structure.logic.CallGraphBuild;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
//        test1();
//        test2();
//        test3();
        test4();
    }

    public static void test1() {
        CallGraphBuild callGraphBuild = new CallGraphBuild(
                "C:\\Users\\lhr\\Desktop\\CodeStructure\\out\\production\\CodeStructure",
                "callex.CallExample"
        );
//        Set<MethodCallRow> callRows = callGraphBuild.getCallers(new Method("callex.CallExample", "void functionA()"));
//        for (MethodCallRow callRow : callRows) {
//            System.out.println(callRow);
//        }
        Set<CommonCaller> callers = callGraphBuild.findCommonCaller(new Method("callex.CallExample", "void functionA(int)"), 17,
                new Method("callex.CallExample", "void functionD()"), 33);

        for (CommonCaller caller : callers) {
            System.out.println(caller);
        }
    }

    public static void test2() {
        CallGraphBuild callGraphBuild = new CallGraphBuild(
                "C:\\Users\\lhr\\Desktop\\CodeStructure\\out\\production\\CodeStructure",
                "accountsubtype.Main"
        );
        Set<CommonCaller> callers = callGraphBuild.findCommonCaller(new Method("accountsubtype.PersonalAccount", "void transfer(accountsubtype.Account,int)"), 9,
                new Method("accountsubtype.BusinessAccount", "void transfer(accountsubtype.Account,int)"), 10);

        for (CommonCaller caller : callers) {
            System.out.println(caller);
        }
    }

    public static void test3() {
        CallGraphBuild callGraphBuild = new CallGraphBuild(
                "C:\\Users\\lhr\\Desktop\\CodeStructure\\out\\production\\CodeStructure",
                "account.Main"
        );
        Set<CommonCaller> callers = callGraphBuild.findCommonCaller(new Method("account.Account", "void transfer()"), 26,
                new Method("account.Account", "void transfer()"), 27);

        for (CommonCaller caller : callers) {
            System.out.println(caller);
        }
    }

    public static void test4() {
        String classpath = "D:\\Patch\\out\\production\\Patch";
        String mainClass = "datarace.Main";

        CallGraphBuild callGraphBuild = new CallGraphBuild(
                classpath,
                mainClass
        );
        Tools.getContainMethod(classpath, mainClass, "datarace.Account", 12);
        String left_sign = Tools.containMethod.getSignature();
        Tools.getContainMethod(classpath, mainClass, "datarace.Account", 8);
        String right_sign = Tools.containMethod.getSignature();

        Set<CommonCaller> callers = callGraphBuild.findCommonCaller(new Method("datarace.Account", left_sign), 12,
                new Method("datarace.Account", right_sign), 8);

        for (CommonCaller caller : callers) {
            System.out.println(caller);
        }

    }
}
