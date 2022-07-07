package mineplex.core.punish;

import java.util.List;

import com.google.gson.reflect.TypeToken;

import mineplex.core.common.util.Callback;
import mineplex.core.database.MinecraftRepository;
import mineplex.core.punish.Tokens.PunishClientToken;
import mineplex.core.punish.Tokens.PunishToken;
import mineplex.core.punish.Tokens.RemovePunishToken;
import mineplex.serverdata.database.DBPool;

public class PunishRepository extends MinecraftRepository
{
	public PunishRepository()
	{
		super(DBPool.getAccount());
	}

	public void Punish(Callback<String> callback, String target, String category, PunishmentSentence punishment, String reason, double duration, String admin, int severity)
	{
		PunishToken token = new PunishToken();
		token.Target = target;
		token.Category = category;
		token.Sentence = punishment.toString();
		token.Reason = reason;
		token.Duration = duration;
		token.Admin = admin;
		token.Severity = severity;

		handleMSSQLCall("PlayerAccount/Punish", token, String.class, callback::run);
	}

	public void RemovePunishment(Callback<String> callback, int id, String target, String reason, String admin)
	{
		RemovePunishToken token = new RemovePunishToken();
		token.PunishmentId = id;
		token.Target = target;
		token.Reason = reason;
		token.Admin = admin;

		handleMSSQLCall("PlayerAccount/RemovePunishment", token, String.class, callback::run);
	}

	public void LoadPunishClient(String target, Callback<PunishClientToken> callback)
	{
		handleMSSQLCall("PlayerAccount/GetPunishClient", target, PunishClientToken.class, callback::run);
	}

	public void MatchPlayerName(final Callback<List<String>> callback, final String userName)
	{
		handleMSSQLCall("PlayerAccount/GetMatches", userName, new TypeToken<List<String>>(){}.getType(), callback::run);
	}
}
