package verify.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import verify.entity.filter.Filter;
import verify.entity.pattern.Pattern;
import verify.entity.sequence.Sequence;
import verify.listener.SequenceProduceListener;

import java.util.Set;

public class ProduceSequenceRandomly {
    public static void main(String[] args) {
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
				"+search.class=p_heu.search.SingleExecutionSearch",
                "CheckField"};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        SequenceProduceListener listener = new SequenceProduceListener();
        Filter filter = Filter.createFilePathFilter();
        listener.setPositionFilter(filter);

        jpf.addListener(listener);
        jpf.run();
        Sequence seq = listener.getSequence();
        System.out.println(seq.getStates().size());
        Set<Pattern> patterns = seq.getPatterns();
//        if (seq.getResult() == false) {
//            System.out.println(patterns);
//        }
    }
}