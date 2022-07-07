package mineplex.core.newnpc;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.skin.SkinData;
import mineplex.core.utils.UtilGameProfile;

public class NPCBuilder
{

	private EntityType _type;
	private String _name;
	private String _metadata;
	private boolean _useSkin;

	public void build(Player player, NewNPCManager manager)
	{
		Location location = player.getLocation();
		PlayerInventory inventory = player.getInventory();
		Material inHand = inventory.getItemInHand() == null ? null : inventory.getItemInHand().getType();
		byte inHandData = inventory.getItemInHand() == null ? 0 : inventory.getItemInHand().getData().getData();
		Material helmet = inventory.getHelmet() == null ? null : inventory.getHelmet().getType();
		Material chestplate = inventory.getChestplate() == null ? null : inventory.getChestplate().getType();
		Material leggings = inventory.getLeggings() == null ? null : inventory.getLeggings().getType();
		Material boots = inventory.getBoots() == null ? null : inventory.getBoots().getType();

		NPC npc;

		if (_type == EntityType.PLAYER)
		{
			SkinData data;

			if (_useSkin)
			{
				data = SkinData.constructFromGameProfile(UtilGameProfile.getGameProfile(player), true, true);
			}
			else
			{
				data = SkinData.STEVE;
			}

			npc = new PlayerNPC(
					-1,
					_name,
					new ArrayList<>(2),
					location,
					inHand,
					inHandData,
					helmet,
					chestplate,
					leggings,
					boots,
					_metadata,
					data.getProperty().getValue(),
					data.getProperty().getSignature()
			);
		}
		else
		{
			npc = new StoredNPC(
					-1,
					_type,
					_name,
					new ArrayList<>(2),
					location,
					inHand,
					inHandData,
					helmet,
					chestplate,
					leggings,
					boots,
					_metadata
			);
		}

		manager.addNPC(npc, true);
	}

	public void setType(EntityType type)
	{
		_type = type;
	}

	public EntityType getType()
	{
		return _type;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}

	public void setMetadata(String metadata)
	{
		_metadata = metadata;
	}

	public String getMetadata()
	{
		return _metadata;
	}

	public void setUseSkin(boolean useSkin)
	{
		_useSkin = useSkin;
	}
}
