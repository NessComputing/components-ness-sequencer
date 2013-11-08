package com.nesscomputing.sequencer;

/**
 * Indicates that a particular key was not available in the sequencer.
 */
public class SequencerKeyException extends Exception
{
    private static final long serialVersionUID = 1L;

    public SequencerKeyException(String message) {
        super(message);
    }

    public SequencerKeyException(String format, Object... args) {
        super(String.format(format, args));
    }
}
