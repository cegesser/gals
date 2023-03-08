package gesser.gals.util;

import java.util.Iterator;
import java.util.BitSet;

/**
 *  Iterador que avança sobre os elementos de um BitSet.
 * Cada chamada a next() retorna o índice do próximo "1" do BitSet.
 *
 * @author Carlos Eduardo Gesser
 */

public class BitSetIterator implements Iterator
{
    private BitSet bitSet;
    private int next;
    private int current;

    public BitSetIterator(BitSet bitSet)
    {
        this.bitSet = bitSet;
        current = -1;
        for (next = 0; next < bitSet.length() && !bitSet.get(next); next++);
    }

    public boolean hasNext()
    {
        return next < bitSet.length();
    }

    public int nextInt()
    {
        current = next;
        for (next++ ; next < bitSet.length() && !bitSet.get(next); next++);
        return current;
    }

    public Object next()
    {
        return new Integer(nextInt());
    }

    public void remove()
    {
        bitSet.clear(current);
    }
}
