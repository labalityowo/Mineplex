package mineplex.game.clans.fields;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.creature.Creature;
import mineplex.game.clans.fields.commands.FieldMonsterCommand;
import mineplex.game.clans.fields.monsters.FieldMonsterBase;
import mineplex.game.clans.fields.repository.FieldMonsterToken;
import mineplex.game.clans.fields.repository.FieldRepository;

public class FieldMonster extends MiniPlugin
{
	public enum Perm implements Permission
	{
		FIELD_MONSTER_COMMAND,
	}

	private Creature _creature;
	private FieldRepository _repository;
	private Set<FieldMonsterBase> _pits;
	private String _serverName;

	private Map<Player, FieldMonsterInput> _input = new WeakHashMap<Player, FieldMonsterInput>();

	public FieldMonster(JavaPlugin plugin, FieldRepository repository, Creature creature, String serverName) 
	{
		super("Field Monster", plugin);
		
		_repository = repository;
		_creature = creature;
		_pits = new HashSet<FieldMonsterBase>();
		_serverName = serverName;
		
		Load();
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.FIELD_MONSTER_COMMAND, true, true);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new FieldMonsterCommand(this));
	}

	public void Help(Player caller) 
	{
		UtilPlayer.message(caller, F.main(getName(), "Commands List;"));
		UtilPlayer.message(caller, F.help("/fm type <Monster>", "Set Monster Type", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm max <#>", "Set Monster Limit", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm rate <Minutes>", "Set Monster Rate", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm radius <#>", "Set Area Radius", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm height <#>", "Set Area Height", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm create <Name>", "Create at your Location", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm delete <Name>", "Delete Field", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm list", "List Monster Fields", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm info <Name>", "Display Monster Field", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm kill", "Kills all Field Monsters", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fm wipe", "Delete All Monster Field (Database)", ChatColor.DARK_RED));
	}

	public void Create(Player caller, String name) 
	{
		FieldMonsterInput input = _input.get(caller);

		if (input.type == null)
		{
			UtilPlayer.message(caller, F.main(getName(), "You have not set Monster Type."));
			return;
		}
		
		for (FieldMonsterBase pit : _pits)
		{
			if (name.equalsIgnoreCase(pit.GetName()))
			{
				UtilPlayer.message(caller, F.main(getName(), "Monster Field with this name already exists."));
				return;
			}
		}

		FieldMonsterBase pit = new FieldMonsterBase(this, name, "ALL", input.type, input.mobMax, input.mobRate, caller.getLocation(), input.radius, input.height);
		Add(pit, true);

		UtilPlayer.message(caller, F.main(getName(), "You created Monster Field."));
		pit.Display(caller);
	}

	private void Add(FieldMonsterBase pit, boolean repo)
	{
		UtilServer.getServer().getPluginManager().registerEvents(pit, getPlugin());
		_pits.add(pit);
		
		if (repo)
			_repository.addFieldMonster(pit.GetToken());
	}

	public void Delete(Player caller, String name)
	{
		HashSet<FieldMonsterBase> remove = new HashSet<FieldMonsterBase>();

		for (FieldMonsterBase pit : _pits)
			if (pit.GetName().equalsIgnoreCase(name))
				remove.add(pit);

		int i = remove.size();

		for (FieldMonsterBase pit : remove)
			Delete(pit, true);

		UtilPlayer.message(caller, F.main(getName(), "Deleted " + i + " Monster Field(s)."));
	}

	private void Delete(FieldMonsterBase pit, boolean repo)
	{
		_pits.remove(pit);
		pit.RemoveMonsters();
		HandlerList.unregisterAll(pit);
		
		if (repo)
			_repository.deleteFieldMonster(pit.GetToken());
	}

	public void Wipe(Player player, boolean repo)
	{
		HashSet<FieldMonsterBase> remove = new HashSet<FieldMonsterBase>();

		for (FieldMonsterBase pit : _pits)
			remove.add(pit);

		_pits.clear();

		for (FieldMonsterBase pit : remove)
			Delete(pit, repo);

		UtilPlayer.message(player, F.main(_moduleName, "Field Monsters Wiped."));
	}

	private void Load()
	{
		Wipe(null, false);

		for (FieldMonsterToken token : _repository.getFieldMonsters(_serverName))
		{
			System.out.println("Found FM token : " + token.Type + " " + token.Centre);
			EntityType type = UtilEnt.searchEntity(null, token.Type, false);
			if (type == null)	
				continue;

			Location loc = UtilWorld.strToLoc(token.Centre);
			if (loc == null)	
				continue;

			FieldMonsterBase pit = new FieldMonsterBase(this, token.Name, _serverName, type, token.MobMax, token.MobRate, loc, token.Radius, token.Height);
			Add(pit, false);
		}
	}

	private void Clean()
	{
		for (FieldMonsterBase pit : _pits)
			pit.RemoveMonsters();
	}

	public Map<Player, FieldMonsterInput> getInput()
	{
		return _input;
	}

	public Set<FieldMonsterBase> getPits()
	{
		return _pits;
	}

	public Creature getCreature()
	{
		return _creature;
	}
}