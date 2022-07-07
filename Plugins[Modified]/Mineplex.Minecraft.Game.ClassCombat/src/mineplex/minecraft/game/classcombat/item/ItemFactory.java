package mineplex.minecraft.game.classcombat.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import mineplex.core.MiniPlugin;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.minecraft.game.core.IRelation;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.core.energy.Energy;
import mineplex.minecraft.game.core.fire.Fire;
import mineplex.core.projectile.ProjectileManager;
import mineplex.minecraft.game.classcombat.item.Consume.*;
import mineplex.minecraft.game.classcombat.item.Throwable.*;
import mineplex.minecraft.game.classcombat.item.weapon.*;
import mineplex.minecraft.game.core.damage.DamageManager;

import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemFactory extends MiniPlugin implements IItemFactory
{

	private final BlockRestore _blockRestore;
	private final ConditionManager _condition;
	private final DamageManager _damage;
	private final Energy _energy;
	private final Fire _fire;
	private final ProjectileManager _projectileManager;
	private final ProximityManager _proxyManager;
	private final IRelation _relation;
    private java.lang.reflect.Field _itemMaxDurability;
	private final HashMap<String, Item> _items;
	private final HashSet<String> _ignore;
	
	public ItemFactory(JavaPlugin plugin, BlockRestore blockRestore, ConditionManager condition, DamageManager damage, Energy energy, Fire fire, ProjectileManager projectileManager, IRelation relation)
	{
		this(plugin, blockRestore, condition, damage, energy, fire, projectileManager, relation, new HashSet<>());
	}
	
	public ItemFactory(JavaPlugin plugin, BlockRestore blockRestore, ConditionManager condition, DamageManager damage, Energy energy, Fire fire, ProjectileManager projectileManager, IRelation relation, HashSet<String> ignore)
	{
		super("Item Factory", plugin);
		
		_blockRestore = blockRestore;
		_condition = condition;
		_damage = damage;
		_energy = energy;
		_fire = fire;
		_projectileManager = projectileManager;
		_proxyManager = new ProximityManager();
		_relation = relation;

		_items = new HashMap<>();
		_ignore = ignore;
        
		try
        {
            _itemMaxDurability = net.minecraft.server.v1_8_R3.Item.class.getDeclaredField("durability");
            _itemMaxDurability.setAccessible(true);
        }
        catch (SecurityException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }

		PopulateFactory();
	}
	
	private void PopulateFactory()
	{
	    _items.clear();
			    
	    AddConsumables();
	    AddPassive();
	    AddThrowable();
	    AddTools();
	    AddOther();
	    addWeapons();
		
		for (Item cur : _items.values())
			registerEvents(cur);
	}

	private void AddConsumables() 
	{
		/*
		AddItem(new Apple(this, Material.APPLE, 1, false, 0, 1,
				ActionType.R, true, 500, 0, 
				ActionType.L, true, 500, 4, 1.2f, 
				-1, true, true, true, false));
		*/
		AddItem(new Soup(this, Material.MUSHROOM_SOUP, 1, true, 0, 1,
				ActionType.ANY, true, 500, 0, 
				null, false, 0, 0, 0f, 
				-1, true, true, true, false));
	}
	
	private void addWeapons()
	{
		AddItem(new StandardSword(this, 0, 2));
		AddItem(new StandardAxe(this, 0, 2));
		
		AddItem(new BoosterSword(this, 2000, 4));
		AddItem(new BoosterAxe(this, 2000, 4));
		
		AddItem(new PowerSword(this, 2000, 4));
		AddItem(new PowerAxe(this, 2000, 4));
		
		AddItem(new StandardBow(this, 0, 1));
	}
	
	private void AddPassive()
	{
		
	}
	
	private void AddThrowable()
	{
		AddItem(new WaterBottle(this, Material.POTION, 1, false, 0, 2,
				ActionType.R, true, 500, 0, 
				ActionType.L, true, 500, 0, 1f, 
				-1, true, true, true, false));
		
		AddItem(new Web(this, Material.WEB, 3, false, 500, 1,
				null, true, 0, 0, 
				ActionType.L, true, 1500, 0, 1f, 
				-1, true, true, true, false));
		
		/*
		AddItem(new PoisonBall(this, Material.SLIME_BALL, 1, false, 1500, 2,
				null, true, 0, 0, 
				ActionType.L, true, 0, 6, 1.2f, 
				-1, true, true, true, false));
		*/
		
		AddItem(new ProximityExplosive(this, Material.TNT, 1, false, 1000, 2,
				null, true, 0, 0, 
				ActionType.L, true, 250, 0, 0.8f, 
				4000, false, false, false, true));
		
		AddItem(new ProximityZapper(this, Material.REDSTONE_LAMP_OFF, 1, false, 1000, 2,
				null, true, 0, 0, 
				ActionType.L, true, 250, 0, 0.8f, 
				4000, false, false, false, true));
	}
	
	private void AddTools()
	{
		/*
		AddItem(new Scanner(this, 303, 
				Material.SHEARS, 1, true, 1000,
				ActionType.R, false, 2000, 20, 
				null, true, 250, 6, 1.8f, 
				-1, true, true, true, false));
				*/
	}
	
	private void AddOther()
	{
		Item assassinArrows = new Item(this, "Assassin Arrows", new String[] { "Arrows for your bow." }, Material.ARROW, 12, true, 0, 1);
		Item rangerArrows = new Item(this, "Ranger Arrows", new String[] { "Arrows for your bow." }, Material.ARROW, 24, true, 0, 1);
		assassinArrows.setFree(true);
		rangerArrows.setFree(true);
		
		AddItem(assassinArrows);
		AddItem(rangerArrows);
	}

	public IItem GetItem(String weaponName)
	{
		return _items.get(weaponName);
	}

	@Override
	public Collection<Item> GetItems()
	{
		return _items.values();
	}

	public void AddItem(Item newItem)
	{
		if (_ignore.contains(newItem.GetName()))
		{
			System.out.println("Item Factory: Ignored " + newItem.GetName());
			return;
		}
		
	    //try
        //{
            //_itemMaxDurability.setInt(net.minecraft.server.v1_8_R3.Item.getById(newItem.GetType().getId()), 56);
        //}
        //catch (IllegalArgumentException e)
        //{
           //e.printStackTrace();
        //}
        //catch (IllegalAccessException e)
        //{
           //e.printStackTrace();
        //}
	    
		_items.put(newItem.GetName(), newItem);
	}

	public BlockRestore BlockRestore()
	{
		return _blockRestore;
	}
	
	public ConditionManager Condition()
	{
		return _condition;
	}
	
	public DamageManager Damage()
	{
		return _damage;
	}

	public Energy Energy()
	{
		return _energy;
	}
	
	public Fire Fire()
	{
		return _fire;
	}
	
	public ProjectileManager Throw()
	{
		return _projectileManager;
	}

	public IRelation getRelation()
	{
		return _relation;
	}

	public ProximityManager getProximityManager()
	{
		return _proxyManager;
	}

	@Override
	public void registerSelf()
	{
		registerEvents(this);
		
		for (Item item : _items.values())
			registerEvents(item);
		
		registerEvents(_proxyManager);
	}
	
	@Override
	public void deregisterSelf()
	{
		HandlerList.unregisterAll(this);
		
		for (Item item : _items.values())
			HandlerList.unregisterAll(item);
		
		HandlerList.unregisterAll(_proxyManager);
	}
}