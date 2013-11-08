package com.nesscomputing.sequencer;


import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import gnu.trove.map.TObjectIntMap;

class DelegatingSequencer<T> implements Sequencer<T>
{
    private static final long serialVersionUID = 1L;

    private final Sequencer<T> delegate;

    DelegatingSequencer(Sequencer<T> delegate)
    {
        this.delegate = delegate;
    }

    protected Sequencer<T> getDelegate()
    {
        return delegate;
    }

    @Override
    public List<T> getKeys()
    {
        return delegate.getKeys();
    }

    @Override
    public int sequenceOrAdd(T key)
    {
        return delegate.sequenceOrAdd(key);
    }

    @Override
    public Set<Entry<T, Integer>> entrySet()
    {
        return delegate.entrySet();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return delegate.containsKey(key);
    }

    @Override
    public int sequence(T key) throws SequencerKeyException
    {
        return delegate.sequence(key);
    }

    @Override
    public int sequenceIfExists(T key)
    {
        return delegate.sequenceIfExists(key);
    }

    @Override
    public TObjectIntMap<T> sequenceExisting(Iterable<T> keys)
    {
        return delegate.sequenceExisting(keys);
    }

    @Override
    public void sequenceExisting(Iterable<T> keys, TObjectIntMap<T> result)
    {
        delegate.sequenceExisting(keys, result);
    }

    @Override
    public T unsequence(int index)
    {
        return delegate.unsequence(index);
    }

    @Override
    public int size()
    {
        return delegate.size();
    }
}
