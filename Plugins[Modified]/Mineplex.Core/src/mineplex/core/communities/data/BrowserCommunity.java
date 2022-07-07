package mineplex.core.communities.data;

import com.google.common.base.Preconditions;

import mineplex.core.game.GameDisplay;

public class BrowserCommunity implements ICommunity
{
	private int _id;
	private int _members;
	private String _name;
	private String _description;
	private GameDisplay _favoriteGame;
	private Community.PrivacySetting _privacySetting;

	public BrowserCommunity(int id, String name)
	{
		_id = id;
		_name = name;
		_description = "No Description Set";
		_favoriteGame = GameDisplay.ChampionsCTF;
		_privacySetting = Community.PrivacySetting.RECRUITING;
	}

	@Override
	public Integer getId()
	{
		return _id;
	}

	@Override
	public Integer getMemberCount()
	{
		return _members;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public String getDescription()
	{
		return _description;
	}

	@Override
	public GameDisplay getFavoriteGame()
	{
		return _favoriteGame;
	}

	@Override
	public Community.PrivacySetting getPrivacySetting()
	{
		return _privacySetting;
	}

	public BrowserCommunity setMembers(int members)
	{
		_members = members;
		return this;
	}

	public void addMember()
	{
		_members++;
	}

	public BrowserCommunity setDescription(String description)
	{
		_description = Preconditions.checkNotNull(description);
		return this;
	}

	public BrowserCommunity setFavoriteGame(GameDisplay favoriteGame)
	{
		_favoriteGame = Preconditions.checkNotNull(favoriteGame);
		return this;
	}

	public BrowserCommunity setPrivacySetting(Community.PrivacySetting privacySetting)
	{
		_privacySetting = Preconditions.checkNotNull(privacySetting);
		return this;
	}

	@Override
	public int hashCode()
	{
		return _id;
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || (o instanceof BrowserCommunity && ((BrowserCommunity) o)._id == _id);
	}

	@Override
	public String toString()
	{
		return "BrowserCommunity{" +
		       "_id=" + _id +
		       ", _members=" + _members +
		       ", _name='" + _name + '\'' +
		       ", _description='" + _description + '\'' +
		       ", _favoriteGame=" + _favoriteGame +
		       ", _privacySetting=" + _privacySetting +
		       '}';
	}
}
