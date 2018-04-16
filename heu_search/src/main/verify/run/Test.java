package verify.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import verify.entity.filter.Filter;
import verify.entity.sequence.Sequence;
import verify.listener.SequenceProduceListener;

public class Test {
    public static void main(String[] args) {
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "account.Main"};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
//        TestMethods listener = new TestMethods();
        SequenceProduceListener listener = new SequenceProduceListener();
        Filter filter = Filter.createFilePathFilter();
        listener.setPositionFilter(filter);
        jpf.addListener(listener);
        jpf.run();

        Sequence seq = listener.getSequence();
        System.out.println(seq);
    }
}
