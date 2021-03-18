package nl.thedutchmc.SkinFixer.gson;

public class GetSkinResponse {

	private Data data;
	
	public class Data {
		
		private Texture texture;
		
		public Texture getTexture() {
			return texture;
		}
		
		public class Texture {
			private String value;
			private String signature;
			
			public String getSignature() {
				return signature;
			}
			
			public String getValue() {
				return value;
			}
		}

	}

	public Data getData() {
		return data;
	}
	
}
