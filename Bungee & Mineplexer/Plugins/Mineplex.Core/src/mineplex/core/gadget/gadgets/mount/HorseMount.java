package mineplex.core.gadget.gadgets.mount;

import java.lang.reflect.Field;
import java.util.Iterator;

import net.minecraft.server.v1_8_R3.EntityLiving;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;

public class HorseMount extends Mount<SingleEntityMountData<Horse>>
{

	protected static Field JUMP_FIELD;

	static
	{
		try
		{
			JUMP_FIELD = EntityLiving.class.getDeclaredField("aY");
			JUMP_FIELD.setAccessible(true);
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
	}

	private static final int MIN_OFFSET_SQUARED = 16;
	private static final int MAX_OFFSET_SQUARED = 400;

	protected final Color _color;
	protected final Style _style;
	protected final Variant _variant;
	protected final double _jump;
	protected final Material _armor;

	public HorseMount(GadgetManager manager, String name, String[] desc, int cost, Material material, byte materialData, Color color, Style style, Variant variant, double jump, Material armor)
	{
		super(manager, name, desc, cost, material, materialData);

		_color = color;
		_style = style;
		_variant = variant;
		_jump = jump;
		_armor = armor;
	}
	
	@EventHandler
	public void updateHorse(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Iterator<Player> activeIterator = _active.keySet().iterator();
		
		while (activeIterator.hasNext())
		{
			Player player = activeIterator.next();
			Horse horse = _active.get(player).getEntity();
			
			//Invalid (dead)
			if (!horse.isValid())
			{
				horse.remove();
				activeIterator.remove();
				continue;
			}
			
			//Move
			double dist = UtilMath.offsetSquared(player, horse);

			if (dist > MAX_OFFSET_SQUARED)
			{
				horse.teleport(player);
			}
			else if (dist > MIN_OFFSET_SQUARED)
			{
				UtilEnt.CreatureMove(horse, player.getLocation().add(0, 0.5, 0), 1.5F);
			}
		}
	}

	@Override
	public SingleEntityMountData<Horse> spawnMount(Player player)
	{
		Horse horse = UtilVariant.spawnHorse(player.getLocation(), _variant);
		horse.setAdult();
		horse.setAgeLock(true);
		horse.setColor(_color);
		horse.setStyle(_style);
		horse.setOwner(player);
		horse.setMaxDomestication(1);
		horse.setJumpStrength(_jump);
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));

		if (horse.getVariant() == Variant.MULE)
		{
			horse.setCarryingChest(true);
		}

		if (_armor != null)
		{
			horse.getInventory().setArmor(new ItemStack(_armor));
		}

		horse.setCustomName(player.getName() + "'s " + getName());
		horse.setCustomNameVisible(true);

		return new SingleEntityMountData<>(player, horse);
	}

	@Override
	protected void setPassenger(Player player, Entity clicked, PlayerInteractEntityEvent event)
	{
		event.setCancelled(false);
	}
}
