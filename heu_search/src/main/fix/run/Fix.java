package fix.run;

import fix.analyzefile.*;
import fix.entity.GlobalStaticObject;
import fix.entity.ImportPath;
import fix.entity.lock.ExistLock;
import fix.entity.type.AddSyncType;
import fix.entity.type.FixType;
import fix.io.ExamplesIO;
import fix.io.InsertCode;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.pattern.Pattern;
import p_heu.run.Unicorn;
import soot.UseSoot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;

public class Fix {
    static ExamplesIO examplesIO = ExamplesIO.getInstance();
    static String dirPath = ImportPath.examplesRootPath + "/examples/" + ImportPath.projectName;//第一次修复的文件路径
    //    static String dirPath = ImportPath.examplesRootPath + "/exportExamples/" + ImportPath.projectName;//第一次修复的文件路径
    static String iterateDirPath = ImportPath.examplesRootPath + "/exportExamples/" + ImportPath.projectName;//迭代修复的文件路径

    static String whichCLassNeedSync = "";//需要添加同步的类，此处需不需考虑在不同类之间加锁的情况？
    static LockAdjust lockAdjust = LockAdjust.getInstance();//当锁交叉时，用来合并锁

    static String fixMethods = "";//记录修复方法，写入文件中

    static String sourceClassPath = "";//源代码的生成类，记录下来，以后用jpf分析class

    static String addSyncFilePath = "";//加锁路径

    //用来计时
    static long startUnicornTime = 0;
    static long endUnicornTime = 0;
    static long startFixTime = 0;
    static long endFixTime = 0;

    //全局静态变量
    static GlobalStaticObject globalStaticObject = GlobalStaticObject.getInstance();

    //用于跨类修复
    static UseSoot useSoot = UseSoot.getInstance();

    public static void main(String[] args) {
        startUnicornTime = System.currentTimeMillis();
        fix(FixType.firstFix);
//        fix(FixType.iterateFix);
        endFixTime = System.currentTimeMillis();
        System.out.println("修复需要的时间:" + (endFixTime - startFixTime));

    }

    private static void fix(int type) {
        String verifyClasspath = ImportPath.verifyPath + "/generateClass";//要验证的class路径

        //处理包名有几层的情况
        if (dirPath.contains(".")) {
            dirPath = dirPath.replaceAll("\\.", "/");
        }

        if (type == FixType.firstFix) {
            //先将项目拷贝到exportExamples
            dirPath = examplesIO.copyFromOneDirToAnotherAndChangeFilePath("examples", "exportExamples", dirPath);
            sourceClassPath = ImportPath.examplesRootPath + "/out/production/Patch";
        } else if (type == FixType.iterateFix) {
            dirPath = iterateDirPath;
            sourceClassPath = ImportPath.verifyPath + "/generateClass";
        }

        //拿到最后一个元素
        List<Unicorn.PatternCounter> tempList = Unicorn.getPatternCounterList(sourceClassPath);

        //将长度为2的删除
        //实际上对，理论上考虑不全面
        /*for(int i = tempList.size() - 1;i >=0; i--) {
            if(tempList.get(i).getPattern().getNodes().length == 2)
                tempList.remove(i);
        }*/

        endUnicornTime = System.currentTimeMillis();
        System.out.println("得到pattern的时间:" + (endUnicornTime - startUnicornTime));

        //将所有的pattern打印出来，方便以后选择
        System.out.println(tempList);

        System.out.print("输入准确的pattern（最下面的是0）：");
        //此处需要手动选择
        Scanner sc = new Scanner(System.in);
        int whichToUse = sc.nextInt();//使用第几个pattern
        //最下面那个是0，依次往上，因为当时排序的时候是倒着排的

        startFixTime = System.currentTimeMillis();
        Unicorn.PatternCounter patternCounter = tempList.get(tempList.size() - 1 - whichToUse);

        //根据pattern知道需要在哪个类中加锁
        String position = patternCounter.getPattern().getNodes()[0].getPosition();
        String[] tempSplit = position.split(":")[0].split("/");
        whichCLassNeedSync = tempSplit[tempSplit.length - 1];

        addSyncFilePath = ImportPath.examplesRootPath + "/exportExamples/" + position.split(":")[0];

        //将拿到的pattern写入文件中
        InsertCode.writeLogFile(patternCounter.toString(), "修复得到的pattern");

        //拿到该pattern对应的sequence
        //第一次在失败运行中出现的sequence
        RecordSequence.display(patternCounter.getFirstFailAppearPlace());

        //将sequence写入文件中
        InsertCode.writeLogFile(patternCounter.getFirstFailAppearPlace().toString(), "修复得到的sequence");

        //对拷贝的项目进行修复
        divideByLength(patternCounter);

        //检测修复完的程序是否正确
        fixMethods += "结果: ";
        if (Unicorn.verifyFixSuccessful(verifyClasspath)) {
            fixMethods += "修复成功";
        } else {
            fixMethods += "修复失败";
        }

        //将修复方法写入文件中
        InsertCode.writeLogFile(fixMethods, "修复方法及结果");
    }

    //根据pattern的长度执行不同的fix策略
    private static void divideByLength(Unicorn.PatternCounter patternCounter) {
        int length = patternCounter.getPattern().getNodes().length;
        if (length == 2) {
            fixMethods += "修复一\n";
            usePatternToDistinguish(patternCounter.getPattern());
        } else if (length == 3) {
            fixMethods += "修复二\n";
            usePatternToAddSync(patternCounter.getPattern());
        } else if (length == 4) {
            fixMethods += "修复三\n";
            usePatternToAddSync(patternCounter.getPattern());
        }
    }

    //长度为2，先分情况
    private static void usePatternToDistinguish(Pattern patternCounter) {


        if (RecordSequence.isLast(patternCounter.getNodes()[0]) || RecordSequence.isFirst(patternCounter.getNodes()[1])) {
            //为长度为2的pattern添加同步
            fixMethods += "添加信号量\n";
            addSignal(patternCounter);
        } else {
            //为长度为2的pattern添加同步,与3和4是不同的情况
            fixMethods += "添加同步\n";
            addSyncPatternOneToThree(patternCounter);
        }
    }

    //长度为3或4，添加同步
    private static void usePatternToAddSync(Pattern patternCounter) {
        //根据线程将三个结点分为两个list
        List<ReadWriteNode> threadA = new ArrayList<ReadWriteNode>();//线程A的结点
        List<ReadWriteNode> threadB = new ArrayList<ReadWriteNode>();//线程B的结点
        String threadName = "";
        for (int i = 0; i < patternCounter.getNodes().length; i++) {
            ReadWriteNode node = patternCounter.getNodes()[i];
            if (i == 0) {//把第一个结点放入A的list
                threadName = node.getThread();
                threadA.add(node);
            } else {
                if (threadName.equals(node.getThread())) {//线程相同，放入同一个list
                    threadA.add(node);
                } else {//不同就放入另一个list
                    threadB.add(node);
                }
            }
        }

        //长度为3加this锁
        if (patternCounter.getNodes().length == 3) {
            //根据获得的list，进行加锁
            addSynchronized(threadA, AddSyncType.localSync);
            lockAdjust.setOneLockFinish(true);//表示第一次执行完
            addSynchronized(threadB, AddSyncType.localSync);
            lockAdjust.adjust(addSyncFilePath);//合并锁
        } else if (patternCounter.getNodes().length == 4) {//长度为4加静态锁？
            //根据获得的list，进行加锁
            addSynchronized(threadA, AddSyncType.globalStaticSync);
            lockAdjust.setOneLockFinish(true);//表示第一次执行完
            addSynchronized(threadB, AddSyncType.globalStaticSync);
            lockAdjust.adjust(addSyncFilePath);//合并锁
        }

    }

    //对一个线程中的node进行加锁
    private static void addSynchronized(List<ReadWriteNode> rwnList, int type) {
        int firstLoc = 0, lastLoc = 0;

        String lockName = "";//用来表示加锁的名称

        String lockFile = "";//用来表示加锁文件

        //判断A中有几个变量
        if (rwnList.size() > 1) {//两个变量
            //如果有两个变量，需要分析
            //判断它们在不在一个函数中
            boolean flagSame = UseASTAnalysisClass.assertSameFunction(rwnList, addSyncFilePath);
            if (flagSame) {//在一个函数中
                //先找找原来有没有锁
                boolean varHasLock = false;//记录当前pattern是否加锁
                ExistLock existLock = null;
                //判断它们有没有加锁，需要加何种锁，加锁位置
                //对A的list分析
                for (int i = 0; i < rwnList.size(); i++) {
                    ReadWriteNode node = rwnList.get(i);
                    if (CheckWhetherLocked.check(node.getPosition(), node.getField(), sourceClassPath, addSyncFilePath)) {//检查是否存在锁
                        if (i == 1 && varHasLock == true) {//表示两个都有锁
                            return;//直接结束
                        } else {
                            varHasLock = true;//有锁标为true
                            existLock = existLockName(rwnList.get(i));
                            UseASTAnalysisClass.setFlagUseASTCheckWhetherLock(false);//防止上一次的true结果，影响到下一次
//                            existLock = null;
                        }
                    }
                    //应该要加什么锁
                    if (type == AddSyncType.localSync) {//需要添加局部锁
                        //这个步骤实际是用分析字符串来完成的
                        //实际上是不对的
                        lockName = acquireLockName(node);
                    } else if (type == AddSyncType.globalStaticSync) {//需要添加全局锁
                        if (!globalStaticObject.isDefineObject) {
                            lockName = UseASTAnalysisClass.useASTToaddStaticObject(addSyncFilePath);
                            globalStaticObject.objectName = lockName;
                            globalStaticObject.isDefineObject = true;
                        } else {
                            lockName = globalStaticObject.objectName;
                        }
                    }
                    int poi = Integer.parseInt(node.getPosition().split(":")[1]);
                    if (i == 0) {
                        firstLoc = poi;
                        lastLoc = firstLoc;
                    } else {
                        if (poi < firstLoc) {
                            firstLoc = poi;
                        } else {
                            lastLoc = poi;
                        }
                    }
                }

                //判断加锁区域在不在构造函数，或者加锁变量是不是成员变量
                if (!UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(firstLoc, lastLoc, addSyncFilePath)) {

                    //判断加锁会不会和for循环等交叉
                    UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, addSyncFilePath);
                    firstLoc = lockLine.getFirstLoc();
                    lastLoc = lockLine.getLastLoc();

                    //两个地方都没有加锁
                    if (!varHasLock) {
                        //加锁
                        examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, addSyncFilePath);
                    } else {//有加锁的，直接修改原有锁
                        UseOldSyncToFix.adjustOldSync(existLock.getLockName(), firstLoc, lastLoc + 1, existLock.getStartLine(), existLock.getEndLine(), addSyncFilePath);
                    }

                    lockFile = addSyncFilePath;
                }
            } else {//不在一个函数中
                //跨类搜索
                useSoot.getCallGraph(rwnList.get(0), rwnList.get(1));

                //得到加锁位置
                firstLoc = useSoot.getMinLine();
                lastLoc = useSoot.getMaxLine();
                /*//如果pattern来自同一个类，那么跨类之后加的是this锁
                String classNameOne = rwnList.get(0).getPosition().split("\\.")[0].replaceAll("/", ".");
                String classNameTwo = rwnList.get(1).getPosition().split("\\.")[0].replaceAll("/", ".");
                if (classNameOne.equals(classNameTwo)) {
                    examplesIO.addLockToOneVar(useSoot.getMinLine(), useSoot.getMaxLine() + 1, "this", ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava());
                } else {
                    examplesIO.addLockToOneVar(useSoot.getMinLine(), useSoot.getMaxLine() + 1, "obj", ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava());
                }*/


                //判断加锁区域在不在构造函数，或者加锁变量是不是成员变量
                if (!UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(firstLoc, lastLoc, ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava())) {
                    //判断加锁会不会和for循环等交叉
                    UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava());
                    firstLoc = lockLine.getFirstLoc();
                    lastLoc = lockLine.getLastLoc();

                    //检查会不会定义变量在锁内，使用变量在锁外
                    lockLine = UseASTAnalysisClass.useASTCheckVariableInLock(firstLoc, lastLoc,ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava());
                    firstLoc = lockLine.getFirstLoc();
                    lastLoc = lockLine.getLastLoc();

                    //暂定为都加静态锁，this锁不一定对
                    examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, "obj", ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava());
                    lockFile = ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava();
                }
            }
        } else {
            //对于一个变量，检查它是否已经被加锁
            ReadWriteNode node = rwnList.get(0);
            if (!CheckWhetherLocked.check(node.getPosition(), node.getField(), sourceClassPath, addSyncFilePath)) {
                //没被加锁，获得需要加锁的行数
                firstLoc = Integer.parseInt(node.getPosition().split(":")[1]);
                lastLoc = firstLoc;
                //然后获得需要加何种锁
                lockName = acquireLockName(node);

                //判断加锁会不会和for循环等交叉
                UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, addSyncFilePath);
                firstLoc = lockLine.getFirstLoc();
                lastLoc = lockLine.getLastLoc();

                //然后加锁
                examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, addSyncFilePath);

                lockFile = addSyncFilePath;
            }
        }

        //记录加锁位置
        //便于以后调整
        if (!lockAdjust.isOneLockFinish()) {
            lockAdjust.setOneLockFile(lockFile);
            lockAdjust.setOneFirstLoc(firstLoc);
            lockAdjust.setOneLastLoc(lastLoc + 1);
        } else {
            lockAdjust.setTwoLockFile(lockFile);
            lockAdjust.setTwoFirstLoc(firstLoc);
            lockAdjust.setTwoLastLoc(lastLoc + 1);
        }

        //关联变量处理
        LockPolicyPopularize.fixRelevantVar(firstLoc, lastLoc, rwnList.get(0).getThread(), whichCLassNeedSync, lockName, addSyncFilePath);//待定

        //表示能加锁
        if (firstLoc > 0 && lastLoc > 0) {
            fixMethods += "对" + rwnList.get(0) + "加锁起止位置" + firstLoc + "->" + lastLoc + '\n';
        }
    }

    //读到那一行，然后对字符串处理
    //获取锁的名称
    private static String acquireLockName(ReadWriteNode node) {
        BufferedReader br = null;
        String read = "";//用来读
        String result = "";//用来处理
        int line = 0;
        int poi = Integer.parseInt(node.getPosition().split(":")[1]);
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(addSyncFilePath)), "UTF-8"));
            while (((read = br.readLine()) != null)) {
                line++;
                if (line == poi) {//找到哪一行
                    String field = node.getField();//得的变量
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("^.*?((\\w+\\.)?" + field + ").*$");
                    Matcher m = p.matcher(read);
                    if (m.matches()) {
                        result = m.group(1);
                        int indexTemp = result.indexOf('.');
                        if (indexTemp == -1) {
                            result = "this";
                        } else {
                            result = result.substring(0, indexTemp);
                        }
                    } else {
                        result = "this";
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //如果是类的static变量，直接加this锁？？
        //认为如果不含有@符号就是静态变量
        if (!node.getElement().contains("@")) {
            result = "this";
        }

        fixMethods += "锁的名字" + result.trim() + '\n';
        return result.trim();
    }

    //输出锁的名称
    private static ExistLock existLockName(ReadWriteNode node) {
        ExistLock existLock = UseASTAnalysisClass.useASTCFindLockLine(node, addSyncFilePath);
        existLock = AcquireSyncName.acquireSync(existLock, addSyncFilePath);
        return existLock;
    }


    //对长度为2的pattern添加同步
    private static void addSyncPatternOneToThree(Pattern patternCounter) {

        int firstLoc = 0, lastLoc = 0;
        //判断在不在一个 函数中
        List<ReadWriteNode> rwnList = new ArrayList<ReadWriteNode>();
        for (int i = 0; i < patternCounter.getNodes().length; i++) {
            rwnList.add(patternCounter.getNodes()[i]);
        }
        boolean flagSame = UseASTAnalysisClass.assertSameFunction(rwnList, addSyncFilePath);

        if (flagSame) {//在一个函数中
            int oneLoc = Integer.parseInt(patternCounter.getNodes()[0].getPosition().split(":")[1]);
            int twoLoc = Integer.parseInt(patternCounter.getNodes()[1].getPosition().split(":")[1]);
            firstLoc = Math.min(oneLoc, twoLoc);
            lastLoc = Math.max(oneLoc, twoLoc);
            String lockName = acquireLockName(patternCounter.getNodes()[0]);
            if (!UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(firstLoc, lastLoc + 1, addSyncFilePath)) {
                //加锁
                //检查是否存在锁再加锁
                if (!CheckWhetherLocked.check(patternCounter.getNodes()[0].getPosition(), patternCounter.getNodes()[0].getField(), sourceClassPath, addSyncFilePath)) {
                    //判断加锁会不会和for循环等交叉
                    UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, addSyncFilePath);
                    firstLoc = lockLine.getFirstLoc();
                    lastLoc = lockLine.getLastLoc();
                    fixMethods += "加锁位置" + firstLoc + "->" + (lastLoc + 1) + '\n';
                    examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, lockName, addSyncFilePath);//待定
                }
            }
        } else {//需要跨类修复

            //这里需要考虑到一点，比如pattern是来自一个类，但是在它们可能被不同类调用
            //这时要还是加静态object锁
            //暂定全部加静态object锁

            //跨类搜索
            useSoot.getCallGraph(patternCounter.getNodes()[0], patternCounter.getNodes()[1]);

            //添加静态变量定义
            UseASTAnalysisClass.useASTToaddStaticObject(ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava());
            //得到加锁起始终止行
            firstLoc = useSoot.getMinLine();
            lastLoc = useSoot.getMaxLine();
            //是不是成员变量或者在构造函数里面
            if (!UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(firstLoc, lastLoc + 1, ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava())) {
                //判断加锁会不会和for循环等交叉
                UseASTAnalysisClass.LockLine lockLine = UseASTAnalysisClass.changeLockLine(firstLoc, lastLoc, ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava());
                firstLoc = lockLine.getFirstLoc();
                lastLoc = lockLine.getLastLoc();

                //加锁
                examplesIO.addLockToOneVar(firstLoc, lastLoc + 1, "objectFix", ImportPath.examplesRootPath + "/exportExamples/" + useSoot.getSyncJava());

            }

        }
    }

    //添加信号量修复顺序违背
    private static void addSignal(Pattern patternCounter) {
        //得到pattern中较小的行数
        int flagDefineLocation = Integer.MAX_VALUE;//flag应该在哪行定义
        int flagAssertLocation = Integer.MIN_VALUE;//flag应该在那行判断
        for (int i = 0; i < 2; i++) {
            String position = patternCounter.getNodes()[i].getPosition();
            String[] positionArg = position.split(":");
            flagDefineLocation = Integer.parseInt(positionArg[1]) < flagDefineLocation ? Integer.parseInt(positionArg[1]) : flagDefineLocation;
            flagAssertLocation = Integer.parseInt(positionArg[1]) > flagAssertLocation ? Integer.parseInt(positionArg[1]) : flagAssertLocation;
        }

        fixMethods += "信号量定义位置:" + flagDefineLocation + '\n';
        fixMethods += "信号量使用位置:" + flagAssertLocation + '\n';

        //构造函数不能加信号量
        if (!UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(flagAssertLocation, flagAssertLocation, addSyncFilePath) &&
                !UseASTAnalysisClass.isConstructOrIsMemberVariableOrReturn(flagAssertLocation, flagAssertLocation, addSyncFilePath)) {
            //添加信号量的定义
            examplesIO.addVolatileDefine(flagDefineLocation, "volatile bool flagFix = false;", addSyncFilePath);//待修订

            //添加信号量判断,
            examplesIO.addVolatileIf(flagAssertLocation, addSyncFilePath);//待修订

            //添加信号为true的那条语句，那条语句应该在定义的后一行
            examplesIO.addVolatileToTrue(flagDefineLocation + 1, addSyncFilePath);//待修订
        }
    }
}
