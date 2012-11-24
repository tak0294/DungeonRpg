package all.jp.Game.Town;

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL11;

import cx.fam.tak0294.storage.DBHelper;

import all.jp.Game.GameMode;
import all.jp.Game.Base.GameManager;
import all.jp.Game.Camp.CampMode;
import all.jp.Game.Event.Battle.BattleMode;
import all.jp.Game.Item.ItemBase;
import all.jp.Game.Town.Inn.InnManager;
import all.jp.Game.Town.Shop.ShopManager;
import all.jp.Game.Town.Temple.TempleManager;
import all.jp.util.FontTexture;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;

public class TownManager
{
	private GameManager game;
	private InnManager inn;
	private TempleManager temple;
	private ShopManager shop;
	
	private int MODE;
	public int mainWindowTexture;
	public int subWindowTexture;
	public int mainMenuTexture;
	public int menuRightButtonTexture;
	public int menuUpButtonTexture;
	public int okButtonTexture;
	public int campButtonTexture;
	public int backButtonTexture;
	public int houseTexture;
	public int toDungeonTexture;
	public int churchTexture;
	public int innRoomTexture;
	public int shopRoomTexture;
	public int shopTexture;
	
	public int yesButtonTexture;
	public int noButtonTexture;
	
	public int buyButtonTexture;
	public int sellButtonTexture;
	
	public int reviveButtonTexture;
	
	public FontTexture fontTexture;

	private boolean isCursorEnd 	= false;
	private int currentLineIndex  	= 0;
	private int currentCharIndex    = 0;
	private ArrayList<Integer> lineIndexes;
	private String currentEventText;
	private boolean nextPageRequest = false;
	
	private int innTexture;
	private int templeTexture;
	private int dungeonTexture;
	
	private int currentIndex = 0;
	
	private boolean isTouch;
	
	public TownManager(GameManager game)
	{
		this.game = game;
		this.inn 	= new InnManager(this, game);
		this.temple = new TempleManager(this, game);
		this.shop   = new ShopManager(this, game);
		this.fontTexture = new FontTexture();
		this.fontTexture.maxLineCount = 3;
	}
	
	
	//-------------------------------------------------
	//	変数初期化処理.
	//-------------------------------------------------
	public void init()
	{
		MODE = TownMode.WaitingWhatToDo;
		currentIndex = 0;
		isTouch = false;
		
		//行の折り返しIndex配列.
		this.isCursorEnd = false;
		this.currentLineIndex = 0;
		this.currentCharIndex = 0;
		this.lineIndexes = new ArrayList<Integer>();
		this.currentEventText = "";
	}
	
	public void setMode(int mode)
	{
		this.MODE = mode;
	}
	
	//-------------------------------------------------
	//	テクスチャ関連初期化処理.
	//-------------------------------------------------
	public void initTextures(GL11 gl)
	{
		mainWindowTexture  = GraphicUtil.loadTexture(gl, "battlemainwindow");
		subWindowTexture   = GraphicUtil.loadTexture(gl, "battlesubwindow");
		mainMenuTexture	   = GraphicUtil.loadTexture(gl, "battlemainmenu");
		menuRightButtonTexture = GraphicUtil.loadTexture(gl, "menu_right_button");
		menuUpButtonTexture = GraphicUtil.loadTexture(gl, "menu_up_button");
		okButtonTexture 	= GraphicUtil.loadTexture(gl, "fight_target_ok_button");
		campButtonTexture = GraphicUtil.loadTexture(gl, "menu_camp_button");
		backButtonTexture = GraphicUtil.loadTexture(gl, "back_button");

		yesButtonTexture = GraphicUtil.loadTexture(gl, "yes_button");
		noButtonTexture  = GraphicUtil.loadTexture(gl, "no_button");

		innTexture 		= GraphicUtil.loadTexture(gl, "town_inn");
		templeTexture 	= GraphicUtil.loadTexture(gl, "town_temple");
		dungeonTexture 	= GraphicUtil.loadTexture(gl, "town_dungeon");
		shopTexture		= GraphicUtil.loadTexture(gl, "town_shopicon");
		houseTexture 	= GraphicUtil.loadTexture(gl, "house001");
		toDungeonTexture = GraphicUtil.loadTexture(gl, "dungeon001");
		churchTexture 	 = GraphicUtil.loadTexture(gl, "church");
		innRoomTexture	 = GraphicUtil.loadTexture(gl, "inn00");
		shopRoomTexture	 = GraphicUtil.loadTexture(gl, "shop00");

		
		reviveButtonTexture = GraphicUtil.loadTexture(gl, "revive_button");
		
		buyButtonTexture = GraphicUtil.loadTexture(gl, "shop_menu_buy");
		sellButtonTexture = GraphicUtil.loadTexture(gl, "shop_menu_sell");
		
		shop.item.initTexture(gl);
		
		this.fontTexture.createTextBuffer(gl);
	}

	//------------------------------------------------------
	//	テキストを設定し、表示する.
	//------------------------------------------------------
	public void setTextAndShow(String text)
	{
		//行の折り返しIndex配列.
		this.isCursorEnd = false;
		this.currentLineIndex = 0;
		this.currentCharIndex = 0;
		this.lineIndexes = new ArrayList<Integer>();
		this.currentEventText = text;
		
		this.fontTexture.calcIndexMode = true;
		int index = 0;
		while(true)
		{
			index = this.fontTexture.drawStringToTexture(text, 400, index, 0);
			if(index != -1)
				this.lineIndexes.add(index);
			else
				break;
		}
		this.lineIndexes.add(text.length()-1);

		this.fontTexture.calcIndexMode = false;
		this.fontTexture.resetPreDrawCount();
	}

	public void nextPage()
	{
		this.isCursorEnd = false;
		this.currentCharIndex = this.lineIndexes.get(this.currentLineIndex);
		this.currentLineIndex++;
		if(this.currentLineIndex > this.lineIndexes.size())
			this.currentLineIndex = this.lineIndexes.size();
	}

	private int getPreviousCharIndex(int lineIndex)
	{
		if(lineIndex == 0)
			return 0;
		else
		{
			return this.lineIndexes.get(lineIndex-1);
		}
	}
	
	//-------------------------------------------------
	//	描画処理(2D).
	//-------------------------------------------------
	public void draw2d(GL11 gl)
	{
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//背景.
		int tex;
		if(currentIndex == 0 || currentIndex == 1)
			tex = houseTexture;
		else if(currentIndex == 2)
			tex = churchTexture;
		else
			tex = toDungeonTexture;
		
		GraphicUtil.drawTexture(gl, 0.0f, 0.0f, 3.0f, 2.2f, tex, 1.0f, 1.0f, 1.0f, 0.7f);
		
		//ウィンドウ枠の描画.
		GraphicUtil.drawTexture(gl, -0.3f, 0.2f, 2.3f, 1.5f, mainWindowTexture, 1.0f, 1.0f, 1.0f, 0.8f);
		GraphicUtil.drawTexture(gl, -0.3f, -0.8f, 2.3f, 0.35f, subWindowTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		//GraphicUtil.drawTexture(gl, -0.3f, 0.035f, 2.2f, 1.055f, mainWindowTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		
		//タブ内ウィンドウ.
		GraphicUtil.drawTexture(gl, 1.19f, -0.40f, 0.5f, 1.18f, mainMenuTexture, 1.0f, 1.0f, 1.0f, 1.0f);

		//----------------------------------------------
		//	行動選択時.
		//----------------------------------------------
		if(MODE == TownMode.WaitingWhatToDo)
		{
			GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, okButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.55f, 0.4f, 0.2f, menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.85f, -0.4f, 0.2f, menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.25f, 0.4f, 0.2f, campButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);

			GraphicUtil.drawTexture(gl, (currentIndex*0.55f)-1.1f, -0.36f, 0.55f, 0.25f, mainMenuTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			
			GraphicUtil.drawTexture(gl, -1.1f, -0.15f, 0.7f, 0.7f, innTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, -0.53f, -0.15f, 0.7f, 0.7f, shopTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, -0.0f, -0.15f, 0.6f, 0.7f, templeTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 0.53f,  -0.15f, 0.6f, 0.7f, dungeonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		//----------------------------------------------
		//	宿屋.
		//----------------------------------------------
		else if(MODE == TownMode.ShowInn)
		{
			this.inn.draw(gl);
		}
		//----------------------------------------------
		//	寺院.
		//----------------------------------------------
		else if(MODE == TownMode.ShowTemple)
		{
			this.temple.draw(gl);
		}
		//----------------------------------------------
		//	ショップ.
		//----------------------------------------------
		else if(MODE == TownMode.ShowShop)
		{
			this.shop.draw(gl);
		}
		
		//------------------------------------------------------------
		//	テキスト描画モード.
		//------------------------------------------------------------
		if(true)
		{
			this.fontTexture.resetPreDrawCount();
			
			if(nextPageRequest)
			{
				this.nextPage();
				nextPageRequest = false;
			}

			if(this.currentLineIndex >= this.lineIndexes.size()-1)
				this.currentLineIndex = this.lineIndexes.size()-1;
			
			try{
				
				if(this.lineIndexes.size() > 0 &&
				   this.currentLineIndex < this.lineIndexes.size() &&
				   this.currentCharIndex <= this.lineIndexes.get(currentLineIndex))
				{
					this.fontTexture.preDrawBegin();
					this.currentCharIndex = this.fontTexture.drawStringToTexture(this.currentEventText, 400, this.getPreviousCharIndex(currentLineIndex), this.currentCharIndex+1);
					this.fontTexture.preDrawEnd(gl);
					
					if(this.currentCharIndex == this.lineIndexes.get(currentLineIndex))
						this.isCursorEnd = true;	
				
					
				}
			}catch(Exception e)
			{
				this.isCursorEnd = true;
				this.currentLineIndex = this.lineIndexes.size()-1;
			}
			
			int no	= fontTexture.getTexture();
			int sx	= fontTexture.getWidth();
			int sy	= fontTexture.getHeight();
			float offset= fontTexture.getOffset();
	
			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GraphicUtil.drawTexture(gl,-(2.82f - sx/180.0f)*0.5f,(-1.3f - sy/140.0f) * 0.5f,sx/180.0f,sy/140.0f,no, 0,offset/256.0f,sx/512.0f, sy/256.0f, 1.0f,1.0f,1.0f, 1.0f);
			gl.glDisable(GL11.GL_BLEND);
		}
		
		gl.glDisable(GL11.GL_BLEND);
	}
	
	//-------------------------------------------------
	//	更新処理.
	//-------------------------------------------------
	public void update()
	{
		if(game.isTouch && !this.isTouch)
		{
			if(MODE == TownMode.WaitingWhatToDo)
			{
				//決定.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
				{
					//宿屋へ入る.
					if(currentIndex == 0)
					{
						MODE = TownMode.ShowInn;
						this.inn.initInn();
						this.setTextAndShow("いらっしゃいませ！宿泊料は200Gです。\n泊まりますか？（OKで泊まります）");
					}
					//ショップへ入る.
					else if(currentIndex == 1)
					{
						MODE = TownMode.ShowShop;
						this.shop.initShop();
						this.setTextAndShow(shop.getGoldStr() + "いらっしゃいませ！何でも屋「迷宮の星屑」へようこそ！\nどういったご用件でしょう？");
					}
					//寺院へ入る.
					else if(currentIndex == 2)
					{
						MODE = TownMode.ShowTemple;
						this.temple.initTemple();
						this.setTextAndShow("ここは教会です。何をお望みかな？");
					}
					//ダンジョンへ入る.
					else if(currentIndex == 3)
					{
						game.openDungeon();
					}
				}
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
				{
					game.openCamp(GameMode.Town);
				}
				//
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
				{
					this.currentIndex = ++this.currentIndex%4;
				}
				//
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
				{
					this.currentIndex--;
					if(this.currentIndex<0)
						this.currentIndex = 3;	
				}
			}
			else if(MODE == TownMode.ShowInn)
			{
				this.inn.update();
			}
			else if(MODE == TownMode.ShowTemple)
			{
				this.temple.update();
			}
			else if(MODE == TownMode.ShowShop)
			{
				this.shop.update();
			}
		}
		
		this.isTouch = game.isTouch;
		
	}
}
