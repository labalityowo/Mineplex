package mineplex.core.gadget.gadgets.morph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.hologram.Hologram;
import mineplex.core.recharge.Recharge;
import mineplex.core.utils.UtilGameProfile;

public class MorphAwkwardRabbit extends MorphGadget
{

	private List<String> _quotes = new ArrayList<>();

	private static final long COOLDOWN = 10000;
	private static final long HOLOGRAM_TIME = 3000;

	public MorphAwkwardRabbit(GadgetManager manager)
	{
		super(manager, "Wascally Wabbit Morph",
				UtilText.splitLinesToArray(new String[]{C.cGray + "Be absolutely silent my friends we are searching for rabbits currently!"}, LineFormat.LORE),
				-19, Material.SKULL_ITEM, (byte) 0);
		_quotes.addAll(Arrays.asList(
				"Eh, what's up, doc?",
				"That's all, folks.",
				"Gee, ain't I a stinker?",
				"Carrots are devine...",
				"I know this defies the\n law of gravity,\n but I never studied law!",
				"I don’t ask questions,\n I just have fun",
				"Hey, just a minute you!\n Them’s fightin’ words!"));
		setDisplayItem(SkinData.BUGS_BUNNY.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.BUGS_BUNNY.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void spawnHolograms(PlayerToggleSneakEvent event)
	{
		if (!isActive(event.getPlayer()))
			return;

		if (event.isSneaking())
			return;

		if (!Recharge.Instance.use(event.getPlayer(), getName(), COOLDOWN, true, false, "Cosmetics"))
			return;

		Location randomLoc = event.getPlayer().getLocation().clone();
		int[] rPos = new int[]{-2, -1, 0, 1, 2};
		int rX = rPos[UtilMath.random.nextInt(rPos.length)], rZ = rPos[UtilMath.random.nextInt(rPos.length)];

		randomLoc.add(rX, 1, rZ);

		String quote = _quotes.get(UtilMath.random.nextInt(_quotes.size()));
		Hologram hologram;
		if (quote.contains("\n"))
		{
			String[] lines = quote.split("\n");
			hologram = new Hologram(Manager.getHologramManager(), randomLoc, true, HOLOGRAM_TIME, lines);
		}
		else
		{
			hologram = new Hologram(Manager.getHologramManager(), randomLoc, true, HOLOGRAM_TIME, quote);
		}
		hologram.start();
	}

}
