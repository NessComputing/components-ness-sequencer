package com.nesscomputing.sequencer;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gnu.trove.map.TObjectIntMap;


@JsonDeserialize(as=ImmutableSequencerImpl.class)
public interface Sequencer<K> extends Serializable
{

    /** Convert the given sparse key to its dense representation, and defines
     * a mapping for it if the sparse key is unknown.
     *
     * @param key sparse key
     * @return dense key
     */
    int sequenceOrAdd(K key);

    /** Indicates if the given key can be sequenced
     *
     * @param key
     * @return
     */
    boolean containsKey(Object key);

    /**
     * Convert the given sparse key to its dense representation.
     *
     * @param key sparse key
     * @return dense key
     * @throws SequencerKeyException if there is no mapping for this key.
     */
    int sequence(K key) throws SequencerKeyException;

    /**
     * Convert the given sparse key to its dense representation.
     * Returns -1 if no element is found.
     *
     * @param key sparse key
     * @return dense key
     */
    int sequenceIfExists(K key);

    /**
     * Sequence many elements.  Instead of throwing an exception, missing elements
     * are simply missing from the return value.  Be sure to check for {@link TObjectIntMap#getNoEntryValue()}!
     * @param keys the keys to sequence
     * @return all keys' sequences that exist
     */
    TObjectIntMap<K> sequenceExisting(Iterable<K> keys);

    /**
     * Sequence many elements into a preallocated result.  Instead of throwing an exception, missing elements
     * are simply ignored.  Be sure to check for {@link TObjectIntMap#getNoEntryValue()}!
     * @param keys the keys to sequence
     * @return all keys' sequences that exist
     */
    void sequenceExisting(Iterable<K> keys, TObjectIntMap<K> result);

    /** Convert the given dense key back to its sparse key representation.
     *
     * @param index dense key
     * @return sparse key, or null if not known
     */
    K unsequence(int index);

    /** @return The number of sequenced items. */
    int size();

    /**
     * @return the list of known keys, in sequence order.
     */
    @JsonValue
    List<K> getKeys();

    /**
     * @return the set of known entries, in sequence order.
     */
    Set<Entry<K, Integer>> entrySet();
}
