package com.nesscomputing.sequencer;

import java.util.AbstractList;

class SequencerKeyList<K> extends AbstractList<K>
{

    private final Sequencer<K> sequencer;

    public SequencerKeyList(Sequencer<K> sequencer)
    {
        this.sequencer = sequencer;
    }

    @Override
    public K get(int index)
    {
        return sequencer.unsequence(index);
    }

    @Override
    public int size()
    {
        return sequencer.size();
    }
}
