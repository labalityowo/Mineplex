package nautilus.game.arcade.game.games.moba.kit.bardolf;

import mineplex.core.common.util.F;
import mineplex.core.common.util.SpigotUtil;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.kit.bardolf.HeroBardolf.WolfData;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SkillSummonWolf extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Click to summon a Wolf to your pack.",
			"Wolves are tamed and will attack your target or people who damage you.",
			"Wolves are weak and can be killed.",
			"Maximum of 5."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.BONE);
	private static final int MAX_WOLVES = 5;
	private static final int HEALTH = 8;

	public SkillSummonWolf(int slot)
	{
		super("Summon Wolf", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);
		setCooldown(5000);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isSkillItem(event))
		{
			return;
		}

		WolfData data = ((HeroBardolf) Kit).getWolfData(player);

		if (data == null)
		{
			return;
		}
		else if (data.getWolves().size() == MAX_WOLVES)
		{
			player.sendMessage(F.main("Game", "You have already summoned the maximum amount of wolves."));
			return;
		}
		else if (data.isUltimate())
		{
			player.sendMessage(F.main("Game", "You cannot summon new wolves right now."));
			return;
		}

		Game game = Manager.GetGame();
		GameTeam team = game.GetTeam(player);

		if (team == null)
		{
			return;
		}

		game.CreatureAllowOverride = true;

		Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
		DyeColor dyeColor = team.GetColor() == ChatColor.RED ? DyeColor.RED  : DyeColor.BLUE;

		wolf.setCollarColor(dyeColor);
		wolf.setTamed(true);
		SpigotUtil.setOldOwner_RemoveMeWhenSpigotFixesThis(wolf, player);
		wolf.setOwner(player);
		wolf.setHealth(HEALTH);
		wolf.setMaxHealth(HEALTH);
		UtilEnt.vegetate(wolf);
		MobaUtil.setTeamEntity(wolf, team);

		player.getWorld().playSound(player.getLocation(), Sound.WOLF_BARK, 1, 1.1F);

		data.getWolves().add(wolf);

		game.CreatureAllowOverride = false;

		useSkill(player);
	}

	@EventHandler
	public void updateWolfItem(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		HeroBardolf kit = (HeroBardolf) Kit;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			WolfData data = kit.getWolfData(player);
			ItemStack itemStack = player.getInventory().getItem(getSlot());

			if (data == null || itemStack == null || itemStack.getType() != SKILL_ITEM.getType())
			{
				continue;
			}

			itemStack.setAmount(data.getWolves().size());
		}
	}
}
