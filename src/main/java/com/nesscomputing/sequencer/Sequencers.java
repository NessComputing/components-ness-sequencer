package com.nesscomputing.sequencer;

public class Sequencers
{
    private Sequencers() { }

    public static <T> Sequencer<T> unmodifiableSequencer(Sequencer<T> seq)
    {
        if (seq instanceof UnmodifiableSequencer || seq instanceof ImmutableSequencer) {
            return seq;
        }
        return new UnmodifiableSequencer<>(seq);
    }

    private static class UnmodifiableSequencer<T> extends DelegatingSequencer<T>
    {
        private static final long serialVersionUID = 1L;

        public UnmodifiableSequencer(Sequencer<T> seq)
        {
            super(seq);
        }

        @Override
        public int sequenceOrAdd(T key)
        {
            throw new UnsupportedOperationException("Unable to add to unmodifiable sequencer");
        }
    }

    private static final ImmutableSequencer<Object> EMPTY_SEQUENCER = ImmutableSequencer.copyOf(new HashSequencer<>());
    @SuppressWarnings("unchecked")
    public static <T> ImmutableSequencer<T> emptySequencer()
    {
        return (ImmutableSequencer<T>) EMPTY_SEQUENCER;
    }
}
