package nautilus.game.arcade.game.games.evolution;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.common.util.UtilText;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeFormat;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public abstract class EvoKit extends Kit
{
	/**
	 * @author Mysticate
	 */
	
	private EntityType _entity;

	private final String _name;
	private final String[] _description;
	protected final double _health;
	protected final double _damage;

	public EvoKit(ArcadeManager manager, String name, String[] desc, double health, double damage, Perk[] kitPerks, EntityType type)
	{
		this(manager, name, desc, health, damage, kitPerks, type, false);
	}
	
	public EvoKit(ArcadeManager manager, String name, String[] desc, double health, double damage, Perk[] kitPerks, EntityType type, boolean visible)
	{
		super(manager, GameKit.NULL_PLAYER, kitPerks);
		
		_entity = type;

		_name = name;
		_description = desc;
		_health = health;
		_damage = damage;		
	}

	@Override
	public String GetName()
	{
		return _name;
	}

	@Override
	public String[] GetDesc()
	{
		return _description;
	}

	public EntityType getEntity()
	{
		return _entity;
	}
	
	public double getHealth()
	{
		return _health;
	}
	
	public double getDamage()
	{
		return _damage;
	}
	
	public String[] buildHologram()
	{
		ArrayList<String> text = new ArrayList<>();
		
		text.add(C.cAqua + C.Bold + GetName());
		text.add(C.cYellow + "Melee: " + C.cWhite + _damage + "  " + C.cYellow + "Health: " + C.cWhite + _health);
		
		if (GetDesc().length > 0)
		{
			text.add(C.Line);

			text.addAll(Arrays.asList(GetDesc()));
		}
		
		return text.toArray(new String[0]);
	}
	
	public void upgradeGive(Player player)
	{
		String def = C.cWhite + C.Bold + "You evolved into " + (UtilText.startsWithVowel(GetName()) ? "a " : "an ") + F.elem(C.cGreen + C.Bold + GetName());
		
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 0.1F);
		
		for (int i=0 ; i<3 ; i++)
			UtilPlayer.message(player, "");
		
		UtilPlayer.message(player, ArcadeFormat.Line);
		UtilPlayer.message(player, def);	

		UtilPlayer.message(player, C.Line);
		
		UtilPlayer.message(player, C.cYellow + "  Melee: " + C.cWhite + _damage + "  " + C.cYellow + "Health: " + C.cWhite + _health);
		
		UtilPlayer.message(player, C.Line);
		
		for (String desc : GetDesc())
			UtilPlayer.message(player, C.cGray + "  " + desc);
		
		UtilPlayer.message(player, ArcadeFormat.Line);
		
//		UtilTextMiddle.display(null, def, player);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;
		
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;
		
		if (!HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
		double mod = _damage - event.GetDamageInitial();
				
		event.AddMod(damager.getName(), "Attack", mod, true);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public final void GiveItems(final Player player)
	{
		player.setMaxHealth(_health);
		player.setHealth(player.getMaxHealth());
		player.getInventory().setHeldItemSlot(0);
		
		player.setLevel(0);
		player.setExp(0F);
		
		if (UtilSkull.isPlayerHead(UtilSkull.getSkullData(getEntity())))
		{
			
			final ItemStack skull = ItemStackFactory.Instance.CreateStack(Material.SKULL_ITEM.getId(), (byte) 3, 1, C.cGreen + C.Bold + GetName());
			SkullMeta meta = (SkullMeta) skull.getItemMeta();
			meta.setOwner(UtilSkull.getPlayerHeadName(getEntity()));
			skull.setItemMeta(meta);

			Bukkit.getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					player.getInventory().setItem(8, skull);
				}
			}, 4);
		}
		else
		{
			player.getInventory().setItem(8, ItemStackFactory.Instance.CreateStack(Material.SKULL_ITEM.getId(), (byte) UtilSkull.getSkullData(getEntity()), 1, C.cGreen + C.Bold + GetName()));
		}
		
		giveItems(player);
	}
	
	protected abstract void giveItems(Player player);
}
