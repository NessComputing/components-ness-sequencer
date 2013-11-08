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

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import gnu.trove.map.TObjectIntMap;

/**
 * Sequencer implementation that does nothing but forward invocations
 * to a delegate.
 */
class DelegatingSequencer<T> implements Sequencer<T>
{
    private static final long serialVersionUID = 1L;

    private final Sequencer<T> delegate;

    protected DelegatingSequencer(Sequencer<T> delegate)
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
