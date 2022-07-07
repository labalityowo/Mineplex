package mineplex.core.gadget.gadgets.morph;

import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

/**
 * Bob Ross morph for Power Play Club (June 2017)
 */
public class MorphBobRoss extends MorphGadget
{
	/** Radius within which painting is not allowed near treasure chests */
	private static final int TREASURE_RADIUS = 4;

	/** The inventory slot in which the paint brush is placed */
	private static final int PAINT_BRUSH_SLOT = 2;

	/** The # of milliseconds for which paint blocks exist */
	private static final long PAINT_MILLISECONDS = 30000;

	/** Height above a player's location at which quotes are to be displayed */
	private static final double QUOTE_HEIGHT = 2.25;

	/** The number of seconds for which quotes are displayed */
	private static final int QUOTE_PERSISTENCE = 10;

	/** Cooldown key for changing paint colors */
	private static final String COLOR_KEY = "Change Paint Color";

	/** Cooldown time for changing paint colors (milliseconds) */
	private static final long COLOR_COOLDOWN = 220;

	/** Cooldown key for displaying a Bob Ross quote above head */
	private static final String QUOTE_KEY = "Bob Ross Quote";

	/** Cooldown time for displaying a Bob Ross quote (milliseconds) */
	private static final long QUOTE_COOLDOWN = 10100;

	/** Distance (in blocks) from which the quotes can be seen */
	private static final int VIEW_DISTANCE = 14;

	/** Quote attribution for bob */
	private static final String ATTRIBUTION = C.cGray + "- " + C.cYellow + "Bob Ross";

	/** Formatted name for the clean brush */
	private static final String BRUSH_NAME = C.cYellow + "Clean Paintbrush";

	/** Determines the order in which colors are selected */
	private static final byte[] COLOR_ORDER = {
			(byte) 1,
			(byte) 14,
			(byte) 11,
			(byte) 10,
			(byte) 2,
			(byte) 6,
			(byte) 12,
			(byte) 4,
			(byte) 5,
			(byte) 13,
			(byte) 9,
			(byte) 15,
			(byte) 7,
			(byte) 8,
			(byte) 0,
			(byte) 3
	};

	/** Paint colors for displaying in players' hotbars */
	private static final String[] PAINT_COLORS = {
			C.cBlackB   + "Midnight Black",
			C.cRedB     + "Alizarin Crimson",
			C.cDGreenB  + "Sap Green",
			C.cGoldB    + "Van Dyke Brown",
			C.cDBlueB   + "Prussian Blue",
			C.cDPurpleB + "Studio Purple",
			C.cDAquaB   + "Phthalo Green",
			C.cGrayB    + "Dusty Gray",
			C.cDGrayB   + "Tundora Gray",
			C.cPurpleB  + "Soft Flower Pink",
			C.cGreenB   + "Lima Green",
			C.cYellowB  + "Cadmium Yellow",
			C.cBlueB    + "Danube Blue",
			C.cPurpleB  + "Soft Magenta",
			C.cGoldB    + "Yellow Ochre",
			C.cWhiteB   + "Titanium White"
	};

	/** Brush types for displaying in players' hotbars */
	private static final String[] PAINT_BRUSHES = {
			"Landscape Brush",
			"Foliage Brush",
			"Background Brush",
			"Blender Brush",
			"Oval Brush",
			"Round Brush",
			"Fan Brush",
			"Painting Knife"
	};

	/** List of wholesome Bob Ross quotes. */
	private static final String[][] QUOTES = {
			{"We don't make mistakes,", "just happy little accidents."},
			{"Anything that you're willing to practice,", "you can do."},
			{"There's nothing wrong with having a tree as a friend."},
			{"Let's get a little crazy here!"},
			{"Express yourself to others through painting."},
			{"All you need to paint is a few tools,", "a little instruction,", "and a vision in your mind."},
			{"I can't think of anything more", "rewarding than being able to express", "yourself to others through painting."},
			{"The secret to doing anything is", "believing that you can do it."},
			{"Anything that you believe you can do strong enough,", "you can do."},
			{"Wash the brush, just beat the devil out of it!"},
			{"Beat the devil out of it!"},
			{"I started painting as a hobby when I was little."},
			{"Anybody can do what I do."},
			{"I believe talent is just a pursued interest."},
			{"Mix up a little more shadow color here,", "then we can put us a little shadow right in there."},
			{"You have unlimited power on this canvas!"},
			{"Believe that you can do it cause you can do it."},
			{"There's nothing in the world", "that breeds success like success."},
			{"Lets build a happy little cloud."},
			{"Lets build some happy little trees."},
			{"Everyday is a good day when you paint."},
			{"The only thing worse than", "yellow snow is green snow."},
			{"Look around.", "Look at what we have.", "Beauty is everywhere—", "you only have to look to see it."},
			{"Just go out and talk to a tree.", "Make friends with it."},
			{"How do you make a round circle with a square knife?", "That’s your challenge for the day."},
			{"Water's like me. It's laaazy...", "Boy, it always looks for the easiest way to do things"},
			{"Oooh, if you have never been to Alaska,", "go there while it is still wild."},
			{"If I paint something,", "I don't want to have to explain what it is."},
			{"We artists are a different breed of people.", "We're a happy bunch."},
			{"Any way you want it to be, that's just right."},
			{"As my son Steve says, just 'smoosh' it in there."},
			{"Use odorless paint-thinner.", "If it's not odorless, you'll find yourself", "working alone very, very quickly."},
			{"Let's just blend this little rascal here, ha!", "Happy as we can be."},
			{"Clouds are very, very free."},
			{"Maybe in our world there lives a", "happy little tree over there."},
			{"Shwooop! Hehe.", "You have to make those little noises,", "or it just doesn't work."},
			{"No pressure. Just relax and watch it happen."},
			{"Find freedom on this canvas."},
			{"It’s so important to do something every", "day that will make you happy."},
			{"Every day is a good day when you paint."},
			{"Hi, I'm Bob Ross!"},
			{"Everyone needs a friend."},
			{"Don’t forget to tell these special", "people in your life just how special", "they are to you."},
			{"I taught my son to paint mountains like these!"},
			{"You need the dark in order to show the light."},
			{"In nature, dead trees are just", "as normal as live trees."},
			{"This is happy place;", "little squirrels live here and play."},
			{"It’s life.", "It’s interesting.", "It’s fun."},
			{"I really believe that", "if you practice enough you could paint the", "'Mona Lisa' with a two-inch brush."},
			{"Don't be afraid to go out on a limb,", "because that's where the fruit is!"}
	};

	/** Map of items in players' inventories */
	private final Map<UUID, ItemStack> _inventoryItems = new HashMap<>();

	/** Colors that are being used by painting players */
	private final Map<UUID, Byte> _paintColors = new HashMap<>();

	private final HologramManager _holograms;

	public MorphBobRoss(GadgetManager manager, HologramManager holograms)
	{
		super(manager, "Bob Ross Morph", UtilText.splitLinesToArray(new String[] {
				C.cGray + "Become the creator of your own world!",
				C.cGray + "Leave a trail of paint behind you as you walk.",
				"",
				C.cGreen + "Hold " + C.cWhite + "your " + C.cYellow + "paintbrush" + C.cWhite + " (stick) to paint.",
				"",
				C.cGreen + "Left" + C.cWhite + " and " + C.cGreen + "right click" + C.cWhite + " on your",
				C.cYellow + "paintbrush" + C.cWhite + " and " + C.cYellow + "paints" + C.cWhite + " (dyes)",
				C.cWhite + "to change paint colors.",
				"",
				C.cGreen + "Crouch " + C.cWhite + "to say a Bob Ross quote."
		}, LineFormat.LORE), -14, Material.PAINTING, (byte) 0);

		setPPCYearMonth(YearMonth.of(2017, Month.JUNE));

		_holograms = holograms;
	}

	/**
	 * Sets the player's skin to Bob Ross, then gives the player a 'paintbrush' item.
	 */
	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile profile = UtilGameProfile.getGameProfile(player);
		profile.getProperties().clear();
		profile.getProperties().put("textures", SkinData.BOB_ROSS.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, profile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);

		givePaintbrush(player);
	}

	/**
	 * Restores the player's skin and takes their 'paintbrush' item away.
	 */
	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		takePaintbrush(player);
	}

	/**
	 * Detect when a player clicks their paint brush item.
	 */
	@EventHandler
	public void handlePlayerInteract(PlayerInteractEvent event)
	{
		if (!isActive(event.getPlayer()))
		{
			return;
		}

		if (!_inventoryItems.containsKey(event.getPlayer().getUniqueId()))
		{
			return;
		}

		if (!_inventoryItems.get(event.getPlayer().getUniqueId()).equals(event.getPlayer().getItemInHand()))
		{
			return;
		}

		if (UtilEvent.isAction(event, UtilEvent.ActionType.L))
		{
			if (Recharge.Instance.use(event.getPlayer(), COLOR_KEY, COLOR_COOLDOWN, false, false))
			{

				changePaintColor(event.getPlayer(), true);
			}
		}
		else if (UtilEvent.isAction(event, UtilEvent.ActionType.R))
		{
			if (Recharge.Instance.use(event.getPlayer(), COLOR_KEY, COLOR_COOLDOWN, false, false))
			{

				changePaintColor(event.getPlayer(), false);
			}
		}
	}

	/**
	 * Display a Bob Ross quote above players' heads when they sneak.
	 * Destroy old paint after a certain amount of time has elapsed.
	 */
	@EventHandler
	public void updateEvent(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK) // do quote displaying
		{
			for (Player player : getActive())
			{
				if (player.isSneaking())
				{
					if (Recharge.Instance.use(player, QUOTE_KEY, QUOTE_COOLDOWN, false, false))
					{
						// select quote
						String[] quote = QUOTES[ThreadLocalRandom.current().nextInt(0, QUOTES.length)];
						final Collection<Hologram> holograms = new ArrayList<>();

						// add attribution
						holograms.add(new Hologram(_holograms, player.getLocation().add(0, QUOTE_HEIGHT, 0), ATTRIBUTION));

						// display the quote
						double offset = 0.3;
						for (int i = quote.length - 1; i >= 0; --i)
						{
							holograms.add(new Hologram(_holograms, player.getLocation().add(0, QUOTE_HEIGHT + offset, 0),
									C.cWhite + quote[i]));
							offset += 0.25;
						}

						for (Hologram hologram : holograms)
						{
							hologram.setViewDistance(VIEW_DISTANCE);
							hologram.setFollowEntity(player);
							hologram.start();
						}

						// remove hologram a certain number of seconds later
						Bukkit.getServer().getScheduler().runTaskLater(UtilServer.getPlugin(), () ->
								holograms.forEach(Hologram::stop), QUOTE_PERSISTENCE * 20);
					}
				}
			}
		}
		else if (event.getType() == UpdateType.FASTEST)
		{
			for (Player player : getActive())
			{
				if (_inventoryItems.containsKey(player.getUniqueId()))
				{
					ItemStack item = _inventoryItems.get(player.getUniqueId());

					if (item.getType() == Material.STICK && player.getItemInHand().equals(item))
					{

						togglePainting(player);
					}
					else if (!player.getItemInHand().equals(item) && item.getType() != Material.STICK)
					{

						togglePainting(player);
					}
				}
			}
		}
	}

	/**
	 * When a player moves, paint the ground below them if they have it enabled.
	 */
	@EventHandler
	public void paintGround(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
		{
			return;
		}

		// check if the player has been issued a paintbrush
		if (_inventoryItems.containsKey(player.getUniqueId()))
		{
			ItemStack item = _inventoryItems.get(player.getUniqueId());

			if (item.getType() == Material.STICK)
			{
				return; // player is not painting, do nothing
			}

			Block block = player.getLocation().getBlock();
			Block down = block.getRelative(BlockFace.DOWN);

			boolean carpet = block.getType() == Material.CARPET;

			// check that there is room to paint and that the block below is solid and not more paint.
			if ((block.isEmpty() || carpet) && UtilBlock.fullSolid(down) && !UtilBlock.bottomSlab(down))
			{
				// if the block is a non-paint carpet
				if (carpet && !Manager.getBlockRestore().contains(block) || !Manager.selectBlocks(this, block))
				{
					return; // don't paint
				}

				// mark block as painted
				Manager.getBlockRestore().add(block, Material.CARPET.getId(), (byte) (15 - item.getData().getData()),
						block.getTypeId(), block.getData(), PAINT_MILLISECONDS);
			}
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
			UUID uuid = event.getPlayer().getUniqueId();
			_inventoryItems.remove(uuid);
			_paintColors.remove(uuid);

		}
	}

	/**
	 * Cycle the selected paint color for a player.
	 *
	 * @param reverse Whether to cycle backwards through colors.
	 */
	private void changePaintColor(Player player, boolean reverse)
	{

		ItemStack item = _inventoryItems.remove(player.getUniqueId());
		byte data = selectPaintColor(player, reverse);

		ItemStack newItem = ItemStackFactory.Instance.CreateStack(Material.INK_SACK, data, 1,
				PAINT_COLORS[data] + " " + PAINT_BRUSHES[ThreadLocalRandom.current().nextInt(0, PAINT_BRUSHES.length)]);

		_inventoryItems.put(player.getUniqueId(), newItem);
		player.getInventory().remove(item);
		player.getInventory().setItem(PAINT_BRUSH_SLOT, newItem);
		player.updateInventory();
	}

	/**
	 * Toggle whether a player is currently painting or not.
	 */
	private void togglePainting(Player player)
	{

		ItemStack item = _inventoryItems.remove(player.getUniqueId());

		ItemStack newItem;
		if (item.getType() == Material.STICK)
		{
			byte data;
			if (!_paintColors.containsKey(player.getUniqueId()))
			{
				data = selectPaintColor(player, false);
			}
			else
			{
				data = COLOR_ORDER[_paintColors.get(player.getUniqueId())];
			}

			newItem = ItemStackFactory.Instance.CreateStack(Material.INK_SACK, data, 1,
					PAINT_COLORS[data] + " " + PAINT_BRUSHES[ThreadLocalRandom.current().nextInt(0, PAINT_BRUSHES.length)]);
		}
		else
		{
			newItem = ItemStackFactory.Instance.CreateStack(Material.STICK, (byte) 0, 1, BRUSH_NAME);
		}

		_inventoryItems.put(player.getUniqueId(), newItem);
		player.getInventory().remove(item);
		player.getInventory().setItem(PAINT_BRUSH_SLOT, newItem);
		player.updateInventory();
	}

	/**
	 * Changes the paint color currently assigned to a player.
	 * If one is not assigned, a new one will be given.
	 *
	 * @param player  The player to whom to assign the paint color.
	 * @param reverse Whether to reverse through paint colors when choosing a new one.
	 *
	 * @return the dye data value for the newly selected color.
	 */
	private byte selectPaintColor(Player player, boolean reverse)
	{
		UUID uuid = player.getUniqueId();

		byte value;

		if (!_paintColors.containsKey(uuid))
		{
			value = (byte) ThreadLocalRandom.current().nextInt(0, 16);
			_paintColors.put(uuid, value);
		}
		else
		{
			value = _paintColors.get(uuid);

			if (reverse)
			{
				if (--value < 0)
				{
					value = 15;
				}
			}
			else
			{
				if (++value > 15)
				{
					value = 0;
				}
			}

			_paintColors.put(uuid, value);
		}

		return COLOR_ORDER[value];
	}

	/**
	 * Give a paintbrush item to a player.
	 */
	private void givePaintbrush(Player player)
	{
		if (!_inventoryItems.containsKey(player.getUniqueId()))
		{
			ItemStack item = ItemStackFactory.Instance.CreateStack(Material.STICK, (byte) 0, 1, BRUSH_NAME);
			player.getInventory().setItem(PAINT_BRUSH_SLOT, item);
			_inventoryItems.put(player.getUniqueId(), item);
			player.updateInventory();
		}
	}

	/**
	 * Take the paintbrush item from the player
	 */
	private void takePaintbrush(Player player)
	{
		// check that paintbrush has been issued
		if (_inventoryItems.containsKey(player.getUniqueId()))
		{
			ItemStack item = _inventoryItems.remove(player.getUniqueId());

			// if player has paintbrush, take it
			if (player.getInventory().contains(item))
			{
				player.getInventory().remove(item);
			}

			player.updateInventory();
		}
	}
}
