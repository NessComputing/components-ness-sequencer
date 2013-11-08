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

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

public class SequencerJsonSerializationTest
{
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEmptySequencerSerialize() throws Exception
    {
        byte[] emptySeq = mapper.writeValueAsBytes(Sequencers.emptySequencer());
        Sequencer<String> newEmptySeq = mapper.readValue(emptySeq, new TypeReference<Sequencer<String>>() {});
        assertEquals(0, newEmptySeq.size());
    }

    @Test
    public void testHashSequencerSerialize1() throws Exception
    {
        HashSequencer<String> origSeq = new HashSequencer<>();

        origSeq.sequenceOrAdd("a");
        origSeq.sequenceOrAdd("b");
        origSeq.sequenceOrAdd("c");

        byte[] seq = mapper.writeValueAsBytes(origSeq);
        Sequencer<String> newSeq = mapper.readValue(seq, new TypeReference<Sequencer<String>>() {});
        assertEquals(origSeq, newSeq);
    }
}
