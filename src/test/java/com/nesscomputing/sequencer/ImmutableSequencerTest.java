package com.nesscomputing.sequencer;

import org.junit.Test;

public class ImmutableSequencerTest extends AbstractSequencerTest<ImmutableSequencer<String>>
{
    @Override
    protected ImmutableSequencer<String> createEmpty()
    {
        return Sequencers.emptySequencer();
    }

    @Override
    protected ImmutableSequencer<String> extend(ImmutableSequencer<String> fromIn, String... newKeys)
    {
        ImmutableShadowingSequencerBuilder<String> from = fromIn.extendImmutableSequence();
        for (String newKey : newKeys) {
            from.sequenceOrAdd(newKey);
        }
        return from.buildAndCompact();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAddFails()
    {
        createEmpty().sequenceOrAdd("xxx");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAddFails2()
    {
        extend(createEmpty(), "aaa", "bbb").sequenceOrAdd("xxx");
    }
}
