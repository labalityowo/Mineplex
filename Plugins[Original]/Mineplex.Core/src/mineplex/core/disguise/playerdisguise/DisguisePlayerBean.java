package mineplex.core.disguise.playerdisguise;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;

import mineplex.core.common.Constants;
import mineplex.core.status.ServerStatusManager;
import mineplex.serverdata.data.Data;

import java.lang.reflect.Type;
import java.util.UUID;

public class DisguisePlayerBean implements Data
{
	private int _accountID;
	private String _playerName;

	private String _disguisedPlayer;
	private String _disguisedSkin;

	private String _serializedGameProfile;

	public DisguisePlayerBean(int playerAccountID, String playerName, String disguiseAs, String disguiseSkin, GameProfile targetProfile)
	{
		this._accountID = playerAccountID;
		this._playerName = playerName;

		this._disguisedPlayer = disguiseAs;
		this._disguisedSkin = disguiseSkin;

		this._serializedGameProfile = serialize(targetProfile);
	}

	public int getAccountID()
	{
		return _accountID;
	}

	public String getDisguisedPlayer()
	{
		return _disguisedPlayer;
	}

	public String getDisguisedSkin()
	{
		return this._disguisedSkin;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	@Override
	public String getDataId()
	{
		return _accountID + _playerName;
	}

	public GameProfile getGameProfile()
	{
		return deserialize(this._serializedGameProfile);
	}

	private String serialize(GameProfile gameProfile)
	{
		return Constants.GSON.toJson(gameProfile);
	}

	private GameProfile deserialize(String in)
	{
		return Constants.GSON.fromJson(in, GameProfile.class);
	}
}
