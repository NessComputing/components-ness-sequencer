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

import static com.google.common.base.Preconditions.checkState;

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.util.concurrent.atomic.AtomicBoolean;

import gnu.trove.function.TIntFunction;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import com.nesscomputing.logging.Log;

public class ImmutableShadowingSequencerBuilder<K> extends AbstractSequencer<K>
{
    private static final Log LOG = Log.findLog();
    private static final AtomicBoolean WARNED = new AtomicBoolean();
    private static final long serialVersionUID = 1L;

    private final ImmutableSequencer<K> originalBase;
    private final ImmutableSequencer<K> base;
    private final Sequencer<K> additional = new HashSequencer<K>();

    private final int additionalStartingSize;

    ImmutableShadowingSequencerBuilder(ImmutableSequencer<K> base)
    {
        this.originalBase = base;
        /*
         * ImmutableShadowingSequencers stack up on top of each other.  Usually there is one very
         * large sequencer at the very bottom, and then a varying number of incremental sequencers
         * with a small number of elements on top.  This ensures that we only have a small number
         * of incremental sequencers on top, because those are cheap to copy and extend.
         *
         * This algorithm could be improved later if we wish to support having larger extensions in the
         * middle, but that is not a use case today.  So just drop a warning in that case.
         */
        if (base instanceof ImmutableShadowingSequencer) {
            ImmutableShadowingSequencer<K> castBase = (ImmutableShadowingSequencer<K>) base;
            this.base = castBase.getFirst();
            ImmutableSequencer<K> collapsee = castBase.getSecond();

            if (collapsee.size() > 5000 && WARNED.compareAndSet(false, true)) {
                LOG.warn("Relatively large shadow copies are being made.  The shadowing sequencer algorithm was not" +
                         " written with this use case in mind, and performance or GC efficiency may suffer.");
            }

            for (int i = 0; i < collapsee.size(); i++) {
                checkState(i == additional.sequenceOrAdd(collapsee.unsequence(i)), "expected resequence to match");
            }
        } else {
            this.base = originalBase;
        }

        additionalStartingSize = additional.size();
    }

    public ImmutableSequencer<K> build()
    {
        if (additional.size() == additionalStartingSize) {
            return originalBase;
        }

        if (base.size() == 0) {
            return ImmutableSequencer.copyOf(additional);
        }

        return new ImmutableShadowingSequencer<K>(base, ImmutableSequencer.copyOf(additional));
    }

    public ImmutableSequencer<K> buildAndCompact()
    {
        return new ImmutableSequencerImpl<K>(build());
    }

    @Override
    public int sequenceOrAdd(K key)
    {
        int result = base.sequenceIfExists(key);
        if (result != -1) {
            return result;
        }
        return additional.sequenceOrAdd(key) + base.size();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return base.containsKey(key) || additional.containsKey(key);
    }

    @Override
    public int sequenceIfExists(K key)
    {
        return ImmutableShadowingSequencer.sequenceIfExists(base, additional, key);
    }

    @Override
    public void sequenceExisting(Iterable<K> keys, TObjectIntMap<K> result)
    {
        ImmutableShadowingSequencer.sequenceExisting(base, additional, keys, result);
    }

    @Override
    public K unsequence(int index)
    {
        return ImmutableShadowingSequencer.unsequence(base, additional, index);
    }

    @Override
    public int size()
    {
        return base.size() + additional.size();
    }

    private Object writeReplace() throws ObjectStreamException
    {
        throw new NotSerializableException("Shadowing builders may not be serialized");
    }
}

class ImmutableShadowingSequencer<K> extends ImmutableSequencer<K>
{
    private static final long serialVersionUID = 1L;

    private final ImmutableSequencer<K> first;
    private final ImmutableSequencer<K> second;
    private final int size;
    private final int depth;

    ImmutableShadowingSequencer(ImmutableSequencer<K> first, ImmutableSequencer<K> second)
    {
        this.first = first;
        this.second = second;

        size = first.size() + second.size();
        depth = first.depth() + second.depth();
    }

    @Override
    protected int depth()
    {
        return depth;
    }

    ImmutableSequencer<K> getFirst()
    {
        return first;
    }
    ImmutableSequencer<K> getSecond()
    {
        return second;
    }

    @Override
    public boolean containsKey(Object key)
    {
        return first.containsKey(key) || second.containsKey(key);
    }

    @Override
    public int sequenceIfExists(K key)
    {
        return sequenceIfExists(first, second, key);
    }

    @Override
    public void sequenceExisting(Iterable<K> keys, TObjectIntMap<K> result)
    {
        sequenceExisting(first, second, keys, result);
    }

    @Override
    public K unsequence(int index)
    {
        return unsequence(first, second, index);
    }

    @Override
    public int size()
    {
        return size;
    }

    private Object writeReplace() throws ObjectStreamException
    {
        // Do not use ImmutableSequencer.copyOf, since it avoids copying subclasses of ImmutableSequencer, defeating the writeReplace!
        return new ImmutableSequencerImpl<>(this);
    }

    static <K> int sequenceIfExists(Sequencer<K> first, Sequencer<K> second, K key)
    {
        int result = first.sequenceIfExists(key);
        if (result != -1) {
            return result;
        }
        result = second.sequenceIfExists(key);
        if (result != -1) {
            return first.size() + result;
        }
        return result;
    }

    static <K> void sequenceExisting(Sequencer<K> first, Sequencer<K> second, Iterable<K> keys, TObjectIntMap<K> result)
    {
        first.sequenceExisting(keys, result);

        TObjectIntMap<K> tmp = new TObjectIntHashMap<K>(second.size());

        second.sequenceExisting(keys, tmp);

        final int offset = first.size();

        tmp.transformValues(new TIntFunction() {
            @Override
            public int execute(int value)
            {
                return value + offset;
            }
        });

        result.putAll(tmp);
    }

    static <K> K unsequence(Sequencer<K> first, Sequencer<K> second, int index)
    {
        if (index < first.size()) {
            return first.unsequence(index);
        } else {
            return second.unsequence(index - first.size());
        }
    }
}
