package all.jp.util;

import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import all.jp.MainActivity;
import android.app.Activity;

public class Global {
	
	// MainActivity
	public static MainActivity mainActivity;
	public static Activity activity;
	
	//Game setting.
	public static boolean isAnime = true;
	public static boolean isSound = true;
	
	public static boolean isES11;//ES 1.1�ł����true
	
	//GL�R���e�L�X�g��ێ�����ϐ�
	public static GL10 gl;
	
	//�����_���Ȓl�𐶐�����
	public static Random rand = new Random(System.currentTimeMillis());
	
	//�f�o�b�N���[�h�ł��邩
	public static boolean isDebuggable;
	
	//��{�}�`.
	public static MyVbo[] primitives;
	
	//����A�C�e����item_code
	public static final String ITEMCODE_NO_EQUIP= "9999"; 
	
	public static final String EVENTCODE_NOTHING   = "1";		//�u�Ȃɂ��Ȃ��v�C�x���g�R�[�h.
	public static final String EVENTCODE_STAIRUP 	 = "2";		//���K�i�̃C�x���g�R�[�h.
	public static final String EVENTCODE_STAIRDOWN = "3";		//����K�i�̃C�x���g�R�[�h.
	
	public static final String EVENTCODE_BATTLE	 = "4";		//TODO:�Œ�퓬.
}
