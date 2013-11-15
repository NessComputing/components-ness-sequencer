package com.nesscomputing.sequencer;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import gnu.trove.map.TObjectIntMap;

class SubSequenceView<K> extends ImmutableSequencer<K>
{
    private static final long serialVersionUID = 1L;
    private final AbstractSequencer<K> outerSequence;
    private final int numElements;

    SubSequenceView(AbstractSequencer<K> outerSequence, int numElements)
    {
        Preconditions.checkArgument(numElements >= 0 && outerSequence.size() >= numElements,
                "can't take subsequence of size %s from sequencer of size %s", numElements, outerSequence.size());
        this.outerSequence = outerSequence;
        this.numElements = numElements;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key)
    {
        if (!outerSequence.containsKey(key)) {
            return false;
        }
        return sequenceIfExists((K) key) != -1;
    }

    @Override
    public int sequenceIfExists(K key)
    {
        int index = outerSequence.sequenceIfExists(key);
        return index < numElements ? index : -1;
    }

    @Override
    public void sequenceExisting(Iterable<K> keys, TObjectIntMap<K> result)
    {
        for (K key : keys) {
            int index = sequenceIfExists(key);
            if (index != -1) {
                result.put(key, index);
            }
        }
    }

    @Override
    @Nonnull
    public K unsequence(int index)
    {
        if (index < 0 || index >= numElements) {
            throw new IndexOutOfBoundsException("index " + index + " out of range [0," + numElements + ")");
        }
        return outerSequence.unsequence(index);
    }

    @Override
    public int size()
    {
        return numElements;
    }

    @Override
    protected int depth()
    {
        return 1;
    }
}
