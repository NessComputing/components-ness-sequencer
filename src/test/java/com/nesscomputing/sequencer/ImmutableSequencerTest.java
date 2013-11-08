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
