package org.opensextant.tagger;

import org.opensextant.tagger.interval.TagTree;

public class IntervalTreeTest {

	public static void main(String[] args) {

		TagTree it = new TagTree();
		
		it.addTag(new Tag(10,14));
		it.addTag(new Tag(10,14));
		it.addTag(new Tag(10,14));
		it.addTag(new Tag(10,14));
		

		
		it.build();
	System.out.println(it.toString());	
	}

}
