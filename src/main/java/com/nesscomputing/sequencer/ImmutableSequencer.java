package com.nesscomputing.sequencer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as=ImmutableSequencerImpl.class)
public abstract class ImmutableSequencer<K> extends AbstractSequencer<K>
{
    private static final long serialVersionUID = 1L;

    public static <K> ImmutableSequencer<K> copyOf(Sequencer<K> seq)
    {
        if (seq instanceof ImmutableSequencer){
            return (ImmutableSequencer<K>) seq;
        }
        return new ImmutableSequencerImpl<>(seq);
    }

    @Override
    public final int sequenceOrAdd(K key)
    {
        throw new UnsupportedOperationException("Immutable sequencers may not be modified");
    }


    public ImmutableShadowingSequencerBuilder<K> extendImmutableSequence()
    {
        return new ImmutableShadowingSequencerBuilder<K>(this);
    }

    /**
     * @return the number of Sequencers backing this immutable instance.  Usually 1.
     */
    protected abstract int depth();
}
