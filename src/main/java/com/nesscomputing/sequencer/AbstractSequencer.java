package com.nesscomputing.sequencer;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;


abstract class AbstractSequencer<K> implements Sequencer<K>
{
    private static final long serialVersionUID = 1L;

    private transient volatile Set<Entry<K, Integer>> entrySet;
    private transient volatile List<K> keyList;

    @Override
    public TObjectIntMap<K> sequenceExisting(Iterable<K> keys)
    {
        int startSize = 10;
        if (keys instanceof Collection) {
            startSize = ((Collection<?>) keys).size();
        }
        TObjectIntMap<K> result = new TObjectIntHashMap<K>(startSize);

        sequenceExisting(keys, result);

        return result;
    }

    @Override
    public int sequence(K key) throws SequencerKeyException
    {
        int result = sequenceIfExists(key);
        if (result == -1) {
            throw new SequencerKeyException("no key %s", key);
        }
        return result;
    }

    @Override
    public Set<Entry<K, Integer>> entrySet()
    {
        if (entrySet != null) {
            return entrySet;
        }
        return entrySet = new SequencerEntrySet<>(this);
    }

    @Override
    public List<K> getKeys()
    {
        if (keyList != null) {
            return keyList;
        }
        return keyList = new SequencerKeyList<>(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Sequencer) {
            return entrySet().equals(((Sequencer<?>) obj).entrySet());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return entrySet().hashCode();
    }

    @Override
    public String toString()
    {
        int size = size();
        if (size > 5) {
            return String.format("[%s: %d elements]", getClass().getSimpleName(), size);
        } else {
            return String.format("[%s: %s]", getClass().getSimpleName(), entrySet());
        }
    }
}
