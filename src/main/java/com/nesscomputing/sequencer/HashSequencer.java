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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Maps a sparse range of keys to a dense range (starting at 0).
 *
 * @param <K> the type of sparse keys to map
 */
@NotThreadSafe
public class HashSequencer<K> extends AbstractSequencer<K> {
    private static final long serialVersionUID = 1L;
    private static final float LOAD_FACTOR = 0.5f;

    private final TObjectIntHashMap<K> keyToInt;
    private final List<K> intToKey;
    private int nextInt = 0;

    private HashSequencer(int startingSize) {
        keyToInt = new TObjectIntHashMap<K>(startingSize, LOAD_FACTOR, -1);
        intToKey = Lists.newArrayListWithCapacity(startingSize);
    }

    private HashSequencer(Sequencer<K> other) {
        this(other.size(), other.getKeys());
    }

    private HashSequencer(int size, Iterable<K> elements) {
        keyToInt = new TObjectIntHashMap<K>(size, LOAD_FACTOR, -1);
        intToKey = Lists.newArrayListWithCapacity(size);

        int i = 0;
        Iterator<K> iter = elements.iterator();
        while (iter.hasNext()) {
            K key = iter.next();
            this.intToKey.add(key);
            this.keyToInt.put(key, i++);
        }

        this.nextInt = intToKey.size();
    }

    /**
     * Create a new HashSequencer with a default initial size.
     */
    public static <K> HashSequencer<K> create()
    {
        return createWithInitialCapacity(10);
    }

    /**
     * Create a new HashSequencer with a specified initial size.
     */
    public static <K> HashSequencer<K> createWithInitialCapacity(int startingSize)
    {
        return new HashSequencer<>(startingSize);
    }

    /**
     * Create a new HashSequencer with the elements of another sequencer.
     */
    public static <K> HashSequencer<K> copyOf(Sequencer<K> other)
    {
        return new HashSequencer<K>(other);
    }

    /**
     * Create a new HashSequencer with the elements of an array.
     */
    @SafeVarargs
    public static <K> HashSequencer<K> copyOf(K... elements)
    {
        return new HashSequencer<>(elements.length, Arrays.asList(elements));
    }

    /**
     * Create a new HashSequencer with the elements of an array.
     */
    public static <K> HashSequencer<K> copyOf(Iterable<K> elements)
    {
        return new HashSequencer<>(Iterables.size(elements), elements);
    }

    @Override
    public List<K> getKeys() {
        return Collections.unmodifiableList(intToKey);
    }

    @Override
    public int sequenceOrAdd(K key) {
        assert keyToInt.getNoEntryValue() == -1 : "noEntryValue must be == -1";

        int result = keyToInt.get(key);
        if (result == keyToInt.getNoEntryValue()) {
            keyToInt.put(key, result = nextInt);
            intToKey.add(key);
            nextInt++;
        }

        return result;
    }

    @Override
    public boolean containsKey(Object key) {
        return keyToInt.containsKey(key);
    }

    @Override
    public int sequenceIfExists(K key) {
        assert keyToInt.getNoEntryValue() == -1 : "noEntryValue must be == -1";
        return keyToInt.get(key);
    }

    @Override
    public void sequenceExisting(Iterable<K> keys, TObjectIntMap<K> result)
    {
        assert keyToInt.getNoEntryValue() == -1 : "noEntryValue must be == -1";
        for (K key : keys) {
            int val = keyToInt.get(key);
            if (val != keyToInt.getNoEntryValue()) {
                result.put(key, val);
            }
        }
    }

    @Override
    public K unsequence(int index) {
        return intToKey.get(index);
    }

    @Override
    public int size() {
        return nextInt;
    }
}
