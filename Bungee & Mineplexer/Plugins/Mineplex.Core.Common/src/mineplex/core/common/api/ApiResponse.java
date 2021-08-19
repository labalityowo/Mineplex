package mineplex.core.common.api;

import java.util.Date;

/**
 * @author Shaun Bennett
 */
public class ApiResponse implements HttpStatusCode
{
	// These do not have _ prefix because of gson. Please do not add underscores!
	private int statusCode;
	private boolean success;
	private String error;

	public ApiResponse()
	{

	}

	public boolean isSuccess()
	{
		return success;
	}

	public String getError()
	{
		return error;
	}

	@Override
	public String toString()
	{
		return "ApiResponse{" +
				"success=" + success +
				", error='" + error + '\'' +
				'}';
	}

	@Override
	public int getStatusCode()
	{
		return statusCode;
	}

	@Override
	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
	}
}
