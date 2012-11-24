package all.jp.util;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import cx.fam.tak0294.storage.DBHelper;

import all.jp.Game.Direction;
import all.jp.Game.GameMode;
import all.jp.Game.Base.GameManager;
import all.jp.Game.Character.Player;
import all.jp.Game.Event.Event;
import all.jp.Game.Event.EventMode;
import all.jp.opengl.particle.ParticleSystem;
import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

public class MyRenderer implements Renderer
{
	private MyGameThread mGame;
	private GameManager game;
	private Activity activity;
	private boolean isReadyVbo = false;
	
	private int frame = 0;

	private int particleTexture;
	private int bloodTexture;
	private int upButtonTexture;
	private int leftButtonTexture;
	private int checkButtonTexture;
	private int campButtonTexture;
	private int savingTexture;
	
	
	private int mWidth;
	private int mHeight;
	private int mWidthOffset;
	private int mHeightOffset;
	

	
	public ParticleSystem particleSystem;
	public ParticleSystem bloodParticleSystem; 
	
	public MyRenderer(Activity activity, MyGameThread gameThread, GameManager game)
	{
		this.mGame = gameThread;
		this.game = game;
		game.renderer = this;
		this.activity = activity;
		Global.activity = activity;
	}
	
	public MyRenderer() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public void draw3D(GL11 gl)
	{
		gl.glMatrixMode(GL11.GL_PROJECTION);
		gl.glLoadIdentity();
		float znear = 0.3f; //scene.zoom2;
		float zfar = 10.0f; //position[2];
	    gl.glFrustumf(-0.3f, 0.3f, -0.2f, 0.2f, znear, zfar);
		
	    // ライトとマテリアルの設定
	    float lightPos[]     = { 0.0f, 0.0f, 0.0f, 0.0f };
	    float lightColor[]   = { 1.0f, 1.0f, 1.0f, 1.0f };
	    float lightAmbient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	    float diffuse[]      = { 1.0f, 1.0f, 1.0f, 1.0f };
	    float ambient[]      = { 0.4f, 0.4f, 0.4f, 1.0f };
	    
	    
	    // カメラの設定(デフォルト)
		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glLoadIdentity();
		
	    gl.glEnable(GL11.GL_LIGHTING);
	    gl.glEnable(GL11.GL_LIGHT0);
	    
	    gl.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPos, 0);
	    gl.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightColor, 0);
	    gl.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT, lightAmbient, 0);
	    gl.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, diffuse, 0);
	    gl.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, ambient, 0);
		
	    float[] ambients = {2.0f, 2.0f, 2.0f, 1};
	    float[] position = {0, 0, 2.0f, 1};
	    float[] direction = {0, 0, -1};

	    gl.glEnable(GL10.GL_LIGHT1);
	    gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, ambients, 0 );
	    gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, position, 0);
	    gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPOT_DIRECTION, direction, 0);
	    gl.glLightf(GL10.GL_LIGHT1, GL10.GL_SPOT_CUTOFF, 50.0f);

	    
	    //深度テストを有効.
		gl.glEnable(GL11.GL_DEPTH_TEST);

	    //フォグ設定.
	    float fogColor[]= {0.0f, 0.0f, 0.0f, 1.0f}; //フォグの色
	    gl.glEnable(GL11.GL_FOG);
	    gl.glFogx(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
	    gl.glFogfv(GL11.GL_FOG_COLOR, fogColor, 0);
	    gl.glFogf(GL11.GL_FOG_DENSITY, 0.3f);
	    gl.glHint(GL11.GL_FOG_HINT, GL11.GL_DONT_CARE);
	    gl.glFogf(GL11.GL_FOG_START, 2.0f);
	    gl.glFogf(GL11.GL_FOG_END, 3.0f);
	    

		//-----------------------------------------------
		//	ダンジョン.
	    //-----------------------------------------------
		if(game.gameMode == GameMode.Dungeon)
		{
			game.dungeon.draw3d(gl);
		}
	    
	    //Fog終了.
	    gl.glDisable(GL11.GL_FOG);

		//深度テスト終了
		gl.glDisable(GL11.GL_DEPTH_TEST);
	    gl.glDisable(GL11.GL_LIGHTING);
	    gl.glDisable(GL11.GL_LIGHT0);
		
		game.isTouch = game.isSurfaceTouch;
 	}
	
	public void draw2D(GL11 gl)
	{
		// 2D描画用に座標系を設定します
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(-1.5f, 1.5f, -1.0f, 1.0f, 0.5f, -0.5f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		
		//==========================================================
		//	コントロール部の描画.
		//==========================================================
		if(!game.event.isEnableEvent)
		{
			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			if(game.gameMode != GameMode.Title)
			{
				game.upButtonSprite.draw(gl, upButtonTexture);
				game.downButtonSprite.draw(gl, upButtonTexture);
				game.leftButtonSprite.draw(gl, leftButtonTexture);
				game.rightButtonSprite.draw(gl, leftButtonTexture);
				game.checkButtonSprite.draw(gl, checkButtonTexture);
				game.campButtonSprite.draw(gl, campButtonTexture);
			}
			gl.glDisable(GL11.GL_BLEND);
		}
		
		if(game.gameMode == GameMode.Dungeon 
		   //&& game.event.getMode() != EventMode.Battle
		   )
		{
			game.dungeon.draw2d(gl);
		}
		
		if(game.gameMode == GameMode.Camp)
		{
			game.activeParty.setNamePlateBattlePosition();
			game.camp.draw2d(gl);
		}
		
		if(game.gameMode == GameMode.Town)
		{
			game.town.draw2d(gl);
		}
		
		if(game.gameMode == GameMode.Title)
		{
			game.title.draw(gl);
		}
		
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		if(game.isOverrayBlack && game.event.getMode() == EventMode.Battle)
		{
			GraphicUtil.drawRectangle(gl, 0.0f, 0.0f, 3.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.2f);
		}
		
		if(game.isOverrayBlack && game.event.getMode() != EventMode.Battle)
		{
			GraphicUtil.drawRectangle(gl, 0.0f, 0.0f, 3.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.7f);
		}

		gl.glDisable(GL11.GL_BLEND);

		//============================================================
		//	イベント実行中の描画.
		//============================================================
		if(game.event.isEnableEvent)
		{
			game.event.draw(gl);
		}
		
		//============================================================
		//	キャラクターステータスの描画.
		//============================================================
		//TODO
		//debug.
		if(game.gameMode != GameMode.Title)
		{
			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			game.activeParty.draw(gl);
			gl.glDisable(GL11.GL_BLEND);
		}

		//==========================================================
		//	保存中Overlay.
		//==========================================================
		if(game.isSavingOverlay)
		{
			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GraphicUtil.drawTexture(gl, 0.0f, 0.0f, 3.0f, 2.5f, savingTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			gl.glDisable(GL11.GL_BLEND);
		}

		
	    //パーティクルを描画.
	    particleSystem.update();
	    bloodParticleSystem.update();
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		particleSystem.draw(gl, particleTexture);
		bloodParticleSystem.draw(gl, bloodTexture);
		gl.glDisable(GL10.GL_BLEND);
	}
	
	
	public void onDrawFrame(GL10 gl10)
	{
		GL11 gl = (GL11)gl10;
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glViewport(mWidthOffset, mHeightOffset, mWidth, mHeight);
		
		draw3D(gl);
		draw2D(gl);
		frame++;
	}
		
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		//----------------------------------------------
		//	VBOを作成する.
		//----------------------------------------------
		if(!isReadyVbo)
		{
			makeVbo((GL11)gl);
			mGame.init();
			isReadyVbo = true;
		}
		
		this.mWidth 		= width;
		this.mHeight 		= height;
		this.mWidthOffset   = 0;
		this.mHeightOffset  = 0;
		
		Global.gl = gl;// GLコンテキストを保持する

		// バージョンチェックを行う
		String vertion = gl.glGetString(GL10.GL_VERSION);
		Global.isES11 = false;
		if (vertion.contains("1.1"))
		{
			Global.isES11 = true;
		}
		
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		
		//----------------------------------------------
		//	テクスチャの読み込み.
		//----------------------------------------------
		particleTexture = GraphicUtil.loadTexture(gl, "particle");
		upButtonTexture	  = GraphicUtil.loadTexture(gl, "up_button");
		leftButtonTexture = GraphicUtil.loadTexture(gl, "left_button");
		checkButtonTexture = GraphicUtil.loadTexture(gl, "check_button");
		bloodTexture	= GraphicUtil.loadTexture(gl, "bloodtexture");
		campButtonTexture = GraphicUtil.loadTexture(gl, "camp_button");
		savingTexture	  = GraphicUtil.loadTexture(gl, "saving");
		
		game.initSprites();
		game.initTextures((GL11)gl);
		

		//----------------------------------------------
		//	パーティクルシステムの作成.
		//----------------------------------------------
	    particleSystem 		= new ParticleSystem(200, 20);
	    bloodParticleSystem = new ParticleSystem(50, 22);
	    
		//----------------------------------------------
		//	ゲーム開始.
		//----------------------------------------------
	    game.init();

	}

	public void onSurfaceCreated(GL10 gl10, EGLConfig config)
	{
	}

	private void makeVbo(GL11 gl)
	{
		Global.primitives = new MyVbo[5];
		for(int ii=0;ii<5;ii++)
		{
			Global.primitives[ii] = new MyVbo();
		}
		
		GraphicUtil.makePlane(gl, Global.primitives[SpriteType.PLANE]);
		GraphicUtil.makeCube(gl, Global.primitives[SpriteType.CUBE]);
		GraphicUtil.makePyramid(gl, Global.primitives[SpriteType.PYRAMID]);
		GraphicUtil.makeSphere(gl, Global.primitives[SpriteType.SPHERE], 10);
		GraphicUtil.makeCylinder(gl, Global.primitives[SpriteType.CYLINDER]);
	}

	//画面がタッチされたときに呼ばれるメソッド
	public void touched(float x, float y, float glX, float glY)
	{
		game.touchX = x;
		game.touchY = y;
		game.touchGlX = glX;
		game.touchGlY = glY;
		
		if(glX < -0.6f && glX > -1.4f && glY > 0.6f && glY < 0.9f)
		{
			game.isUp = true;
		}
		
		if(glX < -0.6f && glX > -1.4f && glY > -0.3f && glY < 0.1f)
		{
			game.isDown = true;
		}
		
		if(glX < -0.6f && glX > -1.0f && glY > 0.2f && glY < 0.5f)
		{
			game.isRight = true;
		}
		
		if(glX < -1.0f && glX > -1.4f && glY > 0.2f && glY < 0.5f)
		{
			game.isLeft = true;
		}

		if(glX < -0.6f && glX > -1.4f && glY > -0.6f && glY < -0.3f)
		{
			game.isA = true;
		}
		
		if(glX < -0.6f && glX > -1.4f && glY > -1.0f && glY < -0.75f)
		{
			game.isB = true;
		}

		
		game.isSurfaceTouch = true;
	}

	public void unTouched()
	{
		game.isA = false;
		game.isB = false;
		game.isUp = false;
		game.isDown = false;
		game.isRight = false;
		game.isLeft = false;
		game.dungeon.isTurned = false;
		game.isSurfaceTouch = false;
	}


}
