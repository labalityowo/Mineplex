package mineplex.core.party;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import org.bukkit.entity.Player;

import java.text.MessageFormat;

/**
 * All chat messages for the party system to align with PE
 */
public enum Lang
{

	//Party
	SUCCESS_SERVER_CONNECT("Sending you and your party to {0}..."),
	INVITE_SUCCESS_PLAYER("Successfully invited {0} to the party."),
	SUCCESS_INVITE("{0} has invited {1} to the party."),
	INVITE_ACCEPT("{0} has joined the party."),
	INVITE_DENY("{0} declined your invite."),
	INVITE_EXPIRED("{0} did not respond in time."),
	INVITE_EXPIRED_PLAYER("Your invite to {0}'s party has expired."),

	SHOW_MEMBERS("Party members: {0}."),
	REMOVE_PLAYER("{0} has left the party."),
	REMOVE_PLAYER_KICK("{0} has been removed from the party."),
	ADD_MEMBER("{0} has joined the party."),
	LEFT("You have left your party."),
	PARTY_OWNER_LEAVE("{0} has left the party. {1} is the new party owner."),
	TRANSFER_OWNER("{0} has given party leadership to {1}."),
	DISBANDED("Your party has been disbanded, due to a lack of players."),
	DISBANDED_BY_OWNER("Your party has been disbanded!"),
	REMOVED("You have been removed from the party."),

	NOT_EXIST("Error: {0} is not in a party right now."),
	ALREADY_MEMBER("Error: {0} is already in the party!"),
	NO_PARTY("Error: You are not in a party!"),
	ALREADY_IN("Error: You are already in a party!"),
	NOT_MEMBER("Error: {0} is not a member of your party."),
	NOT_INVITED("Error: You do not have a pending invite to {0}'s party."),
	NOT_OWNER("Error: You must be the party owner to do this!"),
	NOT_OWNER_SERVER("Error: You must be the owner to move servers!"),
	ALREADY_INVITED("Error: {0} has already been invited."),
	PARTY_FULL("Error: Your party is full!"),
	SERVER_CLOSED("Error: Your server is closed and you cannot invite players to join it right now!"),
	SERVER_FULL("Error: Your server is full and you cannot invite more players to join it right now!"),
	PLAYER_IN_DIFFERENT_PARTY("Error: {0} is in a different party."),

	PARTNER_ALREADY_INVITED("Error: You have already invited {0} to be your partner for {1}."),
	PARTNER_NO_GAME("Error: {0} is not a valid game name."),
	PARTNER_NO_PLAYER("Error: You didn't specify a player to partner with."),
	PARTNER_NOT_ONLINE("Error: {0} is not on your server!"),
	PARTNER_PLAYER_NOT_REQUESTED("Error: {0} hasn't made a partner request with you yet!"),
	PARTNER_USAGE("Usage: /teampref accept/deny <player>."),
	PARTNER_HOVER_TEXT_ACCEPT("Click to be {0}''s partner for {1}."),
	PARTNER_HOVER_TEXT_DENY("Click to decline being {0}''s partner for {1}."),
	PARTNER_REQUEST_SENT("Partner request sent to {0} for {1}."),
	PARTNER_REQUEST_RECEIVED("Partner request from {0} for {1}."),
	PARTNER_REQUEST_DENIED_SENDER("{0} has declined your partner request for {1}."),
	PARTNER_REQUEST_DENIED_PLAYER("You have denied {0}''s partner request for {1}."),
	PARTNER_REQUEST_ACCEPT_SENDER("{0} has accepted your partner request for {1}."),
	PARTNER_REQUEST_ACCEPT_PLAYER("You have accepted {0}''s partner request for {1}."),
	;


	private String _message;

	Lang(String message)
	{
		_message = message;
	}

	public String toString(String... args)
	{
		return getFormatted(args);
	}

	public void sendHeader(Player player, String header, String... args)
	{
		player.sendMessage(F.main(header, getFormatted(args)));
	}

	public void send(Player player, String... args)
	{
		player.sendMessage(C.mHead + "Party> " + getFormatted(args));
	}

	public void send(Party party, String... args)
	{
		party.sendMessage(C.mHead + "Party> " + getFormatted(args));
	}

	private String getFormatted(String... args)
	{
		String color = C.mBody;

		if (args == null || args.length == 0)
		{
			return color + _message;
		}

		if (_message.startsWith("Error:"))
		{
			color = C.cRed;
		}

		int firstIndex = _message.indexOf("{");

		String[] coloredArgs = new String[args.length];

		for (int i = 0; i < args.length; i++)
		{
			coloredArgs[i] = C.cYellow + args[i] + color;
		}

		String message = MessageFormat.format(_message, coloredArgs);

		String coloredRest = message.substring(firstIndex);

		message = color + message.substring(0, firstIndex) + coloredRest;
		return message;
	}

}