package com.adaptris.core.services;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;

import javax.validation.constraints.NotBlank;

public class SequenceNumber
{
	@Getter
	@NotBlank
	@AutoPopulated
	@InputFieldDefault("0")
	private String numberFormat;

	@Getter
	@Setter
	@AdvancedConfig
	@InputFieldDefault(value = "true")
	private Boolean alwaysReplaceMetadata;

	@Getter
	@NotBlank
	@AffectsMetadata
	private String metadataKey;

	@Getter
	@Setter
	private OverflowBehaviour overflowBehaviour;

	@Getter
	@Setter
	@AdvancedConfig
	private Long maximumSequenceNumber;

	public SequenceNumber()
	{
		setNumberFormat("0");
	}


	/**
	 * Metadata will be formatted using the pattern specified.
	 *
	 * <p>
	 * This allows you to format the number precisely to the value that is required; e.g if you use "000000000" then the metadata
	 * value is always 9 characters long, the number being prefixed by leading zeros
	 * </p>
	 *
	 * @see java.text.DecimalFormat
	 * @param format the numberFormat to set. The default is '0'; which coupled with the default overflow behaviour of 'Continue'
	 *          means it will just use the raw number.
	 */
	public void setNumberFormat(String format) {
		numberFormat = Args.notBlank(format, "numberFormat");
	}

	/**
	 * Set the metadata key where the resulting sequence number will be stored.
	 *
	 * @param key the metadataKey to set
	 */
	public void setMetadataKey(String key) {
		metadataKey = Args.notBlank(key, "metadataKey");
	}

	public boolean alwaysReplaceMetadata()
	{
		return BooleanUtils.toBooleanDefaultIfNull(getAlwaysReplaceMetadata(), true);
	}

	public static OverflowBehaviour getBehaviour(OverflowBehaviour s)
	{
		return s != null ? s : OverflowBehaviour.Continue;
	}

	/**
	 * The behaviour of the sequence number generator when the number exceeds that specified by the number format.
	 *
	 *
	 */
	public enum OverflowBehaviour
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
	}
}
