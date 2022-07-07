package nautilus.game.arcade.game.modules.antixray;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public interface AntiXrayService
{
	void setEnabled(boolean enabled);

	void setUpdateOnDamage(boolean updateOnDamage);

	void setEngineMode(int engineMode);

	void setInitialRadius(int initialRadius);

	void setUpdateRadius(int updateRadius);

	void setUseWorldsAsBlacklist(boolean useWorldsAsBlacklist);

	void setUseCache(boolean useCache);

	void setMaxLoadedCacheFiles(int maxLoadedCacheFiles);

	void clearCache();

	void setProximityHiderRate(int proximityHiderRate);

	void setProximityHiderDistance(int proximityHiderDistance);

	void setProximityHiderId(int proximityHiderId);

	void setProximityHiderEnd(int proximityHiderEnd);

	void setUseSpecialBlockForProximityHider(boolean useSpecialBlockForProximityHider);

	void setUseYLocationProximity(boolean useYLocationProximity);

	void setAirGeneratorMaxChance(int airGeneratorMaxChance);

	void setWorlds(List<String> worlds);

	void setUseProximityHider(boolean useProximityHider);

	void setAntiTexturePacksAndFreecam(boolean antiTexturePacksAndFreecam);

	void setDarknessHideBlocks(boolean darknessHideBlocks);

	void setObfuscateBlocks(List<Material> materials);

	void setRandomBlocks(List<Material> materials);

	void setDarknessBlocks(List<Material> materials);

	void setProximityBlocks(List<Material> materials);
}
