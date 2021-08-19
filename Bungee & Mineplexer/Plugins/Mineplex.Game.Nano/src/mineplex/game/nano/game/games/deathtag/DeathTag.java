package mineplex.game.nano.game.games.deathtag;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.disguise.disguises.DisguiseZombie;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GamePlacements;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.game.nano.game.components.player.GiveItemComponent;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.PlayerGameApplyEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class DeathTag extends SoloGame
{

	private final GiveItemComponent _itemComponent;

	private GameTeam _runners, _chasers;

	public DeathTag(NanoManager manager)
	{
		super(manager, GameType.DEATH_TAG, new String[]
				{
						C.cGreen + "Run" + C.Reset + " from the " + C.cRed + "Undead!",
						"If you die, you become " + C.cRed + "Undead!",
						C.cYellow + "Last Human" + C.Reset + " alive wins!"
				});

		_spectatorComponent.setDeathOut(false);

		_teamComponent.setSelector(player -> _runners);

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent.setTeamSelf(false);

		_playerComponent.setRegainHealth(false);

		_compassComponent.setGiveToAlive(true);

		_itemComponent = new GiveItemComponent(this)
				.setItems(new ItemStack[]
						{
								new ItemBuilder(Material.IRON_AXE)
										.setUnbreakable(true)
										.build()
						});

		_scoreboardComponent.setSidebar((player, scoreboard) ->
		{
			scoreboard.writeNewLine();

			for (GameTeam team : getTeams())
			{
				scoreboard.write(team.getChatColour() + C.Bold + team.getName());
				scoreboard.write(team.getAlivePlayers().size() + " Alive");

				scoreboard.writeNewLine();
			}

			scoreboard.draw();
		});
	}

	@Override
	protected void createTeams()
	{
		_runners = addTeam(new GameTeam(this, "Humans", ChatColor.GREEN, Color.LIME, DyeColor.GREEN, getPlayerTeamSpawns()));
		_chasers = addTeam(new GameTeam(this, "Undead", ChatColor.RED, Color.RED, DyeColor.RED, getPlayerTeamSpawns()));
	}

	@Override
	protected void parseData()
	{

	}

	@Override
	public boolean endGame()
	{
		return super.endGame() || _runners.getAlivePlayers().size() <= 1;
	}

	@Override
	public void disable()
	{

	}

	@Override
	protected GamePlacements createPlacements()
	{
		return GamePlacements.fromTeamPlacements(_runners.getPlaces(true));
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Live)
		{
			return;
		}

		_itemComponent.setItems(new ItemStack[]
				{
						new ItemBuilder(Material.IRON_SWORD)
								.setUnbreakable(true)
								.build()
				});
	}

	@EventHandler
	public void playerApply(PlayerGameApplyEvent event)
	{
		Player player = event.getPlayer();

		if (isLive())
		{
			if (!event.getTeam().equals(_chasers))
			{
				setChaser(player, false);
				event.setRespawnLocation(player.getLocation());
			}

			event.setClearPlayer(false);
		}
		else
		{
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
		}
	}

	@EventHandler
	public void updateChasers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !isLive())
		{
			return;
		}

		int req = 1 + (int) ((System.currentTimeMillis() - getStateTime()) / 45000);

		while (_chasers.getAlivePlayers().size() < req)
		{
			Player player = UtilAlg.Random(_runners.getAlivePlayers());

			if (player == null)
			{
				return;
			}

			setChaser(player, true);
		}

		if (_mineplexWorld.getWorld().getEntitiesByClass(Zombie.class).size() < 100)
		{
			Location location = UtilAlg.Random(getPlayerTeamSpawns());

			if (location != null)
			{
				_worldComponent.setCreatureAllowOverride(true);

				Zombie zombie = location.getWorld().spawn(location, Zombie.class);
				zombie.setTarget(UtilAlg.Random(_runners.getAlivePlayers()));
				zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));

				_worldComponent.setCreatureAllowOverride(false);
			}
		}
	}

	private void setChaser(Player player, boolean forced)
	{
		_runners.setPlayerAlive(player, false);
		_teamComponent.joinTeam(player, _chasers);

		DisguiseZombie disguise = new DisguiseZombie(player);

		disguise.setName(_chasers.getChatColour() + player.getName());
		disguise.setCustomNameVisible(true);

		UtilPlayer.clearInventory(player);

		if (forced)
		{
			announce(F.main(getManager().getName(), F.elem(_runners.getChatColour() + player.getName()) + " has become an " + F.elem(_chasers.getChatColour() + "Alpha Zombie") + "."));
			player.getWorld().strikeLightningEffect(player.getLocation());
			_itemComponent.giveItems(player);
			player.getInventory().setArmorContents(new ItemStack[]
					{
							null,
							null,
							null,
							new ItemBuilder(Material.SKULL_ITEM, (byte) 1)
									.build()
					});
		}

		UtilTextMiddle.display(C.cRedB + "Zombie", "You are now a zombie!", 0, 60, 10, player);
		_manager.getDisguiseManager().disguise(disguise);

		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 4, false, false));
		player.removePotionEffect(PotionEffectType.SPEED);

		for (LivingEntity entity : player.getWorld().getLivingEntities())
		{
			if (entity instanceof Creature)
			{
				Creature creature = (Creature) entity;

				if (player.equals(creature.getTarget()))
				{
					creature.setTarget(null);
				}
			}
		}
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		event.setDroppedExp(0);
		event.getDrops().clear();
	}

	@EventHandler
	public void entityTarget(EntityTargetEvent event)
	{
		if (event.getEntity() instanceof Zombie && event.getTarget() instanceof Player && _chasers.hasPlayer((Player) event.getTarget()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void entityCombust(EntityCombustEvent event)
	{
		if (event.getEntity() instanceof Zombie)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void chaserDamage(CustomDamageEvent event)
	{
		if (_chasers.hasPlayer(event.GetDamagerPlayer(false)))
		{
			if (event.GetDamageeEntity() instanceof Zombie)
			{
				event.SetCancelled("Zombie vs Zombie");
			}
			else
			{
				event.AddMod("Chaser", 2);
			}
		}
	}
}
