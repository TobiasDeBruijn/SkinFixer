package dev.array21.skinfixer.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class StorageManifest {
	
	public SkinData[] skinData;
	
	public void addSkinData(SkinData sd) {
		List<SkinData> skinDataList = new ArrayList<>(Arrays.asList(this.skinData));
		skinDataList.removeIf(x -> sd.playerUuid == sd.playerUuid);
		skinDataList.add(sd);
	}
	
	public SkinData getForPlayer(UUID player) {
		for(SkinData sd : this.skinData) {
			if(sd.playerUuid == player.toString()) {
				return sd;
			}
		}
		
		return null;
	}
}
