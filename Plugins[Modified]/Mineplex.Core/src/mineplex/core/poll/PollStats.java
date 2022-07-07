package mineplex.core.poll;

/**
 * Created by Shaun on 8/26/2014.
 */
public class PollStats
{
	private int _aCount;
	private int _bCount;
	private int _cCount;
	private int _dCount;

	public int getTotal()
	{
		return _aCount + _bCount + _cCount + _dCount;
	}

	public int getACount()
	{
		return _aCount;
	}

	public int getBCount()
	{
		return _bCount;
	}

	public int getCCount()
	{
		return _cCount;
	}

	public int getDCount()
	{
		return _dCount;
	}

	public double getAPercent()
	{
		return (double) _aCount / getTotal();
	}

	public double getBPercent()
	{
		return (double) _bCount / getTotal();
	}

	public double getCPercent()
	{
		return (double) _cCount / getTotal();
	}

	public double getDPercent()
	{
		return (double) _dCount / getTotal();
	}

	public void setACount(int aCount)
	{
		_aCount = aCount;
	}

	public void setBCount(int bCount)
	{
		_bCount = bCount;
	}

	public void setCCount(int cCount)
	{
		_cCount = cCount;
	}

	public void setDCount(int dCount)
	{
		_dCount = dCount;
	}
}