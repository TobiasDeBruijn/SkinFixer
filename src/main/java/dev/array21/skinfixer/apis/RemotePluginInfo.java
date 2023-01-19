package dev.array21.skinfixer.apis;

import com.google.gson.Gson;
import dev.array21.httplib.Http;
import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.apis.gson.RemoteInfoManifest;
import dev.array21.skinfixer.util.Utils;
import org.bukkit.Bukkit;

import java.io.IOException;

public class RemotePluginInfo {

    public static RemoteInfoManifest getManifest(SkinFixer plugin) {
        Http.ResponseObject response;
        try {
            response = new Http().makeRequest(Http.RequestMethod.GET, plugin.getConfigManifest().remotePluginInfoUrl, null, null, null, null);
        } catch (IOException e) {
            SkinFixer.logWarn("Failed to retrieve remote plugin info. This is fatal");
            SkinFixer.logWarn(Utils.getStackTrace(e));
            Bukkit.getPluginManager().disablePlugin(plugin);
            return null;
        }

        return new Gson().fromJson(response.getMessage(), RemoteInfoManifest.class);
    }
}
