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

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import gnu.trove.map.TObjectIntMap;

public abstract class AbstractSequencerTest<S extends Sequencer<String>>
{
    protected abstract S createEmpty();
    protected abstract S extend(S from, String... newKeys);

    protected S create(String... keys)
    {
        return extend(createEmpty(), keys);
    }

    @Test
    public void testEmptySequencerSize()
    {
        assertEquals(0, createEmpty().size());
    }

    @Test
    public void testEmptySequencerContainsKey()
    {
        assertFalse(createEmpty().containsKey("aaa"));
    }

    @Test
    public void testEmptySequencerSequence()
    {
        Sequencer<String> seq = createEmpty();
        try {
            seq.sequence("aaa");
            fail();
        } catch (SequencerKeyException expected) {
            // ignored
        }
        try {
            seq.sequence("bbb");
            fail();
        } catch (SequencerKeyException expected) {
            // ignored
        }

        assertEquals(-1, seq.sequenceIfExists("aaa"));
        assertEquals(-1, seq.sequenceIfExists("bbb"));
    }

    @Test
    public void testEmptySequencerBatchSequence()
    {
        Sequencer<String> seq = createEmpty();
        TObjectIntMap<String> result = seq.sequenceExisting(ImmutableSet.of("aaa", "bbb", "ccc", "ddd"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testEmptySequencerEntrySet()
    {
        assertTrue(createEmpty().entrySet().isEmpty());
    }

    @Test
    public void testEmptySequencerKeys()
    {
        assertTrue(createEmpty().getKeys().isEmpty());
    }

    @Test
    public void testEmptySerialization()
    {
        final S seq = createEmpty();
        byte[] bytes = SerializationUtils.serialize(seq);
        assertEquals(seq, SerializationUtils.deserialize(bytes));
    }

    @Test
    public void testFullSequencerSize()
    {
        S seq = extend(createEmpty(), "aaa");
        assertEquals(1, seq.size());

        seq = extend(seq, "bbb", "ccc");
        assertEquals(3, seq.size());
    }

    @Test
    public void testFullSequencerContainsKey()
    {
        S seq = extend(createEmpty(), "aaa");
        assertTrue(seq.containsKey("aaa"));

        seq = extend(seq, "bbb", "ccc");
        assertTrue(seq.containsKey("aaa"));
        assertTrue(seq.containsKey("ccc"));
    }

    @Test
    public void testFullSequencerSequence() throws SequencerKeyException
    {
        S seq = extend(createEmpty(), "aaa");
        assertEquals(0, seq.sequence("aaa"));

        seq = extend(seq, "bbb", "ccc");

        assertEquals(0, seq.sequenceIfExists("aaa"));
        assertEquals(2, seq.sequenceIfExists("ccc"));
    }

    @Test
    public void testFullSequencerBatchSequence()
    {
        S seq = extend(createEmpty(), "aaa", "bbb", "ddd");
        TObjectIntMap<String> result = seq.sequenceExisting(ImmutableSet.of("aaa", "bbb", "ccc", "ddd"));
        assertEquals(3, result.size());
        assertEquals(ImmutableSet.copyOf(seq.getKeys()), result.keySet());
        assertEquals(0, result.get("aaa"));
        assertEquals(1, result.get("bbb"));
        assertEquals(2, result.get("ddd"));
    }

    @Test
    public void testFullSequencerEntrySet()
    {
        S seq = extend(createEmpty(), "aaa", "bbb", "ccc", "ddd");
        assertEquals(ImmutableSet.of(
                Maps.immutableEntry("aaa", 0),
                Maps.immutableEntry("bbb", 1),
                Maps.immutableEntry("ccc", 2),
                Maps.immutableEntry("ddd", 3)), seq.entrySet());
    }

    @Test
    public void testFullSequencerKeys()
    {
        S seq = extend(createEmpty(), "aaa", "bbb", "ddd");
        assertEquals(ImmutableList.of("aaa", "bbb", "ddd"), seq.getKeys());
    }

    @Test
    public void testFullSerialization()
    {
        final S seq = extend(createEmpty(), "aaa", "bbb", "ccc", "ddd");
        byte[] bytes = SerializationUtils.serialize(seq);
        assertEquals(seq, SerializationUtils.deserialize(bytes));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSubsequenceNegativeSize()
    {
        create("a", "b").subSequence(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSubsequenceBadSize()
    {
        create("a", "b").subSequence(3);
    }

    @Test
    public void testSubsequence()
    {
        assertEquals(create("a", "b"), create("a", "b", "c").subSequence(2));
        assertEquals(create(), create("a", "b", "c").subSequence(0));
    }
}
