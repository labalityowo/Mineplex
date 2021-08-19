package mineplex.core.portal;

/**
 * The intention for transferring a player. Different intents will cause different behaviours to occur
 */
public enum Intent
{
	/**
	 * Requested by a player (i.e. /server)
	 */
	PLAYER_REQUEST,
	/**
	 * Forcibly kicked by the server (i.e. MPS kick)
	 */
	KICK,
	/**
	 * A transfer was initiated from a remote server (i.e. /send)
	 */
	FORCE_TRANSFER
}