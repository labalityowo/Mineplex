package mineplex.hub.modules.mavericks.basketball;

import java.util.ArrayList;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.event.StackerEvent;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.gadget.gadgets.outfit.OutfitTeam;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import mineplex.hub.modules.mavericks.MavericksWorldManager;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

/**
 * Manager class for creating and maintaining basketball games in the hub
 */
public class BasketballManager extends MiniPlugin
{
	private HubManager _hubManager;
	private MavericksWorldManager _world;
	private ArrayList<OutfitTeam> _teamArmor = Lists.newArrayList();
	private BasketballGame _game;
	
	public BasketballManager(JavaPlugin plugin, MavericksWorldManager world, HubManager hub)
	{
		super("Basketball Manager", plugin);
		_world = world;
		_hubManager = hub;
		
		for (Gadget outfit : hub.GetGadget().getGadgets(GadgetType.COSTUME))
		{
			if (outfit instanceof OutfitTeam)
			{
				_teamArmor.add((OutfitTeam)outfit);
			}
		}
	}
	
	@Override
	public void disable()
	{
		if (_game != null)
		{
			_game.end();
			_game = null;
		}
	}
	
	private void setBasketballMode(Player player, boolean enabled, Color color)
	{
		if (_game == null)
		{
			return;
		}
		
		if (enabled)
		{
			_game.getPlayers().add(player);

			if (color == null)
				UtilPlayer.message(player, F.main("Basketball", "You have entered " + F.elem("Basketball Mode") + "."));
			else if (color == Color.RED)
				UtilPlayer.message(player, F.main("Basketball", "You have joined " + F.elem(C.cRed + "Red Basketball Team") + "."));
			else if (color == Color.AQUA)
				UtilPlayer.message(player, F.main("Basketball", "You have joined " + F.elem(C.cAqua + "Blue Basketball Team") + "."));

			ArrayList<String> outfit = new ArrayList<String>();
			outfit.add("Team Helmet");
			outfit.add("Team Shirt");
			outfit.add("Team Pants");
			outfit.add("Team Boots");

			_hubManager.GetGadget().disableAll(player, outfit);
			_hubManager.getPetManager().disableAll(player);
		}
		else
		{
			_game.getPlayers().remove(player);
			UtilPlayer.message(player, F.main("Parkour", "You have exited " + F.elem("Basketball Mode") + "."));
		}
	}
	
	/**
	 * Fetches the color of a player's team based on their outfit
	 * @param player The player to check
	 * @return The color of the player's team, or null if they are not wearing a team outfit
	 */
	public Color getTeamColor(Player player)
	{
		//All pieces are always same color!
		for (OutfitTeam outfit : _teamArmor)
		{
			if (outfit.isActive(player))
				return outfit.getTeamColor(player);
		}

		return null;
	}
	
	/**
	 * Checks whether an entity is in the bounds considered the basketball court
	 * @param entity The entity to check
	 * @return Whether the entity is in the bounds considered the basketball court
	 */
	public boolean inPlayerArena(Entity entity)
	{
		if(!entity.getWorld().equals(_world.getWorld()))
		{
			return false;
		}
		
		Location loc = entity.getLocation();
		
		return (loc.getX() > DataLoc.CORNER_MIN.getLocation(_world.getWorld()).getX() && loc.getX() < DataLoc.CORNER_MAX.getLocation(_world.getWorld()).getX()) && (loc.getZ() > DataLoc.CORNER_MIN.getLocation(_world.getWorld()).getZ() && loc.getZ() < DataLoc.CORNER_MAX.getLocation(_world.getWorld()).getZ());
	}
	
	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		for (Entity ent : _world.getWorld().getEntities())
		{
			if (ent instanceof Player)
				continue;
			
			if (_game != null && _game.getBall() != null && _game.getBall().equals(ent))
				continue;

			if (inPlayerArena(ent))
			{
				if (ent instanceof Bat || ent instanceof WitherSkull || ent instanceof TNTPrimed || ent instanceof Firework)
				{
					ent.remove();
					continue;
				}
				
				Location bounce = UtilAlg.getMidpoint(DataLoc.CORNER_MAX.getLocation(_world.getWorld()), DataLoc.CORNER_MIN.getLocation(_world.getWorld()));
				bounce.setY(Math.min(DataLoc.CORNER_MAX.getLocation(_world.getWorld()).getY(), DataLoc.CORNER_MIN.getLocation(_world.getWorld()).getY()));

				Entity bottom = ent;
				if (bottom.getVehicle() != null)
					bottom = bottom.getVehicle();

				UtilAction.velocity(bottom, UtilAlg.getTrajectory(bounce, ent.getLocation()), 1, false, 0, 0.4, 1, true);
			}
		}
		
		if (_game != null && _game.getGameAge() > 5000 && _game.getPlayers().size() < 1)
		{
			_game.end();
			_game = null;
		}
	}

	@EventHandler
	public void joinLeaveGame(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (_game != null && _game.getPlayers().contains(player))
			{
				if (!inPlayerArena(player))
				{
					setBasketballMode(player, false, null);
					continue;
				}

				//Took armor off
				Color color = getTeamColor(player);
				if (color == null || (color != Color.RED && color != Color.AQUA))
				{
					setBasketballMode(player, false, null);
				}
			}
			else
			{
				if (inPlayerArena(player))
				{					
					Color color = getTeamColor(player);
					
					if (color != null && (color == BasketballTeam.RED.getColor() || color == BasketballTeam.BLUE.getColor()) && _game == null)
					{
						_game = new BasketballGame(getPlugin(), _hubManager, _world.getWorld());
					}
					
					//Join
					if (color != null && (color == Color.RED || color == Color.AQUA) && _game != null && _game.getPlayers().size() < 10)
					{
						setBasketballMode(player, true, color);
					}
					//Eject
					else
					{
						Location bounce = UtilAlg.getMidpoint(DataLoc.CORNER_MAX.getLocation(_world.getWorld()), DataLoc.CORNER_MIN.getLocation(_world.getWorld()));
						bounce.setY(Math.min(DataLoc.CORNER_MAX.getLocation(_world.getWorld()).getY(), DataLoc.CORNER_MIN.getLocation(_world.getWorld()).getY()));

						Entity bottom = player;
						if (bottom.getVehicle() != null)
							bottom = bottom.getVehicle();

						UtilAction.velocity(bottom, UtilAlg.getTrajectory2d(bounce, player.getLocation()), 1, false, 0, 0.8, 1, true);

						if (Recharge.Instance.use(player, "Basketball Eject", 5000, false, false))
						{
							if (_game == null || (_game != null && _game.getPlayers().size() < 10))
							{
								UtilPlayer.message(player, F.main("Slimeball", "You must be wearing Red/Blue Team Outfit."));
								UtilPlayer.message(player, F.main("Slimeball", "Type " + F.elem("/team red") + " or " + F.elem("/team blue") + "!"));
							}
							else
							{
								UtilPlayer.message(player, F.main("Slimeball", "The game is currently full!"));
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void disableGadgets(GadgetEnableEvent event)
	{
		if (_game != null && _game.getPlayers().contains(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerGrabSlime(PlayerInteractEntityEvent event)
	{
		if (_game != null && _game.getPlayers().contains(event.getPlayer()))
		{
			event.setCancelled(true);
		}
		
		if (_game != null && _game.getBall() != null && event.getRightClicked().equals(_game.getBall()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void disableStacker(StackerEvent event)
	{
		if (_game != null && _game.getPlayers().contains(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void disableGuardianLazer(GadgetSelectLocationEvent event)
	{
		if (_game != null && !_game.isOutOfBounds(event.getLocation(), false))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void disableVelocity(PlayerVelocityEvent event)
	{
		// Disable velocity but allow double jumping.

		if (_game != null && _game.getPlayers().contains(event.getPlayer()) && !_hubManager.getJumpManager().hasDoubleJumped(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		if (_game != null)
		{
			_game.getPlayers().remove(event.getPlayer());
		}
	}
}