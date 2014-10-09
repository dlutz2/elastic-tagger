package org.opensextant.tagger.interval;

import java.util.ArrayList;
import java.util.List;

/**
 * The Interval class maintains an interval with some associated data
 * 
 * @author Kevin Dolan
 * 
 * @param <Type>
 *            The type of data being stored
 */
public class Interval<Type> implements Comparable<Interval<Type>> {

	private long start;
	private long end;
	private List<Type> data;

	public Interval(long start, long end, Type data) {
		this.start = start;
		this.end = end;
		this.data =  new ArrayList<Type>();
		this.data.add(data);
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public List<Type> getData() {
		return data;
	}

	public void setData(List<Type> data) {
		this.data = data;
	}

	public void addData(Type data) {
		if(this.data == null){
			this.data=  new ArrayList<Type>();
		}
		this.data.add(data);
	}
	
	
	
	/**
	 * @param time
	 * @return true if this interval contains time (inclusive)
	 */
	public boolean contains(long time) {
		return time < end && time > start;
	}

	/**
	 * @param other
	 * @return return true if this interval intersects other
	 */
	public boolean intersects(Interval<?> other) {
		return other.getEnd() > start && other.getStart() < end;
	}

	/**
	 * @param other
	 * @return return true if this interval contains but identical to other
	 */
	public boolean contains(Interval<?> other) {
		return !this.identical(other)  && this.contains(other.getStart()) && this.contains(other.getEnd());
	}

	/**
	 * @param other
	 * @return return true if this interval identical
	 */
	public boolean identical(Interval<?> other) {
		return other.getStart() == start && other.getEnd() == end;
	}

	/**
	 * Return -1 if this interval's start time is less than the other, 1 if
	 * greater In the event of a tie, -1 if this interval's end time is less
	 * than the other, 1 if greater, 0 if same
	 * 
	 * @param other
	 * @return 1 or -1
	 */
	public int compareTo(Interval<Type> other) {
		if (start < other.getStart())
			return -1;
		else if (start > other.getStart())
			return 1;
		else if (end < other.getEnd())
			return -1;
		else if (end > other.getEnd())
			return 1;
		else
			return 0;
	}

	
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append(this.start);
		buf.append(this.end);
		buf.append(this.data);
		
		return buf.toString();
	}
	
}
