package mineplex.core.boosters;

import mineplex.core.common.api.ApiResponse;

import java.util.Date;

/**
 * @author Shaun Bennett
 */
public class BoosterApiResponse extends ApiResponse
{
	private Date startTime;

	public Date getStartTime()
	{
		return startTime;
	}

	@Override
	public String toString()
	{
		return "BoosterApiResponse{" +
				"startTime='" + startTime + '\'' +
				"} " + super.toString();
	}
}
