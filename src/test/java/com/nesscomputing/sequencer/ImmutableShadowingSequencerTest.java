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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;

import gnu.trove.map.TObjectIntMap;

public class ImmutableShadowingSequencerTest
{
    private ImmutableSequencer<String> baseSeq;

    @Before
    public void setUp()
    {
        Sequencer<String> seq = HashSequencer.create();

        seedSeq(seq);

        baseSeq = ImmutableSequencer.copyOf(seq);
    }

    private void seedSeq(Sequencer<String> seq)
    {
        seq.sequenceOrAdd("a");
        seq.sequenceOrAdd("b");
        seq.sequenceOrAdd("c");
        seq.sequenceOrAdd("d");
        seq.sequenceOrAdd("e");
    }

    @Test
    public void testNoOverrides()
    {
        ImmutableSequencer<String> shadow = baseSeq.extendImmutableSequence().build();
        assertEquals(baseSeq.size(), shadow.size());
        assertEquals(baseSeq.entrySet(), shadow.entrySet());
    }

    @Test
    public void testNoBase()
    {
        ImmutableShadowingSequencerBuilder<String> seq = Sequencers.<String>emptySequencer().extendImmutableSequence();

        seedSeq(seq);

        assertEquals(baseSeq.entrySet(), seq.build().entrySet());
    }

    @Test
    public void testDuplicates()
    {
        ImmutableShadowingSequencerBuilder<String> seq = baseSeq.extendImmutableSequence();

        seedSeq(seq);

        assertEquals(baseSeq.entrySet(), seq.build().entrySet());
    }

    @Test
    public void testDiverging()
    {
        ImmutableShadowingSequencerBuilder<String> builder = baseSeq.extendImmutableSequence();

        builder.sequenceOrAdd("f");
        seedSeq(builder);
        builder.sequenceOrAdd("g");

        ImmutableSequencer<String> seq = builder.build();

        assertEquals(2 + baseSeq.size(), seq.size());
        assertEquals(baseSeq.size(), seq.sequenceIfExists("f"));
        assertEquals(baseSeq.size() + 1, seq.sequenceIfExists("g"));

        assertEquals("a", seq.unsequence(0));
        assertEquals("e", seq.unsequence(4));
        assertEquals("f", seq.unsequence(5));
        assertEquals("g", seq.unsequence(6));
    }

    @Test
    public void testSequenceMany()
    {
        ImmutableShadowingSequencerBuilder<String> builder = baseSeq.extendImmutableSequence();

        builder.sequenceOrAdd("f");
        seedSeq(builder);
        builder.sequenceOrAdd("g");

        ImmutableSequencer<String> seq = builder.build();

        TObjectIntMap<String> result = seq.sequenceExisting(ImmutableSet.of("a", "c", "g"));
        assertEquals(3, result.size());
        assertEquals(0, result.get("a"));
        assertEquals(2, result.get("c"));
        assertEquals(6, result.get("g"));
    }

    @Test
    public void testCompact()
    {
        ImmutableShadowingSequencerBuilder<String> builder = baseSeq.extendImmutableSequence();

        builder.sequenceOrAdd("f");
        seedSeq(builder);
        builder.sequenceOrAdd("g");

        ImmutableSequencer<String> seq = builder.build();
        ImmutableSequencer<String> compacted = builder.buildAndCompact();

        assertTrue(seq instanceof ImmutableShadowingSequencer);
        assertFalse(compacted instanceof ImmutableShadowingSequencer);
        assertEquals(seq, compacted);
    }

    @Test
    public void testSerializationProxy() throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ImmutableShadowingSequencerBuilder<String> builder = baseSeq.extendImmutableSequence();

        builder.sequenceOrAdd("f");
        seedSeq(builder);
        builder.sequenceOrAdd("g");

        ImmutableSequencer<String> seq = builder.build();

        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(seq);
        }

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));

        @SuppressWarnings("unchecked")
        ImmutableSequencer<String> newSeq = (ImmutableSequencer<String>) ois.readObject();

        assertTrue(newSeq instanceof ImmutableSequencer);
        assertFalse(newSeq instanceof ImmutableShadowingSequencer);

        assertEquals(seq.entrySet(), newSeq.entrySet());
    }

    @Test
    public void testSequenceOrAdd()
    {
        ImmutableShadowingSequencerBuilder<String> builder = baseSeq.extendImmutableSequence();
        assertEquals(baseSeq.size(), builder.sequenceOrAdd("f"));
    }

    @Test
    public void testNoUpdate()
    {
        ImmutableSequencer<Object> base = Sequencers.emptySequencer();
        assertSame(base, base.extendImmutableSequence().build());
    }

    @Test
    public void testCollapsing()
    {
        Random r = new Random(123456);

        ImmutableShadowingSequencerBuilder<String> builder = Sequencers.<String>emptySequencer().extendImmutableSequence();

        int i = 0;

        for (int count = 0; count < 300; count++) {
            for (int innerCount = 0; innerCount < r.nextInt(100); innerCount++) {
                builder.sequenceOrAdd(Integer.toString(i++));
            }
            builder = builder.build().extendImmutableSequence();
        }

        ImmutableSequencer<String> result = builder.build();

        assertEquals(i, result.size());

        for (i--; i >= 0; i--) {
            assertEquals(Integer.toString(i), result.unsequence(i));
        }
        assertEquals(2, result.depth());
    }
}
