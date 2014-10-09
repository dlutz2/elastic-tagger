package org.opensextant.tagger;

import org.opensextant.tagger.interval.IntervalTree;

public class IntervalTreeTest {

	public static void main(String[] args) {

		IntervalTree<Integer> it = new IntervalTree<Integer>();
		
		//it.addInterval(0L,10L,1);
		it.addInterval(10L,12L,2);
		it.addInterval(10L,12L,3);
		it.addInterval(11L,15L,4);
		it.addInterval(11L,15L,5);
		
		//it.addInterval(11L,15L,3);
		
		it.build();
	System.out.println(it.toString());	
	}

}
