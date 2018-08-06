package gesser.gals.util;


public class IntList
{
    private int[] elementData;
    private int size;

    public IntList() 
    {
    	elementData = new int[10];
		size = 0;
    }

    public IntList(int... c) 
    {
        size = c.length;
        // Allow 10% room for growth
        elementData = new int[(int)Math.min((size*110L)/100,Integer.MAX_VALUE)]; 
        
        System.arraycopy(c, 0, elementData, 0, size);
    }

    public int size() 
    {
		return size;
    }

    public boolean isEmpty() 
    {
		return size == 0;
    }

    public boolean contains(int elem) 
    {
		return indexOf(elem) >= 0;
    }

    public int indexOf(int elem) 
    {
		for (int i = 0; i < size; i++)
			if (elem == elementData[i])
				return i;
				
		return -1;
	}

    public int lastIndexOf(int elem) 
    {
		for (int i = size-1; i >= 0; i--)
			if (elem == elementData[i])
				return i;
	
		return -1;
    }
    
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof IntList)		
		{
			IntList l = (IntList) obj;
			
			if (size != l.size)
				return false;
				
			for (int i=0; i<size; i++)
			{
				if (elementData[i] != l.elementData[i])
					return false;
			}
			return true;
		}
		else
		{
			return false;
		}
	}

    
    public Object clone() 
    {
		try 
		{ 
	    	IntList v = (IntList)super.clone();
	    	v.elementData = new int[size];
	    	System.arraycopy(elementData, 0, v.elementData, 0, size);
		    return v;
			}
			catch (CloneNotSupportedException e) 
			{ 
			    // this shouldn't happen, since we are Cloneable
			    throw new InternalError();
			}
    }

    public int[] toArray() 
    {
		int[] result = new int[size];
		System.arraycopy(elementData, 0, result, 0, size);
		return result;
    }

    public int[] toArray(int a[]) 
    {
		if (a.length < size)
            a = (int[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);

		System.arraycopy(elementData, 0, a, 0, size);

        if (a.length > size)
            a[size] = 0;

        return a;
    }

	public void ensureCapacity(int minCapacity) 
	{
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) 
		{
	    	int oldData[] = elementData;
	    	int newCapacity = (oldCapacity * 3)/2 + 1;
    	    	if (newCapacity < minCapacity)
			newCapacity = minCapacity;
	    	elementData = new int[newCapacity];
	    	System.arraycopy(oldData, 0, elementData, 0, size);
	}
    }

    public int get(int index) 
    {
		return elementData[index];
    }

    public int set(int index, int element) 
    {
		int oldValue = elementData[index];
		elementData[index] = element;
		return oldValue;
    }

    public boolean add(int i) 
    {
		ensureCapacity(size + 1);  // Increments modCount!!
		elementData[size++] = i;
		return true;
    }

    public void add(int index, int element) 
    {
		if (index > size || index < 0)
	    	throw new IndexOutOfBoundsException(
				"Index: "+index+", Size: "+size);

		ensureCapacity(size+1);  // Increments modCount!!
		System.arraycopy(elementData, index, elementData, index + 1, size - index);
		elementData[index] = element;
		size++;
    }

    public int remove(int index) 
    {
		int oldValue = elementData[index];

		int numMoved = size - index - 1;
		if (numMoved > 0)
	    	System.arraycopy(elementData, index+1, elementData, index, numMoved);
	    	
	    size--;
		return oldValue;
    }

    public void clear() 
    {
		size = 0;
    }
    
    public boolean addAll(int[] c) 
    {
		int numNew = c.length;
		ensureCapacity(size + numNew);

		for (int i=0; i<numNew; i++)
	    	elementData[size++] = c[i];

		return numNew != 0;
    }
    
    public boolean addAll(IntList c) 
    {
		int numNew = c.size();
		ensureCapacity(size + numNew);

		for (int i=0; i<numNew; i++)
	    	elementData[size++] = c.get(i);

		return numNew != 0;
    }
   
    public boolean addAll(int index, int[] c) 
    {
		if (index > size || index < 0)
	    	throw new IndexOutOfBoundsException(
				"Index: "+index+", Size: "+size);

		int numNew = c.length;
		ensureCapacity(size + numNew);  // Increments modCount!!

		int numMoved = size - index;
		if (numMoved > 0)
	    	System.arraycopy(elementData, index, elementData, index + numNew, numMoved);

		for (int i=0; i<numNew; i++)
	    	elementData[index++] = c[i];

		size += numNew;
		return numNew != 0;
    }

    public void removeRange(int fromIndex, int toIndex) 
    {
		int numMoved = size - toIndex;
        	System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

		size -= (toIndex-fromIndex);
	}   
	
	public String toString()
	{
		StringBuffer result = new StringBuffer("[");
		if (size >= 1)
			result.append(get(0));
		for (int i=1; i<size; i++ )
		{
			result.append(", ").append(get(i));		
		}
		result.append("]");
		return result.toString();
	} 
}
