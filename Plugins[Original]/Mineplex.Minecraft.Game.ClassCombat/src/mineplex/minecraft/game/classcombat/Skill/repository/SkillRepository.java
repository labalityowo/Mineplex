package mineplex.minecraft.game.classcombat.Skill.repository;

import java.util.List;

import com.google.gson.reflect.TypeToken;

import mineplex.minecraft.game.classcombat.Skill.repository.token.SkillToken;
import mineplex.core.database.MinecraftRepository;
import mineplex.serverdata.database.DBPool;

public class SkillRepository extends MinecraftRepository
{
	public SkillRepository()
	{
		super(DBPool.getAccount());
	}

	public List<SkillToken> GetSkills(List<SkillToken> skills)
	{
		return handleSyncMSSQLCall("Dominate/GetSkills", skills, new TypeToken<List<SkillToken>>(){}.getType());
	}
}
