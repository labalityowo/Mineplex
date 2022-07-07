package mineplex.core.gadget.persistence;

import org.bukkit.entity.Player;

import mineplex.core.common.util.BukkitFuture;
import mineplex.core.database.PlayerKeyValueRepository;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.GameModifierGadget;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.gadget.types.WeaponNameGadget;

public class UserGadgetPersistence
{

	private final GadgetManager _manager;
	private final PlayerKeyValueRepository<String> _repository;
	private boolean _enabled;

	public UserGadgetPersistence(GadgetManager manager)
	{
		_manager = manager;
		_repository = new PlayerKeyValueRepository<>("gadgets", String.class);
		_enabled = true;
	}

	public void setEnabled(boolean enabled)
	{
		_enabled = enabled;
	}

	public void load(Player player)
	{
		if (!_enabled)
		{
			return;
		}

		_repository.getAll(player.getUniqueId()).thenCompose(BukkitFuture.accept(values ->
		{
			if (player.isOnline())
			{
				for (Gadget gadget : _manager.getAllGadgets())
				{
					if (!gadget.ownsGadget(player))
					{
						continue;
					}

					GadgetType gadgetType = gadget.getGadgetType();
					switch (gadgetType)
					{
						case MUSIC_DISC:
						case ITEM:
						case MORPH:
						case BALLOON:
						case MOUNT:
							break;
						case COSTUME:
							OutfitGadget outfitGadget = (OutfitGadget) gadget;
							String key = "activeCostume" + outfitGadget.getSlot().getDatabaseKey();
							if (!values.containsKey(key))
								continue;
							if (values.get(key).equals(gadget.getName()))
							{
								gadget.enable(player, false);
							}
							break;
						case GAME_MODIFIER:
							GameModifierGadget gameModifierGadget = (GameModifierGadget) gadget;
							GameCosmeticCategory category = gameModifierGadget.getCategory();

							if (category.isAllowingMultiple())
							{
								if (!values.containsKey(gadget.getName()))
									continue;
								if (values.get(gadget.getName()).equals("enabled"))
								{
									gadget.enable(player, false);
								}
							}
							else
							{
								key = "active" + category.getType().getName().replace(" ", "") + category.getCategoryName().replace(" ", "");
								if (!values.containsKey(key))
									continue;
								if (values.get(key).equals(gadget.getName()))
								{
									gadget.enable(player, false);
								}
							}
							break;
						case WEAPON_NAME:
							WeaponNameGadget weaponNameGadget = (WeaponNameGadget) gadget;
							key = weaponNameGadget.getGadgetType().getDatabaseKey() + weaponNameGadget.getWeaponNameType().getWeaponType().getId();

							if (gadget.getName().equals(values.get(key)))
							{
								gadget.enable(player, false);
							}
							break;
						default:
							if (!values.containsKey(gadgetType.getDatabaseKey()))
							{
								continue;
							}

							if (values.get(gadgetType.getDatabaseKey()).equals(gadget.getName()))
							{
								gadget.enable(player, false);
							}
							break;
					}
				}
			}
		}));
	}

	public void save(Player player, Gadget gadget, boolean enabled)
	{
		String value = "disabled", key;
		GadgetType gadgetType = gadget.getGadgetType();

		switch (gadgetType)
		{
			case MUSIC_DISC:
			case ITEM:
			case MORPH:
			case BALLOON:
			case MOUNT:
				return;
			case COSTUME:
				OutfitGadget outfitGadget = (OutfitGadget) gadget;
				key = "activeCostume" + outfitGadget.getSlot().getDatabaseKey();

				if (enabled)
				{
					value = outfitGadget.getName();
				}
				break;
			case GAME_MODIFIER:
				GameModifierGadget gameModifierGadget = (GameModifierGadget) gadget;
				GameCosmeticCategory category = gameModifierGadget.getCategory();

				if (category.isAllowingMultiple())
				{
					key = gameModifierGadget.getName();
					if (enabled)
					{
						value = "enabled";
					}
				}
				else
				{
					key = "active" + category.getType().getName().replace(" ", "") + category.getCategoryName().replace(" ", "");
					if (enabled)
					{
						value = gameModifierGadget.getName();
					}
				}
				break;
			case WEAPON_NAME:
				WeaponNameGadget weaponNameGadget = (WeaponNameGadget) gadget;
				key = weaponNameGadget.getGadgetType().getDatabaseKey() + weaponNameGadget.getWeaponNameType().getWeaponType().getId();

				if (enabled)
				{
					value = gadget.getName();
				}
				break;
			default:
				key = gadgetType.getDatabaseKey();
				if (enabled)
				{
					value = gadget.getName();
				}
		}

		if (!enabled)
		{
			_repository.remove(player.getUniqueId(), key);
		}
		else
		{
			_repository.put(player.getUniqueId(), key, value);
		}
	}

}
