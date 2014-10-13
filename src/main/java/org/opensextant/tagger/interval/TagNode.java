package org.opensextant.tagger.interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opensextant.tagger.action.Tag;

public class TagNode {

	private SortedSet<Tag> nodeTags;
	private int center;
	private TagNode leftNode;
	private TagNode rightNode;

	public TagNode() {
		nodeTags = new TreeSet<Tag>();
		center = 0;
		leftNode = null;
		rightNode = null;
	}

	public TagNode(List<Tag> tagList) {

		nodeTags = new TreeSet<Tag>();

		SortedSet<Integer> endpoints = new TreeSet<Integer>();

		for (Tag tag : tagList) {
			endpoints.add(tag.getStart());
			endpoints.add(tag.getEnd());
		}

		int median = getMedian(endpoints);
		center = median;

		List<Tag> left = new ArrayList<Tag>();
		List<Tag> right = new ArrayList<Tag>();

		// allocate Tags to this and left, right sub trees
		for (Tag tag : tagList) {
			if (tag.getEnd() < median)
				left.add(tag);
			else if (tag.getStart() > median)
				right.add(tag);
			else {
				nodeTags.add(tag);
			}
		}
		
		if (left.size() > 0)
			leftNode = new TagNode(left);
		if (right.size() > 0)
			rightNode = new TagNode(right);
	}

	public List<Tag> queryPoint(int point) {
		List<Tag> result = new ArrayList<Tag>();

		for (Tag t : nodeTags) {
			if (t.contains(point)) {
				result.add(t);
			} else if (t.getStart() > point)
				break;
		}

		if (point < center && leftNode != null)
			result.addAll(leftNode.queryPoint(point));
		else if (point > center && rightNode != null)
			result.addAll(rightNode.queryPoint(point));
		return result;
	}

	public List<Tag> queryIdentical(Tag target) {
		List<Tag> result = new ArrayList<Tag>();

		for (Tag t : nodeTags) {
			if (t.identical(target)) {
				result.add(t);
			} else if (t.getStart() > target.getStart()) {
				break;
			}
		}

		if (target.getStart() < center && leftNode != null)
			result.addAll(leftNode.queryIdentical(target));
		if (target.getEnd() > center && rightNode != null)
			result.addAll(rightNode.queryIdentical(target));
		return result;
	}

	public List<Tag> queryInteract(Tag target) {
		List<Tag> result = new ArrayList<Tag>();

		for (Tag t : nodeTags) {
			if (t.interact(target)) {
				result.add(t);
			} else if (t.getStart() > target.getEnd()) {
				break;
			}
		}

		if (target.getStart() < center && leftNode != null)
			result.addAll(leftNode.queryInteract(target));
		if (target.getEnd() > center && rightNode != null)
			result.addAll(rightNode.queryInteract(target));
		return result;
	}

	public List<Tag> queryContains(Tag target) {
		List<Tag> result = new ArrayList<Tag>();

		for (Tag t : nodeTags) {
			if (t.containedBy(target)) {
				result.add(t);
			} else if (t.getStart() > target.getEnd()) {
				break;
			}

		}

		if (target.getStart() < center && leftNode != null)
			result.addAll(leftNode.queryContains(target));
		if (target.getEnd() > center && rightNode != null)
			result.addAll(rightNode.queryContains(target));
		return result;
	}

	public List<Tag> queryOverlapLeft(Tag target) {
		List<Tag> result = new ArrayList<Tag>();

		for (Tag t : nodeTags) {
			if (t.overlapLeft(target)) {
				result.add(t);
			} else if (t.getStart() > target.getEnd()) {
				break;
			}

		}

		if (target.getStart() < center && leftNode != null)
			result.addAll(leftNode.queryOverlapLeft(target));
		if (target.getEnd() > center && rightNode != null)
			result.addAll(rightNode.queryOverlapLeft(target));
		return result;
	}

	public List<Tag> queryOverlapRight(Tag target) {
		List<Tag> result = new ArrayList<Tag>();

		for (Tag t : nodeTags) {
			if (t.overlapRight(target)) {
				result.add(t);
			} else if (t.getStart() > target.getEnd()) {
				break;
			}

		}

		if (target.getStart() < center && leftNode != null)
			result.addAll(leftNode.queryOverlapRight(target));
		if (target.getEnd() > center && rightNode != null)
			result.addAll(rightNode.queryOverlapRight(target));
		return result;
	}

	public void mergeIdenticals() {

		if (nodeTags.size() > 1) {
			

		Map<String, List<Tag>> tagMap = new HashMap<String, List<Tag>>();

		for (Tag t : nodeTags) {
			String key = String.valueOf(t.getStart()) + "|"
					+ String.valueOf(t.getEnd());
			if (!tagMap.containsKey(key)) {
				tagMap.put(key, new ArrayList<Tag>());
			}
			tagMap.get(key).add(t);
		}

		for (String key : tagMap.keySet()) {
			List<Tag> tmpList = tagMap.get(key);
			if (tmpList.size() > 1) {

				Tag firstTag = tmpList.get(0);
				for (int c = 1; c < tmpList.size(); c++) {
					Tag tmpTag = tmpList.get(c);
					firstTag.mergeDocs(tmpTag.getDocs());
					tmpTag.setIncluded(false);
				}

			}

		}

		}
		if(this.leftNode != null){
			this.leftNode.mergeIdenticals();
		}
		
		if(this.rightNode != null){
			this.rightNode.mergeIdenticals();
		}
		
		
	}

	// getters and setters
	public int getCenter() {
		return center;
	}

	public void setCenter(int center) {
		this.center = center;
	}

	public TagNode getLeft() {
		return leftNode;
	}

	public void setLeft(TagNode left) {
		this.leftNode = left;
	}

	public TagNode getRight() {
		return rightNode;
	}

	public void setRight(TagNode right) {
		this.rightNode = right;
	}

	private Integer getMedian(SortedSet<Integer> set) {
		int i = 0;
		int middle = set.size() / 2;
		for (Integer point : set) {
			if (i == middle)
				return point;
			i++;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(center + ": ");
		for (Tag t : nodeTags) {
			sb.append("[" + t.getStart() + "," + t.getEnd() + "]");
			sb.append("} ");
		}
		return sb.toString();
	}

}
