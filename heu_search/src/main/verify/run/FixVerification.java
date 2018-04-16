package verify.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import verify.entity.filter.Filter;
import verify.entity.sequence.Sequence;
import verify.listener.BasicPatternFindingListener;
import verify.listener.SequenceProduceListener;

import java.util.HashSet;
import java.util.Set;

public class FixVerification {
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new RuntimeException("need 2 arguments.");
        }
        verify(args[0]);
    }

    public static boolean verify(String testFileName) {
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search.PatternDistanceBasedSearch",
                testFileName};
        Config config = new Config(str);
        Filter filter = Filter.createFilePathFilter();

        Sequence correctSeq = getCorrectSequence(testFileName);
        if (!correctSeq.getResult()) {
            return false;
        }

        Set<Sequence> correctSeqs = new HashSet<>();
        correctSeqs.add(correctSeq);
        BasicPatternFindingListener listener = new BasicPatternFindingListener(correctSeqs);
        listener.setPositionFilter(filter);
        listener.setMod(BasicPatternFindingListener.MOD.VERIFY);
        JPF jpf = new JPF(config);
        jpf.addListener(listener);
        jpf.run();
        boolean findBug = listener.getErrorSequence() != null;
        System.out.println("+++++++++++++++++++++++++++++++find BUG: " + findBug);
        return !findBug;
    }

    public static Sequence getCorrectSequence(String testFileName){

        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search.SingleExecutionSearch",
                testFileName};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        SequenceProduceListener listener = new SequenceProduceListener();
        Filter filter = Filter.createFilePathFilter();
        listener.setPositionFilter(filter);

        jpf.addListener(listener);
        jpf.run();
        return listener.getSequence();
    }
}
