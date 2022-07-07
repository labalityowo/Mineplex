package mineplex.core.particleeffects;

import java.awt.Color;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;

public class RainbowTrailEffect extends Effect
{

	private HashSet<Item> _items;

	private long _count, _jumpingTimer = 0;
	private boolean _isJumping = false;
	private Entity _entity;

	private Color _red = new Color(255, 0, 0);
	private Color _orange = new Color(255, 127, 0);
	private Color _yellow = new Color(255, 255, 0);
	private Color _green = new Color(0, 255, 0);
	private Color _blue = new Color(0, 0, 255);
	private Color _indigo = new Color(75, 0, 130);
	private Color _violet = new Color(143, 0, 255);

	private Color _color = _red;

	public RainbowTrailEffect(Entity entity, HashSet<Item> items)
	{
		super(-1, new EffectLocation(entity));
		_entity = entity;
		_items = items;
	}

	@Override
	public void runEffect()
	{
		if (!_entity.isValid() || _entity.isDead())
		{
			stop();
			return;
		}
		ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
				new DustSpellColor(_color), _effectLocation.getLocation().clone().add(0, .5, 0));
		for (int i = 0; i < 7; i++)
		{
			coloredParticle.setLocation(_effectLocation.getLocation().clone().add(0, .5, 0));
			coloredParticle.display();
			if (_isJumping)
			{
				coloredParticle.setLocation(_effectLocation.getLocation().clone().add(.25, .5, 0));
				coloredParticle.display();
				coloredParticle.setLocation(_effectLocation.getLocation().clone().add(0, .5, .25));
				coloredParticle.display();
			}
		}
		if (_isJumping)
		{
			_jumpingTimer++;
			if (_jumpingTimer >= 30)
			{
				setJumping(false);
				_jumpingTimer = 0;
			}
		}
		_count++;
		if (_count % 5 == 0)
		{
			if (_color.equals(_red))
				_color = _orange;
			else if (_color.equals(_orange))
				_color = _yellow;
			else if (_color.equals(_yellow))
				_color = _green;
			else if (_color.equals(_green))
				_color = _blue;
			else if (_color.equals(_blue))
				_color = _indigo;
			else if (_color.equals(_indigo))
				_color = _violet;
			else
				_color = _red;
		}
		if (_count == Long.MAX_VALUE - 1)
			_count = 0;
	}

	public void setJumping(boolean jumping)
	{
		_isJumping = jumping;
		if (_isJumping)
		{
			ItemStack itemStack = new ItemStack(Material.GOLD_INGOT);
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName("DROPPED" + System.currentTimeMillis() + UtilMath.random.nextInt());
			itemStack.setItemMeta(itemMeta);
			Item gold = _entity.getWorld().dropItem(_entity.getLocation().add(0.5, 1.5, 0.5), itemStack);
			_items.add(gold);
			gold.setVelocity(new Vector((Math.random()-0.5)*0.3, Math.random()-0.4, (Math.random()-0.5)*0.3));
		}
	}

}
