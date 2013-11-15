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

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gnu.trove.map.TObjectIntMap;

/**
 * A Sequencer represents a bidirectional mapping from sparse {@code <K>} to dense integers.
 */
@JsonDeserialize(as=ImmutableSequencerImpl.class)
public interface Sequencer<K> extends Serializable
{
    /**
     * Convert the given sparse key to its dense representation, and defines
     * a mapping for it if the sparse key is unknown.
     *
     * @param key sparse key
     * @return dense key
     */
    int sequenceOrAdd(@Nonnull K key);

    /**
     * Indicates if the given key can be sequenced.
     *
     * @param key
     * @return true iff the key is in the sequencer
     */
    boolean containsKey(Object key);

    /**
     * Convert the given sparse key to its dense representation.
     *
     * @param key sparse key
     * @return dense key
     * @throws SequencerKeyException if there is no mapping for this key.
     */
    int sequence(@Nonnull K key) throws SequencerKeyException;

    /**
     * Convert the given sparse key to its dense representation.
     * Returns -1 if no element is found.
     *
     * @param key sparse key
     * @return dense key
     */
    int sequenceIfExists(@Nonnull K key);

    /**
     * Sequence many elements.  Instead of throwing an exception, missing elements
     * are simply missing from the return value.  Be sure to check for {@link TObjectIntMap#getNoEntryValue()}!
     * @param keys the keys to sequence
     * @return all keys' sequences that exist
     */
    @Nonnull
    TObjectIntMap<K> sequenceExisting(@Nonnull Iterable<K> keys);

    /**
     * Sequence many elements into a preallocated result.  Instead of throwing an exception, missing elements
     * are simply ignored.  Be sure to check for {@link TObjectIntMap#getNoEntryValue()}!
     * @param keys the keys to sequence
     * @return all keys' sequences that exist
     */
    void sequenceExisting(@Nonnull Iterable<K> keys, @Nonnull TObjectIntMap<K> result);

    /**
     * Convert the given dense key back to its sparse key representation.
     *
     * @param index dense key
     * @return sparse key, or null if not known
     */
    @Nonnull
    K unsequence(int index);

    /**
     * @return The number of sequenced items.
     */
    int size();

    /**
     * @return the list of known keys, in sequence order.
     */
    @JsonValue
    @Nonnull
    List<K> getKeys();

    /**
     * @return the set of known entries, in sequence order.
     */
    @Nonnull
    Set<Entry<K, Integer>> entrySet();

    /**
     * Create an subsequence view containing {@code numElements} elements
     * starting from {@code 0}.  Because sequencer elements may not be changed after
     * inserting, the view will be immutable, even for mutable sequencers.
     */
    @Nonnull
    ImmutableSequencer<K> subSequence(int numElements);
}
