package nautilus.game.arcade.game.modules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.events.EntityVelocityChangeEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Kit;

public class TrainingGameModule extends Module
{

	private static final String RETURN_TO_SPAWN_RECHARGE = "Return To Select A Kit";
	private static final int RETURN_TO_SPAWN_COOLDOWN = 8000;
	private static final ItemStack RETURN_TO_SPAWN_ITEM = new ItemBuilder(Material.BED)
			.setTitle(C.cGreenB + RETURN_TO_SPAWN_RECHARGE)
			.addLore("Click to return to the spawn island", "where you can select a new kit.")
			.build();

	private Function<Player, GameTeam> _teamFunction;
	private Predicate<Player> _skillFunction;
	private Predicate<Player> _damageFunction;
	private Predicate<Player> _kitSelectFunction;
	private boolean _giveReturnToSpawn = true;

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != Game.GameState.Live)
		{
			return;
		}

		List<Location> locations = getGame().WorldData.GetDataLocs("PURPLE");
		int i = 0;

		if (locations.isEmpty())
		{
			return;
		}

		for (Kit kit : getGame().GetKits())
		{
			Location location = locations.get(i++);
			kit.getGameKit().createNPC(location);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerLogin(PlayerLoginEvent event)
	{
		Player player = event.getPlayer();
		Game game = getGame();
		GameTeam team = _teamFunction == null ? game.GetTeamList().get(0) : _teamFunction.apply(player);

		team.AddPlayer(player, true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerJoin(PlayerJoinEvent event)
	{
		if (!getGame().InProgress())
		{
			return;
		}

		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player))
		{
			return;
		}

		GameTeam team = getGame().GetTeam(player);

		team.SpawnTeleport(player);
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (_skillFunction == null)
		{
			return;
		}

		Player player = event.getPlayer();

		if (!_skillFunction.test(player))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerInteract(EntityDamageEvent event)
	{
		if (_skillFunction == null || !(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (!_skillFunction.test(player))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDamage(CustomDamageEvent event)
	{
		if (!(event.GetDamageeEntity() instanceof Player))
		{
			return;
		}

		Player player = event.GetDamageePlayer();

		if (!event.isCancelled())
		{
			Recharge.Instance.useForce(player, RETURN_TO_SPAWN_RECHARGE, RETURN_TO_SPAWN_COOLDOWN);

			if (event.GetDamagerEntity(true) instanceof Player)
			{
				Recharge.Instance.useForce(event.GetDamagerPlayer(true), RETURN_TO_SPAWN_RECHARGE, RETURN_TO_SPAWN_COOLDOWN);
			}
		}

		if (_damageFunction != null && !_damageFunction.test(player))
		{
			event.SetCancelled("Training Area");
		}
	}

	@EventHandler
	public void entityVelocity(EntityVelocityChangeEvent event)
	{
		Entity entity = event.getEntity();

		if (!(entity instanceof Player))
		{
			return;
		}

		Player player = (Player) entity;

		if (!_damageFunction.test(player))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void giveReturnToSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_giveReturnToSpawn)
		{
			return;
		}

		for (Player player : getGame().GetPlayers(true))
		{
			if (player.getInventory().contains(RETURN_TO_SPAWN_ITEM))
			{
				continue;
			}

			player.getInventory().setItem(8, RETURN_TO_SPAWN_ITEM);
		}
	}

	@EventHandler
	public void interactReturnToSpawn(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || !itemStack.isSimilar(RETURN_TO_SPAWN_ITEM))
		{
			return;
		}

		if (event.isCancelled())
		{
			player.sendMessage(F.main("Game", "You are already at the " + F.greenElem("Kit Selection Island") + "."));
			return;
		}

		if (Recharge.Instance.usable(player, RETURN_TO_SPAWN_RECHARGE))
		{
			getGame().RespawnPlayer(player);
		}
		else
		{
			player.sendMessage(F.main("Game", "You can't return to the " + F.greenElem("Kit Selection Island") + " if you are in " + F.greenElem("PVP") + "."));
		}
	}

	public void preventReturnToSpawn(Player player)
	{
		Recharge.Instance.useForce(player, RETURN_TO_SPAWN_RECHARGE, RETURN_TO_SPAWN_COOLDOWN);
	}

	public TrainingGameModule setTeamFunction(Function<Player, GameTeam> function)
	{
		_teamFunction = function;
		return this;
	}

	public TrainingGameModule setSkillFunction(Predicate<Player> function)
	{
		_skillFunction = function;
		return this;
	}

	public TrainingGameModule setDamageFunction(Predicate<Player> function)
	{
		_damageFunction = function;
		return this;
	}

	public TrainingGameModule setKitSelectFunction(Predicate<Player> function)
	{
		_kitSelectFunction = function;
		return this;
	}

	public TrainingGameModule setGiveReturnToSpawn(boolean b)
	{
		_giveReturnToSpawn = b;
		return this;
	}
}
