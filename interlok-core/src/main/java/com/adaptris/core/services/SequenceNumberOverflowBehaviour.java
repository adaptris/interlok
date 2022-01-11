package com.adaptris.core.services;

/**
 * The behaviour of the sequence number generator when the number exceeds that specified by the number format.
 *
 *
 */
public enum SequenceNumberOverflowBehaviour
{
	ResetToOne()
	{
		@Override
		public long wrap(long i)
		{
			return 1;
		}
	},
	Continue()
	{
		@Override
		public long wrap(long i)
		{
			return i;
		}
	};

	public abstract long wrap(long i);

	public static SequenceNumberOverflowBehaviour getBehaviour(SequenceNumberOverflowBehaviour s)
	{
		return s != null ? s : SequenceNumberOverflowBehaviour.Continue;
	}
}
