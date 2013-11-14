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

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Immutable
@JsonDeserialize(as=ImmutableSequencerImpl.class)
public abstract class ImmutableSequencer<K> extends AbstractSequencer<K>
{
    private static final long serialVersionUID = 1L;

    @SafeVarargs
    public static <K> ImmutableSequencer<K> of(K... elements)
    {
        return of(Arrays.asList(elements));
    }

    @Nonnull
    public static <K> ImmutableSequencer<K> of(@Nonnull Iterable<K> iter)
    {
        return new ImmutableSequencerImpl<>(iter);
    }

    @Nonnull
    public static <K> ImmutableSequencer<K> copyOf(@Nonnull Sequencer<K> seq)
    {
        if (seq instanceof ImmutableSequencer) {
            return (ImmutableSequencer<K>) seq;
        }
        return new ImmutableSequencerImpl<>(seq);
    }

    @Override
    public final int sequenceOrAdd(@Nonnull K key)
    {
        int result = sequenceIfExists(key);
        if (result == -1) {
            throw new UnsupportedOperationException("Immutable sequencers may not be modified: tried to add " + key);
        }
        return result;
    }

    @Nonnull
    public ImmutableShadowingSequencerBuilder<K> extendImmutableSequence()
    {
        return new ImmutableShadowingSequencerBuilder<K>(this);
    }

    /**
     * @return the number of Sequencers backing this immutable instance.  Usually 1.
     */
    protected abstract int depth();
}
