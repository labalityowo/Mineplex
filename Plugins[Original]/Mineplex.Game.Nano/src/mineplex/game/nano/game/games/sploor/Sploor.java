package mineplex.game.nano.game.games.sploor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.HeadRoll;
import com.mineplex.anticheat.checks.move.Speed;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSquid;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.TeamGame;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.event.GameTimeoutEvent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;
import mineplex.game.nano.game.games.microbattle.components.TeamArmourComponent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Sploor extends TeamGame
{

	private static final Material GUN_MATERIAL = Material.DIAMOND_BARDING;
	private static final String SQUID_MODE_KEY = "Squid Mode";

	private final Map<Projectile, GameTeam> _balls;
	private final Map<GameTeam, Set<Block>> _paintedBlocks;
	private final Set<Player> _squidMode;

	private final ChatColor[] _colours =
			{
					ChatColor.RED,
					ChatColor.GOLD,
					ChatColor.YELLOW,
					ChatColor.GREEN,
					ChatColor.AQUA,
					ChatColor.DARK_AQUA,
					ChatColor.BLUE,
					ChatColor.DARK_PURPLE,
					ChatColor.LIGHT_PURPLE,
					ChatColor.WHITE
			};
	private int _colourIndex;

	public Sploor(NanoManager manager)
	{
		super(manager, GameType.SPLOOR, new String[]
				{
						C.cYellow + "Right-Click" + C.Reset + " your " + C.cYellow + "Paint Gun" + C.Reset + " to shoot!",
						C.cGreen + "Switch Slots" + C.Reset + " to activate " + C.cPurple + "Squid Mode" + C.Reset + "!",
						"In " + C.cYellow + "Squid Mode" + C.Reset + " you can go up " + C.cGreen + "Walls" + C.Reset + "!",
						"The team with the most " + C.cPurple + "Painted" + C.Reset + " blocks wins!"
				});

		_balls = new HashMap<>();
		_paintedBlocks = new HashMap<>();
		_squidMode = new HashSet<>();

		_damageComponent.setFall(false);

		_spectatorComponent.setDeathOut(false);

		_playerComponent.setRegainHealth(false);

		_endComponent.setTimeout(TimeUnit.MINUTES.toMillis(2));

		new TeamArmourComponent(this);

		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(Speed.class);
		antiHack.addIgnoredCheck(Glide.class);
		antiHack.addIgnoredCheck(HeadRoll.class);

		_scoreboardComponent.setSidebar((player, scoreboard) ->
		{
			scoreboard.writeNewLine();

			getTeams().forEach(team ->
			{
				scoreboard.write(team.getChatColour() + C.Bold + team.getName());
				scoreboard.write(_paintedBlocks.get(team).size() + " Blocks");

				scoreboard.writeNewLine();
			});

			scoreboard.draw();
		});
	}

	@Override
	protected void createTeams()
	{
		addTeam(new GameTeam(this, "Red", ChatColor.RED, Color.RED, DyeColor.RED, _mineplexWorld));
		addTeam(new GameTeam(this, "Blue", ChatColor.AQUA, Color.AQUA, DyeColor.LIGHT_BLUE, _mineplexWorld));
	}

	@Override
	public GameTeam addTeam(GameTeam gameTeam)
	{
		_paintedBlocks.put(gameTeam, new HashSet<>());

		return super.addTeam(gameTeam);
	}

	@Override
	protected void parseData()
	{

	}

	@Override
	public void disable()
	{
		_balls.clear();
		_paintedBlocks.clear();
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.SetBroadcastType(DeathMessageType.Simple);
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();

		player.getInventory().setItem(0, new ItemBuilder(GUN_MATERIAL)
				.setTitle(event.getTeam().getChatColour() + C.Bold + "Paint Brush")
				.build());
		player.getInventory().setHeldItemSlot(0);
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R) || !isLive())
		{
			return;
		}

		Player player = event.getPlayer();
		GameTeam team = getTeam(player);
		ItemStack itemStack = player.getItemInHand();

		if (team == null || itemStack == null || itemStack.getType() != GUN_MATERIAL || !Recharge.Instance.use(player, "Shoot Gun", 150, false, false))
		{
			return;
		}

		Snowball snowball = player.launchProjectile(Snowball.class);
		_balls.put(snowball, team);
		player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 0.5F, 1);
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		GameTeam team = _balls.remove(event.getEntity());

		if (team == null)
		{
			return;
		}

		paint(event.getEntity().getLocation(), team, 3);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void damage(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
		{
			event.SetCancelled("No Projectile");
			return;
		}

		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);

		if (damagee == null || damager == null)
		{
			return;
		}

		GameTeam damagerTeam = getTeam(damager);

		if (damagerTeam == null)
		{
			return;
		}

		damager.playSound(damager.getLocation(), Sound.ORB_PICKUP, 0.5F, 1);
		paint(damagee, damagerTeam);

		event.AddMod(damager.getName(), "Painting", -event.GetDamage() + 5, true);
		event.SetIgnoreArmor(true);
	}

	private void paint(Location location, GameTeam painterTeam, int radius)
	{
		Set<Block> painted = _paintedBlocks.get(painterTeam);

		for (Block block : UtilBlock.getBlocksInRadius(location, radius))
		{
			if (block.getType() != Material.STAINED_GLASS && block.getType() != Material.WOOL)
			{
				continue;
			}

			for (GameTeam other : getTeams())
			{
				if (other.equals(painterTeam))
				{
					continue;
				}

				if (block.getData() == other.getWoolData())
				{
					_paintedBlocks.get(other).remove(block);
				}
			}

			painted.add(block);
			MapUtil.QuickChangeBlockAt(block.getLocation(), block.getType(), painterTeam.getWoolData());
		}
	}

	private void paint(Player painted, GameTeam painterTeam)
	{
		PlayerInventory inventory = painted.getInventory();
		double health = painted.getHealth();

		if (health > 15)
		{
			inventory.setBoots(new ItemBuilder(Material.LEATHER_BOOTS)
					.setColor(Color.FUCHSIA)
					.build());
		}
		else if (health > 10)
		{
			inventory.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setColor(Color.FUCHSIA)
					.build());
		}
		else if (health > 5)
		{
			inventory.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setColor(Color.FUCHSIA)
					.build());
		}
		else
		{
			inventory.setHelmet(new ItemBuilder(Material.LEATHER_HELMET)
					.setColor(Color.FUCHSIA)
					.build());

			paint(painted.getLocation(), painterTeam, 5);
		}
	}

	@EventHandler
	public void updateSquids(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !isLive())
		{
			return;
		}

		for (GameTeam team : getTeams())
		{
			for (Player player : team.getAlivePlayers())
			{
				ItemStack itemStack = player.getItemInHand();
				boolean wantsTo = itemStack == null || itemStack.getType() != GUN_MATERIAL;
				boolean onColour = isOnColour(player, team);
				boolean onGround = UtilEnt.isGrounded(player);
				boolean squid = _squidMode.contains(player);
				boolean canUse = Recharge.Instance.usable(player, SQUID_MODE_KEY);

				if (squid)
				{
					if (!wantsTo || (onGround && !onColour))
					{
						setSquid(player, false);
					}
					else
					{
						Location location = player.getLocation();
						Block block = location.getBlock();

						for (BlockFace face : UtilBlock.horizontals)
						{
							Block next = block.getRelative(face);

							if (next.getData() != team.getWoolData() || UtilMath.offset2dSquared(next.getLocation().add(0.5, 0, 0.5), location) > 1)
							{
								continue;
							}

							UtilAction.velocity(player, new Vector(0, 0.5, 0));
							break;
						}
					}
				}
				else
				{
					if (wantsTo && onColour && canUse)
					{
						setSquid(player, true);
					}
				}
			}
		}
	}

	@EventHandler
	public void updateSquidText(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !isLive())
		{
			return;
		}

		String squidText = getSquidText();

		for (GameTeam team : getTeams())
		{
			for (Player player : team.getAlivePlayers())
			{
				if (_squidMode.contains(player))
				{
					UtilTextBottom.display(team.getChatColour() + C.Bold + "YOU'VE GOT " + squidText, player);
				}
			}
		}

		_colourIndex = (_colourIndex + 1) % _colours.length;
	}

	private String getSquidText()
	{
		String text = "SUPER SQUID SPEED";
		StringBuilder builder = new StringBuilder();
		int colourIndex = _colourIndex;

		for (char ch : text.toCharArray())
		{
			builder
					.append(_colours[colourIndex])
					.append(C.Bold)
					.append(ch);

			colourIndex = (colourIndex + 1) % _colours.length;
		}

		return builder.toString();
	}

	@EventHandler
	public void playerQut(PlayerStateChangeEvent event)
	{
		if (!event.isAlive())
		{
			setSquid(event.getPlayer(), false);
		}
	}

	private boolean isOnColour(Player player, GameTeam team)
	{
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

		for (BlockFace next : UtilBlock.horizontals)
		{
			if (block.getRelative(next).getData() == team.getWoolData())
			{
				return true;
			}
		}

		return block.getData() == team.getWoolData();
	}

	private void setSquid(Player player, boolean enabled)
	{
		GameTeam team = getTeam(player);

		if (team == null)
		{
			return;
		}

		if (enabled && _squidMode.add(player))
		{
			DisguiseSquid disguise = new DisguiseSquid(player);

			disguise.setName(team.getChatColour() + player.getName());
			disguise.setCustomNameVisible(true);

			getManager().getDisguiseManager().disguise(disguise);

			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
		}
		else if (!enabled && _squidMode.remove(player))
		{
			DisguiseBase disguise = getManager().getDisguiseManager().getActiveDisguise(player);

			if (disguise != null)
			{
				getManager().getDisguiseManager().undisguise(disguise);
			}

			player.removePotionEffect(PotionEffectType.SPEED);
			UtilTextBottom.display("", player);
		}

		player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 0.5F, 1);
	}

	@EventHandler
	public void timeout(GameTimeoutEvent event)
	{
		GameTeam winner = null;
		int winnerBlocks = 0;

		for (Entry<GameTeam, Set<Block>> entry : _paintedBlocks.entrySet())
		{
			int blocks = entry.getValue().size();

			if (blocks > winnerBlocks)
			{
				winner = entry.getKey();
				winnerBlocks = blocks;
			}
		}

		setWinningTeam(winner);
	}
}
