package com.nesscomputing.sequencer;

public class HashSequencerTest extends AbstractSequencerTest<HashSequencer<String>>
{
    @Override
    protected HashSequencer<String> createEmpty()
    {
        return new HashSequencer<>();
    }

    @Override
    protected HashSequencer<String> extend(HashSequencer<String> from, String... newKeys)
    {
        for (String newKey : newKeys) {
            from.sequenceOrAdd(newKey);
        }
        return from;
    }
}
