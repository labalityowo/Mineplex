package mineplex.core.common.api.enderchest;

import java.io.File;
import java.io.IOException;

import mineplex.core.common.api.ApiHost;
import mineplex.core.common.api.ApiWebCall;
import mineplex.core.common.util.ZipUtil;
import mineplex.core.common.timing.TimingManager;

/**
 * Load worlds from the `enderchest` microservice
 */
public class EnderchestWorldLoader
{
	private static final String TIMINGS_PREFIX = "Enderchest LoadMap::";
	private ApiWebCall _webCall;

	public EnderchestWorldLoader()
	{
		String url = "http://" + ApiHost.getEnderchestService().getHost() + ":" + ApiHost.getEnderchestService().getPort() + "/";
		_webCall = new ApiWebCall(url);
	}

	public void loadMap(String mapType, String folder) throws HashesNotEqualException, IOException
	{
		TimingManager.start(TIMINGS_PREFIX + "DownloadMap");
		String fileName = mapType + "_map.zip";
		File f = _webCall.getFile("map/" + mapType + "/next", fileName);
		TimingManager.stop(TIMINGS_PREFIX + "DownloadMap");

		TimingManager.start(TIMINGS_PREFIX + "CreateFolders");
		new File(folder).mkdir();
		new File(folder + java.io.File.separator + "region").mkdir();
		new File(folder + java.io.File.separator + "data").mkdir();
		TimingManager.stop(TIMINGS_PREFIX + "CreateFolders");

		TimingManager.start(TIMINGS_PREFIX + "UnzipToDirectory");
		ZipUtil.UnzipToDirectory(f.getAbsolutePath(), folder);
		TimingManager.stop(TIMINGS_PREFIX + "UnzipToDirectory");

		TimingManager.start(TIMINGS_PREFIX + "DeleteZip");
		f.delete();
		TimingManager.stop(TIMINGS_PREFIX + "DeleteZip");
	}
}
