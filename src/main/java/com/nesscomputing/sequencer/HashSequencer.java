package com.nesscomputing.sequencer;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/** Maps a sparse range of keys to a dense range (starting at 0).
 *
 * Not thread safe, but it is only written to while retraining. It is then passed (as part of the
 * larger LocalRecommender) to the other threads in a recommender instance by way of an
 * AtomicReference.
 *
 * @param <K> the type of sparse keys to map
 */
public class HashSequencer<K> extends AbstractSequencer<K> {
    private static final long serialVersionUID = 1L;

    private final TObjectIntHashMap<K> keyToInt;
    private final List<K> intToKey;
    private int nextInt = 0;

    public HashSequencer() {
        keyToInt = new TObjectIntHashMap<K>();
        intToKey = Lists.newArrayList();
    }

    private HashSequencer(Sequencer<K> other) {
        keyToInt = new TObjectIntHashMap<K>(other.size());
        intToKey = Lists.newArrayListWithCapacity(other.size());

        this.nextInt = other.size();
        for (int i = 0; i < this.nextInt; i++) {
            K key = other.unsequence(i);
            this.intToKey.add(key);
            this.keyToInt.put(key, i);
        }
    }

    public static <K> HashSequencer<K> copyOf(Sequencer<K> other)
    {
        return new HashSequencer<K>(other);
    }

    @Override
    public List<K> getKeys() {
        return Collections.unmodifiableList(intToKey);
    }

    @Override
    public int sequenceOrAdd(K key) {
        int result = keyToInt.get(key);
        if (result == keyToInt.getNoEntryValue() && !keyToInt.containsKey(key)) {
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
        int val = keyToInt.get(key);

        if (val == keyToInt.getNoEntryValue() && !keyToInt.containsKey(key))
            return -1;

        return val;
    }

    @Override
    public void sequenceExisting(Iterable<K> keys, TObjectIntMap<K> result)
    {
        for (K key : keys) {
            int val = keyToInt.get(key);
            if (val != keyToInt.getNoEntryValue() || keyToInt.containsKey(key)) {
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
