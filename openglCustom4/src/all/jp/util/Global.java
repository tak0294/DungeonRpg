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
	
	public static boolean isES11;//ES 1.1であればtrue
	
	//GLコンテキストを保持する変数
	public static GL10 gl;
	
	//ランダムな値を生成する
	public static Random rand = new Random(System.currentTimeMillis());
	
	//デバックモードであるか
	public static boolean isDebuggable;
	
	//基本図形.
	public static MyVbo[] primitives;
	
	//特殊アイテムのitem_code
	public static final String ITEMCODE_NO_EQUIP= "9999"; 
	
	public static final String EVENTCODE_NOTHING   = "1";		//「なにもない」イベントコード.
	public static final String EVENTCODE_STAIRUP 	 = "2";		//上り階段のイベントコード.
	public static final String EVENTCODE_STAIRDOWN = "3";		//下り階段のイベントコード.
	
	public static final String EVENTCODE_BATTLE	 = "4";		//TODO:固定戦闘.
}
