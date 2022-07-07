package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityVillager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;

public class DisguiseVillager extends DisguiseAgeable
{

	private Profession _profession;

	public DisguiseVillager(Entity entity)
	{
		super(EntityType.VILLAGER, entity);
		DataWatcher.a(16, 0, EntityVillager.META_TYPE, 0);
	}

	public void setProfession(Profession profession)
	{
		_profession = profession;

		int id = profession.getId();
		DataWatcher.watch(16, id, EntityVillager.META_TYPE, id);
	}

	public Profession getProfession()
	{
		return _profession;
	}

	@Override
	protected String getHurtSound()
	{
		return "mob.villager.hit";
	}
}
