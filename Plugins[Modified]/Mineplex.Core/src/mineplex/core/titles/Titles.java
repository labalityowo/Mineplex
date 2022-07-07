package mineplex.core.titles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntitySlime;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutHeldItemSlot;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutNewAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PacketPlayOutWindowItems;
import net.minecraft.server.v1_8_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.book.BookBuilder;
import mineplex.core.common.DummyEntity;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.stats.event.PlayerStatsLoadedEvent;
import mineplex.core.titles.commands.TrackCommand;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.TrackTier;
import mineplex.core.twofactor.TwoFactorAuth;

/**
 * This manager handles displaying titles above a player's head, as well as their tracks
 */
@ReflectivelyCreateMiniPlugin
public class Titles extends MiniDbClientPlugin<TitleData> implements IPacketHandler
{
	public static int BOOK_SLOT = 8;

	// Maps player to their selected track
	private final Map<UUID, Track> _selectedTrack = new HashMap<>();
	private final Map<UUID, BukkitTask> _trackAnimationProgress = new HashMap<>();

	// Maps player to map of player and the id for the armorstand nametag
	private final Map<Integer, Map<UUID, Integer>> _armorStandIds = new HashMap<>();
	// Maps player to map of player and all ids that it owns
	private final Map<Integer, Map<UUID, List<Integer>>> _allIds = new HashMap<>();

	private final TrackManager _trackManager = require(TrackManager.class);
	private final TitlesRepository _titlesRepository = new TitlesRepository();

	private final GadgetManager _gadgetManager = require(GadgetManager.class);
	private final TwoFactorAuth _twofactor = require(TwoFactorAuth.class);

	private final BaseComponent[] CLICK_ENABLE_TRACK = new ComponentBuilder("")
			.append("Click to enable this track")
			.color(ChatColor.GREEN)
			.create();

	private final BaseComponent[] CLICK_DISABLE_TRACK = new ComponentBuilder("")
			.append("Click to disable this track")
			.color(ChatColor.RED)
			.create();

	private static final BaseComponent[] RETURN_TO_TABLE_OF_CONTENTS = new ComponentBuilder("")
			.append("Click to return to Table of Contents")
			.color(ChatColor.YELLOW)
			.create();

	private final Set<Player> _disabledPlayers;
	private boolean _disabled;

	private Titles()
	{
		super("Titles");

		_disabledPlayers = new HashSet<>();

		require(PacketHandler.class).addPacketHandler(this, PacketHandler.ListenerPriority.LOW, PacketPlayOutNamedEntitySpawn.class, PacketPlayOutEntityDestroy.class);
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		if (resultSet.next())
		{
			Set(uuid, new TitleData(resultSet.getString(1)));
		}
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT trackName FROM accountTitle WHERE accountId = '" + accountId + "';";
	}

	@Override
	protected TitleData addPlayer(UUID uuid)
	{
		return new TitleData(null);
	}

	@Override
	public void addCommands()
	{
		addCommand(new TrackCommand(this));
	}

	@EventHandler
	private void onJoin(PlayerStatsLoadedEvent event)
	{
		Player player = event.getPlayer();
		TitleData data = Get(player);

		if (data != null)
		{
			Track track = _trackManager.getTrackById(data.getTrackId());

			if (track != null && track.getRequirements().getTier(player) != null)
			{
				toggleActiveTrack(player, track);
			}
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent event)
	{
		_armorStandIds.values().forEach(map -> map.keySet().removeIf(key -> key.equals(event.getPlayer().getUniqueId())));
		_allIds.values().forEach(map -> map.keySet().removeIf(key -> key.equals(event.getPlayer().getUniqueId())));
		_selectedTrack.remove(event.getPlayer().getUniqueId());
		BukkitTask task = _trackAnimationProgress.remove(event.getPlayer().getUniqueId());
		if (task != null)
		{
			task.cancel();
		}
		_disabledPlayers.remove(event.getPlayer());
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (!_twofactor.isAuthenticating(event.getPlayer()) && event.getItem() != null && UtilEvent.isAction(event, UtilEvent.ActionType.R))
		{
			if (event.getItem().getType() == Material.WRITTEN_BOOK)
			{
				event.setCancelled(true);
				giveBook(event.getPlayer(), true);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSneak(PlayerToggleSneakEvent event)
	{
		if (event.isSneaking())
		{
			updateTitle(event.getPlayer(), (String) null);
		}
		else
		{
			updateTitle(event.getPlayer(), getActiveTrack(event.getPlayer()));
		}
	}

	public void setOrToggleTrackForPlayer(Player player, Track track, boolean clickedBook)
	{
		if (_disabled || _disabledPlayers.contains(player))
		{
			UtilPlayer.message(player, F.main("Track", "Tracks are disabled right now!"));
			return;
		}

		toggleActiveTrack(player, track);

		giveBook(player, clickedBook);

		runAsync(() ->
		{
			_titlesRepository.savePlayerSelection(player, getActiveTrack(player));
		});
	}

	public Track getActiveTrack(Player player)
	{
		return _selectedTrack.get(player.getUniqueId());
	}

	public void giveBookIfNotExists(Player player, boolean open)
	{
		if (player.getInventory().getItem(BOOK_SLOT) != null)
		{
			Material type = player.getInventory().getItem(BOOK_SLOT).getType();
			if (type == Material.WRITTEN_BOOK || type == Material.BOOK)
			{
				return;
			}
		}

		giveBook(player, open);
	}

	public void giveBook(Player player, boolean open)
	{
		switch (UtilPlayer.getVersion(player))
		{
			case Version1_9:
			case Version1_8:
				giveBook19(player, open);
				break;
		}
	}

	private void giveBook18(Player player, boolean open)
	{
		ItemStack book = new ItemBuilder(Material.BOOK)
				.setTitle(ChatColor.GREEN + "Titles " + ChatColor.WHITE + "- " + ChatColor.RED + "Unavailable")
				.setLore(
						ChatColor.RESET + "" + ChatColor.GRAY + "by Mineplex Games",
						ChatColor.RESET + "" + ChatColor.GRAY + "Original",
						"",
						ChatColor.RESET + "" + ChatColor.GRAY + "Unavailable in 1.8.x"
				)
				.setGlow(true)
				.build();

		player.getInventory().setItem(BOOK_SLOT, book);
	}

	private void giveBook19(Player player, boolean open)
	{
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		net.minecraft.server.v1_8_R3.ItemStack book;
		if (player.getInventory().getItem(BOOK_SLOT) != null && player.getInventory().getItem(BOOK_SLOT).getType() == Material.WRITTEN_BOOK)
		{
			book = ((CraftItemStack) player.getInventory().getItem(BOOK_SLOT)).getHandle();
		}
		else
		{
			book = new net.minecraft.server.v1_8_R3.ItemStack(Items.WRITTEN_BOOK);
			((CraftPlayer) player).getHandle().inventory.setItem(BOOK_SLOT, book);
		}

		List<Track> tracks = _trackManager.getAllTracks();

		tracks.removeIf(track -> track.getRequirements().getTier(player) == null && track.hideIfUnowned());

		tracks.sort(Comparator.comparing(Track::getShortName));

		String bookTitle = C.cGreen + "Titles";

		if (getActiveTrack(player) != null)
		{
			Track track = getActiveTrack(player);
			if (track.getRequirements().getTier(player) != null)
			{
				bookTitle += ChatColor.RESET + " - " + ChatColor.RESET + track.getRequirements().getTier(player).getDisplayName();
			}
		}

		BookBuilder bookBuilder = BookBuilder.newBuilder()
				.title(bookTitle)
				.author("Mineplex Games");

		// Build Table of Contents
		{
			int pagesRequired = (int) Math.ceil(tracks.size() / 10.0);
			int currentPage = 1;
			int current = 0;
			int trackPage = pagesRequired + 1;
			while (current < tracks.size())
			{
				List<Track> subList = tracks.subList(current, Math.min(current + 10, tracks.size()));

				ComponentBuilder tableOfContentsComponents = new ComponentBuilder("")
						.append("Table of Contents")
						.color(ChatColor.DARK_RED)
						.bold(true)
						.append("\n", ComponentBuilder.FormatRetention.NONE)
						.append("Page " + currentPage + "/" + pagesRequired, ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.RED)
						.append("\n\n", ComponentBuilder.FormatRetention.NONE);

				for (Track track : subList)
				{
					if (track.getRequirements().getTier(player) != null)
					{
						if (getActiveTrack(player) == track)
						{
							tableOfContentsComponents
									.append("[+]", ComponentBuilder.FormatRetention.NONE)
									.color(ChatColor.DARK_GREEN)
									.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_DISABLE_TRACK))
									.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/track " + track.getId() + " open"));
						}
						else
						{
							tableOfContentsComponents
									.append("[-]", ComponentBuilder.FormatRetention.NONE)
									.color(ChatColor.RED)
									.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_ENABLE_TRACK))
									.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/track " + track.getId() + " open"));
						}
					}
					else
					{
						tableOfContentsComponents
								.append("[x]", ComponentBuilder.FormatRetention.NONE)
								.color(ChatColor.GRAY);
					}
					tableOfContentsComponents
							.append(" ", ComponentBuilder.FormatRetention.NONE)
							.append(track.getShortName())
							.color(ChatColor.BLACK)
							.event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(trackPage++)))
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createTrackHover(player, track, true)));

					if (track.getRequirements().getNextTier(player) == null)
					{
						tableOfContentsComponents
								.append(" ", ComponentBuilder.FormatRetention.NONE)
								.append("★", ComponentBuilder.FormatRetention.NONE)
								.color(ChatColor.DARK_AQUA);
					}
					tableOfContentsComponents
							.append("\n", ComponentBuilder.FormatRetention.NONE);
				}

				bookBuilder.newPage().component(tableOfContentsComponents.create());

				current += 10;
				currentPage++;
			}
		}

		for (Track track : tracks)
		{
			ComponentBuilder pageContent = new ComponentBuilder("");
			pageContent
					.append("☰")
					.event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, RETURN_TO_TABLE_OF_CONTENTS))
					.append(" ", ComponentBuilder.FormatRetention.NONE)
					.append(track.getShortName())
					.bold(true)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createTrackHover(player, track, false)));
			if (track.getRequirements().getTier(player) != null)
			{
				if (getActiveTrack(player) == track)
				{
					pageContent
							.color(ChatColor.DARK_GREEN)
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/track " + track.getId() + " open"));
				}
				else
				{
					pageContent
							.color(ChatColor.BLACK)
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/track " + track.getId() + " open"));
				}
			}
			else
			{
				pageContent
						.color(ChatColor.BLACK);
			}
			pageContent
					.append("\n\n", ComponentBuilder.FormatRetention.NONE)
					.append("Next Tier ")
					.color(ChatColor.DARK_AQUA)
					.bold(true)
					.append("\n", ComponentBuilder.FormatRetention.NONE);

			TrackTier nextTier = track.getRequirements().getNextTier(player);
			if (nextTier != null)
			{
				ComponentBuilder progressHover = new ComponentBuilder("")
						.append("Progress: ")
						.color(ChatColor.YELLOW)
						.append(String.valueOf(nextTier.get(player)), ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.WHITE)
						.append("/")
						.append(String.valueOf(nextTier.getGoal()));


				int totalTicks = 20;
				double progress = nextTier.getProgress(player);
				String percent = ((int) (progress * 100.0)) + "%";
				int ticks = ((int) (progress * totalTicks * 1.0));
				pageContent.append("[", ComponentBuilder.FormatRetention.NONE);
				StringBuilder pipes = new StringBuilder();
				for (int i = 0; i < ticks; i++)
				{
					pipes.append("|");
				}
				BaseComponent[] components = progressHover.create();

				pageContent
						.append(pipes.toString(), ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.DARK_GREEN)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, components));
				pipes.setLength(0);
				for (int i = ticks; i < totalTicks; i++)
				{
					pipes.append("|");
				}
				pageContent
						.append(pipes.toString(), ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.GRAY)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, components))
						.append("] (" + percent + ")", ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.BLACK);
			}
			else
			{
				pageContent
						.append("No more tiers!");
			}

			pageContent
					.append("\n\n")
					.append("Progress")
					.color(ChatColor.DARK_AQUA)
					.bold(true)
					.append("\n", ComponentBuilder.FormatRetention.NONE);

			for (TrackTier tier : track.getRequirements().getTiers())
			{
				int rank = track.getRequirements().getRank(tier);

				ComponentBuilder tierHover = new ComponentBuilder("")
						.append(track.getLongName())
						.color(track.getColor())
						.bold(true)
						.append(" Tier " + rank, ComponentBuilder.FormatRetention.NONE)
						.color(tier.getFormat().getColor())
						.append("\n\n", ComponentBuilder.FormatRetention.NONE);

				if (tier.getDescription() != null)
				{
					tierHover
							.append(
									Arrays.stream(
											UtilText.splitLineToArray(tier.getDescription(), LineFormat.LORE)
									).collect(Collectors.joining("\n")),
									ComponentBuilder.FormatRetention.NONE
							)
							.append("\n\n", ComponentBuilder.FormatRetention.NONE);
				}
				tierHover
						.append("Title: ")
						.color(ChatColor.YELLOW);

				tierHover.append("", ComponentBuilder.FormatRetention.NONE);
				tier.getFormat().preFormat(tierHover);
				tierHover.append(tier.getTitle(), ComponentBuilder.FormatRetention.NONE);
				tier.getFormat().format(tierHover);
				tierHover.append("", ComponentBuilder.FormatRetention.NONE);
				tier.getFormat().postFormat(tierHover);

				tierHover.append("\n", ComponentBuilder.FormatRetention.NONE)
						.append("Progress: ")
						.color(ChatColor.YELLOW)
						.append(Math.min(tier.get(player), tier.getGoal()) + "/" + tier.getGoal(), ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.WHITE);

				if (tier.getProgress(player) >= 1.0)
				{
					tierHover
							.append("\n\n", ComponentBuilder.FormatRetention.NONE)
							.append("Complete!")
							.color(ChatColor.AQUA);
				}

				pageContent
						.append(track.getShortName() + " " + UtilText.toRomanNumeral(rank), ComponentBuilder.FormatRetention.NONE)
						.color(tier.getProgress(player) >= 1.0 ? ChatColor.DARK_GREEN : ChatColor.GRAY)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tierHover.create()))
						.append("\n", ComponentBuilder.FormatRetention.NONE);
			}

			bookBuilder.newPage().component(pageContent.create());
		}

		book.setTag(bookBuilder.toCompound());

		entityPlayer.playerConnection.sendPacket(new PacketPlayOutWindowItems(entityPlayer.activeContainer.windowId, entityPlayer.activeContainer.a()));

		if (open)
		{
			int old = entityPlayer.inventory.itemInHandIndex;
			if (old != BOOK_SLOT)
			{
				entityPlayer.playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(BOOK_SLOT));
			}
			((CraftPlayer) player).getHandle().openBook(book);
			if (old != BOOK_SLOT)
			{
				entityPlayer.playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(old));
			}
		}
	}

	private BaseComponent[] createTrackHover(Player player, Track track, boolean isMainPage)
	{
		ComponentBuilder trackHover = new ComponentBuilder("")
				.append(track.getLongName())
				.color(track.getColor())
				.bold(true)
				.append("\n\n", ComponentBuilder.FormatRetention.NONE)
				.append(
						Arrays.stream(
								UtilText.splitLineToArray(track.getDescription(), LineFormat.LORE)
						).collect(Collectors.joining("\n")),
						ComponentBuilder.FormatRetention.NONE
				)
				.color(ChatColor.WHITE)
				.append("\n\n", ComponentBuilder.FormatRetention.NONE);

		if (!isMainPage)
		{
			track.getRequirements().appendLore(trackHover);

			if (track.getRequirements().getTier(player) != null)
			{
				if (getActiveTrack(player) == track)
				{
					trackHover
							.append("Click to disable: ", ComponentBuilder.FormatRetention.NONE)
							.color(ChatColor.RED);
				}
				else
				{
					trackHover
							.append("Click to enable: ", ComponentBuilder.FormatRetention.NONE)
							.color(ChatColor.GREEN);
				}
				TrackTier tier = track.getRequirements().getTier(player);
				trackHover.append("", ComponentBuilder.FormatRetention.NONE);
				tier.getFormat().preFormat(trackHover);
				trackHover.append(tier.getTitle(), ComponentBuilder.FormatRetention.NONE);
				tier.getFormat().format(trackHover);
				trackHover.append("", ComponentBuilder.FormatRetention.NONE);
				tier.getFormat().postFormat(trackHover);
			}
			else
			{
				trackHover
						.append("You have not unlocked any tiers in this track!", ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.GRAY);
			}
			trackHover
					.append("\n\n", ComponentBuilder.FormatRetention.NONE);
		}
		else
		{
			if (track.getRequirements().getNextTier(player) != null)
			{
				TrackTier nextTier = track.getRequirements().getNextTier(player);

				trackHover
						.append("Progress: ")
						.color(ChatColor.YELLOW)
						.append(String.valueOf(nextTier.get(player)), ComponentBuilder.FormatRetention.NONE)
						.color(ChatColor.WHITE)
						.append("/")
						.append(String.valueOf(nextTier.getGoal()))
						.append("\n\n");
			}
			trackHover
					.append("Click to view this track", ComponentBuilder.FormatRetention.NONE)
					.color(ChatColor.GREEN)
					.append("\n\n", ComponentBuilder.FormatRetention.NONE);
		}
		trackHover
				.append("ID: ")
				.color(ChatColor.YELLOW)
				.append(track.getId(), ComponentBuilder.FormatRetention.NONE)
				.color(ChatColor.WHITE);

		return trackHover.create();
	}

	private void toggleActiveTrack(Player player, Track track)
	{
		if (getActiveTrack(player) != track)
		{
			if (track.getRequirements().getTier(player) != null)
			{
				_selectedTrack.put(player.getUniqueId(), track);
				if (!_trackAnimationProgress.containsKey(player.getUniqueId()))
				{
					_trackAnimationProgress.put(player.getUniqueId(), runSyncTimer(new BukkitRunnable()
					{
						private AtomicInteger _lastRun = new AtomicInteger();
						private int _frame = 0;

						@Override
						public void run()
						{
							Track curTrack = _selectedTrack.get(player.getUniqueId());
							if (curTrack != null)
							{
								TrackTier curTier = curTrack.getRequirements().getTier(player);
								if (curTier != null)
								{
									TrackFormat format = curTier.getFormat();
									if (format.isAnimated())
									{
										if (_lastRun.get() > format.getDelay())
										{
											_lastRun.set(0);

											List<String> lines = format.getAnimatedLines();
											if (_frame >= lines.size())
											{
												_frame = 0;
											}

											updateTitle(player, lines.get(_frame));

											_frame++;
										}
										_lastRun.incrementAndGet();
									}
								}
							}
						}
					}, 0L, 1L));
				}
				UtilPlayer.message(player, F.main("Track", "Your active track has been updated to " + track.getColor() + track.getLongName()));
			}
			else
			{
				UtilPlayer.message(player, F.main("Track", "Uh oh. Couldn't set your active track because you don't have any unlocked tiers on " + track.getColor() + track.getLongName() + C.mBody + "!"));
			}
		}
		else
		{
			_selectedTrack.remove(player.getUniqueId());
			UtilPlayer.message(player, F.main("Track", "Your have disabled your active track"));
		}

		updateTitle(player, getActiveTrack(player));
	}

	private void updateTitle(Player player, Track track)
	{
		if (_disabled || _disabledPlayers.contains(player))
			return;

		if (track != null && track.getRequirements().getTier(player) != null)
		{
			TrackTier currentTier = track.getRequirements().getTier(player);
			updateTitle(player, currentTier.getDisplayName());
		}
		else
		{
			updateTitle(player, (String) null);
		}
	}


	private void updateTitle(Player player, String str)
	{
		if (_disabled || _disabledPlayers.contains(player))
			return;

		Map<UUID, Integer> map = _armorStandIds.get(player.getEntityId());

		if (map == null) return;

		map.forEach((uuid, entityId) ->
		{
			Player other = Bukkit.getPlayer(uuid);
			if (other == null)
				return;

			updateArmorStand(player, other, str, entityId);
		});
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.isCancelled())
			return;

		if (_disabled)
			return;

		if (packetInfo.getPacket() instanceof PacketPlayOutNamedEntitySpawn)
		{
			PacketPlayOutNamedEntitySpawn packet = (PacketPlayOutNamedEntitySpawn) packetInfo.getPacket();
			Entity entity = UtilEnt.getEntityById(packet.a);

			if (!(entity instanceof Player) || _disabledPlayers.contains(entity))
			{
				return;
			}

			Player owner = (Player) entity;
			if (_gadgetManager.getActive(owner, GadgetType.MORPH) == null)
			{
				summonForEntity(packetInfo.getPlayer(), owner);
			}
		}
		else if (packetInfo.getPacket() instanceof PacketPlayOutEntityDestroy)
		{
			PacketPlayOutEntityDestroy packet = (PacketPlayOutEntityDestroy) packetInfo.getPacket();
			for (int id : packet.a)
			{
				destroyForEntity(packetInfo.getPlayer(), id);
			}
		}
	}

	private void summonForEntity(Player receiver, Player player)
	{
		switch (UtilPlayer.getVersion(receiver))
		{
			case Version1_9:
				summonForEntity19(receiver, player);
				break;
			case Version1_8:
				summonForEntity18(receiver, player);
				break;
		}
	}

	private void summonForEntity19(Player receiver, Player player)
	{
		World world = ((CraftWorld) receiver.getWorld()).getHandle();

		DataWatcher armorStandWatcher = getArmorStandWatcher(player, null);
		armorStandWatcher.a(10, (byte) 0x10, EntityArmorStand.META_ARMOR_OPTION, (byte) 0x10); // Small

		DataWatcher squidWatcher = new DataWatcher(new DummyEntity(world));
		squidWatcher.a(0, (byte) 0x20, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0x20);

		DataWatcher slimeWatcher = new DataWatcher(new DummyEntity(world));
		slimeWatcher.a(0, (byte) 0x20, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0x20);
		slimeWatcher.a(16, -1, EntitySlime.META_SIZE, -1);

		PacketPlayOutSpawnEntityLiving spawnSlime = new PacketPlayOutSpawnEntityLiving();
		spawnSlime.a = UtilEnt.getNewEntityId();
		spawnSlime.b = EntityType.SLIME.getTypeId();
		spawnSlime.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnSlime.d = -150;
		spawnSlime.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnSlime.i = 0;
		spawnSlime.j = 0;
		spawnSlime.k = 0;
		spawnSlime.f = 0;
		spawnSlime.g = 0;
		spawnSlime.h = 0;
		spawnSlime.uuid = UUID.randomUUID();
		spawnSlime.l = slimeWatcher;

		PacketPlayOutSpawnEntityLiving spawnSquid = new PacketPlayOutSpawnEntityLiving();
		spawnSquid.a = UtilEnt.getNewEntityId();
		spawnSquid.b = EntityType.SQUID.getTypeId();
		spawnSquid.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnSquid.d = -150;
		spawnSquid.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnSquid.i = 0;
		spawnSquid.j = 0;
		spawnSquid.k = 0;
		spawnSquid.f = 0;
		spawnSquid.g = 0;
		spawnSquid.h = 0;
		spawnSquid.uuid = UUID.randomUUID();
		spawnSquid.l = squidWatcher;

		PacketPlayOutSpawnEntityLiving spawnArmorStand = new PacketPlayOutSpawnEntityLiving();
		spawnArmorStand.a = UtilEnt.getNewEntityId();
		spawnArmorStand.b = EntityType.ARMOR_STAND.getTypeId();
		spawnArmorStand.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnArmorStand.d = -150;
		spawnArmorStand.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnArmorStand.i = 0;
		spawnArmorStand.j = 0;
		spawnArmorStand.k = 0;
		spawnArmorStand.f = 0;
		spawnArmorStand.g = 0;
		spawnArmorStand.h = 0;
		spawnArmorStand.uuid = UUID.randomUUID();
		spawnArmorStand.l = armorStandWatcher;

		PacketPlayOutNewAttachEntity attachSlimeToPlayer = new PacketPlayOutNewAttachEntity(player.getEntityId(), new int[]{spawnSlime.a});
		PacketPlayOutNewAttachEntity attachSquidtoSlime = new PacketPlayOutNewAttachEntity(spawnSlime.a, new int[]{spawnSquid.a});
		PacketPlayOutNewAttachEntity attachArmorStandToSquid = new PacketPlayOutNewAttachEntity(spawnSquid.a, new int[]{spawnArmorStand.a});

		_armorStandIds.computeIfAbsent(player.getEntityId(), key -> new HashMap<>()).put(receiver.getUniqueId(), spawnArmorStand.a);
		_allIds.computeIfAbsent(player.getEntityId(), key -> new HashMap<>()).put(receiver.getUniqueId(), Arrays.asList(spawnSlime.a, spawnSquid.a, spawnArmorStand.a));

		runSync(() ->
		{
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnSlime);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnSquid);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnArmorStand);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachSlimeToPlayer);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachSquidtoSlime);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachArmorStandToSquid);

			updateTitle(player, getActiveTrack(player));
		});
	}

	// Unused, but I've put my heart and soul into it and so it's staying here
	private void summonForEntity18(Player receiver, Player player)
	{
		World world = ((CraftWorld) receiver.getWorld()).getHandle();

		DataWatcher armorStandWatcher = getArmorStandWatcher(player, null);
		armorStandWatcher.a(10, (byte) 0x10, EntityArmorStand.META_ARMOR_OPTION, (byte) 0x10); // Small

		DataWatcher squidWatcher = new DataWatcher(new DummyEntity(world));
		squidWatcher.a(0, (byte) 0x20, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0x20);

		DataWatcher slimeWatcher = new DataWatcher(new DummyEntity(world));
		slimeWatcher.a(0, (byte) 0x20, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0x20);
		slimeWatcher.a(16, (byte) -1, EntitySlime.META_SIZE, -1);

		PacketPlayOutSpawnEntityLiving spawnSlime = new PacketPlayOutSpawnEntityLiving();
		spawnSlime.a = UtilEnt.getNewEntityId();
		spawnSlime.b = EntityType.SLIME.getTypeId();
		spawnSlime.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnSlime.d = -150;
		spawnSlime.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnSlime.i = 0;
		spawnSlime.j = 0;
		spawnSlime.k = 0;
		spawnSlime.f = 0;
		spawnSlime.g = 0;
		spawnSlime.h = 0;
		spawnSlime.uuid = UUID.randomUUID();
		spawnSlime.l = slimeWatcher;

		PacketPlayOutSpawnEntityLiving spawnSquid = new PacketPlayOutSpawnEntityLiving();
		spawnSquid.a = UtilEnt.getNewEntityId();
		spawnSquid.b = EntityType.WOLF.getTypeId();
		spawnSquid.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnSquid.d = -150;
		spawnSquid.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnSquid.i = 0;
		spawnSquid.j = 0;
		spawnSquid.k = 0;
		spawnSquid.f = 0;
		spawnSquid.g = 0;
		spawnSquid.h = 0;
		spawnSquid.uuid = UUID.randomUUID();
		spawnSquid.l = squidWatcher;

		PacketPlayOutSpawnEntityLiving spawnArmorStand = new PacketPlayOutSpawnEntityLiving();
		spawnArmorStand.a = UtilEnt.getNewEntityId();
		spawnArmorStand.b = EntityType.ARMOR_STAND.getTypeId();
		spawnArmorStand.c = MathHelper.floor(player.getLocation().getX() * 32.0D);
		spawnArmorStand.d = -150;
		spawnArmorStand.e = MathHelper.floor(player.getLocation().getZ() * 32.0D);
		spawnArmorStand.i = 0;
		spawnArmorStand.j = 0;
		spawnArmorStand.k = 0;
		spawnArmorStand.f = 0;
		spawnArmorStand.g = 0;
		spawnArmorStand.h = 0;
		spawnArmorStand.uuid = UUID.randomUUID();
		spawnArmorStand.l = armorStandWatcher;

		PacketPlayOutAttachEntity attachSlimeToPlayer = new PacketPlayOutAttachEntity();
		attachSlimeToPlayer.a = 0;
		attachSlimeToPlayer.b = spawnSlime.a;
		attachSlimeToPlayer.c = player.getEntityId();

		PacketPlayOutAttachEntity attachSquidtoSlime = new PacketPlayOutAttachEntity();
		attachSquidtoSlime.a = 0;
		attachSquidtoSlime.b = spawnSquid.a;
		attachSquidtoSlime.c = spawnSlime.a;

		PacketPlayOutAttachEntity attachArmorStandToSquid = new PacketPlayOutAttachEntity();
		attachArmorStandToSquid.a = 0;
		attachArmorStandToSquid.b = spawnArmorStand.a;
		attachArmorStandToSquid.c = spawnSquid.a;

		_armorStandIds.computeIfAbsent(player.getEntityId(), key -> new HashMap<>()).put(receiver.getUniqueId(), spawnArmorStand.a);
		_allIds.computeIfAbsent(player.getEntityId(), key -> new HashMap<>()).put(receiver.getUniqueId(), Arrays.asList(spawnSlime.a, spawnSquid.a, spawnArmorStand.a));

		runSync(() ->
		{
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnSlime);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnSquid);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(spawnArmorStand);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachSlimeToPlayer);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachSquidtoSlime);
			((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(attachArmorStandToSquid);

			updateTitle(player, getActiveTrack(player));
		});
	}

	private void updateArmorStand(Player owner, Player receiver, String newName, int entityId)
	{
		if (_disabled || _disabledPlayers.contains(owner))
			return;

		switch (UtilPlayer.getVersion(receiver))
		{
			case Version1_9:
				updateArmorStand19(owner, receiver, newName, entityId);
				break;
			case Version1_8:
				updateArmorStand18(owner, receiver, newName, entityId);
				break;
		}
	}

	private void updateArmorStand19(Player owner, Player player, String newName, int entityId)
	{
		DataWatcher armorStandWatcher = getArmorStandWatcher(owner, newName);
		armorStandWatcher.a(10, (byte) 0x10, EntityArmorStand.META_ARMOR_OPTION, (byte) 0x10); // Small

		PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata();
		entityMetadata.a = entityId;
		entityMetadata.b = armorStandWatcher.c();

		((CraftPlayer) player).getHandle().playerConnection.networkManager.handle(entityMetadata);
	}

	private void updateArmorStand18(Player owner, Player player, String newName, int entityId)
	{
		DataWatcher armorStandWatcher = getArmorStandWatcher(owner, newName);

		PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata();
		entityMetadata.a = entityId;
		entityMetadata.b = armorStandWatcher.c();

		((CraftPlayer) player).getHandle().playerConnection.networkManager.handle(entityMetadata);
	}

	private void destroyForEntity(Player receiver, int id)
	{
		Map<UUID, Integer> innerMap = _armorStandIds.get(id);
		if (innerMap != null)
		{
			innerMap.remove(receiver.getUniqueId());

			if (innerMap.isEmpty())
			{
				_armorStandIds.remove(id);
			}
		}

		Map<UUID, List<Integer>> allIdsMap = _allIds.get(id);

		if (allIdsMap != null)
		{
			List<Integer> ids = allIdsMap.remove(receiver.getUniqueId());
			if (ids != null)
			{
				int[] idsArr = ids.stream().mapToInt(Integer::intValue).toArray();

				PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(idsArr);
				((CraftPlayer) receiver).getHandle().playerConnection.networkManager.handle(destroy);
			}

			if (allIdsMap.isEmpty())
			{
				_allIds.remove(id);
			}
		}
	}

	private DataWatcher getArmorStandWatcher(Player ownerOfTrack, String newName)
	{
		World world = ((CraftWorld) ownerOfTrack.getWorld()).getHandle();

		DataWatcher armorStandWatcher = new DataWatcher(new DummyEntity(world));
		armorStandWatcher.a(0, (byte) 0x20, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0x20);
		armorStandWatcher.a(1, (short) 300, net.minecraft.server.v1_8_R3.Entity.META_AIR, 0);

		if (newName != null && !newName.isEmpty())
		{
			armorStandWatcher.a(2, newName, net.minecraft.server.v1_8_R3.Entity.META_CUSTOMNAME, newName);
			armorStandWatcher.a(3, (byte) 1, net.minecraft.server.v1_8_R3.Entity.META_CUSTOMNAME_VISIBLE, true);
		}
		else
		{
			armorStandWatcher.a(2, "", net.minecraft.server.v1_8_R3.Entity.META_CUSTOMNAME, "");
			armorStandWatcher.a(3, (byte) 0, net.minecraft.server.v1_8_R3.Entity.META_CUSTOMNAME_VISIBLE, false);
		}

		return armorStandWatcher;
	}

	public void forceEnable()
	{
		if (_disabled)
		{
			_disabled = false;

			for (Player player : UtilServer.getPlayersCollection())
			{
				forceEnable(player);
			}
		}
	}

	public void forceDisable()
	{
		if (!_disabled)
		{
			_disabled = true;

			for (Player player : UtilServer.getPlayers())
			{
				forceDisable(player);
			}
		}
	}

	public void forceEnable(Player player)
	{
		if (_disabledPlayers.remove(player))
		{
			for (Player other : UtilServer.getPlayersCollection())
			{
				if (player.equals(other))
				{
					continue;
				}

				summonForEntity(player, other);
			}
		}
	}
	
	public void forceDisable(Player player)
	{
		if (_disabledPlayers.add(player))
		{
			for (Player other : UtilServer.getPlayersCollection())
			{
				if (player.equals(other))
				{
					continue;
				}

				destroyForEntity(player, other.getEntityId());
			}
		}
	}
}
