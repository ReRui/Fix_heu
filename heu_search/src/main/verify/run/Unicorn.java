package verify.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import verify.entity.filter.Filter;
import verify.entity.pattern.Pattern;
import verify.entity.sequence.Sequence;
import verify.listener.SequenceProduceListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Unicorn {
    public static void main(String[] args) {
        Pattern.setPatternSet("unicorn");
        List<PatternCounter> patternCounters = new ArrayList<>();
        for (int i = 0; i < 100; ++i) {
            String[] str = new String[]{
                    "+classpath=out/production/heu_search",
                    "+search.class=p_heu.search.SingleExecutionSearch",
                    "mergesort.MergeSort"};
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
                return -Double.compare(r1, r2) == 0 ?
                        Integer.compare(o1.getFailCount(), o2.getFailCount()) : -Double.compare(r1, r2);//changed
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
    }
}
