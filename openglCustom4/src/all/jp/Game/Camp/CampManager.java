package all.jp.Game.Camp;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.GameMode;
import all.jp.Game.Base.GameManager;
import all.jp.Game.Character.CharacterBase;
import all.jp.Game.Character.Player;
import all.jp.Game.Event.Battle.BattleActType;
import all.jp.Game.Event.Battle.BattleMode;
import all.jp.Game.Item.ItemBase;
import all.jp.Game.Item.ItemType;
import all.jp.Game.Magic.MagicBase;
import all.jp.util.FontTexture;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;
import android.graphics.Typeface;

public class CampManager
{
	private GameManager game;

	private int MODE;
	private int mainWindowTexture;
	private int subWindowTexture;

	private int mainMenuTexture;
	private int itemButtonTexture;
	private int magicButtonTexture;
	private int menuUpButtonTexture;
	private int menuRightButtonTexture;
	private int okButtonTexture;
	private int backButtonTexture;
	private int charaBackgroundTabTexture;
	private int statusButtonTexture;
	private int equipButtonTexture;
	private int backgroundTexture;
	private int saveButtonTexture;
	private int dropButtonTexture;
	private int yesButtonTexture;
	private int noButtonTexture;
	
	public boolean isTouch;
	private boolean isConfirm = false;

	private boolean isCursorEnd 	= false;
	private int currentLineIndex  	= 0;
	private int currentCharIndex    = 0;
	private ArrayList<Integer> lineIndexes;
	private String currentEventText;
	private boolean nextPageRequest = false;
	private FontTexture fontTexture;
	private FontTexture statusFontTexture;
	
	//使用するアイテム.
	private ItemBase currentItem;
	private MagicBase currentMagic;
	private Player targetPlayer;
	private Player usePlayer;
	
	private int currentTargetPlayerIndex = 0;
	private int currentStatusPlayerIndex = 0;
	
	private int[] equipOrder = {ItemType.ITEM_WEAPON,
								 ItemType.ITEM_SHIELD,
								 ItemType.ITEM_EQUIP_HEAD,
								 ItemType.ITEM_EQUIP_BODY,
								 ItemType.ITEM_EQUIP_HAND};

	//メニュー位置調整.
	private float menuYPos = 0.315f;

	
	private int currentEquipOrder = 0;
	
	public boolean redrawStatusRequest = false;
	
	public int returnGameMode = -1;
	
	public CampManager(GameManager game)
	{
		MODE = CampMode.WaitingWhatToDo;
		this.game = game;
		this.fontTexture = new FontTexture();
		this.fontTexture.maxLineCount = 3;
		this.statusFontTexture = new FontTexture();
		this.statusFontTexture.maxLineCount = 50;
		init();
	}
	
	//----------------------------------------
	//	初期化処理.
	//----------------------------------------
	public void init()
	{
		currentItem = null;
		targetPlayer = null;
		usePlayer = null;
		currentTargetPlayerIndex = 0;
		currentStatusPlayerIndex = 0;
		returnGameMode = -1;
		isConfirm = false;
	}
	
	public void initTextures(GL11 gl)
	{
		mainWindowTexture  = GraphicUtil.loadTexture(gl, "battlemainwindow");
		subWindowTexture   = GraphicUtil.loadTexture(gl, "battlesubwindow");
		
		itemButtonTexture  = GraphicUtil.loadTexture(gl, "item_button");
		magicButtonTexture = GraphicUtil.loadTexture(gl, "magic_button");
		mainMenuTexture	   = GraphicUtil.loadTexture(gl, "battlemainmenu");
		menuUpButtonTexture= GraphicUtil.loadTexture(gl, "menu_up_button");
		menuRightButtonTexture = GraphicUtil.loadTexture(gl, "menu_right_button");
		okButtonTexture 	= GraphicUtil.loadTexture(gl, "fight_target_ok_button");
		backButtonTexture 	= GraphicUtil.loadTexture(gl, "back_button");
		charaBackgroundTabTexture = GraphicUtil.loadTexture(gl, "chara_background_tab");
		statusButtonTexture = GraphicUtil.loadTexture(gl, "status_button");
		equipButtonTexture  = GraphicUtil.loadTexture(gl, "equip_button");
		backgroundTexture   = GraphicUtil.loadTexture(gl, "camp00");
		saveButtonTexture	= GraphicUtil.loadTexture(gl, "menu_save_button");
		dropButtonTexture	= GraphicUtil.loadTexture(gl, "drop_button");
		
		yesButtonTexture	= GraphicUtil.loadTexture(gl, "yes_button");
		noButtonTexture		= GraphicUtil.loadTexture(gl, "no_button");
		
		fontTexture.createTextBuffer(gl);
		statusFontTexture.createTextBuffer(gl);
		this.statusFontTexture.m_paint.setTypeface(Typeface.MONOSPACE);
		this.setTextAndShow("");
	}

	private void drawStatusWindow(GL11 gl)
	{
		String text = "";
		Player pl = game.activeParty.members.get(currentStatusPlayerIndex);
		
		int orgStr = pl.getStr();
		int orgDef = pl.getDef();
		
		int str = pl.getStr();
		int def = pl.getDef();
		int dex = pl.dex;
		
		if(MODE == CampMode.WaitingEquipSelect && 
		   game.item.currentItemSet.size() > 0)
		{
			ItemBase item = game.item.get();

			if(this.equipOrder[this.currentEquipOrder] == ItemType.ITEM_WEAPON)
			{
				str = pl.str;
				str += item.str;
			}
			else
			{
				def = pl.getDef(this.equipOrder[this.currentEquipOrder]);
				def += item.def;
			}
			
			dex += item.dex;
		}
		
		String diffStrString = "";
		
		if(str > orgStr)
			diffStrString = " ↑" + (str - orgStr);
		else if(str < orgStr)
			diffStrString = " ↓" + (orgStr - str);

		String diffDefString = "";
		
		if(def > orgDef)
			diffDefString = " ↑" + (def - orgDef);
		else if(def < orgDef)
			diffDefString = " ↓" + (orgDef - def);

		text = "";
		text += pl.name + "  Level:" + pl.level + "\n";
		text += "Gold:" + game.activeParty.gold + "\n";
		text += "Exp:" + String.format("%4d", pl.exp) + "\n";
		text += "HP/MAX : " + String.format("%4d", pl.hp) + " / " + String.format("%4d", pl.maxHp) + "\n";
		text += "MP/MAX : " + String.format("%4d", pl.mp) + " / " + String.format("%4d", pl.maxMp) + "\n";
		text += "STR:" + String.format("%4d", str) + diffStrString;
		text += "   |   Weapon:" + ( pl.Weapon!=null?pl.Weapon.name:"なし") + "\n";
		text += "DEF:" + String.format("%4d", def) + diffDefString;
		text += "   |   Shield:" + ( pl.Shield!=null?pl.Shield.name:"なし") + "\n";
		text += "DEX:" + String.format("%4d", dex);
		text += "   |     Head:" + ( pl.HeadEquip!=null?pl.HeadEquip.name:"なし") + "\n";
		text += "";
		text += "           |     Body:" + ( pl.BodyEquip!=null?pl.BodyEquip.name:"なし") + "\n";
		text += "           |     Hand:" + ( pl.HandEquip!=null?pl.HandEquip.name:"なし") + "\n";

		statusFontTexture.preDrawBegin();
		statusFontTexture.drawStringToTexture(text, 700);
		statusFontTexture.preDrawEnd(gl);
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
	
	
	//=================================================================================================
	//
	//	2D描画関数.
	//
	//
	//=================================================================================================
	public void draw2d(GL11 gl)
	{
		if(this.redrawStatusRequest)
		{
			drawStatusWindow(gl);
			this.redrawStatusRequest = false;
		}
		
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//背景.
		GraphicUtil.drawTexture(gl, 0.0f, 0.0f, 3.0f, 2.7f, backgroundTexture, 1.0f, 1.0f, 1.0f, 0.7f);
		
		//ウィンドウ枠の描画.
		//GraphicUtil.drawTexture(gl, -0.3f, 0.2f, 2.3f, 1.5f, mainWindowTexture, 1.0f, 1.0f, 1.0f, 0.5f);
		GraphicUtil.drawTexture(gl, -0.3f, -0.8f, 2.3f, 0.35f, subWindowTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, -0.3f, 0.010f, 2.2f, 1.105f, mainWindowTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		
		//タブ内ウィンドウ.
		if(MODE == CampMode.WaitingWhatToDo)
			GraphicUtil.drawTexture(gl, 1.19f, -0.40f, 0.5f, 1.18f, mainMenuTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		else
			GraphicUtil.drawTexture(gl, 1.19f, -0.53f + menuYPos, 0.5f, 1.55f, mainMenuTexture, 1.0f, 1.0f, 1.0f, 0.7f);
		
		//タブ.
		GraphicUtil.drawTexture(gl, game.activeParty.members.get(currentStatusPlayerIndex).x, game.activeParty.members.get(currentStatusPlayerIndex).y - 0.01f, 0.6f, 0.36f, charaBackgroundTabTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		
		//ステータス.
		int tex	= this.statusFontTexture.getTexture();
		int w	= this.statusFontTexture.getWidth();
		int h	= this.statusFontTexture.getHeight();
		float o = this.statusFontTexture.getOffset();

		GraphicUtil.drawTexture(gl,-(2.70f - w/180.0f)*0.5f,(1.0f - h/140.0f) * 0.5f,w/180.0f,h/140.0f,tex, 0,o/256.0f,w/512.0f, h/256.0f, 1.0f,1.0f,1.0f, 1.0f);

		if(MODE == CampMode.WaitingWhatToDo)
		{
			GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, statusButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.25f, 0.4f, 0.2f, itemButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.55f, 0.4f, 0.2f, magicButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.85f, 0.4f, 0.2f, backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);

			//Saveボタン.
			GraphicUtil.drawTexture(gl, 1.19f, 0.6f, 0.5f, 0.5f, saveButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		else if(MODE == CampMode.WaitingItemSelect ||
				MODE == CampMode.WaitingMagicSelect)
		{
			GraphicUtil.drawTexture(gl, 1.19f, 0.05f + menuYPos, 0.4f, 0.2f, okButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.25f + menuYPos, 0.4f, 0.2f, menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.55f + menuYPos, 0.4f, -0.2f, menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.85f, 0.4f, 0.2f, backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			
			if(MODE == CampMode.WaitingItemSelect && game.item.MODE == ItemType.ITEM_MODE_SELECT_ITEM)
			{
				GraphicUtil.drawTexture(gl, 1.19f, -0.55f, 0.4f, 0.2f, dropButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			}
			
			if(MODE == CampMode.WaitingItemSelect)
				game.item.draw(gl);
			else
				game.magic.draw(gl);
		}
		else if(MODE == CampMode.WaitingItemTargetSelect ||
				MODE == CampMode.WaitingMagicSpellerSelect ||
				MODE == CampMode.WaitingMagicTargetSelect ||
				MODE == CampMode.WaitingWhatToDoStatus
				)
		{
			if(MODE != CampMode.WaitingWhatToDoStatus)
				GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, okButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			else
				GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, equipButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.25f, 0.4f, 0.2f, menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.55f, -0.4f, 0.2f, menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.85f, 0.4f, 0.2f, backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);

			if(MODE == CampMode.WaitingItemTargetSelect)
				game.item.draw(gl);
			
		}
		else if(MODE == CampMode.WaitingEquipSelect)
		{
			GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, okButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.25f, 0.4f, 0.2f, menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.55f, 0.4f, -0.2f, menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.85f, 0.4f, 0.2f, backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);

			game.item.draw(gl);
		}
		
		if(isConfirm)
		{
			GraphicUtil.drawTexture(gl, 0.3f, 0.2f, 1.0f, 0.5f, yesButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, -0.9f, 0.2f, 1.0f, 0.5f, noButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		
		gl.glDisable(GL11.GL_BLEND);
		
		
		//------------------------------------------------------------
		//	テキスト描画モード.
		//------------------------------------------------------------
		if( MODE == CampMode.WaitingWhatToDo ||
			MODE == CampMode.WaitingItemSelect ||
			MODE == CampMode.WaitingItemTargetSelect ||
			MODE == CampMode.WaitingMagicSpellerSelect ||
			MODE == CampMode.WaitingMagicSelect ||
			MODE == CampMode.WaitingMagicTargetSelect ||
			MODE == CampMode.WaitingWhatToDoStatus)
			
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
	}

	

	//=================================================================================================
	//
	//	処理関数.
	//
	//
	//=================================================================================================
	public void update()
	{
		if(game.isTouch && !this.isTouch)
		{
			if(MODE == CampMode.WaitingWhatToDo)
			{
				//ステータス.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
				{
					MODE = CampMode.WaitingWhatToDoStatus;	//ステータス.
				}
				//アイテム.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
				{
					game.item.init();
					game.item.DISPLAY_MODE   = ItemType.ITEM_DISPLAY_MODE_CAMP;
					game.item.setItemSet(game.activeParty.items);
					game.item.setUpdateRequest();
					
					MODE = CampMode.WaitingItemSelect;
				}
				//魔法.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
				{

					game.magic.init();
					MODE = CampMode.WaitingMagicSpellerSelect;	//詠唱者選択.
					usePlayer = game.activeParty.members.get(0);
					usePlayer.startSelectedAnimation();
					this.setTextAndShow("誰の魔法？");
				}
				//キャンプ閉じる
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
				{
					this.setTextAndShow(" ");
					
					if(this.returnGameMode == -1)
					{
						game.activeParty.setNamePlateDungeonPosition();
						game.gameMode = GameMode.Dungeon;
					}
					else
					{
						if(this.returnGameMode == GameMode.Town)
						{
							game.activeParty.setNamePlateBattlePosition();
						}
						game.gameMode = this.returnGameMode;
						this.returnGameMode = -1;
					}
				}
				//SAVE.
				else if(game.touchGlX > 0.94f && game.touchGlX < 1.39f && game.touchGlY > 0.35f && game.touchGlY < 0.75f)
				{
					game.saveData();
				}				
			}
			else if(MODE == CampMode.WaitingWhatToDoStatus)
			{
				//装備.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
				{
					currentEquipOrder = 0;
					game.item.initWithNoUpdate();
					//アイテムウィンドウを装備スタイルに変更する.
					game.item.MODE = ItemType.ITEM_MODE_SELECT_ITEM;
					game.item.setEquipStyle();
					game.item.setFilterType(this.equipOrder[currentEquipOrder]);
					game.item.setFirstItem(game.activeParty.members.get(currentStatusPlayerIndex));
					MODE = CampMode.WaitingEquipSelect;

					//TODO:全部剥がすんじゃなくてカーソル上の武具との差異を表示させたい.
					//装備を全部剥がす.
					//game.activeParty.members.get(currentStatusPlayerIndex).removeAllEquip();
					this.redrawStatusRequest = true;

				}
				//→.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
				{
					moveStatusIndex("right");
					this.redrawStatusRequest = true;
				}
				//←
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
				{
					moveStatusIndex("left");
					this.redrawStatusRequest = true;
				}
				//戻る.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
				{
					this.setTextAndShow(" ");
					currentTargetPlayerIndex = 0;
					MODE = CampMode.WaitingWhatToDo;
				}
			}
			else if(MODE == CampMode.WaitingEquipSelect)
			{
				//OK.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
				{
					//-------------------------------------------------
					//	選択したアイテムを装備させる.
					//-------------------------------------------------
					//まず外す.
					game.activeParty.members.get(this.currentStatusPlayerIndex).unEquipItem(this.equipOrder[currentEquipOrder]);
					//そして装備.
					game.activeParty.members.get(this.currentStatusPlayerIndex).equipItem(game.item.get());
					game.item.remove();
					this.redrawStatusRequest = true;
					
					currentEquipOrder++;
					if(currentEquipOrder >= this.equipOrder.length)
					{
						this.setTextAndShow(" ");
						MODE = CampMode.WaitingWhatToDoStatus;
					}
					else
					{
						game.item.initWithNoUpdate();
						game.item.MODE = ItemType.ITEM_MODE_SELECT_ITEM;
						game.item.setEquipStyle();
						game.item.setFilterType(this.equipOrder[currentEquipOrder]);
						game.item.setFirstItem(game.activeParty.members.get(currentStatusPlayerIndex));
					}
				}
				//↑.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
				{
					game.item.cursorUp();
					this.redrawStatusRequest = true;
				}
				//↓.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
				{
					game.item.cursorDown();
					this.redrawStatusRequest = true;
				}
				//BACK
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
				{
					//前の装備部位に戻る.
					currentEquipOrder--;
					if(currentEquipOrder < 0)
					{
						this.setTextAndShow(" ");
						MODE = CampMode.WaitingWhatToDoStatus;
					}
					else
					{
						game.item.initWithNoUpdate();
						game.item.setEquipStyle();
						game.item.setFilterType(this.equipOrder[currentEquipOrder]);
						game.item.setFirstItem(game.activeParty.members.get(currentStatusPlayerIndex));

					}
				}
			}

			else if(MODE == CampMode.WaitingMagicSpellerSelect)
			{
				//決定.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
				{
					if(usePlayer.magics.size() == 0)
					{
						this.setTextAndShow("魔法を覚えていない！");
					}
					else
					{
						this.setTextAndShow(" ");
						usePlayer.stopSelectedAnimation();
						game.activeParty.activePlayerIndex = currentTargetPlayerIndex;
						MODE = CampMode.WaitingMagicSelect;
					}
				}
				//→.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
				{
					this.movePlayerTarget("right");
					usePlayer = targetPlayer;
				}
				//←
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
				{
					this.movePlayerTarget("left");
					usePlayer = targetPlayer;
				}
				//戻る.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
				{
					this.setTextAndShow(" ");
					currentTargetPlayerIndex = 0;
					usePlayer.stopSelectedAnimation();
					MODE = CampMode.WaitingWhatToDo;
				}
			}
			else if(MODE == CampMode.WaitingMagicSelect)
			{
				//魔法決定.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//TODO.
					//魔法の使用.
					currentMagic = game.magic.get();
					
					//
					//	戦闘中に使用可能?
					//
					if(!currentMagic.usableDungeon)
					{
						this.setTextAndShow(" ");
						game.magic.updateMagicDescriptionWithString("この魔法は使えない！");
						return;
					}
					
					//MP不足?
					if(usePlayer.mp < currentMagic.mp)
					{
						this.setTextAndShow(" ");
						game.magic.updateMagicDescriptionWithString("MPが足りない！");
						return;
					}
					
					//使用対象を選択.
					targetPlayer = game.activeParty.members.get(0);
					MODE = CampMode.WaitingMagicTargetSelect;
					targetPlayer.startSelectedAnimation();
					game.magic.updateMagicDescriptionWithString("");
					this.setTextAndShow("誰に使う？");
				}
				//上.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
				{
					this.setTextAndShow(" ");
					game.magic.cursorUp();
				}
				//下.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
				{
					this.setTextAndShow(" ");
					game.magic.cursorDown();
				}
				//戻る.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
				{
					game.activeParty.activePlayerIndex = -1;
					this.setTextAndShow("誰の魔法？");
					usePlayer.startSelectedAnimation();
					MODE = CampMode.WaitingMagicSpellerSelect;
				}
			}
			else if(MODE == CampMode.WaitingMagicTargetSelect)
			{
				//決定.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
				{
					
					String resultText = usePlayer.name + "は" + currentMagic.name + "を" + targetPlayer.name + "に使った！\n";
					
					usePlayer.mp -= currentMagic.mp;
					
					if(currentMagic.cureHp > 0 && targetPlayer.hp < targetPlayer.maxHp)
					{
						int healAmount = Global.rand.nextInt(currentMagic.cureHp/2) + currentMagic.cureHp/2;
						targetPlayer.healHp(healAmount);
						resultText += targetPlayer.name + "はHPが" + healAmount + "回復！\n";
					}
					if(currentMagic.cureMp > 0 && targetPlayer.mp < targetPlayer.maxMp)
					{
						int healAmount = Global.rand.nextInt(currentMagic.cureMp/2) + currentMagic.cureMp/2;
						targetPlayer.healMp(healAmount);
						resultText += targetPlayer.name + "はMPが" + healAmount + "回復！\n";
					}

					usePlayer.setUpdateTexture();
					targetPlayer.setUpdateTexture();
					targetPlayer.stopSelectedAnimation();
					//game.activeParty.activePlayerIndex = -1;
					this.setTextAndShow(resultText);
					
					MODE = CampMode.WaitingMagicSelect;
				}
				//→.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
				{
					this.movePlayerTarget("right");
				}
				//←
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
				{
					this.movePlayerTarget("left");
				}
				//戻る.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
				{
					this.setTextAndShow(" ");
					MODE = CampMode.WaitingMagicSelect;
				}
			}
			else if(MODE == CampMode.WaitingItemSelect)
			{
				//捨てるかどうか.
				if(isConfirm)
				{
					//YES.
					if(game.touchGlX > -0.2f && game.touchGlX < 0.8f && game.touchGlY > -0.05f && game.touchGlY < 0.45f)
					{
						ItemBase targetItem = game.item.get();
						this.setTextAndShow(targetItem.name + "を捨てました。");
						game.activeParty.items.remove(targetItem);
						game.item.setUpdateRequest();
						isConfirm = false;
						game.item.MODE = ItemType.ITEM_MODE_SELECT_FILTER;
					}
					
					//NO.
					if(game.touchGlX > -1.4f && game.touchGlX < -0.4f && game.touchGlY > -0.05f && game.touchGlY < 0.45f)
					{
						this.setTextAndShow("やめとこう");
						isConfirm = false;	
					}
				}
				else
				{
					//アイテム決定.
					if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
					{
						if(game.item.currentItemSet.size() == 0)
							return;
						
						if(game.item.MODE == ItemType.ITEM_MODE_SELECT_FILTER)
						{
							game.item.MODE = ItemType.ITEM_MODE_SELECT_ITEM;
						}
						else
						{
							//TODO.
							//アイテムの使用.
							//アイテムの在庫減らす.
							currentItem = game.item.get();
							
							//
							//	キャンプ中に使用可能?
							//
							if(!currentItem.usableDungeon)
							{
								game.item.updateItemDescriptionWithString("このアイテムは使えない！");
								return;
							}
							
							//使用対象を選択.
							targetPlayer = game.activeParty.members.get(0);
							MODE = CampMode.WaitingItemTargetSelect;
							targetPlayer.startSelectedAnimation();
							game.item.updateItemDescriptionWithString("");
							this.setTextAndShow("誰に使う？");
						}
					}
					//上.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
					{
						//if(game.item.currentItemSet.size() == 0)
						//	return;
	
						this.setTextAndShow(" ");
						game.item.cursorUp();
					}
					//下.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f  + menuYPos&& game.touchGlY < -0.45f + menuYPos)
					{
						//if(game.item.currentItemSet.size() == 0)
						//	return;
	
						this.setTextAndShow(" ");
						game.item.cursorDown();
					}
					//捨てる.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
					{
						ItemBase targetItem = game.item.get();
						if(targetItem != null)
						{
							isConfirm = true;
							this.setTextAndShow(targetItem.name + "を捨てますか？");
						}
					}
					//戻る.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
					{
						if(game.item.MODE == ItemType.ITEM_MODE_SELECT_ITEM)
							game.item.MODE = ItemType.ITEM_MODE_SELECT_FILTER;
						else
							MODE = CampMode.WaitingWhatToDo;
					}
				}
			}
			else if(MODE == CampMode.WaitingItemTargetSelect)
			{
				//ターゲット決定.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
				{
					String resultText = currentItem.name + "を" + targetPlayer.name + "に使った\n";
					
					boolean isUsed = true;

					//死亡時は使えない.
					if(!targetPlayer.isAlive)
					{
						resultText += targetPlayer.name + "は死んでいる！";
						isUsed = false;
					}
					else
					{
						if(currentItem.hp > 0 && targetPlayer.hp < targetPlayer.maxHp)
						{
							int healAmount = Global.rand.nextInt(currentItem.hp/2) + currentItem.hp/2;
							targetPlayer.healHp(healAmount);
							resultText += targetPlayer.name + "はHPが" + healAmount + "回復！\n";
						}
						if(currentItem.mp > 0 && targetPlayer.mp < targetPlayer.maxMp)
						{
							int healAmount = Global.rand.nextInt(currentItem.mp/2) + currentItem.mp/2;
							targetPlayer.healMp(healAmount);
							resultText += targetPlayer.name + "はMPが" + healAmount + "回復！\n";
						}
					}
	
					//アイテム減らす.
					if(isUsed)
					{
						currentItem.stock--;
						if(currentItem.stock == 0)
						{
							game.activeParty.items.remove(currentItem);
						}
					}


					targetPlayer.setUpdateTexture();
					targetPlayer.stopSelectedAnimation();
					this.setTextAndShow(resultText);
					game.item.setUpdateRequest();
					
					MODE = CampMode.WaitingItemSelect;

				}
				//上.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
				{
					this.movePlayerTarget("right");
				}
				//下.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
				{
					this.movePlayerTarget("left");
				}
				//戻る.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
				{
					targetPlayer.stopSelectedAnimation();
					targetPlayer = null;
					currentItem  = null;
					MODE = CampMode.WaitingItemSelect;
				}
			}
		}
		
		this.isTouch = game.isTouch;
	}
	

	private void moveStatusIndex(String direct)
	{
		if(direct.equals("left"))
		{
			//選択したプレイヤーを【選択済み】状態にする.
			game.activeParty.members.get(currentStatusPlayerIndex).stopSelectedAnimation();

			currentStatusPlayerIndex--;
			if(currentStatusPlayerIndex < 0)
				currentStatusPlayerIndex = game.activeParty.members.size()-1;
		}
		else
		{
			//選択したプレイヤーを【選択済み】状態にする.
			game.activeParty.members.get(currentStatusPlayerIndex).stopSelectedAnimation();
			
			currentStatusPlayerIndex++;
			if(currentStatusPlayerIndex >  game.activeParty.members.size()-1)
				currentStatusPlayerIndex = 0;
		}
	}

	
	private void movePlayerTarget(String direct)
	{
		if(direct.equals("left"))
		{
			//選択したプレイヤーを【選択済み】状態にする.
			game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();

			currentTargetPlayerIndex--;
			if(currentTargetPlayerIndex < 0)
				currentTargetPlayerIndex = game.activeParty.members.size()-1;

			Player pl = game.activeParty.members.get(currentTargetPlayerIndex);
			pl.startSelectedAnimation();
			targetPlayer = pl;
		}
		else
		{
			//選択したプレイヤーを【選択済み】状態にする.
			game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
			
			currentTargetPlayerIndex++;
			if(currentTargetPlayerIndex >  game.activeParty.members.size()-1)
				currentTargetPlayerIndex = 0;
			
			Player pl = game.activeParty.members.get(currentTargetPlayerIndex);
			pl.startSelectedAnimation();
			targetPlayer = pl;
			
		}
	}
}
