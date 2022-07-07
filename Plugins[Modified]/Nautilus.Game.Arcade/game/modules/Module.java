package nautilus.game.arcade.game.modules;

import com.google.gson.JsonElement;
import nautilus.game.arcade.game.Game;
import org.bukkit.event.Listener;

/**
 * This is a Module
 * <p>
 * A Module represents something which will enhance or change the way games can be played.
 * Modules should function independent of which specific gamemode is being played.
 * If you need game-specific features, put it into that Game implementation or refactor it into a separate class (not a module).
 * <p>
 * Modules should never directly access other Modules via the Game instance.
 * Instead, the game which requires cross-contamination should do so itself.
 * <p>
 * Modules should be associated per-game. Do not make them static
 * <p>
 * If your module is able to accept custom configuration, override the configure(JsonElement) method
 * You can define the format of the json you wish to use.
 * This custom configuration will be used to dynamically adjust gamemodes via Redis if needed
 */
public class Module implements Listener
{
	// The game this module belongs to
	private Game _game;

	/**
	 * Initializes this module with the specific game instance. You should never do this as {@link Game} does it for you
	 */
	public final void initialize(Game game)
	{
		if (_game != null)
		{
			throw new IllegalArgumentException("Attempting to initialize module which has already been initialized for " + _game);
		}
		_game = game;
		setup();
	}

	/**
	 * This method is called once initialization is complete. Do whatever you need to do with the {@link Game} here.
	 *
	 * All modules should have been configured before this method is called
	 */
	protected void setup()
	{

	}

	/**
	 * If this module can be configured via a JsonObject/JsonPrimitive, then override this method
	 * to implement that feature
	 * <p>
	 * You can define how the JsonElement should be formatted.
	 * <p>
	 * It is recommended to have a "force" boolean which will reset this module to a clean state
	 * (to allow extensive customization using json)
	 */
	public void configure(JsonElement element)
	{

	}

	/**
	 * This method is called once this module is no longer needed.
	 * This could be because the game is over
	 * Or because this module was unregistered
	 * <p>
	 * The {@link Game} will unregister this module as a listener for you.
	 * All you need to do is clean up after yourself
	 */
	public void cleanup()
	{

	}

	/**
	 * Gets the game this module is associated with
	 */
	public Game getGame()
	{
		return _game;
	}

	public final void register(Game instance)
	{
		instance.registerModule(this);
	}
}
