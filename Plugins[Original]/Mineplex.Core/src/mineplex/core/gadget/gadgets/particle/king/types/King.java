package mineplex.core.gadget.gadgets.particle.king.types;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class King
{

	private Player _king;
	private List<Peasant> _peasants = new ArrayList<>();

	public King(Player king)
	{
		_king = king;
	}

	public Player getKing()
	{
		return _king;
	}

	public int amountOfPeasants()
	{
		return _peasants.size();
	}

	 public void addPeasant(Peasant peasant)
	 {
	 	_peasants.add(peasant);
	 }

	 public void removePeasant(Peasant peasant)
	 {
	 	_peasants.remove(peasant);
	 }

	 public boolean hasPeasant(Peasant peasant)
	 {
	 	return _peasants.contains(peasant);
	 }

	 public void clearPeasants()
	 {
	 	_peasants.forEach(Peasant::removeKing);
	 	_peasants.clear();
	 }
}

