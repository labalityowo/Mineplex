package mineplex.game.clans.clans.mounts;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mineplex.core.common.Pair;
import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.clans.mounts.Mount.MountType;

public class MountOwnerData
{
	private Map<MountToken, MountStatToken> _mounts = new LinkedHashMap<>();
	
	public List<Pair<MountToken, MountStatToken>> getOwnedMounts(boolean onlyInitialized)
	{
		return _mounts.entrySet().stream().filter(entry -> onlyInitialized ? entry.getKey().Id != -1 : true).map(entry -> Pair.create(entry.getKey(), entry.getValue())).collect(Collectors.toList());
	}
	
	public List<Pair<MountToken, MountStatToken>> getOwnedMounts(boolean onlyInitialized, MountType type)
	{
		return _mounts.entrySet().stream().filter(entry -> (onlyInitialized ? entry.getKey().Id != -1 : true) && type == entry.getKey().Type).map(entry -> Pair.create(entry.getKey(), entry.getValue())).collect(Collectors.toList());
	}
	
	public MountStatToken getStatToken(MountToken mountToken)
	{
		return _mounts.get(mountToken);
	}
	
	public boolean ownsMount(MountType type)
	{
		return getAmountOwned(type) > 0;
	}
	
	public long getAmountOwned(MountType type)
	{
		return _mounts.keySet().stream().filter(token -> token.Type == type).count();
	}
	
	public void acceptLoad(MountToken token, MountStatToken statToken)
	{
		_mounts.put(token, statToken);
	}
	
	public Pair<MountToken, MountStatToken> grantMount(MountType type, int speed, int jump, int strength)
	{
		MountToken token = new MountToken();
		token.Type = type;
		MountStatToken statToken = new MountStatToken();
		statToken.JumpStars = jump;
		statToken.SpeedStars = speed;
		statToken.StrengthStars = strength;
		_mounts.put(token, statToken);
		
		return Pair.create(token, statToken);
	}
	
	public Pair<MountToken, MountStatToken> grantMount(MountType type)
	{
		int speed = UtilMath.rRange(1, 3);
		int jump = UtilMath.rRange(1, 3);
		int strength = UtilMath.rRange(1, 3);
		
		return grantMount(type, speed, jump, strength);
	}
	
	public Integer[] removeMounts(MountType type)
	{
		Integer[] array = _mounts.keySet().stream().filter(token->token.Type == type).map(token->token.Id).toArray(size->new Integer[size]);
		_mounts.keySet().removeIf(token->token.Type == type);
		
		return array;
	}
	
	public void removeMount(int id)
	{
		_mounts.keySet().removeIf(token->token.Id == id);
	}
}