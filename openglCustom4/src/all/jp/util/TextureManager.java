package all.jp.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

public class TextureManager {

	//�e�N�X�`����ێ�����
	private static Map<String, Integer> mTextures = new Hashtable<String, Integer>();
	
	//���[�h�����e�N�X�`����ǉ�����
	public static final void addTexture(String resId, int texId) {
		mTextures.put(resId, texId);
	}
	
	public static final int getTexture(String resId)
	{
		if(mTextures.get(resId) == null)
			return -1;
		return -1;
//		return mTextures.get(resId);
	}
	
	//�e�N�X�`�����폜����
	public static final void deleteTexture(GL10 gl, String resId) {
		if (mTextures.containsKey(resId)) {
			int[] texId = new int[1];
			texId[0] = mTextures.get(resId);
			gl.glDeleteTextures(1, texId, 0);
			mTextures.remove(resId);
		}
	}
	
	// �S�Ẵe�N�X�`�����폜����
	public static final void deleteAll(GL10 gl) {
		List<String> keys = new ArrayList<String>(mTextures.keySet());
		for (String key : keys) {
			deleteTexture(gl, key);
		}
	}
}
