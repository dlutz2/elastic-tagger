package org.opensextant.tagger.interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensextant.tagger.action.Tag;

public class TagTree {

	private TagNode head;
	private List<Tag> tagList;
	private boolean inSync;
	private int size;

	public TagTree() {
		this.head = new TagNode();
		this.tagList = new ArrayList<Tag>();
		this.inSync = true;
		this.size = 0;

	}

	public TagTree(List<Tag> tagList) {
		this.head = new TagNode(tagList);
		this.tagList = new ArrayList<Tag>();
		this.tagList.addAll(tagList);
		this.inSync = true;
		this.size = tagList.size();
	}

	public void addTag(Tag tag) {
		tagList.add(tag);
		inSync = false;
	}

	public void addTags(List<Tag> tags) {
		for (Tag t : tags) {
			this.addTag(t);
		}
	}

	public void reduceTree(String mode) {

		if (mode.equalsIgnoreCase("NONE")) {
			return;
		}

		List<Tag> tags = this.getAllTags();

		// merge tags with identical spans
		if (mode.equalsIgnoreCase("IDENTICAL") || mode.equalsIgnoreCase("SUB")
				|| mode.equalsIgnoreCase("OVERLAP_LEFT")
				|| mode.equalsIgnoreCase("OVERLAP_RIGHT")) {
			for (Tag t : tags) {
				if (t.isIncluded()) {
					List<Tag> ids = this.getTagsIdentical(t.getStart(),
							t.getEnd());
					for (Tag i : ids) {
						if (i.isIncluded() && i != t) {
							t.mergeTag(i);
							i.setIncluded(false);
						}
					}
				}
			}

		}

		// remove sub tags
		if (mode.equalsIgnoreCase("SUB")
				|| mode.equalsIgnoreCase("OVERLAP_LEFT")
				|| mode.equalsIgnoreCase("OVERLAP_RIGHT")) {
			for (Tag t : tags) {
				if (t.isIncluded()) {
					List<Tag> subs = this.getTagsContains(t.getStart(),
							t.getEnd());
					for (Tag s : subs) {
						s.setIncluded(false);
					}
				}
			}
		}

		// remove overlapping tags, preferring LEFT tags
		if (mode.equalsIgnoreCase("OVERLAP_LEFT")) {
			Collections.sort(tags);
			for (Tag t : tags) {
				if (t.isIncluded()) {
					List<Tag> ovrs = this.getTagsOverlapLeft(t.getStart(),
							t.getEnd());
					for (Tag o : ovrs) {
						o.setIncluded(false);
					}
				}
			}
		}

		// remove overlapping tags, preferring RIGHT
		if (mode.equalsIgnoreCase("OVERLAP_RIGHT")) {
			Collections.sort(tags);
			for (Tag t : tags) {
				if (t.isIncluded()) {
					List<Tag> ovrs = this.getTagsOverlapRight(t.getStart(),
							t.getEnd());
					for (Tag o : ovrs) {
						o.setIncluded(false);
					}
				}
			}
		}

	}

	public List<Tag> getTagsPoint(int point) {
		build();
		return head.queryPoint(point);
	}

	public List<Tag> getTagsIdentical(int start, int end) {
		build();
		return head.queryIdentical(new Tag(start, end));
	}

	public List<Tag> getTagsIntersect(int start, int end) {
		build();
		return head.queryInteract(new Tag(start, end));
	}

	public List<Tag> getTagsContains(int start, int end) {
		build();
		return head.queryContains(new Tag(start, end));
	}

	private List<Tag> getTagsOverlapLeft(int start, int end) {
		build();
		return head.queryOverlapLeft(new Tag(start, end));
	}

	private List<Tag> getTagsOverlapRight(int start, int end) {
		build();
		return head.queryOverlapRight(new Tag(start, end));
	}

	public List<Tag> getAllTags() {
		return this.tagList;
	}

	public List<Tag> getIncludedTags() {
		List<Tag> results = new ArrayList<Tag>();
		for (Tag t : this.tagList) {
			if (t.isIncluded()) {
				results.add(t);
			}
		}
		return results;
	}

	public boolean inSync() {
		return inSync;
	}

	public void build() {
		if (!inSync) {
			head = new TagNode(tagList);
			inSync = true;
			size = tagList.size();
		}
	}

	public int currentSize() {
		return size;
	}

	public int listSize() {
		return tagList.size();
	}

	@Override
	public String toString() {
		return nodeString(head, 0);
	}

	private String nodeString(TagNode node, int level) {
		if (node == null)
			return "";

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++)
			sb.append("\t");
		sb.append(node + "\n");
		sb.append(nodeString(node.getLeft(), level + 1));
		sb.append(nodeString(node.getRight(), level + 1));
		return sb.toString();
	}

}
