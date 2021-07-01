package dev.array21.skinfixer.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class StorageManifest {
	
	public SkinData[] skinData;
	
	public void addSkinData(SkinData sd) {
		List<SkinData> skinDataList = new ArrayList<>(Arrays.asList(this.skinData));
		skinDataList.removeIf(x -> x.playerUuid.equals(sd.playerUuid));
		skinDataList.add(sd);
		this.skinData = skinDataList.toArray(new SkinData[0]);
	}
	
	public void deleteForPlayer(UUID player) {
		List<SkinData> skinDataList = new ArrayList<>(Arrays.asList(this.skinData));
		skinDataList.removeIf(x -> x.playerUuid.equals(player.toString()));
		this.skinData = skinDataList.toArray(new SkinData[0]);
	}
	
	public SkinData getForPlayer(UUID player) {
		for(SkinData sd : this.skinData) {
			if(sd.playerUuid.equals(player.toString())) {
				return sd;
			}
		}
		return null;
	}
}
