package nautilus.game.minekart.item;

import java.util.List;

import nautilus.game.minekart.kart.Kart;

public abstract class KartItemActive 
{
	public enum ActiveType
	{
		Behind,
		Orbit,
		Trail
	}
	
	private Kart _kart;
	private ActiveType _type;
	private List<KartItemEntity> _ents;
	
	public KartItemActive(KartItemManager manager, Kart kart, ActiveType type, List<KartItemEntity> ents)
	{
		_kart = kart;
		_type = type;
		_ents = ents;
		
		for (KartItemEntity item : ents)
			item.SetHost(this);
		
		kart.SetItemActive(this);
		manager.RegisterKartItem(this);
	}
	
	public Kart GetKart()
	{
		return _kart;
	}
	
	public ActiveType GetType()
	{
		return _type;
	}
	
	public List<KartItemEntity> GetEntities()
	{
		return _ents;
	}
	
	public abstract boolean Use();
}
