package mineplex.minecraft.game.classcombat.Class.repository;

import mineplex.core.database.MinecraftRepository;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;
import mineplex.serverdata.database.DBPool;

public class ClassRepository extends MinecraftRepository
{
	public ClassRepository()
	{
		super(DBPool.getAccount());
	}

	public void SaveCustomBuild(CustomBuildToken token)
	{
		handleAsyncMSSQLCall("PlayerAccount/SaveCustomBuild", token);
	}
}
