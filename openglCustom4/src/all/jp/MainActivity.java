package all.jp;

import cx.fam.tak0294.storage.DBHelper;
import all.jp.Game.Base.GameManager;
import all.jp.util.MyGLSurfaceView;
import all.jp.util.MyGameThread;
import all.jp.util.MyRenderer;
import android.app.Activity;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
	
	MyGameThread mGameThread;
	GameManager game;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        boolean isDebug = true;

        //===========================================
        //	マスタテーブル作成
        //===========================================
        DBHelper db = new DBHelper(this);
        db.createTableWithXml("item_mt", true);
        db.createTableWithXml("player_mt", false);
        db.createTableWithXml("enemy_mt", true);
        db.createTableWithXml("event_mt", false);
        db.createTableWithXml("magic_mt", false);
        db.createTableWithXml("treasurebox_mt", false);
        db.insertWithCsv("eventMaster", "event_mt", true);
        db.insertWithCsv("enemyMaster", "enemy_mt", true);
        //db.insertWithCsv("playerMaster", "player_mt", true);
        db.insertWithCsv("itemMaster", "item_mt", true);
        db.insertWithCsv("magicMaster", "magic_mt", true);
        db.insertWithCsv("treasureboxMaster", "treasurebox_mt", true);
        
        //===========================================
        //	フラグマスタテーブル作成
        //===========================================
        db.createTableWithXml("flag_mt", false);
        
        //===========================================
        //	SAVE用テーブル作成.
        //===========================================
        db.createTableWithXml("saved_item", false);
        db.createTableWithXml("magic_player_relation", false);
        db.createTableWithXml("equip_player_relation", false);
        db.createTableWithXml("saved_position", false);
        db.createTableWithXml("current_flag", true);
        db.createTableWithXml("saved_party", false);
        
        //ショップ用.
        db.createTableWithXml("saved_shopitem", false);		//ショップのアイテム保存用.
        db.createTableWithXml("current_shopitem", true);	//ショップのアイテム運用.
        
        
        if(isDebug)
        {
        	db.insertWithCsv("saved_shopitemSample", "saved_shopitem", true);
        }
        
        game = new GameManager(this);
        mGameThread = new MyGameThread(game);

		// フルスクリーン、タイトルバーの非表示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 音量変更をハードウェアボタンで出来るようにする
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		MyRenderer renderer 			= new MyRenderer(this, mGameThread, game);
		MyGLSurfaceView glSurfaceView 	= new MyGLSurfaceView(this);
		glSurfaceView.setRenderer(renderer);
        setContentView(glSurfaceView);
        
        mGameThread.start();
    }
    
    @Override 
    public void onDestroy(){ 
        super.onDestroy();
        //音楽を停止する
        game.bgm.stop();        
    }
}