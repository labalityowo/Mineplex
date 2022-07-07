package mineplex.core.itemstack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemBuilder
{

	private static ArrayList<String> split(String string, int maxLength)
	{
		String[] split = string.split(" ");
		string = "";
		ArrayList<String> newString = new ArrayList<String>();
		for (int i = 0; i < split.length; i++)
		{
			string += (string.length() == 0 ? "" : " ") + split[i];
			if (ChatColor.stripColor(string).length() > maxLength)
			{
				newString
						.add((newString.size() > 0 ? ChatColor.getLastColors(newString.get(newString.size() - 1)) : "") + string);
				string = "";
			}
		}
		if (string.length() > 0)
			newString.add((newString.size() > 0 ? ChatColor.getLastColors(newString.get(newString.size() - 1)) : "") + string);
		return newString;
	}

	private int _amount;
	private Color _color;
	private short _data;
	private short _durability;
	private final Map<Enchantment, Integer> _enchants = new HashMap<>();
	private final List<String> _lore = new ArrayList<>();
	private Material _mat;
	private String _title = null;
	private boolean _unbreakable;
	private boolean _glow;
	private String _playerHeadName = null;
	private Set<ItemFlag> _itemFlags = new HashSet<>();
	private List<PotionEffect> _potionEffects = new ArrayList<>();

	public ItemBuilder(ItemStack item)
	{
		this(item.getType(), item.getDurability());
		_amount = item.getAmount();
		_enchants.putAll(item.getEnchantments());

		if (item.hasItemMeta())
		{
			ItemMeta meta = item.getItemMeta();

			if (meta.hasDisplayName())
			{
				_title = meta.getDisplayName();
			}

			if (meta.hasLore())
			{
				_lore.addAll(meta.getLore());
			}

			if (meta instanceof LeatherArmorMeta)
			{
				setColor(((LeatherArmorMeta) meta).getColor());
			}
			else if (meta instanceof PotionMeta)
			{
				for (PotionEffect effect : ((PotionMeta) meta).getCustomEffects())
				{
					addPotionEffect(effect);
				}
			}

			_itemFlags.addAll(meta.getItemFlags());

			_unbreakable = meta.spigot().isUnbreakable();
			_durability = item.getDurability();
		}
	}

	public ItemBuilder(Material mat)
	{
		this(mat, 1);
	}

	public ItemBuilder(Material mat, int amount)
	{
		this(mat, amount, (short) 0);
	}

	public ItemBuilder(Material mat, int amount, short data)
	{
		_mat = mat;
		_amount = amount;
		_data = data;
		_durability = 0;
	}

	public ItemBuilder(Material mat, short data)
	{
		this(mat, 1, data);
	}

	public ItemBuilder setDurability(short durability)
	{
		_durability = durability;
		
		return this;
	}
	
	public Set<ItemFlag> getItemFlags()
	{
		return _itemFlags;
	}

	public ItemBuilder addItemFlags(ItemFlag... flags)
	{
		getItemFlags().addAll(Arrays.asList(flags));

		return this;
	}

	public ItemBuilder setItemFlags(ItemFlag... flags)
	{
		getItemFlags().clear();
		addItemFlags(flags);

		return this;
	}

	public ItemBuilder setGlow(boolean glow)
	{
		_glow = glow;

		return this;
	}

	public ItemBuilder setItemFlags(Collection<ItemFlag> flags)
	{
		getItemFlags().clear();
		addItemFlags(flags.toArray(new ItemFlag[0]));

		return this;
	}

	public ItemBuilder setHideInfo(boolean hideInfo)
	{
		if (hideInfo)
		{
			for (ItemFlag flag : ItemFlag.values())
			{
				getItemFlags().add(flag);
			}
		}
		else
		{
			getItemFlags().clear();
		}

		return this;
	}

	public ItemBuilder addEnchantment(Enchantment enchant, int level)
	{
		if (_enchants.containsKey(enchant))
		{
			_enchants.remove(enchant);
		}

		_enchants.put(enchant, level);

		return this;
	}

	public ItemBuilder addLore(String... lores)
	{
		for (String lore : lores)
		{
			if (lore == null) continue;
			_lore.add(C.cGray + lore);
		}

		return this;
	}

	public ItemBuilder setLore(String... lores)
	{
		_lore.clear();
		_lore.addAll(Arrays.asList(lores));

		return this;
	}

	public ItemBuilder addLore(String lore, int maxLength)
	{
		_lore.addAll(split(lore, maxLength));

		return this;
	}

	public ItemBuilder addLores(List<String> lores)
	{
		_lore.addAll(lores);

		return this;
	}

	public ItemBuilder addLores(List<String> lores, int maxLength)
	{
		for (String lore : lores)
		{
			addLore(lore, maxLength);
		}

		return this;
	}

	public ItemBuilder addLores(String[] description, int maxLength)
	{
		return addLores(Arrays.asList(description), maxLength);
	}

	public ItemStack build()
	{
		Material mat = _mat;

		if (mat == null)
		{
			mat = Material.AIR;
			Bukkit.getLogger().warning("Null material!");
		}
		else if (mat == Material.AIR)
		{
			Bukkit.getLogger().warning("Air material!");
		}

		ItemStack item = new ItemStack(mat, _amount, _data);
		ItemMeta meta = item.getItemMeta();

		if (meta != null)
		{
			if (_title != null)
			{
				meta.setDisplayName(_title);
			}
			if (!_lore.isEmpty())
			{
				meta.setLore(_lore);
			}
			if (meta instanceof LeatherArmorMeta && _color != null)
			{
				((LeatherArmorMeta) meta).setColor(_color);
			}
			else if (meta instanceof SkullMeta && _playerHeadName != null)
			{
				((SkullMeta) meta).setOwner(_playerHeadName);
			}
			else if (meta instanceof FireworkEffectMeta && _color != null)
			{
				((FireworkEffectMeta) meta).setEffect(FireworkEffect.builder().withColor(_color).build());
			}
			else if (meta instanceof BannerMeta)
			{
				((BannerMeta) meta).setBaseColor(DyeColor.getByColor(_color));
			}
			else if (meta instanceof PotionMeta)
			{
				PotionMeta potionMeta = (PotionMeta) meta;

				for (PotionEffect effect : _potionEffects)
				{
					potionMeta.addCustomEffect(effect, true);
				}

				if (!_potionEffects.isEmpty())
				{
					potionMeta.setMainEffect(_potionEffects.get(0).getType());
				}

				meta = potionMeta;
			}

			meta.addItemFlags(getItemFlags().toArray(new ItemFlag[0]));
			meta.spigot().setUnbreakable(isUnbreakable());
			item.setItemMeta(meta);
		}

		item.addUnsafeEnchantments(_enchants);
		if (_glow) item.addEnchantment(UtilInv.getDullEnchantment(), 1);
		if (_durability != 0) item.setDurability(_durability); 
		
		return item;
	}

	@Override
	public ItemBuilder clone()
	{
		ItemBuilder newBuilder = new ItemBuilder(_mat);

		newBuilder.setTitle(_title);
		
		_lore.forEach(newBuilder::addLore);
		
		for (Map.Entry<Enchantment, Integer> entry : _enchants.entrySet())
		{
			newBuilder.addEnchantment(entry.getKey(), entry.getValue());
		}

		newBuilder.setColor(_color);

		newBuilder.setDurability(_durability);
		
		newBuilder.setData(_data);
		
		newBuilder.setAmount(_amount);
		
		newBuilder.setUnbreakable(_unbreakable);
		
		newBuilder.setGlow(_glow);
		
		newBuilder.setItemFlags(_itemFlags);
		
		newBuilder.setPlayerHead(_playerHeadName);

		for (PotionEffect potionEffect : _potionEffects)
		{
			newBuilder.addPotionEffect(potionEffect);
		}
		
		return newBuilder;
	}

	public Map<Enchantment, Integer> getAllEnchantments()
	{
		return _enchants;
	}

	public Color getColor()
	{
		return _color;
	}

	public short getData()
	{
		return _data;
	}

	public int getEnchantmentLevel(Enchantment enchant)
	{
		return _enchants.get(enchant);
	}

	public List<String> getLore()
	{
		return _lore;
	}

	public String getTitle()
	{
		return _title;
	}

	public Material getType()
	{
		return _mat;
	}

	public boolean hasEnchantment(Enchantment enchant)
	{
		return _enchants.containsKey(enchant);
	}

	public boolean isUnbreakable()
	{
		return _unbreakable;
	}

	public ItemBuilder setAmount(int amount)
	{
		_amount = amount;

		return this;
	}

	public ItemBuilder setColor(Color color)
	{
		_color = color;

		return this;
	}

	public ItemBuilder setData(short newData)
	{
		_data = newData;

		return this;
	}

	public ItemBuilder setRawTitle(String title)
	{
		_title = title;

		return this;
	}

	public ItemBuilder setTitle(String title)
	{
		if (title == null)
		{
			_title = null;
		}
		else
		{
			_title = ((title.length() > 2 && ChatColor.getLastColors(title.substring(0, 2)).length() == 0) ? ChatColor.WHITE : "") + title;
		}

		return this;
	}

	public ItemBuilder setTitle(String title, int maxLength)
	{
		if (title != null && ChatColor.stripColor(title).length() > maxLength)
		{
			ArrayList<String> lores = split(title, maxLength);

			for (int i = 1; i < lores.size(); i++)
			{
				_lore.add(lores.get(i));
			}

			title = lores.get(0);
		}

		setTitle(title);

		return this;
	}

	public ItemBuilder setType(Material mat)
	{
		_mat = mat;

		return this;
	}

	public ItemBuilder setUnbreakable(boolean setUnbreakable)
	{
		_unbreakable = setUnbreakable;

		return this;
	}

	public ItemBuilder setPlayerHead(String playerName)
	{
		_playerHeadName = playerName;

		return this;
	}

	public ItemBuilder addPotionEffect(PotionEffect effect)
	{
		_potionEffects.add(effect);
		return this;
	}
}