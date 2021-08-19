package mineplex.core.communities.data;

import mineplex.core.game.GameDisplay;

public interface ICommunity
{
	Integer getId();

	Integer getMemberCount();

	String getName();

	String getDescription();

	GameDisplay getFavoriteGame();

	Community.PrivacySetting getPrivacySetting();

	default boolean isBrowserEligible()
	{
		return getPrivacySetting() != Community.PrivacySetting.PRIVATE && getMemberCount() >= 5;
	}
}
