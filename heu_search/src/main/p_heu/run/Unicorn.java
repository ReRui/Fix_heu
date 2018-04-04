package p_heu.run;

import fix.entity.ImportPath;
import fix.entity.type.UnicornType;
import fix.io.InsertCode;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.filter.Filter;
import p_heu.entity.pattern.Pattern;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.SequenceProduceListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Unicorn {


    //�޸���������pattern
    static List<PatternCounter> patternCountersList = new ArrayList<>();

    //��֤�޸����
    static boolean verifyFlag = true;

    //�����õ�main����
    public static void main(String[] args) {
        /*System.setProperty("user.dir", "C:\\Users\\lhr\\Desktop\\verify\\HashSet");
        System.setProperty("projectName", "HashSet");

        System.out.println(System.getProperty("projectName"));*/

//        GenerateClass.compileJava(ImportPath.verifyPath + "\\exportExamples\\" + ImportPath.projectName, ImportPath.verifyPath + "\\generateClass");
        System.out.println(Unicorn.getPatternCounterList(ImportPath.examplesRootPath + "\\out\\production\\Patch"));
//        System.out.println(Unicorn.getPatternCounterList(ImportPath.verifyPath + "\\generateClass"));
//        System.out.println(Unicorn.verifyFixSuccessful(ImportPath.verifyPath + "\\generateClass"));
//        System.out.println(Unicorn.verifyFixSuccessful(ImportPath.examplesRootPath + "\\out\\production\\Patch"));
//        System.out.println(Unicorn.getPatternCounterList("C:\\Users\\lhr\\Desktop\\verify\\HashSet\\outputClasses"));
//        System.out.println(Unicorn.getPatternCounterList());
//        System.out.println(Unicorn.getPatternCounterList());
        /*List<PatternCounter> patternCounters = Unicorn.getPatternCounterList(ImportPath.examplesRootPath + "/out/production/Patch");
        for (PatternCounter p : patternCounters) {
            System.out.println(p);
        }

        //��ȡsequence��Ϣ
        System.out.println(patternCounters.get(0).getFirstFailAppearPlace() + "sequence");*/
    }

    //��ȡpattern
    public static List<PatternCounter> getPatternCounterList(String classpath) {
        useUnicorn(UnicornType.getPattern, classpath);
        return patternCountersList;
    }

    //��ȡ��֤���
    public static boolean verifyFixSuccessful(String classpath) {
        useUnicorn(UnicornType.verify, classpath);
        //���õ���patternд���ļ���
        InsertCode.writeToFile(patternCountersList.toString(), ImportPath.examplesRootPath + "\\logFile\\" + ImportPath.projectName + "\\verify pattern.txt");
        return verifyFlag;
    }

    private static void useUnicorn(int type, String classpath) {
//        Pattern.setPatternSet("unicorn");
        //��ԭ�������
        patternCountersList.clear();

        /*if (type == UnicornType.getPattern) {
            classpath = ImportPath.examplesRootPath + "\\out\\production\\Patch";
        } else if (type == UnicornType.verify) {
            classpath = ImportPath.verifyPath + "\\generateClass";
        }*/

        if (type == UnicornType.verify) {
            //�Ƚ����ɲ�����ĳ�������class�ļ�
            //��Ϊjpf�ļ�Ҫ��class�ļ�����
            //Դ·����Ŀ��·��
            GenerateClass.compileJava(ImportPath.verifyPath + "\\exportExamples\\" + ImportPath.projectName, classpath);
        }

        for (int i = 0; i < 50; ++i) {
            String[] str = new String[]{
                    "+classpath=" + classpath,
                    "+search.class=p_heu.search.SingleExecutionSearch",
                    ImportPath.projectName + "." + ImportPath.mainClassName
//                    "benchmarks.dstest.MTSetTest"
            };
            Config config = new Config(str);
            JPF jpf = new JPF(config);
            SequenceProduceListener listener = new SequenceProduceListener();

            /*Filter filter = Filter.createFilePathFilter();
            listener.setPositionFilter(filter);*/

            jpf.addListener(listener);
            jpf.run();


            Sequence seq = listener.getSequence();
            //sequence����ʱ������ͬһ���̶߳�ĳ���ط��ظ�ִ�����ε����
            //����ֻ��¼�ڶ��Σ�������һ��
            //��Ϊʵ�ʲ���Ч�����ǵڶ���
            //jpf�в������������ԭ����
            seq = reduceSeq(seq);

            if (type == UnicornType.verify) {
                if (!seq.getResult()) {
                    verifyFlag = false;
                }
            }

            outer:
            for (Pattern pattern : seq.getPatterns()) {
                for (PatternCounter p : patternCountersList) {
                    if (p.getPattern().isSameExecptThread(pattern)) {
                        if (!seq.getResult() && p.getFirstFailAppearPlace() == null) {
                            p.setFirstFailAppearPlace(seq);
                        }
                        p.addOne(seq.getResult());
                        continue outer;
                    }
                }
                patternCountersList.add(new PatternCounter(pattern, seq.getResult(), seq.getResult() ? null : seq));
            }
        }

        Collections.sort(patternCountersList, new Comparator<PatternCounter>() {
            @Override
            public int compare(PatternCounter o1, PatternCounter o2) {
                double r1 = (double) o1.getSuccessCount() / (o1.getSuccessCount() + o1.getFailCount());
                double r2 = (double) o2.getSuccessCount() / (o2.getSuccessCount() + o2.getFailCount());
                return -Double.compare(r1, r2) == 0 ?
                        Integer.compare(o1.getFailCount(), o2.getFailCount()) : -Double.compare(r1, r2);//changed
            }
        });
    }


    private static Sequence reduceSeq(Sequence seq) {
        List<Node> nodesList = seq.getNodes();
        for (int i = 0; i < nodesList.size(); i++) {
            if (nodesList.get(i) instanceof ReadWriteNode) {
                for (int j = i - 1; j >= 0; j--) {
                    if (nodesList.get(j) instanceof ReadWriteNode) {
                        ReadWriteNode rwi = (ReadWriteNode) nodesList.get(i);
                        ReadWriteNode rwj = (ReadWriteNode) nodesList.get(j);
                        if ((rwi.getId() != rwj.getId()) && rwi.getElement().equals(rwj.getElement()) && rwi.getField().equals(rwj.getField()) && rwi.getType().equals(rwj.getType()) && rwi.getThread().equals(rwj.getThread()) && rwi.getPosition().equals(rwj.getPosition())) {
                            seq.getNodes().remove(j);
                            i--;
                        }
                    }
                }
            }
        }
        return seq;
    }


    public static class PatternCounter {
        private Pattern pattern;
        private int successCount;
        private int failCount;
        private Sequence firstFailAppearPlace;

        public Sequence getFirstFailAppearPlace() {
            return firstFailAppearPlace;
        }

        public void setFirstFailAppearPlace(Sequence firstFailAppearPlace) {
            this.firstFailAppearPlace = firstFailAppearPlace;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public PatternCounter(Pattern pattern, boolean result) {
            this.pattern = pattern;
            this.firstFailAppearPlace = null;
            if (result) {
                successCount = 1;
                failCount = 0;
            } else {
                successCount = 0;
                failCount = 1;
            }
        }

        public PatternCounter(Pattern pattern, boolean result, Sequence ffap) {
            this.pattern = pattern;
            this.firstFailAppearPlace = ffap;
            if (result) {
                successCount = 1;
                failCount = 0;
            } else {
                successCount = 0;
                failCount = 1;
            }
        }

        public void addOne(boolean result) {
            if (result) {
                successCount += 1;
            } else {
                failCount += 1;
            }
        }

        public String toString() {
            return pattern.toString() + "\nsuccess count: " + this.successCount + "\nfail count: " + this.failCount;
        }
    }
    /*
    public static void main(String[] args) {
        List<PatternCounter> patternCounters = new ArrayList<>();
        for (int i = 0; i < 100; ++i) {
            String[] str = new String[]{
//                    "+classpath=out/production/heu_search",
                    "+classpath="+"D:\\Patch\\out\\production\\Patch",
                    "+search.class=p_heu.search.SingleExecutionSearch",
                    "atmoerror.Main"};
            Config config = new Config(str);
            JPF jpf = new JPF(config);
            SequenceProduceListener listener = new SequenceProduceListener();
            Filter filter = Filter.createFilePathFilter();
            listener.setPositionFilter(filter);

            jpf.addListener(listener);
            jpf.run();
            Sequence seq = listener.getSequence();
            outer:
            for (Pattern pattern : seq.getPatterns()) {
                for (PatternCounter p : patternCounters) {
                    if (p.getPattern().isSameExecptThread(pattern)) {
                        p.addOne(seq.getResult());
                        continue outer;
                    }
                }
                patternCounters.add(new PatternCounter(pattern, seq.getResult()));
            }
        }
        Collections.sort(patternCounters, new Comparator<PatternCounter>() {
            @Override
            public int compare(PatternCounter o1, PatternCounter o2) {
                double r1 = (double)o1.getSuccessCount() / (o1.getSuccessCount() + o1.getFailCount());
                double r2 = (double)o2.getSuccessCount() / (o2.getSuccessCount() + o2.getFailCount());
                return Double.compare(r1, r2);
            }
        });
        System.out.println(patternCounters);
    }

    public static class PatternCounter {
        private Pattern pattern;
        private int successCount;
        private int failCount;

        public Pattern getPattern() {
            return pattern;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public PatternCounter(Pattern pattern, boolean result) {
            this.pattern = pattern;
            if (result) {
                successCount = 1;
                failCount = 0;
            }
            else {
                successCount = 0;
                failCount = 1;
            }
        }

        public void addOne(boolean result) {
            if (result) {
                successCount += 1;
            }
            else {
                failCount += 1;
            }
        }

        public String toString() {
            return pattern.toString() + "\nsuccess count: " + this.successCount + "\nfail count: " + this.failCount;
        }
    }*/
}
