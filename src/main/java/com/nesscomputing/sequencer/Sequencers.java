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

/**
 * Utility methods that work with {@link Sequencer}s.
 */
public class Sequencers
{
    private Sequencers() { }

    /**
     * Decorate the given Sequencer with a wrapper that rejects writes.
     * The returned Sequencer may still change if the original referant
     * is modified.
     */
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

    private static final ImmutableSequencer<Object> EMPTY_SEQUENCER = ImmutableSequencer.copyOf(HashSequencer.create());

    /**
     * Return a Sequencer which is empty and may never change.
     */
    @SuppressWarnings("unchecked")
    public static <T> ImmutableSequencer<T> emptySequencer()
    {
        return (ImmutableSequencer<T>) EMPTY_SEQUENCER;
    }
}
