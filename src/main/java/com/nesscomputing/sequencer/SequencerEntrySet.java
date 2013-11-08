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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;

class SequencerEntrySet<K> extends AbstractSet<Entry<K, Integer>>
{
    private final Sequencer<K> sequencer;

    public SequencerEntrySet(Sequencer<K> sequencer)
    {
        this.sequencer = sequencer;
    }

    @Override
    public boolean contains(Object o)
    {
        if (! (o instanceof Entry<?, ?>)) {
            return false;
        }

        Entry<?, ?> e = (Entry<?, ?>) o;
        Object value = e.getValue();

        if (! (value instanceof Integer)) {
            return false;
        }

        int intVal = ((Integer) value).intValue();

        return intVal <= sequencer.size() && sequencer.unsequence(intVal).equals(e.getKey());
    }

    @Override
    public Iterator<Entry<K, Integer>> iterator()
    {
        return new SequencerIterator<>(sequencer);
    }

    @Override
    public int size()
    {
        return sequencer.size();
    }

    static class SequencerIterator<K> extends UnmodifiableIterator<Entry<K, Integer>>
    {
        private final Sequencer<K> seq;
        private final int size;

        private int position = 0;

        SequencerIterator(Sequencer<K> seq)
        {
            this.seq = seq;
            size = seq.size();
        }

        @Override
        public boolean hasNext()
        {
            return position < size;
        }

        @Override
        public Entry<K, Integer> next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final Entry<K, Integer> result = Maps.immutableEntry(seq.unsequence(position), position);
            position++;
            return result;
        }
    }
}
