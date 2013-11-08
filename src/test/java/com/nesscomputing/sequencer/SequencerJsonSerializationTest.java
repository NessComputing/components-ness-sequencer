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
