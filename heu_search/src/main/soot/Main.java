package soot;

import analysis.Tools;
import fix.entity.ImportPath;
import soot.structure.entity.CommonCaller;
import soot.structure.entity.Method;
import soot.structure.logic.CallGraphBuild;

import java.util.Set;

public class Main {

    public static Set<CommonCaller> getCallGraphInfo(String classNameOne, int classLineOne, String classNameTwo, int classLineTwo) {
        String classpath = ImportPath.examplesRootPath +  "/out/production/Patch";
        String mainClass = ImportPath.projectName + "." + ImportPath.mainClassName;

        CallGraphBuild callGraphBuild = new CallGraphBuild(
                classpath,
                mainClass
        );
        Tools.getContainMethod(classpath, mainClass, classNameOne, classLineOne);
        String left_sign = Tools.containMethod.getSignature();
        Tools.getContainMethod(classpath, mainClass, classNameOne, classLineTwo);
        String right_sign = Tools.containMethod.getSignature();

        Set<CommonCaller> callers = callGraphBuild.findCommonCaller(new Method(classNameOne, left_sign), classLineOne,
                new Method(classNameTwo, right_sign), classLineTwo);

        return callers;

    }
}
