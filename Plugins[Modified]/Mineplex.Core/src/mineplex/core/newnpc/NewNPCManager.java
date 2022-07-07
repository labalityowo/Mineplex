package mineplex.core.newnpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.events.EntityVelocityChangeEvent;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.creature.Creature;
import mineplex.core.creature.event.CreatureKillEntitiesEvent;
import mineplex.core.newnpc.command.NPCCommand;
import mineplex.core.newnpc.command.NPCDeleteCommand;
import mineplex.core.newnpc.command.NPCEditCommand;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

/**
 * NPCManager is a new version of the previous NpcManager to improve usability and more general purpose.
 * <br>
 * NPCs are saved in the table <b>newNPCs</b> in the <b>accounts</b> database.
 * <br>
 * Details on how to create NPCs can be found in the {@link NPC} and {@link PlayerNPC} classes.
 */
@ReflectivelyCreateMiniPlugin
public class NewNPCManager extends MiniPlugin
{

	public enum Perm implements Permission
	{
		NPC_COMMAND,
		BUILD_NPC_COMMAND,
		CLEAR_NPC_COMMAND,
		DELETE_NPC_COMMAND,
		EDIT_NPC_COMMAND,
		MOVE_NPC_COMMAND
	}

	private final Creature _creature;

	private final NewNPCRepository _repository;

	private final List<NPC> _npcs;
	private final Map<Player, NPCBuilder> _builders;

	private NewNPCManager()
	{
		super("NPC");

		_creature = require(Creature.class);
		_repository = new NewNPCRepository();
		_npcs = new ArrayList<>();
		_builders = new HashMap<>();

		// Loading NPCs on the main thread makes sure that _npcs is populated before anything tries to spawn NPCs.
		loadNPCs();
		// Any NPC with the metadata DUMMY will always be spawned regardless, useful for testing designs
		spawnNPCs("DUMMY", null);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.NPC_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.BUILD_NPC_COMMAND, true, true);
		PermissionGroup.DEV.setPermission(Perm.CLEAR_NPC_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.DELETE_NPC_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.EDIT_NPC_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.MOVE_NPC_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new NPCCommand(this));
	}

	/**
	 * Loads synchronously all NPCs from the database and populates the {@link #_npcs} list if the npc was not already
	 * contained within the collection. Thus this method can be called more than once during runtime. Though it is
	 * recommended that if the caller is not calling this during startup then this should not be called from the main
	 * thread.
	 */
	private void loadNPCs()
	{
		_repository.getNPCs(npcs ->
		{
			npcs.forEach(npc ->
			{
				if (_npcs.contains(npc))
				{
					return;
				}

				_npcs.add(npc);
			});
		});
	}

	/**
	 * Spawns all NPCs that have the metadata specified.
	 *
	 * @param metadata The metadata that must equal the NPC's metadata.
	 * @param after    A callback consumer for each npc.
	 */
	public void spawnNPCs(String metadata, Consumer<NPC> after)
	{
		_creature.SetForce(true);

		List<NPC> npcs = getNPCs(metadata, true);

		npcs.forEach(npc ->
		{
			npc.spawnEntity();

			if (after != null)
			{
				after.accept(npc);
			}
		});

		_creature.SetForce(false);
	}

	/**
	 * Removes an NPC's entity and removes it from the database.
	 *
	 * @param npc The NPC you want to delete.
	 */
	public void deleteNPC(NPC npc)
	{
		if (npc instanceof StoredNPC)
		{
			runAsync(() -> _repository.deleteNPC((StoredNPC) npc));
		}

		if (npc.getEntity() != null)
		{
			npc.getEntity().remove();
		}

		_npcs.remove(npc);
	}

	/**
	 * Deletes every NPC.
	 *
	 * @param deleteFromDB If true, all NPC's are deleted from the database. If false this action is only temporary.
	 */
	public void clearNPCS(boolean deleteFromDB)
	{
		if (deleteFromDB)
		{
			runAsync(_repository::clearNPCs);
		}

		_npcs.forEach(npc ->
		{
			if (npc.getEntity() != null)
			{
				npc.getEntity().remove();
			}
		});

		_npcs.clear();
	}

	/**
	 * Spawns an NPC and does not save it to the database.
	 *
	 * @param npc The NPC you want to spawn.
	 */
	public void addNPC(NPC npc)
	{
		addNPC(npc, false);
	}

	/**
	 * Adds and spawns an NPC.
	 *
	 * @param npc      The NPC you want to spawn.
	 * @param saveToDB If true the NPC will be saved to the database, though npc must be a {@link StoredNPC}.
	 */
	public void addNPC(NPC npc, boolean saveToDB)
	{
		_creature.SetForce(true);

		_npcs.add(npc);
		npc.spawnEntity();

		if (saveToDB && npc instanceof StoredNPC)
		{
			runAsync(() -> _repository.insertNPCIfNotExists((StoredNPC) npc));
		}

		_creature.SetForce(false);
	}

	/**
	 * Updates all the fields in the database for this NPC.
	 *
	 * @param npc The NPC you wish to update.
	 */
	public void updateNPCProperties(StoredNPC npc)
	{
		runAsync(() -> _repository.updateNPCProperties(npc));
	}

	/**
	 * Used by {@link mineplex.core.newnpc.command.NPCBuildCommand}. Has no other purpose.
	 */
	public void createBuilder(Player player)
	{
		_builders.put(player, new NPCBuilder());

		player.sendMessage(F.main(_moduleName, "Hello " + F.name(player.getName()) + "! It seems you would like to create a new NPC. Hold or wear any items that you want your NPC to have."));
		player.sendMessage(F.main("Step 1", "Type in chat the Entity Type of your NPC."));
	}

	/**
	 * Cleans up NPC creation maps, if the player quits.
	 */
	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_builders.remove(player);
	}

	/**
	 * Handles {@link NPCBuilder} data input.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void chat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		NPCBuilder builder = _builders.get(player);

		if (builder == null)
		{
			return;
		}

		event.setCancelled(true);

		if (builder.getType() == null)
		{
			try
			{
				builder.setType(EntityType.valueOf(event.getMessage().toUpperCase()));
				player.sendMessage(F.main(_moduleName, "Set the entity type to " + F.name(builder.getType().toString()) + "!"));

				player.sendMessage(F.main("Step 2", "Now type in chat the name of your NPC. Using " + F.elem("&") + " as colour codes"));
			}
			catch (IllegalArgumentException e)
			{
				player.sendMessage(F.main(_moduleName, "That isn't a valid entity type"));
				player.sendMessage(F.main(_moduleName, "Valid Entity Types: " + UtilText.listToString(Arrays.asList(EntityType.values()), true)));
			}
		}
		else if (builder.getName() == null)
		{
			if (event.getMessage().length() > 32)
			{
				player.sendMessage(F.main(_moduleName, "The maximum length for NPC name's is 32 characters."));
				return;
			}

			builder.setName(event.getMessage());

			player.sendMessage(F.main(_moduleName, "Set the name to " + F.name(ChatColor.translateAlternateColorCodes('&', builder.getName())) + "!"));

			player.sendMessage(F.main("Step 3", "Now type in chat the metadata of your NPC. If you don't want to set any metadata, type " + F.elem("null") + "."));
		}
		else if (builder.getMetadata() == null)
		{
			String uppercase = event.getMessage().toUpperCase();

			builder.setMetadata(uppercase.equals("NULL") ? null : event.getMessage());

			if (builder.getType() == EntityType.PLAYER)
			{
				player.sendMessage(F.main("Step 4", "Now type in chat " + F.elem("Yes") + " if you would like your NPC to use your Minecraft Skin. If not, then say anything else"));
			}
			else
			{
				buildNPC(player, builder);
			}
		}
		else if (builder.getType() == EntityType.PLAYER)
		{
			if (event.getMessage().toUpperCase().equals("YES"))
			{
				builder.setUseSkin(true);
				player.sendMessage(F.main(_moduleName, "Set your NPC to use your Minecraft Skin!"));
			}
			else
			{
				player.sendMessage(F.main(_moduleName, "Set your NPC to not use your Minecraft Skin. You (or someone else) can always change it in the database."));
			}

			buildNPC(player, builder);
		}
	}

	private void buildNPC(Player player, NPCBuilder builder)
	{
		player.sendMessage(F.main(_moduleName, "Creating your NPC..."));
		runSync(() -> builder.build(player, this));
		player.sendMessage(F.main(_moduleName, "Created your NPC!"));
		_builders.remove(player);
	}

	/**
	 * Handles NPC command interaction.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void npcInteract(NPCInteractEvent event)
	{
		Player player = event.getPlayer();
		NPC npc = event.getNpc();

		if (!(npc instanceof StoredNPC))
		{
			return;
		}

		StoredNPC storedNPC = (StoredNPC) npc;

		if (!Recharge.Instance.usable(player, NPCDeleteCommand.RECHARGE_KEY))
		{
			event.setCancelled(true);
			deleteNPC(storedNPC);
			player.sendMessage(F.main(_moduleName, "Deleted " + F.name(storedNPC.getColouredName() + " (" + F.elem(storedNPC.getId()) + ").")));
			Recharge.Instance.recharge(player, NPCDeleteCommand.RECHARGE_KEY);
		}
		else if (!Recharge.Instance.usable(player, NPCEditCommand.RECHARGE_KEY))
		{
			event.setCancelled(true);
			Recharge.Instance.recharge(player, NPCEditCommand.RECHARGE_KEY);
			player.sendMessage(F.main(_moduleName, "Editing " + F.name(storedNPC.getColouredName()) + "..."));
			player.sendMessage("");
			new JsonMessage(C.cGreenB + "[EDIT INFO]")
					.click(ClickEvent.SUGGEST_COMMAND, "/newnpc edit " + storedNPC.getId() + " info &e&lExample Line 1//&bExample Line 2")
					.hover(HoverEvent.SHOW_TEXT, C.cYellow + "Use // to separate lines.")
					.extra("  ")
					.extra(C.cYellowB + "[TELEPORT]")
					.click(ClickEvent.RUN_COMMAND, "/newnpc edit " + storedNPC.getId() + " spawn")
					.hover(HoverEvent.SHOW_TEXT, C.cYellow + "Click to teleport the npc to where you are standing.")
					.extra("  ")
					.extra(C.cGreenB + "[EQUIPMENT]")
					.click(ClickEvent.RUN_COMMAND, "/newnpc edit " + storedNPC.getId() + " equipment")
					.hover(HoverEvent.SHOW_TEXT, C.cYellow + "Click to give the npc what armor and items you have.")
					.extra("  ")
					.extra(C.cYellowB + "[SKIN]")
					.click(ClickEvent.RUN_COMMAND, "/newnpc edit " + storedNPC.getId() + " skin")
					.hover(HoverEvent.SHOW_TEXT, C.cYellow + "Click to give the npc your skin. Only works on players.")
					.extra("  ")
					.extra(C.cRedB + "[FINISH]")
					.click(ClickEvent.RUN_COMMAND, "/newnpc edit " + storedNPC.getId() + " update")
					.hover(HoverEvent.SHOW_TEXT, C.cYellow + "Click to finish and update the npc in the database.")
					.sendToPlayer(player);
			player.sendMessage("");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void updateInfo(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		_npcs.forEach(npc ->
		{
			if (npc instanceof StoredNPC && npc.getEntity() != null)
			{
				((StoredNPC) npc).updateInfo();
			}
		});
	}
	/**
	 * Prevents chunk unloading when there is an NPC in it.
	 */
	@EventHandler
	public void chunkUnload(ChunkUnloadEvent event)
	{
		for (Entity entity : event.getChunk().getEntities())
		{
			if (isNPC(entity))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	/*
		A collection of listeners that make sure NPCs don't die.
	 */

	@EventHandler
	public void entityCombust(EntityCombustEvent event)
	{
		if (isNPC(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void entityVelocity(EntityVelocityChangeEvent event)
	{
		if (isNPC(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entityDamage(EntityDamageEvent event)
	{
		if (isNPC(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entityDamage(EntityDamageByEntityEvent event)
	{
		NPC npc = getNPC(event.getEntity());

		if (event.getDamager() instanceof Player && npc != null)
		{
			callInteractEvent((Player) event.getDamager(), npc, true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entityInteract(PlayerInteractEntityEvent event)
	{
		NPC npc = getNPC(event.getRightClicked());

		if (npc != null)
		{
			event.setCancelled(true);
			callInteractEvent(event.getPlayer(), npc, false);
		}
	}

	private void callInteractEvent(Player player, NPC npc, boolean leftClick)
	{
		UtilServer.CallEvent(new NPCInteractEvent(player, npc, leftClick));
	}

	@EventHandler
	public void creatureKill(CreatureKillEntitiesEvent event)
	{
		event.GetEntities().removeIf(this::isNPC);
	}

	/**
	 * @param id The id of the NPC you wish to get the {@link NPC} object of.
	 * @return The {@link NPC} that has the id or null.
	 */
	public StoredNPC getNPC(int id)
	{
		return (StoredNPC) _npcs.stream()
				.filter(npc -> npc instanceof StoredNPC && ((StoredNPC) npc).getId() == id)
				.findFirst()
				.orElse(null);
	}

	/**
	 * @param metadata The metadata you wish to find all the NPCs of.
	 * @return A list of {@link NPC} that have the metadata which starts with that specified.
	 */
	public List<NPC> getNPCs(String metadata)
	{
		return getNPCs(metadata, false);
	}

	/**
	 * @param metadata The metadata you wish to find all the NPCs of.
	 * @return A list of {@link NPC} that are unloaded (have no entity attached to them) and that have the metadata which
	 * starts with that specified.
	 */
	public List<NPC> getNPCs(String metadata, boolean unloaded)
	{
		return _npcs.stream()
				.filter(npc -> npc.getMetadata().startsWith(metadata) && (!unloaded || npc.getEntity() == null))
				.collect(Collectors.toList());
	}

	/**
	 * @param entity The entity you want to get it's NPC of.
	 * @return The {@link NPC} representation of that entity or null if the entity is not an NPC.
	 */
	private NPC getNPC(Entity entity)
	{
		for (NPC npc : _npcs)
		{
			if (npc.getEntity() != null && npc.getEntity().equals(entity))
			{
				return npc;
			}
		}

		return null;
	}

	/**
	 * @param entity The entity you want to check.
	 * @return true if the entity is a {@link NPC}.
	 */
	public boolean isNPC(Entity entity)
	{
		return getNPC(entity) != null;
	}
}
