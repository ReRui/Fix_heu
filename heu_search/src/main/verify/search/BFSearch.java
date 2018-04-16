package verify.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;

import java.util.ArrayList;
import java.util.List;

public class BFSearch extends Search {
	
	List<RestorableVMState> queue;
	boolean iscontinue = true;

	public BFSearch(Config config, VM vm) {
		super(config, vm);
		queue = new ArrayList<>();
	}

	@Override
	public void search() {

		if(hasPropertyTermination()){
			return;
		}
		notifySearchStarted();

		while(!done){

			if(forward() && iscontinue == true){

			}
		}

//		ThreadInfo ti = vm.getCurrentThread();
//		Instruction ins = ti.getPC();
//
//		vm.forward();
//
//		ti = vm.getCurrentThread();
//		ins = ti.getPC();
//
//		vm.forward();
//
//		ti = vm.getCurrentThread();
//		ins = ti.getPC();
//
//
//		System.out.println(vm.getStateId());
//		System.out.println(ti);
//		System.out.println(ins.getFileLocation());
//
//
//		ThreadChoiceFromSet cg = (ThreadChoiceFromSet)vm.getNextChoiceGenerator();
//		ThreadInfo[] tis = cg.getAllThreadChoices();
//		for (ThreadInfo t : tis) {
//			System.out.println("choice in cg: " + t + " " + t.getPC().getFileLocation());
//		}
	}

}
