package soot.structure.logic;

import analysis.CallGraphGetter;
import analysis.CallSite;
import soot.structure.entity.CommonCaller;
import soot.structure.entity.Method;
import soot.structure.entity.MethodCallRow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallGraphBuild {

    private String classpath;
    private String mainClass;

    public CallGraphBuild(String classpath, String mainClass) {
        this.classpath = classpath;
        this.mainClass = mainClass;
    }

    public Set<MethodCallRow> getCallers(Method method) {
        Set<MethodCallRow> callers = new HashSet<>();
        CallGraphGetter.getCallers(classpath, mainClass, method.getClassName(), method.getSignature());
        for (String key : CallGraphGetter.callsites.keySet()) {
            List<CallSite> callSites = CallGraphGetter.callsites.get(key);
            for (CallSite callSite : callSites) {
                callers.add(MethodCallRow.getInstance(
                        new Method(callSite.getClassName(), callSite.getSignature()),
                        callSite.getLine()
                ));
            }
        }
        return callers;
    }

    public Set<CommonCaller> findCommonCaller(Method method1, int row1, Method method2, int row2) {
        Set<MethodCallRow> visited1 = new HashSet<>();
        Set<MethodCallRow> visited2 = new HashSet<>();

        visited1.add(MethodCallRow.getInstance(method1, row1));
        MethodCallRow callRowRight = MethodCallRow.getInstance(method2, row2);
        visited2.add(callRowRight);

        while (extend(visited1)) ;

        Set<CommonCaller> result = new HashSet<>();
        CommonCaller caller = sameMethod(visited1, callRowRight);
        if (caller != null) {
            result.add(caller);
        }
        Set<CommonCaller> resultPart = extend(visited1, visited2);
        while (resultPart != null) {
            result.addAll(resultPart);
            resultPart = extend(visited1, visited2);
        }

        return result;
    }

    private boolean extend(Set<MethodCallRow> callRows) {
        Set<MethodCallRow> newCallers = null;
        for (MethodCallRow callRow : callRows) {
            if (!callRow.isExtend()) {
                newCallers = getCallers(callRow.getMethod());
                callRow.setExtend(true);
                break;
            }
        }
        if (newCallers == null) {
            return false;
        }
        else {
            callRows.addAll(newCallers);
            return true;
        }
    }

    private Set<CommonCaller> extend(Set<MethodCallRow> visited1, Set<MethodCallRow> visited2) {
        Set<MethodCallRow> newCallers = null;
        Set<CommonCaller> result = new HashSet<>();
        for (MethodCallRow callRow : visited2) {
            if (!callRow.isExtend()) {
                newCallers = getCallers(callRow.getMethod());
                callRow.setExtend(true);
                break;
            }
        }
        if (newCallers == null) {
            return null;
        }
        else {
            for (MethodCallRow newCaller : newCallers) {
                CommonCaller caller = sameMethod(visited1, newCaller);
                if (caller != null) {
                    result.add(caller);
                }
            }
        }
        return result;
    }

    private CommonCaller sameMethod(Set<MethodCallRow> callRows, MethodCallRow item) {
        for (MethodCallRow callRow : callRows) {
            if (callRow.getMethod().equals(item.getMethod())) {
                if (callRow.getRow() == item.getRow()) {
                    item.setExtend(true);
                }
                return new CommonCaller(callRow.getMethod(), callRow.getRow(), item.getRow());
            }
        }
        return null;
    }
}