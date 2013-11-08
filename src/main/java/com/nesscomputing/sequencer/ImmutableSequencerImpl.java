package com.nesscomputing.sequencer;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

class ImmutableSequencerImpl<K> extends ImmutableSequencer<K>
{
    private static final long serialVersionUID = 1L;

    private final TObjectIntMap<K> forward;

    private final K[] reverse;

    public ImmutableSequencerImpl(@JsonProperty("forward") TObjectIntMap<K> forward, @JsonProperty("reverse") K[] reverse)
    {
        this.forward = forward;
        this.reverse = reverse;
    }

    @SuppressWarnings("unchecked")
    ImmutableSequencerImpl()
    {
        forward = new TObjectIntHashMap<>(0);
        reverse = (K[]) new Object[0];
    }

    @SuppressWarnings("unchecked")
    @JsonCreator
    ImmutableSequencerImpl(ArrayList<K> elements)
    {
        final int size = elements.size();
        forward = new TObjectIntHashMap<>(size);
        reverse = (K[]) new Object[size];
        int v = 0;
        for (K k : elements) {
            forward.put(k, v);
            reverse[v] = k;
            v++;
        }
    }

    @SuppressWarnings("unchecked")
    ImmutableSequencerImpl(Sequencer<K> sequencer)
    {
        final int size = sequencer.size();
        forward = new TObjectIntHashMap<>(size);
        reverse = (K[]) new Object[size];

        for (int v = 0; v < size; v++) {
            final K k = sequencer.unsequence(v);

            forward.put(k, v);
            reverse[v] = k;
        }
    }

    public static <K> ImmutableSequencerImpl<K> copyOf(Sequencer<K> seq)
    {
        if (seq instanceof ImmutableSequencerImpl){
            return (ImmutableSequencerImpl<K>) seq;
        }
        return new ImmutableSequencerImpl<>(seq);
    }

    @Override
    protected int depth()
    {
        return 1;
    }

    @Override
    public List<K> getKeys()
    {
        return new ListKeyView();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return forward.containsKey(key);
    }

    @Override
    public int sequenceIfExists(K key)
    {
        final int result = forward.get(key);
        if (result == forward.getNoEntryValue() && !containsKey(key)) {
            return -1;
        }
        return result;
    }

    @Override
    public void sequenceExisting(Iterable<K> keys, TObjectIntMap<K> result)
    {
        for (K key : keys) {
            int val = forward.get(key);
            if (val != forward.getNoEntryValue() || forward.containsKey(key)) {
                result.put(key, val);
            }
        }
    }

    @Override
    public K unsequence(int index)
    {
        return reverse[index];
    }

    @Override
    public int size()
    {
        return forward.size();
    }

    private class ListKeyView extends AbstractList<K>
    {
        @Override
        public K get(int index)
        {
            return ImmutableSequencerImpl.this.unsequence(index);
        }

        @Override
        public int size()
        {
            return ImmutableSequencerImpl.this.size();
        }
    }
}
