package mineplex.core.newnpc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;

import com.mojang.authlib.GameProfile;

import mineplex.core.Managers;
import mineplex.core.common.skin.SkinData;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.hologram.Hologram;
import mineplex.serverdata.database.column.Column;
import mineplex.serverdata.database.column.ColumnVarChar;

public class PlayerNPC extends StoredNPC
{

	private static final DisguiseManager MANAGER = Managers.require(DisguiseManager.class);

	private SkinData _skinData;
	private DisguisePlayer _disguise;

	PlayerNPC(int id, String name, List<String> info, Location spawn, Material inHand, byte inHandData, Material helmet, Material chestplate, Material leggings, Material boots, String metadata, String skinValue, String skinSignature)
	{
		super(id, EntityType.ARMOR_STAND, name, info, spawn, inHand, inHandData, helmet, chestplate, leggings, boots, metadata);

		_skinData = skinValue != null && skinSignature != null ? new SkinData(skinValue, skinSignature) : null;
	}

	@Override
	public LivingEntity spawnEntity()
	{
		LivingEntity entity = super.spawnEntity();

		if (entity == null || _skinData == null)
		{
			return null;
		}

		GameProfile profile = new GameProfile(UUID.randomUUID(), SkinData.getUnusedSkullName());
		profile.getProperties().clear();
		profile.getProperties().put("textures", _skinData.getProperty());

		refreshDisguise(entity);
		getNameTag();

		return entity;
	}

	@Override
	public Hologram getNameTag()
	{
		if (!hasNameTag())
		{
			_disguise.getHologram().setText(_colouredName);
		}

		return _disguise.getHologram();
	}

	@Override
	public boolean hasNameTag()
	{
		return _disguise.hasHologram();
	}

	@Override
	public List<Column<?>> toDatabaseQuery()
	{
		List<Column<?>> columns = new ArrayList<>(super.toDatabaseQuery());

		columns.set(0, new ColumnVarChar("entity_type", 32, EntityType.PLAYER.name()));
		columns.set(16, new ColumnVarChar("skin_value", 400, _skinData.getProperty().getValue()));
		columns.set(17, new ColumnVarChar("skin_signature", 700, _skinData.getProperty().getSignature()));

		return columns;
	}

	private void refreshDisguise(LivingEntity entity)
	{
		if (entity == null)
		{
			return;
		}

		GameProfile profile = new GameProfile(UUID.randomUUID(), SkinData.getUnusedSkullName());
		profile.getProperties().clear();
		profile.getProperties().put("textures", _skinData.getProperty());

		DisguisePlayer player = new DisguisePlayer(entity, profile);

		EntityEquipment equipment = entity.getEquipment();
		player.setHeldItem(equipment.getItemInHand());
		player.setHelmet(equipment.getHelmet());
		player.setChestplate(equipment.getChestplate());
		player.setLeggings(equipment.getLeggings());
		player.setBoots(equipment.getBoots());

		if (_disguise != null)
		{
			MANAGER.undisguise(_disguise);
		}

		_disguise = player;

		// Ensure the entity is loaded before disguising
		MANAGER.runSyncLater(() -> MANAGER.disguise(player), 20);
	}

	public void setSkinData(SkinData skinData)
	{
		_skinData = skinData;
		refreshDisguise(_entity);
	}

	public DisguisePlayer getDisguise()
	{
		return _disguise;
	}
}
