package mineplex.core.antispam.repository;

import com.google.gson.Gson;
import mineplex.core.antispam.AntiSpamApiResponse;
import mineplex.core.antispam.ChatPayload;
import mineplex.core.common.api.ApiEndpoint;
import mineplex.core.common.api.ApiHost;
import mineplex.core.common.api.ApiResponse;
import mineplex.core.thread.ThreadPool;

import java.util.Random;

/**
 * @author Shaun Bennett
 */
public class AntiSpamRepository extends ApiEndpoint
{
	public AntiSpamRepository()
	{
		super(ApiHost.getAntispamService(), "/chat");
	}

	public AntiSpamApiResponse sendMessage(String source, ChatPayload payload)
	{
		return getWebCall().post("/" + source, AntiSpamApiResponse.class, payload);
	}
}
