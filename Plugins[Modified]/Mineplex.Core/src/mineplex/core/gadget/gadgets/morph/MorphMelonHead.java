package mineplex.core.gadget.gadgets.morph;

import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.Pair;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.utils.UtilGameProfile;

/**
 * PPC Reward for month of August 2017. Allows users to turn other player's heads into various fruits.
 */
public class MorphMelonHead extends MorphGadget
{
	/** Fruit head texture options */
	private static final Pair[] TEXTURES = {
			Pair.create(SkinData.APPLE, C.cDRedB + "Apple Head"),
			Pair.create(SkinData.MELON, C.cDGreenB + "Melon Head"),
			Pair.create(SkinData.ORANGE, C.cGoldB + "Orange Head"),
			Pair.create(SkinData.STRAWBERRY, C.cRedB + "Berry Head"),
			Pair.create(SkinData.PINEAPPLE, C.cYellowB + "Pineapple Head"),
			Pair.create(SkinData.GREEN_APPLE, C.cGreenB + "Apple Head"),
			Pair.create(SkinData.PLUM, C.cPurpleB + "Plum Head")
	};

	/** Ticks that a fruit head change lasts */
	private static final long TIME = 240;

	/** Map of players to their current fruit heads */
	private final Map<UUID, ItemStack> _heads = new HashMap<>();

	public MorphMelonHead(GadgetManager manager)
	{
		super(manager, "Melonhead Morph", UtilText.splitLinesToArray(new String[] {
				C.cGray + "Transform yourself into a melon.",
				C.cGray + "Tag other players to build your melon army!",
				"",
				C.cGreen + "Left click" + C.cWhite + " players to turn their heads to fruit."
		}, LineFormat.LORE), -14, Material.MELON, (byte) 0);

		setPPCYearMonth(YearMonth.of(2017, Month.AUGUST));
	}

	/**
	 * Sets the player's skin to a Melon texture.
	 */
	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile profile = UtilGameProfile.getGameProfile(player);
		profile.getProperties().clear();
		profile.getProperties().put("textures", SkinData.MELON_PERSON.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, profile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);
	}

	/**
	 * Restores the player's skin.
	 */
	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	/**
	 * Detect when a player punches another player.
	 */
	@EventHandler
	public void handlePlayerInteract(EntityDamageByEntityEvent event)
	{
		// Check it's two players interacting
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player)
		{
			if (!isActive((Player) event.getDamager()))
			{
				return;
			}

			Player player = (Player) event.getEntity();

			// do nothing if the player has a helmet already
			if (player.getInventory().getHelmet() != null)
			{
				return;
			}

			// do nothing if the player is supposed to already have a fruit helmet
			if (_heads.containsKey(player.getUniqueId()))
			{
				return;
			}

			// select a head skin and name it
			Pair<SkinData, String> data = UtilMath.randomElement(TEXTURES);
			ItemStack head = data.getLeft().getSkull(data.getRight(), new ArrayList<>());

			// equip the head and notify the player of the action
			_heads.put(player.getUniqueId(), head);
			player.getInventory().setHelmet(head);
			player.getWorld().playSound(player.getEyeLocation(), Sound.CHICKEN_EGG_POP, 1, 0);
			UtilFirework.playFirework(player.getEyeLocation(), FireworkEffect.builder().withColor(Color.LIME).with(FireworkEffect.Type.BALL).build());
			player.sendMessage(F.main("Melonhead", C.cYellow + "Wham! " + C.cGray + "You just got " + C.cGreen + "MELON'D!"));

			// schedule the head to be removed later
			Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), () ->
			{
				// don't do anything if the player has logged off
				if (_heads.containsKey(player.getUniqueId()))
				{
					ItemStack item = _heads.remove(player.getUniqueId());

					// don't remove the helmet if it has already been changed.
					if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().equals(item))
					{
						player.getInventory().setHelmet(null);

					}
				}

			}, TIME);
		}
	}


	/**
	 * Clean hash maps on player disconnect.
	 */
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent event)
	{
		if (isActive(event.getPlayer()))
		{
			if (_heads.containsKey(event.getPlayer().getUniqueId()))
			{
				_heads.remove(event.getPlayer().getUniqueId());
			}
		}
	}
}
