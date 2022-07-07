package mineplex.core.antihack;

import com.mineplex.anticheat.checks.Check;
import com.mineplex.anticheat.checks.CheckManager;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Locally cached information about a user's max violations and total number of alerts for each
 * check type.
 * <p>
 * Instances of this have no concept of identity i.e. account id is not tracked by this.
 */
public class ViolationLevels
{
    private final TObjectIntMap<Class<? extends Check>> _maxViolations;

    private final TObjectIntMap<Class<? extends Check>> _totalAlerts;

    private final TObjectIntMap<Class<? extends Check>> _lastBan;

    public ViolationLevels()
    {
        _maxViolations = new TObjectIntHashMap<>(CheckManager.AVAILABLE_CHECKS.size());
        _totalAlerts = new TObjectIntHashMap<>(CheckManager.AVAILABLE_CHECKS.size());
        _lastBan = new TObjectIntHashMap<>(CheckManager.AVAILABLE_CHECKS.size());
    }
    
    public void updateMaxViolations(Class<? extends Check> check, int violationLevel)
    {
        if (violationLevel > _maxViolations.get(check))
        {
            _maxViolations.put(check, violationLevel);
        }
    }

	public void updateMaxViolationsSinceLastBan(Class<? extends Check> check, int violationLevel)
	{
		if (violationLevel > _lastBan.get(check))
		{
			_lastBan.put(check, violationLevel);
		}
	}

    public void incrementAlerts(Class<? extends Check> check)
    {
        int cur = _totalAlerts.get(check);

        setTotalAlerts(check, cur + 1);
    }
    
    public void setTotalAlerts(Class<? extends Check> check, int totalAlerts)
    {
        _totalAlerts.put(check, totalAlerts);
    }

    public int getTotalAlertsForCheck(Class<? extends Check> check)
    {
        if (_totalAlerts.containsKey(check))
        {
            return _totalAlerts.get(check);
        }
        
        return -1;
    }

    public int getMaxViolationsForCheck(Class<? extends Check> check)
    {
        if (_maxViolations.containsKey(check))
        {
            return _maxViolations.get(check);
        }
        
        return -1;
    }

	public int getLastBanViolationsForCheck(Class<? extends Check> check)
	{
		if (_lastBan.containsKey(check))
		{
			return _lastBan.get(check);
		}

		return -1;
	}
}
