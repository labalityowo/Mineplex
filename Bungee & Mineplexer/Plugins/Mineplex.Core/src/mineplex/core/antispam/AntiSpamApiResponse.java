package mineplex.core.antispam;

import mineplex.core.common.api.ApiResponse;

/**
 * @author Shaun Bennett
 */
public class AntiSpamApiResponse extends ApiResponse
{
	private boolean isShadowMuted;

	public AntiSpamApiResponse()
	{

	}

	public boolean isShadowMuted()
	{
		return isShadowMuted;
	}
}
