package fix;

import fix.entity.ImportPath;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.filter.Filter;
import p_heu.listener.SequenceProduceListener;

public class Test {
    public static void main(String[] args) {
        for (int i = 0; i < 5; ++i) {
            String[] str = new String[]{
                    "+classpath=" + "D:\\Patch\\out\\production\\Patch",
                    "+search.class=p_heu.search.SingleExecutionSearch",
                    ImportPath.projectName + "." + ImportPath.mainClassName
            };
            Config config = new Config(str);
            JPF jpf = new JPF(config);
            SequenceProduceListener listener = new SequenceProduceListener();

            /*Filter filter = Filter.createFilePathFilter();
            listener.setPositionFilter(filter);
*/
            jpf.addListener(listener);
            jpf.run();
        }
    }
}
