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
