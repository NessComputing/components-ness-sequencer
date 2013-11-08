/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.sequencer;

import java.util.AbstractList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * A Sequencer that is initialized upon construction and may never
 * change afterward.
 */
@Immutable
class ImmutableSequencerImpl<K> extends ImmutableSequencer<K>
{
    private static final long serialVersionUID = 1L;
    private static final float LOAD_FACTOR = 0.5f;

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
        forward = new TObjectIntHashMap<>(0, LOAD_FACTOR, -1);
        reverse = (K[]) new Object[0];
    }

    @SuppressWarnings("unchecked")
    @JsonCreator
    ImmutableSequencerImpl(List<K> elements)
    {
        final int size = elements.size();
        forward = new TObjectIntHashMap<>(size, LOAD_FACTOR, -1);
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
        forward = new TObjectIntHashMap<>(size, LOAD_FACTOR, -1);
        reverse = (K[]) new Object[size];

        for (int v = 0; v < size; v++) {
            final K k = sequencer.unsequence(v);

            forward.put(k, v);
            reverse[v] = k;
        }
    }

    @Nonnull
    public static <K> ImmutableSequencerImpl<K> copyOf(@Nonnull Sequencer<K> seq)
    {
        if (seq instanceof ImmutableSequencerImpl) {
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
        assert forward.getNoEntryValue() == -1 : "noEntryValue must be == -1";
        return forward.get(key);
    }

    @Override
    public void sequenceExisting(Iterable<K> keys, TObjectIntMap<K> result)
    {
        assert forward.getNoEntryValue() == -1 : "noEntryValue must be == -1";
        for (K key : keys) {
            int val = forward.get(key);
            if (val != forward.getNoEntryValue()) {
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
