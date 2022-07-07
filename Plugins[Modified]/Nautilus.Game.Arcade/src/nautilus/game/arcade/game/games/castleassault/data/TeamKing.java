package nautilus.game.arcade.game.games.castleassault.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.GameTeam;

public class TeamKing 
{
	private static final int MAX_HEALTH = 600;	
	private Location _loc;
	private String _name;
	private GameTeam _owner;
	private Zombie _entity;
	private int _health;
	private String _lastDamager;
	private long _lastDamage;
	
	public TeamKing(GameTeam owner, String name, Location loc)
	{
		_owner = owner;
		_loc = loc;
		_name = name;
		_health = MAX_HEALTH;
		_entity = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
		UtilEnt.vegetate(_entity, true);
		_entity.getEquipment().setHelmet(new ItemBuilder(Material.DIAMOND_HELMET).setUnbreakable(true).build());
		_entity.getEquipment().setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE).setUnbreakable(true).build());
		_entity.getEquipment().setLeggings(new ItemBuilder(Material.DIAMOND_LEGGINGS).setUnbreakable(true).build());
		_entity.getEquipment().setBoots(new ItemBuilder(Material.DIAMOND_BOOTS).setUnbreakable(true).build());
		_entity.setRemoveWhenFarAway(false);
		_entity.setCustomName(owner.GetColor() + name);
	}
	
	public GameTeam getOwner()
	{
		return _owner;
	}
	
	public Location getLocation()
	{
		return _loc;
	}
	
	public String getName(boolean bold)
	{
		return _owner.GetColor() + (bold ? C.Bold : "") + _name;
	}
	
	public String getLastDamager()
	{
		return _lastDamager;
	}
	
	public int getHealth()
	{
		return Math.max(_health, 0);
	}
	
	public boolean isDead()
	{
		return getHealth() <= 0;
	}

	public void update(boolean beaconsAlive)
	{
		_entity.teleport(_loc);
		for (int y = 0; y <= 4; y++)
		{
			for (int x = -4; x <= 4; x++)
			{
				for (int z = -4; z <= 4; z++)
				{
					Block block = _loc.clone().add(x, y, z).getBlock();
					if ((block.getType() != Material.IRON_FENCE && block.getType() != Material.IRON_BLOCK) || !beaconsAlive)
					{
						block.setType(Material.AIR);
					}
					if (beaconsAlive)
					{
						if (x == -4 || x == 4 || z == -4 || z == 4)
						{
							if (y != 4)
							{
								block.setType(Material.IRON_FENCE);
							}
						}
						if (y == 4)
						{
							block.setType(Material.IRON_BLOCK);
						}
					}
				}
			}
		}
	}
	
	public boolean handleDamage(String player, double damage)
	{
        return handleDamage(player, damage, false);
    }

    public boolean handleDamage(String player, double damage, boolean force)
    {
		if (!UtilTime.elapsed(_lastDamage, 400) && !force)
		{
			return false;
		}

		_lastDamager = player;
		_lastDamage = System.currentTimeMillis();
		
		int dmg = (int)Math.ceil(damage);
		
		_health -= dmg;
		
		UtilEnt.PlayDamageSound(_entity);
		
		return true;
	}
}