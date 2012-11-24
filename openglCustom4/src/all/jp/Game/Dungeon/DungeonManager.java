package all.jp.Game.Dungeon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import cx.fam.tak0294.storage.DBHelper;

import all.jp.Game.Direction;
import all.jp.Game.GameMode;
import all.jp.Game.Position;
import all.jp.Game.Base.GameManager;
import all.jp.Game.Event.Event;
import all.jp.Game.Event.EventMode;
import all.jp.util.GLSprite;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;
import all.jp.util.SpriteType;
import android.content.res.AssetManager;

public class DungeonManager
{
	private GameManager game;	//統括マネージャ.
	public Map currentMap;		//現在のマップ
	public Position currentPosition;

	private int counter = 0;
	
	//壁、床用スプライト.
	public GLSprite wallSprite;
	private int wallTexture;
	private int wallDoorTexture;
	private int wallMessageTexture;
	private int rockTexture;
	private int cursorTexture;
	private int eventFloorTexture;
	private int radderTexture;;
	private int dungeonCeilTexture;
	
	//ミニマップ用テクスチャ.
	private int miniMapWindowTexture;
	
	//イベント床用スプライト.
	private GLSprite eventSprite;
	
	//階段用スプライト.
	private GLSprite stairSprite;
	
	//ミニマップ.
	private MiniMap miniMap;
	
	//移動.
	private int[][][] currentView;
	public boolean updateViewRequest = true;
	
	public boolean isTurned = false;
	public boolean isForwarding = false;
	public boolean isBackwarding = false;
	public float movePadding = 0.0f;
	public float anglePadding = 0.0f;
	
	private int turnEndTimerInit = 1;
	private int turnEndTimer	 = 0;
	
	
	//-----------------------------------------------
	//	コンストラクタ.
	//-----------------------------------------------
	public DungeonManager(GameManager game)
	{
		this.game = game;
		this.miniMap = new MiniMap();
	}
	
	public void initTextures(GL10 gl)
	{
		//壁描画用Sprite.
		wallSprite = new GLSprite(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		wallSprite.vbo = Global.primitives[SpriteType.PLANE];
		
		//イベント床用Sprite.
		eventSprite = new GLSprite(0.0f,0.0f,0.0f,0.4f,0.4f,0.4f);
		eventSprite.vbo = Global.primitives[SpriteType.CYLINDER];
		
		//階段用Sprite;
		stairSprite = new GLSprite(0.0f, 0.0f, 0.0f, 0.7f, 1.0f, 1.0f);
		stairSprite.vbo = Global.primitives[SpriteType.PLANE];
		
		dungeonCeilTexture = GraphicUtil.loadTexture(gl, "stoneceil2");
		radderTexture   = GraphicUtil.loadTexture(gl, "radder");
		wallTexture		= GraphicUtil.loadTexture(gl, "sewerwall01");
		wallDoorTexture = GraphicUtil.loadTexture(gl, "wall_door");
		wallMessageTexture = GraphicUtil.loadTexture(gl, "wall_message");
		rockTexture 	= GraphicUtil.loadTexture(gl, "rock");
		cursorTexture	= GraphicUtil.loadTexture(gl, "direct");
		eventFloorTexture = GraphicUtil.loadTexture(gl, "event_floor");
		miniMapWindowTexture = GraphicUtil.loadTexture(gl, "mapwindow");
		
		miniMap.createTextBuffer(gl);
	}


	//-----------------------------------------------
	//	タイプ別に壁テクスチャ取得.
	//-----------------------------------------------
	private int getWallTextureByType(int texType)
	{
		if(texType == 1)
			return wallTexture;
		else if(texType == 2)
			return wallDoorTexture;
		else if(texType == 10)
			return wallMessageTexture;
		
		return -1;
	}

	private boolean isWall(int type)
	{
		if(type == 1 || type == 2 || type == 10)
			return true;
		
		return false;
	}

	//-----------------------------------------------
	//	描画.
	//-----------------------------------------------
	public void draw3d(GL11 gl)
	{
		//----------------------------------------------
		//	前進アニメーション中.
		//----------------------------------------------
		if(isForwarding)
		{
			if(Global.isAnime)
				movePadding -= 0.10f;
			else
				movePadding -= 1.00f;
				
			if(movePadding <= -1.0f)
			{
				moveForward();
				checkMessage();
				movePadding = 0.0f;
				isForwarding = false;
				if(Global.isAnime)
					turnEndTimer = 1;
				else
					turnEndTimer = 5;
				//isTurned = false;
				updateViewRequest = true;
			}
		}
		
		
		//----------------------------------------------
		//	後進アニメーション中.
		//----------------------------------------------
		if(isBackwarding)
		{
			movePadding += 0.1f;
			if(movePadding >= 1.0f)
			{
				moveBackward();
				checkMessage();
				movePadding = 0.0f;
				isBackwarding = false;
				updateViewRequest = true;
			}
		}
		

		if(currentPosition.direction == Direction.East)
		{
			gl.glRotatef(90.0f + anglePadding, 0.0f, 1.0f, 0.0f);
			gl.glTranslatef(1.4f, 0.0f, 0.0f);
			gl.glTranslatef(0.0f, 0.0f, 3.5f);
			gl.glTranslatef(movePadding, 0.0f, 0.0f);
		}
		else if(currentPosition.direction == Direction.West)
		{
			gl.glRotatef(-90.0f + anglePadding, 0.0f, 1.0f, 0.0f);
			gl.glTranslatef(-3.4f, 0.0f, 0.0f);
			gl.glTranslatef(0.0f, 0.0f, 3.5f);
			gl.glTranslatef(-movePadding, 0.0f, 0.0f);
		}
		else if(currentPosition.direction == Direction.South)
		{
			gl.glRotatef(180.0f + anglePadding, 0.0f, 1.0f, 0.0f);
			gl.glTranslatef(0.0f, 0.0f, 5.9f);
			gl.glTranslatef(0.0f, 0.0f, movePadding);
		}
		else if(currentPosition.direction == Direction.North)
		{
			gl.glRotatef(anglePadding, 0.0f, 1.0f, 0.0f);
			gl.glTranslatef(0.0f, 0.0f, 0.1f);
			gl.glTranslatef(0.0f, 0.0f, -movePadding);
		}
		
		if(updateViewRequest)
		{
			currentView = getCurrentView();
			updateViewRequest = false;
		}
		
		
		for(int ii=0;ii<6;ii++)
		{
			for(int jj=0;jj<6;jj++)
			{
				
				if(isWall(currentView[0][ii][jj]))
				{
					gl.glPushMatrix();
					gl.glTranslatef(jj*1.0f-2.50f, 0.0f, ii*1.0f-5.50f);
					gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
					wallSprite.draw(gl, getWallTextureByType(currentView[0][ii][jj]));
					gl.glPopMatrix();
				}
				
				if(isWall(currentView[1][ii][jj]))
				{
					gl.glPushMatrix();
					gl.glTranslatef(jj*1.0f-2.0f, 0.0f, ii*1.0f-5.0f);
					wallSprite.draw(gl, getWallTextureByType(currentView[1][ii][jj]));
					gl.glPopMatrix();
				}
				
				if(currentView[2][ii][jj] == 100)
				{
					gl.glPushMatrix();
						gl.glTranslatef(jj*1.0f-2.0f, -0.5f, ii*1.0f-5.50f);
						gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
						wallSprite.draw(gl, rockTexture);
					gl.glPopMatrix();
					
					gl.glPushMatrix();
						gl.glTranslatef(jj*1.0f-2.0f, 0.5f, ii*1.0f-5.50f);
						gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
						wallSprite.draw(gl, dungeonCeilTexture);
					gl.glPopMatrix();
				}
			}
		}
	
	
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		for(int ii=0;ii<6;ii++)
		{
			for(int jj=0;jj<6;jj++)
			{
				if(currentView[3][ii][jj] == 1)
				{
					gl.glPushMatrix();
					gl.glTranslatef(jj*1.0f-2.0f, -0.40f, ii*1.0f-5.50f);
					gl.glTranslatef(0.0f, 0.04f * (float)Math.sin(Math.PI/180 * (counter*3%360)), 0.0f);
					eventSprite.draw(gl, eventFloorTexture);
					gl.glPopMatrix();
				}
				else if(currentView[3][ii][jj] == Integer.parseInt(Global.EVENTCODE_STAIRUP) ||
						 currentView[3][ii][jj] == Integer.parseInt(Global.EVENTCODE_STAIRDOWN))
				{
					float yPos;
					if(currentView[3][ii][jj] == Integer.parseInt(Global.EVENTCODE_STAIRUP))
						yPos = 0.50f;
					else
						yPos = -0.50f;
					
					gl.glPushMatrix();
					gl.glTranslatef(jj*1.0f-2.0f, yPos, ii*1.0f-5.50f);
					if(this.currentPosition.direction == Direction.East ||
					   this.currentPosition.direction == Direction.West)
					{
						gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
					}
					stairSprite.draw(gl, radderTexture);
					gl.glPopMatrix();
				}
				
			}
		}
		gl.glDisable(GL11.GL_BLEND);
	}
	
	
	public void draw2d(GL11 gl)
	{
		//------------------------------------------------
		//	ミニマップの表示範囲設定.
		//------------------------------------------------
		Position position = new Position(currentPosition);
		float cursorRotation = 0.0f;
		if(position.direction == Direction.North)
		{
			position.y += 2;
		}
		else if(position.direction == Direction.South)
		{
			position.y -= 3;
			cursorRotation = 180.0f;
		}
		else if(position.direction == Direction.East)
		{
			position.x -= 1;
			position.y -= 1;
			cursorRotation = -90.0f;
		}
		else if(position.direction == Direction.West)
		{
			position.x += 3;
			position.y -= 1;
			cursorRotation = 90.0f;
		}
		
		miniMap.preDrawBegin();
		miniMap.drawMap(gl, this.getView(position));
		miniMap.preDrawEnd(gl);
		
		//==============================================
		//	ミニマップ.
		//==============================================
		float mapAlpha = game.event.getMode()==EventMode.Battle?0.2f:1.0f;
		float paddX = 0.14f;
		float paddY = 0.32f;
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GraphicUtil.drawTexture(gl, 1.04f+paddX, 0.27f+paddY, 0.6f, 0.75f, miniMapWindowTexture, 1.0f, 1.0f, 1.0f, mapAlpha);
		GraphicUtil.drawTexture(gl, 1.2f+paddX, 0.16f+paddY, 0.86f, 0.89f, miniMap.getTexture(), 1.0f, 1.0f, 1.0f, mapAlpha);
		
		gl.glPushMatrix();
		gl.glTranslatef(1.035f + paddX, 0.215f + paddY, 0.0f);
		gl.glRotatef(cursorRotation, 0.0f, 0.0f, 1.0f);
		GraphicUtil.drawTexture(gl, 0.0f, 0.0f, 0.06f, 0.06f, cursorTexture, 1.0f, 1.0f, 1.0f, mapAlpha);
		gl.glPopMatrix();
		gl.glDisable(GL11.GL_BLEND);
		
	}
	
	//-----------------------------------------------
	//	更新.
	//-----------------------------------------------
	public void update()
	{
		if(isTurned && turnEndTimer > 0)
		{
			turnEndTimer--;
			if(turnEndTimer == 0)
			{
				isTurned 		 = false;
			}
		}
		
		if(game.isRight && !isTurned)
		{
			turnEndTimer = 0;
			turnRight();
			isTurned = true;
			updateViewRequest = true;
		}

		if(game.isLeft && !isTurned)
		{
			turnEndTimer = 0;
			turnLeft();
			isTurned = true;
			updateViewRequest = true;
		}

		if(game.isUp && !isTurned)
		{
			if(canForward())
			{
				isTurned = true;
				isForwarding = true;
			}
		}

		if(game.isDown && !isTurned)
		{
			turnEndTimer = 0;
			turnLeft();
			turnLeft();
			isTurned = true;
			//isBackwarding = true;
			updateViewRequest = true;
		}
		
		if(game.isA && !isTurned)
		{
			if(!checkMessage(true))
				game.event.setEventByCode(Global.EVENTCODE_NOTHING);
		}
		
		if(game.isB && !isTurned)
		{
			game.openCamp();
		}
		
		counter++;
	}
	
	//-----------------------------------------------
	//	現在のビュー配列を取得.
	//-----------------------------------------------
	public int[][][] getCurrentView()
	{
		return currentMap.getCurrentView(currentPosition);
	}

	//-----------------------------------------------
	//	指定座標での現在のビュー配列を取得.
	//-----------------------------------------------
	public int[][][] getView(Position position)
	{
		return currentMap.getCurrentView(position);
	}
	
	//-----------------------------------------------
	//	指定座標のメッセージを取得.
	//-----------------------------------------------
	private String getMessageAt(Position position)
	{
		String xHexCount = Integer.toString((int)((position.x+2)/16));
		String xDecCount = Integer.toHexString((position.x+2)%16);
		String yHexCount = Integer.toString((int)(position.y/16));
		String yDecCount = Integer.toHexString(position.y%16);
		
		return this.currentMap.events.get(position.floor).get(xHexCount + xDecCount + yHexCount + yDecCount);
	}
	
	
	//-----------------------------------------------
	//	現在座標のメッセージを取得.
	//-----------------------------------------------
	public boolean checkMessage()
	{
		return this.checkMessage(false);
	}
	
	public boolean checkMessage(boolean forceStart)
	{
		String message = getMessageAt(currentPosition);
		
		//-----------------------------------------------
		//	メッセージが存在した場合、イベントとして設定.
		//-----------------------------------------------
		if(message != null)
		{
			Pattern pattern = Pattern.compile("event([0-9]+)");
			Matcher matcher = pattern.matcher(message);
			String event_id = "";
			if(matcher.find())
			{
				event_id = matcher.group(1);
			}
			
			//DBからイベントを取得.
			DBHelper db = new DBHelper(game.activity);
			HashMap<String,String> in = new HashMap<String,String>();
			in.put("event_code", event_id);
			ArrayList<HashMap<String, String>> res = db.get("event_mt", in);
			
			//向き.
			if(!res.get(0).get("event_direction").equals("-1") && Integer.parseInt(res.get(0).get("event_direction")) != currentPosition.direction)
				return false;
			
			//自動開始ではないイベントは発生させない.
			if(!res.get(0).get("event_autoStartFlag").equals("1") && !forceStart)
				return false;
			
			game.event.setEvent(res);
			
			return true;
		}
		//-----------------------------------------------
		//	メッセージが存在しない場合、エンカウント判定.
		//-----------------------------------------------
		else
		{
			if(Global.rand.nextInt(20) > 18)
			{
				//game.event.setEventByCode(Global.EVENTCODE_BATTLE);
				game.setRandomEncount();
				return true;
			}
		}
		
		return false;
	}
	
	//-----------------------------------------------
	//	マップファイル読み込み.
	//-----------------------------------------------
	public boolean loadMap(String mapName)
	{
		InputStream in = null;
		AssetManager as = game.activity.getApplicationContext().getResources().getAssets();
		
		/*
		 * datファイルの読み込み.
		 */
		try
		{
			in = as.open(mapName);
		}
		catch(Exception e)
		{
			try
			{
				if(in != null)	in.close();
			}
			catch(Exception e2){}
			
			return false;
		}
		
		//-----------------------------------------
		//	マップの作成、データ読み込み.
		//-----------------------------------------
		currentMap = new Map();
		currentMap.game = game;
		currentMap.load(in);
		
		//-----------------------------------------
		//	初期座標.
		//-----------------------------------------
		//debug.
		int floor = 0;
		int x = 8;
		int y = 19;
		currentPosition = new Position(floor,x,y,Direction.North);
		
		return true;
	}
	
	//-----------------------------------------------
	//	右回転.
	//-----------------------------------------------
	public void turnRight()
	{
		currentPosition.direction = ++currentPosition.direction%4; 
	}

	//-----------------------------------------------
	//	左回転.
	//-----------------------------------------------
	public void turnLeft()
	{
		currentPosition.direction = --currentPosition.direction;
		if(currentPosition.direction < 0)
			currentPosition.direction  = 3;
	}

	//-----------------------------------------------
	//	通過可能な床？.
	//-----------------------------------------------
	private boolean canPassFloor(int floorIndex)
	{
		if(floorIndex == 0 || floorIndex == 2)
		{
			return true;
		}
		
		return false;
	}
	
	//-----------------------------------------------
	//	前進できるかチェックする.
	//-----------------------------------------------
	public boolean canForward()
	{
		if(currentPosition.direction == Direction.North)
		{
			if(!canPassFloor(currentView[1][4][2]))
			{
				return false;
			}
		}
		else if(currentPosition.direction == Direction.South)
		{
			if(!canPassFloor(currentView[1][0][2]))
			{
				return false;
			}
		}
		else if(currentPosition.direction == Direction.East)
		{
			if(!canPassFloor(currentView[0][2][2]))
			{
				return false;
			}
		}
		else if(currentPosition.direction == Direction.West)
		{
			if(!canPassFloor(currentView[0][2][5]))
			{
				return false;
			}
		}
		
		return true;
	}
	
	//-----------------------------------------------
	//	前進.
	//-----------------------------------------------
	public void moveForward()
	{
		int direct = currentPosition.direction;
		if(direct == Direction.North)
			currentPosition.y--;
		else if(direct == Direction.South)
			currentPosition.y++;
		else if(direct == Direction.East)
			currentPosition.x++;
		else if(direct == Direction.West)
			currentPosition.x--;
	}

	//-----------------------------------------------
	//	後進.
	//-----------------------------------------------
	public void moveBackward()
	{
		int direct = currentPosition.direction;
		if(direct == Direction.North)
			currentPosition.y++;
		else if(direct == Direction.South)
			currentPosition.y--;
		else if(direct == Direction.East)
			currentPosition.x--;
		else if(direct == Direction.West)
			currentPosition.x++;
	}

}
