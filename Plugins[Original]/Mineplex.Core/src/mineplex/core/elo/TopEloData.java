package mineplex.core.elo;

/**
 * Elo Wrapper for the TopEloCommand
 */
public class TopEloData
{

    private String _name;
    private int _elo;

    public TopEloData(String name, int elo)
    {
        _name = name;
        _elo = elo;
    }

    public String getName() {
        return _name;
    }

    public int getElo() {
        return _elo;
    }

}
