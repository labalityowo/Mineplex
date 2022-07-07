package nautilus.game.arcade.game.games.moba.progression;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.donation.Donor;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeFormat;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GemData;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaPlayer;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.prepare.PrepareSelection;
import nautilus.game.arcade.game.games.moba.progression.ui.MobaRoleShop;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;

public class MobaProgression implements Listener
{

	private static final int EXP_PER_LEVEL = 1000;
	private static final int EXP_FACTOR = 3;
	public static final DecimalFormat FORMAT = new DecimalFormat("0.0");

	public static int getExpFor(int level)
	{
		return EXP_PER_LEVEL * level;
	}

	public static int getLevel(long exp)
	{
		return (int) Math.floor(exp / EXP_PER_LEVEL);
	}

	private final Moba _host;
	private final Map<ArmorStand, MobaRole> _roleViewers;
	private final MobaRoleShop _roleShop;

	private MobaUnlockAnimation _currentAnimation;

	public MobaProgression(Moba host)
	{
		_host = host;
		_roleViewers = new HashMap<>();
		_roleShop = new MobaRoleShop(host.getArcadeManager());

		host.registerDebugCommand("fakeexp", Perm.DEBUG_FAKEEXP_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			int exp = Integer.parseInt(args[0]);
			_host.GetGems(caller).put("Fake Exp", new GemData(exp, false));
			caller.sendMessage(F.main("Debug", "Gave you " + F.elem(exp) + " fake exp."));
		});
/*		host.registerDebugCommand("setmobalevel", Perm.DEBUG_SETMOBALEVEL_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			MobaRole role = MobaRole.valueOf(args[0].toUpperCase());
			int exp = getExpFor(Integer.parseInt(args[1]) - 1);
			_host.getArcadeManager().GetStatsManager().setStat(caller, _host.GetName() + "." + role.getName() + ".ExpEarned", exp);
			caller.sendMessage(F.main("Debug", "Set your " + role.getChatColor() + role.getName() + C.cGray + " level to " + F.elem(getLevel(exp) + 1) + "."));
		});*/
		host.registerDebugCommand("unlockhero", Perm.DEBUG_UNLOCK_HERO_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			Donor donor = _host.getArcadeManager().GetDonation().Get(caller);
			String input = args[0];
			boolean all = input.equalsIgnoreCase("ALL");

			for (HeroKit kit : _host.getKits())
			{
				if (all || kit.GetName().equalsIgnoreCase(input))
				{
					caller.sendMessage(F.main("Debug", "Unlocked " + F.name(kit.GetName()) + "."));
					donor.addOwnedUnknownSalesPackage(getPackageName(kit));
				}
			}
		});
		host.registerDebugCommand("lockhero", Perm.DEBUG_LOCK_HERO_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			Donor donor = _host.getArcadeManager().GetDonation().Get(caller);
			String input = args[0];
			boolean all = input.equalsIgnoreCase("ALL");

			for (HeroKit kit : _host.getKits())
			{
				if (all || kit.GetName().equalsIgnoreCase(input))
				{
					caller.sendMessage(F.main("Debug", "Locked " + F.name(kit.GetName()) + "."));
					donor.removeOwnedUnknownSalesPackage(getPackageName(kit));
				}
			}
		});
	}

	public enum Perm implements Permission
	{
		DEBUG_FAKEEXP_COMMAND,
		DEBUG_SETMOBALEVEL_COMMAND,
		DEBUG_UNLOCK_HERO_COMMAND,
		DEBUG_LOCK_HERO_COMMAND
	}

	public void spawnRoleViewers(Map<String, List<Location>> lobbyLocations)
	{
		Location center = lobbyLocations.get("SPAWN").get(0);

		for (MobaRole role : MobaRole.values())
		{
			List<Location> locations = lobbyLocations.get(role.name());

			if (locations == null || locations.isEmpty())
			{
				continue;
			}

			Location location = locations.get(0).clone();
			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, center)));

			ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);

			UtilEnt.vegetate(stand);
			UtilEnt.ghost(stand, true, false);

			stand.setCustomName(C.cGreenB + role.getName());
			stand.setCustomNameVisible(true);
			stand.setArms(true);
			stand.setBasePlate(false);
			stand.setHelmet(role.getSkin().getSkull());
			stand.setChestplate(PrepareSelection.buildColouredStack(Material.LEATHER_CHESTPLATE, role));
			stand.setLeggings(PrepareSelection.buildColouredStack(Material.LEATHER_LEGGINGS, role));
			stand.setBoots(PrepareSelection.buildColouredStack(Material.LEATHER_BOOTS, role));

			_roleViewers.put(stand, role);
		}
	}

	public void removeRoleViewers()
	{
		for (ArmorStand stand : _roleViewers.keySet())
		{
			stand.remove();
		}

		_roleViewers.clear();
	}

	@EventHandler
	public void onClick(CustomDamageEvent event)
	{
		onClick(event.GetDamagerEntity(true), event.GetDamageeEntity());
	}

	@EventHandler
	public void onClick(PlayerInteractAtEntityEvent event)
	{
		onClick(event.getPlayer(), event.getRightClicked());
	}

	private void onClick(Entity clicker, Entity clicked)
	{
		if (clicker == null || !(clicker instanceof Player) || !_roleViewers.containsKey(clicked))
		{
			return;
		}

		Player player = (Player) clicker;
		MobaRole role = _roleViewers.get(clicked);

		if (role == null)
		{
			return;
		}

		_roleShop.openShop(player, role);
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End || !_host.getArcadeManager().IsRewardStats())
		{
			return;
		}

		_host.GetPlayers(true).forEach(this::rewardPlayer);
	}

	public long getExperience(Player player, MobaRole role)
	{
		String stat = _host.GetName() + "." + role.getName() + ".ExpEarned";
		return _host.getArcadeManager().GetStatsManager().Get(player).getStat(stat);
	}

	public int getLevel(Player player, HeroKit kit)
	{
		return getLevel(player, kit.getRole());
	}

	public int getLevel(Player player, MobaRole role)
	{
		return getLevel(getExperience(player, role));
	}

	private void rewardPlayer(Player player)
	{
		MobaPlayer mobaPlayer = _host.getMobaData(player);

		if (mobaPlayer == null || mobaPlayer.getRole() == null || mobaPlayer.getKit() == null)
		{
			return;
		}

		MobaRole role = mobaPlayer.getRole();
		String stat = _host.GetName() + "." + role.getName() + ".ExpEarned";
		// EXP before earning
		long currentExp = getExperience(player, role);
		// Level before earning
		int currentLevel = getLevel(currentExp);

		AtomicInteger earnedExp = new AtomicInteger();

		for (GemData data : _host.GetGems(player).values())
		{
			earnedExp.getAndAdd((int) data.Gems);
		}

		earnedExp.getAndAdd(earnedExp.get() * EXP_FACTOR);

		MobaExperienceCalculateEvent event = new MobaExperienceCalculateEvent(player, earnedExp);
		UtilServer.CallEvent(event);

		MobaLevelData levelData = new MobaLevelData(currentExp + earnedExp.get());

		AtomicBoolean levelUp = new AtomicBoolean(levelData.getLevel() > currentLevel);

		_host.getArcadeManager().GetStatsManager().incrementStat(player, stat, earnedExp.get());

		UtilServer.runSyncLater(() ->
		{
			player.sendMessage(ArcadeFormat.Line);
			player.sendMessage("");

			player.sendMessage("                        " + role.getChatColor() + C.Bold + role.getName() + " Progression" + (levelUp.get() ? C.cGreenB + " LEVEL UP" : ""));
			player.sendMessage("");
			player.sendMessage(MobaUtil.getProgressBar(levelData.getExpLevelProgress() - earnedExp.get(), levelData.getExpLevelProgress(), levelData.getExpJustThisLevel(), 100) + " " + C.cGray + "+" + C.cGreen + earnedExp + C.cGray + "/" + C.cAqua + levelData.getExpJustThisLevel());
			player.sendMessage(C.cGreen + FORMAT.format((levelData.getPercentageComplete() * 100D)) + C.cWhite + "% complete for Level " + levelData.getDisplayLevel());

			player.sendMessage("");
			player.sendMessage(ArcadeFormat.Line);

			if (levelUp.get())
			{
				for (HeroKit kit : _host.getKits())
				{
					if (!kit.getRole().equals(role) || kit.getUnlockLevel() != levelData.getDisplayLevel())
					{
						continue;
					}

					player.playSound(player.getLocation(), Sound.ENDERDRAGON_DEATH, 1, 1);
					UtilTextMiddle.display(role.getColor() + kit.GetName(), "You unlocked a new Hero!", 10, 40, 10, player);
					return;
				}

				player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
			}
			else
			{
				player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
			}
		}, 60);
	}

	public String getPackageName(HeroKit kit)
	{
		return "MOBA_KIT_" + kit.GetName().toUpperCase();
	}

	public void setCurrentAnimation(MobaUnlockAnimation animation)
	{
		_currentAnimation = animation;
	}

	public MobaUnlockAnimation getCurrentAnimation()
	{
		return _currentAnimation;
	}
}
