package nautilus.game.arcade.game.games.moba.kit.bardolf;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.skins.HeroSkinGadget;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.skins.HeroSkinGadgetData;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.kit.bardolf.HeroBardolf.WolfData;

public class SkillFullMoon extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Turns the character into a Werewolf.",
			"He gains +1 attack Damage and +5% Movement speed for each Wolf alive in his pack.",
			"The Wolves gain movement speed to catch up and are healed to their full HP.",
			"As wolves die his power decreases.",
			"All Wolves in the pack die after the ultimate ends"
	};
	private static final String WEREWOLF_KEY = "Bardolf-Werewolf";
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);
	private static final long DURATION = TimeUnit.SECONDS.toMillis(10);
	private static final int HEALTH = 20;
	private static final float SPEED_FACTOR = 0.05F;

	private final Set<Player> _active = new HashSet<>();

	public SkillFullMoon(int slot)
	{
		super("Full Moon", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(60000);
		setDropItemActivate(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isSkillItem(event) || _active.contains(player))
		{
			return;
		}

		HeroBardolf kit = (HeroBardolf) Kit;
		WolfData data = kit.getWolfData(player);

		if (data == null)
		{
			return;
		}

		_active.add(player);
		Manager.GetGame().WorldTimeSet = 18000;
		player.getWorld().strikeLightningEffect(player.getLocation());
		data.setUltimate(true);

		List<HeroSkinGadgetData> werewolfDataList = HeroSkinGadget.getSkins().get(WEREWOLF_KEY);
		boolean disguised = false;
		for (HeroSkinGadgetData normalData : HeroSkinGadget.getSkins().get(kit.GetName()))
		{
			if (normalData.getGadget().isActive(player))
			{
				for (HeroSkinGadgetData werewolfData : werewolfDataList)
				{
					if (normalData.getName().equals(werewolfData.getName()))
					{
						kit.disguise(player, werewolfData.getSkinData());
						disguised = true;
						break;
					}
				}

				break;
			}
		}

		if (!disguised)
		{
			kit.disguise(player, SkinData.BARDOLF_WEREWOLF);
		}

		float speedIncrease = (float) data.getWolves().size() * SPEED_FACTOR;
		data.setLastSpeedIncrease(speedIncrease);

		player.setWalkSpeed(player.getWalkSpeed() + speedIncrease);
		for (Wolf wolf : data.getWolves())
		{
			wolf.setMaxHealth(HEALTH);
			wolf.setHealth(wolf.getMaxHealth());
			wolf.setTamed(false);
			wolf.setAngry(true);
			wolf.getWorld().playSound(wolf.getLocation(), Sound.WOLF_GROWL, 1, 1);
		}

		broadcast(player);
		useActiveSkill(() ->
		{
			_active.remove(player);
			Manager.GetGame().WorldTimeSet = 12000;
			data.setUltimate(false);
			kit.disguise(player);
			player.setWalkSpeed(player.getWalkSpeed() - data.getLastSpeedIncrease());

			ItemStack itemStack = player.getInventory().getItem(1);
			if (itemStack != null)
			{
				itemStack.setAmount(1);
			}

			for (Wolf wolf : data.getWolves())
			{
				wolf.setHealth(0);
			}

		}, player, DURATION);
	}

	@EventHandler
	public void damagePlayer(CustomDamageEvent event)
	{
		LivingEntity damagerEntity = event.GetDamagerEntity(true);
		Player damagerPlayer = event.GetDamagerPlayer(true);
		WolfData data = ((HeroBardolf) Kit).getWolfData(damagerEntity);

		if (data == null)
		{
			return;
		}

		// Player Damage
		if (damagerPlayer != null && _active.contains(damagerPlayer))
		{
			event.AddMod(GetName(), data.getWolves().size());
		}
	}
}
