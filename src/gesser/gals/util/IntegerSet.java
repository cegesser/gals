/*
 * Copyright 2004 Carlos Eduardo Gesser
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, you can get it at http://www.gnu.org/licenses/gpl.txt
 */

package gesser.gals.util;

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Iterator;

public class IntegerSet extends AbstractSet<Integer>
{
	private BitSet elements = new BitSet();
	
	public IntegerSet()
	{		
	}
	
	public IntegerSet(IntegerSet otr)
	{
		addAll(otr);
	}
	
	public IntegerSet(BitSet bs)
	{
		elements = (BitSet)bs.clone();
	}

	public Iterator<Integer> iterator() 
	{
		return new MyIterator(elements);
	}

	public int size() 
	{
		return elements.cardinality();
	}

	public boolean add(Integer i)
	{
		return add(i.intValue());
	}
	
	public boolean add(int i)
	{
		boolean added = ! elements.get(i);
		elements.set(i);
		return added;
	}
	
	public int first()
	{
		return elements.nextSetBit(0);
	}
	
	public boolean contains(Integer i)
	{
		return contains(i.intValue());
	}
	
	public boolean contains(int i)
	{
		return elements.get(i);
	}
	
	public boolean remove(Integer i)
	{
		return remove(i.intValue());
	}
	
	public boolean remove(int i)
	{
		boolean removed = elements.get(i);
		elements.clear(i);
		return removed;
	}
	
	public void clear()
	{
		elements.clear();
	}
	
	public boolean addAll(IntegerSet is)
	{
		int size = size();
		elements.or(is.elements);
		return size != size();
	}
	
	public boolean retainAll(IntegerSet is)
	{
		int size = size();
		elements.and(is.elements);
		return size != size();
	}
	
	private static class MyIterator implements Iterator<Integer>
	{
		private BitSet bitSet;
	    private int current;
	    private int previous = -1;

	    private MyIterator(BitSet bitSet)
	    {
	        this.bitSet = bitSet;
	        current = bitSet.nextSetBit(0);
	    }

	    public boolean hasNext()
	    {
	        return current >= 0;
	    }

	    public Integer next()
	    {
	    	previous = current;
	    	current = bitSet.nextSetBit(current+1);
	    	
	    	//TODO: autobox
	        return Integer.valueOf(previous);
	    }

	    public void remove()
	    {
	    	bitSet.clear(previous);
	    }
	}

}
