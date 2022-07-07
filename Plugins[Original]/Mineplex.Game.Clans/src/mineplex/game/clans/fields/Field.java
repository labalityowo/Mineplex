package mineplex.game.clans.fields;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.creature.Creature;
import mineplex.core.energy.Energy;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.fields.repository.FieldRepository;
import mineplex.minecraft.game.core.condition.ConditionManager;

public class Field extends MiniPlugin
{
	private FieldBlock _block;
	private FieldOre _ore;
	private FieldMonster _mob;
	
	public Field(JavaPlugin plugin, Creature creature, ConditionManager condition, 
				ClansManager clansManager, Energy energy, String serverName) 
	{
		super("Field Factory", plugin);
		
		FieldRepository repository = new FieldRepository(plugin);
		_block = new FieldBlock(plugin, condition, clansManager, energy, repository, serverName);
		_ore = new FieldOre(plugin, repository, serverName);
		_mob = new FieldMonster(plugin, repository, creature, serverName);
	}
	
	public FieldBlock GetBlock()
	{
		return _block;
	}
	
	public FieldOre GetOre()
	{
		return _ore;
	}
	
	public FieldMonster GetMonster()
	{
		return _mob;
	}	
}