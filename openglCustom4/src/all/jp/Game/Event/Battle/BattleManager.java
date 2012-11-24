package all.jp.Game.Event.Battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


import javax.microedition.khronos.opengles.GL11;

import cx.fam.tak0294.storage.DBHelper;

import all.jp.Game.GameMode;
import all.jp.Game.Base.GameManager;
import all.jp.Game.Character.CharacterBase;
import all.jp.Game.Character.Enemy;
import all.jp.Game.Character.Player;
import all.jp.Game.Item.ItemBase;
import all.jp.Game.Item.ItemType;
import all.jp.Game.Item.Weapon.WeaponType;
import all.jp.Game.Magic.MagicBase;
import all.jp.Game.Magic.MagicType;
import all.jp.util.FontTexture;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;

public class BattleManager
{

	
	private GameManager game;
	private BattleAnimation animation;
	
	private int MODE;
	private int mainWindowTexture;
	private int subWindowTexture;
	private int mainMenuTexture;
	private int fightButtonTexture;
	private int guardButtonTexture;
	private int fightTargetOkButtonTexture;
	private int itemButtonTexture;
	private int menuUpButtonTexture;
	private int menuRightButtonTexture;
	private int magicButtonTexture;
	private int backButtonTexture;
	private int escapeButtonTexture;
	private FontTexture fontTexture;

	
	private boolean isCursorEnd 	= false;
	private int currentLineIndex  	= 0;
	private int currentCharIndex    = 0;
	private ArrayList<Integer> lineIndexes;
	private String currentEventText;
	private boolean nextPageRequest = false;
	private boolean skipPageRequest = false;
	
	//入力受付停止ウェイト.
	private int inputWait = 0;
	
	//エンカウントアニメーション用.
	private float flashAlpha = 0.8f;
	
	//現在エンカウント中の敵.
	private ArrayList<HashMap<String, String>> activeEnemiesArray;
	private ArrayList<Enemy> activeEnemies;
	private boolean requestTextureLoad = false;
	private int currentTargetEnemyIndex = 0;
	private int currentTargetPlayerIndex = 0;
	
	//行動順.
	private ArrayList<CharacterBase> turnList;
	private int currentTurnIndex = 0;
	private int currentGroupNumCount = 0;
	private int currentGroupNum      = 0;
	private int whatToDoDecidedCount = 0;
	
	//戦闘終了フラグ.
	private boolean isBattleEnded = false;
	private boolean isReleasedTexture = false;
	private boolean isRunAway = false;
	private boolean isGameOver = false;
	
	//獲得経験値、ゴールド.
	private int resultExp;
	private int resultGold;

	//メニュー位置調整.
	private float menuYPos = 0.7f;
	
	//====================================================
	//	コンストラクタ.
	//====================================================
	public BattleManager(GameManager game)
	{
		this.game = game;
		MODE = BattleMode.Waiting;
		this.animation = new BattleAnimation(game);
		this.fontTexture = new FontTexture();
		this.fontTexture.maxLineCount = 3;

	}
	
	public void setMode(int mode)
	{
		this.MODE = mode;
	}

	//====================================================
	//	テクスチャ初期化.
	//====================================================
	public void initTextures(GL11 gl)
	{
		this.fontTexture.createTextBuffer(gl);
		mainWindowTexture  = GraphicUtil.loadTexture(gl, "battlemainwindow");
		subWindowTexture   = GraphicUtil.loadTexture(gl, "battlesubwindow");
		mainMenuTexture    = GraphicUtil.loadTexture(gl, "battlemainmenu");
		fightButtonTexture = GraphicUtil.loadTexture(gl, "fight_button");
		guardButtonTexture = GraphicUtil.loadTexture(gl, "guard_button");
		itemButtonTexture  = GraphicUtil.loadTexture(gl, "item_button");
		menuUpButtonTexture = GraphicUtil.loadTexture(gl, "menu_up_button");
		menuRightButtonTexture = GraphicUtil.loadTexture(gl, "menu_right_button");
		escapeButtonTexture    = GraphicUtil.loadTexture(gl, "escape_button"); 
		
		fightTargetOkButtonTexture = GraphicUtil.loadTexture(gl, "fight_target_ok_button");
		magicButtonTexture = GraphicUtil.loadTexture(gl, "magic_button");
		backButtonTexture = GraphicUtil.loadTexture(gl, "back_button");
		GraphicUtil.loadTexture(gl, "battlemenuindex");
	}
	
	//========================================
	//	初期化処理を行う.
	//========================================
	private void init()
	{
		activeEnemiesArray	= new ArrayList<HashMap<String, String>>();
		activeEnemies		= new ArrayList<Enemy>();
		requestTextureLoad  = false;
		currentTurnIndex = 0;
		currentGroupNumCount = 0;
		currentGroupNum		 = 0;
		isBattleEnded		 = false;
		isReleasedTexture	 = false;
		isRunAway			 = false;
		isGameOver			 = false;
		inputWait			 = 0;
		flashAlpha = 0.8f;
		resultExp = 0;
		resultGold = 0;
		currentTargetEnemyIndex = 0;
		currentTargetPlayerIndex = 0;
	}
	
	public void setupBattle(String enemy_codes)
	{
		//	初期化.
		this.init();
		
		//名前プレート移動.
		game.activeParty.setNamePlateBattlePosition();
		
		//========================================
		//	DBから敵取得.
		//========================================
		//enemy_codes = 1|2|3
		DBHelper db = new DBHelper(game.activity);
		String[] codes_array = enemy_codes.split(";");
		for(int ii=0;ii<codes_array.length;ii++)
		{
			HashMap<String,String> in = new HashMap<String,String>();
			in.put("chara_code", codes_array[ii]);
			HashMap<String, String> res = db.get("enemy_mt", in).get(0); 
			activeEnemiesArray.add(res);
		}

		//テクスチャ読み込みフラグOn.
		requestTextureLoad = true;

		//========================================
		//	パーティの一人目をアクティブにする.
		//========================================
		game.activeParty.activePlayerIndex = 0;
		whatToDoDecidedCount = 0;

		
		MODE = BattleMode.EncountAnime1;
		
		//this.srcRowIndex = -1;
		//this.readNextEvent();
		//this.nextTurn();
	}

	
	//====================================================
	//	プレイヤーに行動を選択させる.
	//====================================================
	private void askWhatToDo()
	{
		//====================================================
		//	全員の行動を決定したら戦闘開始.
		//====================================================
		if(game.activeParty.activePlayerIndex >= game.activeParty.members.size())
		{
			this.decideTurn();
			return;
		}
		
		CharacterBase currentCharacter = game.activeParty.members.get(game.activeParty.activePlayerIndex);
		
		//死んでたら飛ばす.
		if(!currentCharacter.isAlive)
		{
			game.activeParty.activePlayerIndex++;
			this.askWhatToDo();
		}
		else
		{
			MODE = BattleMode.WaitingWhatTodo;
			this.setTextAndShow(currentCharacter.name + "はどうする？");
		}
		
	}
	
	
	
	//====================================================
	//	プレイヤーにアイテムを選択させる.
	//====================================================
	private void askWhatItem()
	{
		game.item.init();
		MODE = BattleMode.WaitingItemSelect;
		this.setTextAndShow("");
	}


	//====================================================
	//	プレイヤーに魔法を選択させる.
	//====================================================
	private void askWhatMagic()
	{
		game.magic.init();
		MODE = BattleMode.WaitingMagicSelect;
		this.setTextAndShow("");
	}

	
	//====================================================
	//	順番を決める.
	//====================================================
	private void decideTurn()
	{
		//順番リスト配列を初期化.
		this.turnList = new ArrayList<CharacterBase>();
		this.currentTurnIndex = -1;
		this.currentGroupNumCount = 1;
		this.currentGroupNum = 0;

		for(int ii=0;ii<game.activeParty.members.size();ii++)
		{
			if(game.activeParty.members.get(ii).isAlive)
				turnList.add(game.activeParty.members.get(ii));
		}
		
		for(int ii=0;ii<this.activeEnemies.size();ii++)
		{
			//TODO.
			//ちゃんと計算する.
			//敵の行動を決定しておく.
			if(this.activeEnemies.get(ii).isAlive)
			{
				this.activeEnemies.get(ii).decideBattleAct();
				turnList.add(this.activeEnemies.get(ii));
			}
		}
		
		//並べ替え.
		Collections.sort(turnList, new Comparator<CharacterBase>(){
			public int compare(CharacterBase c1, CharacterBase c2) {
				return c2.dex - c1.dex;
			}
		});
		
		this.nextTurn();
		
		System.out.println("decideTurn");
	}

	//====================================================
	//	次のターンを実行する.
	//====================================================
	private void nextTurn()
	{
		if(this.currentGroupNumCount < this.currentGroupNum)
		{
			this.currentGroupNumCount++;
		}
		else
		{
			this.currentTurnIndex++;
			this.currentGroupNumCount = 1;
		}
		
		if(this.currentTurnIndex == this.turnList.size())
		{
			System.out.println("全員ターン終了");
			resetTurn();
			return;
		}
		
		CharacterBase currentCharacter = this.turnList.get(this.currentTurnIndex);
		CharacterBase targetCharacter  = (CharacterBase) this.turnList.get(this.currentTurnIndex).Target;

		//死亡していたら次へ回す.
		if(!currentCharacter.isAlive)
		{
			nextTurn();
			return;
		}

		
		this.currentGroupNum = currentCharacter.groupNum;
		
		
		//------------------------------------------
		//	ターンイン時のメッセージ表示.
		//------------------------------------------
		if(currentCharacter.BattleActType == BattleActType.Fight)
		{
			this.setTextAndShow(currentCharacter.name + "の攻撃！");
		}			
		else if(currentCharacter.BattleActType == BattleActType.Guard)
		{
			this.resultTurn();
			return;
		}
		else if(currentCharacter.BattleActType == BattleActType.Item)
		{
			ItemBase item = (ItemBase) currentCharacter.Use;
			this.setTextAndShow(currentCharacter.name + "は" + targetCharacter.name + "に" + item.name + "を使った！");
		}
		else if(currentCharacter.BattleActType == BattleActType.Magic)
		{
			MagicBase magic = (MagicBase) currentCharacter.Use;
			this.setTextAndShow(currentCharacter.name + "は" + targetCharacter.name + "に" + magic.name + "を使った！");
		}
		else if(currentCharacter.BattleActType == BattleActType.Run)
		{
			this.resultTurn();
			return;
		}

		//戦闘ターン開始.
		MODE = BattleMode.EnteringTurn;
		
	}


	//====================================================
	//	ターンの実行結果.
	//====================================================
	private void resultTurn()
	{
		CharacterBase currentCharacter = this.turnList.get(this.currentTurnIndex);
		CharacterBase targetCharacter  = (CharacterBase) this.turnList.get(this.currentTurnIndex).Target;
		String resultText = "";
		
		//===================================================================================
		//	行動.
		//		逃走.
		//===================================================================================
		if(currentCharacter.BattleActType == BattleActType.Run)
		{
			//TODO
			//確率計算を行う
			boolean isRunSuccess = false;
			resultText += currentCharacter.name + "は逃げようとして\n";

			int rand = Global.rand.nextInt(100);
			if(rand < 70)
				isRunSuccess = true;
			
			if(isRunSuccess)
			{
				resultText += "成功した！\n";
				isRunAway 		= true;
				isBattleEnded 	= true;
			}
			else
			{
				resultText += "回りこまれた！\n";
			}
		}
		//===================================================================================
		//	行動.
		//		戦う.
		//===================================================================================
		else if(currentCharacter.BattleActType == BattleActType.Fight)
		{

			//=========================================================
			//	行動の対象が死んでいる.
			//=========================================================
			if(!targetCharacter.isAlive)
			{
				//====================================================
				//	攻撃ターゲットがEnemy.
				//====================================================
				if(targetCharacter.type.equals("enemy"))
				{
					//TODO.
					//Patch	生きているターゲットを取得.
					for(int ii=0;ii<this.activeEnemies.size();ii++)
					{
						if(this.activeEnemies.get(ii).isAlive)
							targetCharacter = this.activeEnemies.get(ii);
					}
				}
				//====================================================
				//	攻撃ターゲットがPlayer.
				//====================================================
				else
				{
					resultText = "しかし"+targetCharacter.name + "は死んでいる！\n";
				}
			}
			
			//=========================================================
			//	行動の対象が生きている.
			//=========================================================
			if(targetCharacter.isAlive)
			{
				int damage = BattleDamageCalculator.getDq3Damage(currentCharacter, targetCharacter);
				
				resultText += targetCharacter.name + "に" + damage + "のダメージ！\n";
				
				//	HPを減らす.
				targetCharacter.hp -= damage;
				
				//====================================================
				//	ターゲットがEnemy.
				//====================================================
				if(targetCharacter.type.equals("enemy"))
				{
					
					this.animation.setWeaponEffect(currentCharacter.getWeaponType(), targetCharacter);
					targetCharacter.isHitAnimation = true;
				}
				//====================================================
				//	ターゲットがPlayer.
				//====================================================
				else if(targetCharacter.type.equals("player"))
				{
					targetCharacter.isHitAnimation = true;
					targetCharacter.setUpdateTexture();
				}
				
			}
		}
		//===================================================================================
		//	行動.
		//		身を守る.
		//===================================================================================
		else if(currentCharacter.BattleActType == BattleActType.Guard)
		{
			resultText = currentCharacter.name + "は身を守っている！";
		}
		//===================================================================================
		//	行動.
		//		アイテムを使う.
		//===================================================================================
		else if(currentCharacter.BattleActType == BattleActType.Item)
		{
			//使用するアイテム.
			ItemBase item = (ItemBase) currentCharacter.Use;
			
			
			boolean isUsed = true;
			
			//死亡時は使えない.
			if(!targetCharacter.isAlive)
			{
				resultText += targetCharacter.name + "は死んでいる！";
				isUsed = false;
			}
			else
			{
				if(item.hp > 0 && targetCharacter.hp < targetCharacter.maxHp)
				{
					int healAmount = Global.rand.nextInt(item.hp/2) + item.hp/2;
					targetCharacter.healHp(healAmount);
					resultText += targetCharacter.name + "はHPが" + healAmount + "回復！\n";
				}
				if(item.mp > 0 && targetCharacter.mp < targetCharacter.maxMp)
				{
					int healAmount = Global.rand.nextInt(item.mp/2) + item.mp/2;
					targetCharacter.healMp(healAmount);
					resultText += targetCharacter.name + "はMPが" + healAmount + "回復！\n";
				}
				
				if(item.def > 0)
				{
					targetCharacter.addBuff(item);
					resultText += targetCharacter.name + "は防御力が上がった！\n";
				}
				if(item.str > 0)
				{
					targetCharacter.addBuff(item);
					resultText += targetCharacter.name + "は攻撃力が上がった！\n";
				}
				if(item.dex > 0)
				{
					targetCharacter.addBuff(item);
					resultText += targetCharacter.name + "は素早さが上がった！\n";
				}
			}

			//アイテム減らす.
			if(isUsed)
			{
				item.stock--;
				if(item.stock == 0)
				{
					game.activeParty.items.remove(item);
				}
			}
			
			if(resultText.equals(""))
				resultText = "効果はなかった";
			else
				targetCharacter.setUpdateTexture();
		}
		//===================================================================================
		//	行動.
		//		魔法を使う.
		//===================================================================================
		else if(currentCharacter.BattleActType == BattleActType.Magic)
		{
			//=========================================================
			//	行動の対象が死んでいる.
			//=========================================================
			if(!targetCharacter.isAlive)
			{
				if(targetCharacter.type.equals("enemy"))
				{
					//Patch	生きているターゲットを取得.
					for(int ii=0;ii<this.activeEnemies.size();ii++)
					{
						if(this.activeEnemies.get(ii).isAlive)
							targetCharacter = this.activeEnemies.get(ii);
					}
				}
				else
				{
					resultText = "しかし"+targetCharacter.name + "は死んでいる！\n";
				}
			}
			
			//使用する魔法.
			MagicBase magic = (MagicBase) currentCharacter.Use;
			currentCharacter.mp -= magic.mp;
			
			//ターゲットがEnemy.
			if(targetCharacter.type.equals("enemy"))
			{
				animation.setAttackMagicEffect(magic.effectType, targetCharacter);
				
				//ダメージ量計算.
				int damage = BattleDamageCalculator.getDq3MagicDamage(magic, targetCharacter);

				resultText += targetCharacter.name + "に" + damage + "のダメージ！\n";
				targetCharacter.isHitAnimation = true;
				
				//	HPを減らす.
				targetCharacter.hp -= damage;
				
			}
			//ターゲットがPlayer.
			else
			{
				animation.setCureMagicEffect(magic.effectType, targetCharacter);
				
				if(magic.cureHp > 0 && targetCharacter.hp < targetCharacter.maxHp)
				{
					int healAmount = Global.rand.nextInt(magic.cureHp/2) + magic.cureHp/2;
					targetCharacter.healHp(healAmount);
					resultText += targetCharacter.name + "はHPが" + healAmount + "回復！\n";
				}
				if(magic.cureMp > 0 && targetCharacter.mp < targetCharacter.maxMp)
				{
					int healAmount = Global.rand.nextInt(magic.cureMp/2) + magic.cureMp/2;
					targetCharacter.healMp(healAmount);
					resultText += targetCharacter.name + "はMPが" + healAmount + "回復！\n";
				}
				
				if(magic.def > 0)
				{
					targetCharacter.addMagicBuff(magic);
					resultText += targetCharacter.name + "は防御力が上がった！\n";
				}
				if(magic.str > 0)
				{
					targetCharacter.addMagicBuff(magic);
					resultText += targetCharacter.name + "は攻撃力が上がった！\n";
				}
				if(magic.dex > 0)
				{
					targetCharacter.addMagicBuff(magic);
					resultText += targetCharacter.name + "は素早さが上がった！\n";
				}
				
				if(resultText.equals(""))
					resultText = "効果はなかった";
			}
			
			currentCharacter.setUpdateTexture();
			targetCharacter.setUpdateTexture();
		}


		
		if(currentCharacter.BattleActType != BattleActType.Guard &&
		   currentCharacter.BattleActType != BattleActType.Run)
		{
			//===================================================================================
			//	死亡確認.
			//===================================================================================
			if(targetCharacter.type.equals("enemy"))
			{
				//死んだ.
				if(targetCharacter.hp <= 0)
				{
					resultText += targetCharacter.name + "は死んだ\n";
					resultGold += Global.rand.nextInt(targetCharacter.gold) + 1;

					//ステータスをリセットする.
					targetCharacter.hp = targetCharacter.maxHp;
					targetCharacter.mp = targetCharacter.maxMp;
					targetCharacter.groupNum--;
					targetCharacter.setUpdateTexture();

					//グループ数が０ = その敵グループの消滅.
					if(targetCharacter.groupNum == 0)
					{
						targetCharacter.isAlive = false;

						//全てのグループを倒した？
						int activeCnt = 0;
						for(int ii=0;ii<this.activeEnemies.size();ii++)
						{
							if(this.activeEnemies.get(ii).isAlive)
								activeCnt++;
						}

						//戦闘終了.
						if(activeCnt == 0)
						{
							isBattleEnded = true;
							resultText += "モンスターは全滅した\n";
							resultText += "それぞれ" + resultExp + "の経験値を手に入れた\n";
							resultText += resultGold + "Goldを手に入れた\n";
							game.activeParty.gold += resultGold;
							resultText += this.makeEndBattleString(resultExp);
						}
					}
				}
			}
			//===================================================================================
			//	ターゲットがPlayer.
			//===================================================================================
			else if(targetCharacter.type.equals("player"))
			{
				//死んだ.
				if(targetCharacter.isAlive && targetCharacter.hp <= 0)
				{
					resultText += targetCharacter.name + "は死んだ\n";
					targetCharacter.hp = 0;
					targetCharacter.isAlive = false;
					
					//全滅判定.
					int aliveCount = 0;
					for(int ii=0;ii<game.activeParty.members.size();ii++)
					{
						if(game.activeParty.members.get(ii).isAlive)
							aliveCount++;
					}
					if(aliveCount == 0)
					{
						resultText += "パーティは全滅した";
						isRunAway 		= true;
						isBattleEnded 	= true;
						isGameOver		= true;
					}
				}

				targetCharacter.setUpdateTexture();
			}
		}
		
		this.setTextAndShow(resultText);
		
		//戦闘ターン結果.
		if(isBattleEnded)
			MODE = BattleMode.Text;
		else
			MODE = BattleMode.ResultTurn;
	}
	

	//====================================================
	//	ターンの終了時.
	//====================================================
	private void resetTurn()
	{
		for(int ii=0;ii<game.activeParty.members.size();ii++)
		{
			game.activeParty.members.get(ii).guardDef = 0;		//防御中の解除.
			game.activeParty.members.get(ii).reduceBuffTurn();	//バフの残りターン数を減らす.
		}
		
		game.activeParty.activePlayerIndex = 0;
		this.askWhatToDo();
	}
	
	//====================================================
	//	プレイヤーに敵を選択させる.
	//====================================================
	private void askAttackTarget()
	{
		MODE = BattleMode.WaitingAttackTargetSelect;
		this.setTextAndShow("ターゲットを選択してください。");
	}

	//====================================================
	//	プレイヤーにアイテムの使用対象を選ばせる.
	//====================================================
	private void askUseTarget()
	{
		MODE = BattleMode.WaitingItemTargetSelect;
		this.setTextAndShow("アイテムを使う対象を選択してください。");
	}

	//====================================================
	//	プレイヤーに魔法の使用対象を選ばせる.
	//====================================================
	private void askMagicTarget()
	{
		MODE = BattleMode.WaitingMagicTargetSelect;
		this.setTextAndShow("魔法を使う対象を選択してください。");
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


	
	//=================================================================================================
	//
	//	描画関数.
	//
	//
	//=================================================================================================
	public void draw(GL11 gl)
	{
		//------------------------------------------------------------
		//	エンカウント中の敵.
		//------------------------------------------------------------
		if(requestTextureLoad)
		{
			for(int ii=0;ii<this.activeEnemiesArray.size();ii++)
			{
				Enemy en = new Enemy(game, activeEnemiesArray.get(ii));
				en.initTexture(gl, this.activeEnemiesArray.get(ii).get("chara_imageName"));
				//TODO
				//xの計算これでいいのか?
				float width = 0.6f;
				if(this.activeEnemiesArray.size() == 4)
					width = 0.5f;
				en.x = (ii*width) - ((width*this.activeEnemiesArray.size()))*0.5f;
				this.activeEnemies.add(en);
				
				//獲得経験値などの設定.
				resultExp += en.exp * en.groupNum;
			}
			
			requestTextureLoad = false;
		}
		
		//------------------------------------------------------------
		//	エンカウントアニメーション中.
		//------------------------------------------------------------
		if(MODE == BattleMode.EncountAnime1)
		{
			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GraphicUtil.drawRectangle(gl, 0.0f, 0.0f, 3.0f, 3.0f, 1.0f, 1.0f, 1.0f, flashAlpha);
			gl.glDisable(GL11.GL_BLEND);
			
			flashAlpha -= 0.05f;
			
			if(flashAlpha < 0)
			{
				flashAlpha = 0.0f;
				this.askWhatToDo();
			}
			
			game.renderer.particleSystem.addSparks(0.0f, 0.0f, 0.0f, 0.05f, 0.09f, 5, 0);
			
			return;
		}
		
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//ウィンドウ枠の描画.
		GraphicUtil.drawTexture(gl, -0.3f, 0.2f, 2.3f, 1.5f, mainWindowTexture, 1.0f, 1.0f, 1.0f, 0.5f);
		GraphicUtil.drawTexture(gl, -0.3f, -0.8f, 2.3f, 0.35f, subWindowTexture, 1.0f, 1.0f, 1.0f, 1.0f);
	
		for(int ii=0;ii<this.activeEnemies.size();ii++)
		{
			this.activeEnemies.get(ii).draw(gl);
		}

		if(inputWait > 0)
		{
			inputWait--;
			gl.glDisable(GL11.GL_BLEND);
			return;
		}
		
		//メニュー枠の描画.
		if(MODE == BattleMode.WaitingWhatTodo ||
		   MODE == BattleMode.WaitingAttackTargetSelect ||
		   MODE == BattleMode.WaitingItemSelect ||
		   MODE == BattleMode.WaitingItemTargetSelect ||
		   MODE == BattleMode.WaitingMagicSelect ||
		   MODE == BattleMode.WaitingMagicTargetSelect)
		{
			GraphicUtil.drawTexture(gl, 1.19f, -0.53f + menuYPos, 0.5f, 1.55f, mainMenuTexture, 1.0f, 1.0f, 1.0f, 0.7f);
			
			if(MODE == BattleMode.WaitingWhatTodo)
			{
				GraphicUtil.drawTexture(gl, 1.19f, 0.05f + menuYPos, 0.4f, 0.2f, fightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.25f + menuYPos, 0.4f, 0.2f, guardButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.55f + menuYPos, 0.4f, 0.2f, itemButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				if(game.activeParty.getActivePlayer() != null && 
				   game.activeParty.getActivePlayer().magics.size() > 0)
					GraphicUtil.drawTexture(gl, 1.19f, -0.85f + menuYPos, 0.4f, 0.2f, magicButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				
				GraphicUtil.drawTexture(gl, 1.19f, -1.15f + menuYPos, 0.4f, 0.2f, escapeButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			}
			else if(MODE == BattleMode.WaitingAttackTargetSelect ||
					MODE == BattleMode.WaitingItemTargetSelect ||
					MODE == BattleMode.WaitingMagicTargetSelect)
			{
				GraphicUtil.drawTexture(gl, 1.19f, 0.05f + menuYPos, 0.4f, 0.2f, fightTargetOkButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.25f + menuYPos, 0.4f, 0.2f, menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.55f + menuYPos, -0.4f, 0.2f, menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.85f + menuYPos, 0.4f, 0.2f, backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			}
			else if(MODE == BattleMode.WaitingItemSelect ||
					MODE == BattleMode.WaitingMagicSelect)
			{
				GraphicUtil.drawTexture(gl, 1.19f, 0.05f + menuYPos, 0.4f, 0.2f, fightTargetOkButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.25f + menuYPos, 0.4f, 0.2f, menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.55f + menuYPos, 0.4f, -0.2f, menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.85f + menuYPos, 0.4f, 0.2f, backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			}
		}
		
		gl.glDisable(GL11.GL_BLEND);



		//------------------------------------------------------------
		//	テキスト描画モード.
		//------------------------------------------------------------
		if(MODE == BattleMode.Text ||
		   MODE == BattleMode.WaitingWhatTodo ||
		   MODE == BattleMode.WaitingItemSelect ||
		   MODE == BattleMode.WaitingAttackTargetSelect ||
		   MODE == BattleMode.EnteringTurn ||
		   MODE == BattleMode.DoingTurn ||
		   MODE == BattleMode.ResultTurn)
		{
			if(this.skipPageRequest)
			{
				this.currentCharIndex = this.lineIndexes.get(currentLineIndex)-1;
				this.skipPageRequest = false; 
			}
			
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
		
		
		//---------------------------------------------------------------------------------------
		//	アイテム選択画面表示.
		//---------------------------------------------------------------------------------------
		if(MODE == BattleMode.WaitingItemSelect)
		{
			game.item.draw(gl);
		}
		
		//---------------------------------------------------------------------------------------
		//	魔法選択画面表示.
		//---------------------------------------------------------------------------------------
		if(MODE == BattleMode.WaitingMagicSelect)
		{
			game.magic.draw(gl);
		}
		
		
		//---------------------------------------------------------------------------------------
		//	敵グラフィックのメモリ開放.
		//---------------------------------------------------------------------------------------
		if(isBattleEnded && !isReleasedTexture)
		{
			int releasedCount = 0;
			for(int ii=0;ii<this.activeEnemies.size();ii++)
			{
				if(this.activeEnemies.get(ii).release(gl, isRunAway))
				{
					releasedCount++;
				}
			}
			
			if(releasedCount == this.activeEnemies.size())
			{
				this.activeEnemies.clear();
				isReleasedTexture = true;
			}
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
		for(int ii=0;ii<this.activeEnemies.size();ii++)
		{
			this.activeEnemies.get(ii).update();
		}

		if(inputWait > 0)
		{
			inputWait--;
			return;
		}

		if(this.skipPageRequest)
			return;
		
		if(game.isTouch && !game.event.isTouch)
		{
			//--------------------------------------
			//	文章の表示途中の場合は一気に表示させる.
			//--------------------------------------
			if(MODE == BattleMode.Text ||
			   MODE == BattleMode.EnteringTurn ||
			   MODE == BattleMode.DoingTurn ||
			   MODE == BattleMode.ResultTurn)
			{
				if(!isCursorEnd)
				{
					this.skipPageRequest = true;
					//this.currentCharIndex = this.lineIndexes.get(currentLineIndex)-1;
					return;
				}
			}
			
			
			//--------------------------------------
			//	行動待ちモード.
			//--------------------------------------
			if(MODE == BattleMode.WaitingWhatTodo)
			{
				//戦う選択.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//プレイヤーの行動種類を［戦闘］にせってい.
					game.activeParty.getActivePlayer().BattleActType = BattleActType.Fight;

					//生きている一匹目の敵を選択状態にする.
					currentTargetEnemyIndex = 0;
					while(!this.activeEnemies.get(currentTargetEnemyIndex).isAlive && currentTargetEnemyIndex < this.activeEnemies.size())
					{
						currentTargetEnemyIndex++;
					}
					this.activeEnemies.get(currentTargetEnemyIndex).startSelectedAnimation();
					
					this.askAttackTarget();
				}
				//防御選択.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
				{
					//プレイヤーの行動種類を［防御］にせってい.
					game.activeParty.getActivePlayer().BattleActType = BattleActType.Guard;
					//防御中は一時的に防御力を上げる.
					game.activeParty.getActivePlayer().guardDef = 3;

					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
				}
				//アイテム選択.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
				{
					//プレイヤーの行動種類を［アイテム］にせってい.
					game.activeParty.getActivePlayer().BattleActType = BattleActType.Item;
					this.askWhatItem();
				}
				//魔法
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					if(game.activeParty.getActivePlayer().magics.size() > 0)
					{
						//プレイヤーの行動種類を［魔法］にせってい.
						game.activeParty.getActivePlayer().BattleActType = BattleActType.Magic;
						this.askWhatMagic();
					}
				}
				//逃走.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.3f + menuYPos && game.touchGlY < -1.05f + menuYPos)
				{
					//プレイヤーの行動種類を[逃走]に設定.
					game.activeParty.getActivePlayer().BattleActType = BattleActType.Run;
					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
				}
			}
			//--------------------------------------
			//	[攻撃]ターゲット選択モード.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingAttackTargetSelect)
			{
				//カーソル左へ.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
				{
					moveEnemyTarget("left");
				}
				//カーソル右へ.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
				{
					moveEnemyTarget("right");
				}
				
				
				//======================================================
				//	決定.
				//======================================================
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//選択した敵の【選択済み】状態を解除する.
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					//プレイヤーの［ターゲット］を設定.
					game.activeParty.getActivePlayer().Target = this.activeEnemies.get(currentTargetEnemyIndex);
					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
					//ウェイト設定.
					//this.inputWait = 5;
				}
				//戻る
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					//選択した敵の【選択済み】状態を解除する.
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					this.askWhatToDo();
				}
			}
			//--------------------------------------
			//	テキスト表示モード.
			//--------------------------------------
			else if(MODE == BattleMode.Text)
			{
				if(this.currentLineIndex < this.lineIndexes.size()-1 && !this.skipPageRequest)
				{
					nextPageRequest = true;
				}
				else
				{
					//---------------------------------------------------------
					//	戦闘終了.
					//---------------------------------------------------------
					if(isBattleEnded && isReleasedTexture)
					{
						//BGM止める.
						game.bgm.stop();
						
						//ネームプレート座標戻す.
						game.activeParty.setNamePlateDungeonPosition();
						
						if(!game.event.readNextEvent())
						{
							
							String res = "";
							//--------------------------------------------------
							//	宝箱の抽選.
							//--------------------------------------------------
							int treasureNum = 0;
							
							if(!isRunAway)
							{
								treasureNum = game.getTreasureNum();
							
								if(treasureNum > 0)
								{
									res += "image[treasure1|モンスターは宝箱を落としていった！]\n";
								}
	
								for(int ii=0;ii<treasureNum;ii++)
								{
									String treasure_rank = game.getTreasureRank();
									ArrayList<HashMap<String,String>> treasures = game.getTreasure(treasure_rank, Integer.toString(game.dungeon.currentPosition.floor+1));
	
									if(treasures.size() > 0)
									{
										HashMap<String, String> itemBase = treasures.get(0);
										ItemBase item = game.getItemByCode(itemBase.get("item_code"));
	
										res += "message[" + item.name + "を手に入れた！]\n";
										if(!game.activeParty.addItem(item, 1))
										{
											res += "message[もちものが一杯だ！"+item.name+"を諦めた]";
										}
									}
								}
							}
							
							if(treasureNum > 0)
							{
								ArrayList<HashMap<String, String>> eventData = new ArrayList<HashMap<String,String>>();
								HashMap<String,String> eventHash = new HashMap<String, String>();
								eventHash.put("event_src", res);
								eventData.add(eventHash);
								game.event.setEvent(eventData);
							}
							else
							{
								game.event.willClose = true;
								if(isGameOver)
								{
									game.event.clearEvent();
									game.gameMode = GameMode.Title;
								}
							}
							
						}
					}
					//willClose = true;
				}
			}
			//--------------------------------------
			//	ターン侵入.
			//--------------------------------------
			else if(MODE == BattleMode.EnteringTurn)
			{
				//タップ時にターン結果.
				this.resultTurn();
			}
			//--------------------------------------
			//	ターン結果.
			//--------------------------------------
			else if(MODE == BattleMode.ResultTurn)
			{
				//タップ時にターンを回す.
				this.nextTurn();
			}
			//--------------------------------------
			//	アイテム選択.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingItemSelect)
			{
				//カーソル上へ.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
				{
					game.item.cursorUp();
				}
				//カーソル下へ.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
				{
					game.item.cursorDown();
				}
				//決定.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//TODO.
					//アイテムの使用.
					//アイテムの在庫減らす.
					ItemBase item = game.item.get();
					
					//
					//	戦闘中に使用可能?
					//
					if(!item.usableBattle)
					{
						game.item.updateItemDescriptionWithString("このアイテムは使えない！");
						return;
					}
					
					//プレイヤーのUseプロパティにアイテムを設定.
					game.activeParty.getActivePlayer().Use = item;
					
					//使用対象に自分自身を設定.
					if(item.type == ItemType.ITEM_PLAYER)
					{
						//行動選択中プレイヤーを【選択状態】にする.
						game.activeParty.getActivePlayer().Target = game.activeParty.getActivePlayer();
						this.currentTargetPlayerIndex = game.activeParty.activePlayerIndex;
						game.activeParty.getActivePlayer().startSelectedAnimation();

					}
					else
					{
						//最初の敵を【選択状態】にする.
						game.activeParty.getActivePlayer().Target = this.activeEnemies.get(0);
						this.currentTargetEnemyIndex = 0;
						this.activeEnemies.get(0).startSelectedAnimation();
					}
					
					
					//使用対象を選択.
					this.askUseTarget();
				}
				//戻る
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					System.out.println("もどる");
					this.askWhatToDo();
				}
			}
			//--------------------------------------
			//	[アイテム]ターゲット選択モード.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingItemTargetSelect)
			{
				ItemBase item = (ItemBase) game.activeParty.getActivePlayer().Use;
				
				//------------------------------------------------------------------------------
				//	敵用の道具.
				//------------------------------------------------------------------------------
				if(item.type == ItemType.ITEM_ENEMY)
				{
					//カーソル左へ.
					if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
					{
						moveEnemyTarget("left");
					}
					//カーソル右へ.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
					{
						moveEnemyTarget("right");
					}
				}
				//------------------------------------------------------------------------------
				//	プレイヤー用の道具.
				//------------------------------------------------------------------------------
				else if(item.type == ItemType.ITEM_PLAYER)
				{
					//カーソル左へ.
					if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
					{
						movePlayerTarget("left");
					}
					//カーソル右へ.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f  + menuYPos && game.touchGlY < -0.15f + menuYPos)
					{
						movePlayerTarget("right");
					}
				}
				
				//======================================================
				//	決定.
				//======================================================
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//選択した敵,プレイヤーの【選択済み】状態を解除する.
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
					
					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
					//ウェイト設定.
					//this.inputWait = 5;
				}
				//戻る
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					System.out.println("もどる");
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
					MODE = BattleMode.WaitingItemSelect;
				}
			}
			//--------------------------------------
			//	魔法選択.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingMagicSelect)
			{
				//カーソル上へ.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
				{
					game.magic.cursorUp();
				}
				//カーソル下へ.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
				{
					game.magic.cursorDown();
				}
				//決定.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//TODO.
					//魔法の使用.
					MagicBase magic = game.magic.get();
					
					
					//
					//	MP足りてる?
					//
					if(magic.mp > game.activeParty.getActivePlayer().mp)
					{
						game.magic.updateMagicDescriptionWithString("MPが足りない！");
						return;
					}
					
					//
					//	戦闘中に使用可能?
					//
					if(!magic.usableBattle)
					{
						game.magic.updateMagicDescriptionWithString("この魔法は使えない！");
						return;
					}
					
					//プレイヤーのUseプロパティにアイテムを設定.
					game.activeParty.getActivePlayer().Use = magic;
					//使用対象に自分自身を設定.
					if(magic.type == MagicType.MAGIC_TYPE_CURE)
					{
						//行動選択中プレイヤーを【選択状態】にする.
						game.activeParty.getActivePlayer().Target = game.activeParty.getActivePlayer();
						this.currentTargetPlayerIndex = game.activeParty.activePlayerIndex;
						game.activeParty.getActivePlayer().startSelectedAnimation();
					}
					else if(magic.type == MagicType.MAGIC_TYPE_ATTACK)
					{
						//最初の敵を【選択状態】にする.
						game.activeParty.getActivePlayer().Target = this.activeEnemies.get(0);
						this.currentTargetEnemyIndex = 0;
						this.activeEnemies.get(0).startSelectedAnimation();
					}
					
					//使用対象を選択.
					this.askMagicTarget();
				}
				//戻る
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					System.out.println("もどる");
					this.askWhatToDo();
				}
			}
			//--------------------------------------
			//	[魔法]ターゲット選択モード.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingMagicTargetSelect)
			{
				MagicBase magic = (MagicBase) game.activeParty.getActivePlayer().Use;
				
				//------------------------------------------------------------------------------
				//	攻撃魔法.
				//------------------------------------------------------------------------------
				if(magic.type == MagicType.MAGIC_TYPE_ATTACK)
				{
					//カーソル左へ.
					if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
					{
						moveEnemyTarget("left");
					}
					//カーソル右へ.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
					{
						moveEnemyTarget("right");
					}
				}
				//------------------------------------------------------------------------------
				//	回復魔法.
				//------------------------------------------------------------------------------
				else if(magic.type == MagicType.MAGIC_TYPE_CURE)
				{
					//カーソル左へ.
					if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
					{
						movePlayerTarget("left");
					}
					//カーソル右へ.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
					{
						movePlayerTarget("right");
					}
				}
				
				//======================================================
				//	決定.
				//======================================================
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//MP減らす.
					//game.activeParty.getActivePlayer().mp -= magic.mp;
					
					//選択した敵,プレイヤーの【選択済み】状態を解除する.
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
					
					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
					//ウェイト設定.
					//this.inputWait = 5;
				}
				//戻る
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					System.out.println("もどる");
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
					MODE = BattleMode.WaitingMagicSelect;
				}
			}
		}
	}
	
	private void moveEnemyTarget(String direct)
	{
		if(direct.equals("left"))
		{
			//選択した敵を【選択済み】状態にする.
			this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
			
			currentTargetEnemyIndex--;
			if(currentTargetEnemyIndex < 0)
				currentTargetEnemyIndex = this.activeEnemies.size()-1;
			
			this.activeEnemies.get(currentTargetEnemyIndex).startSelectedAnimation();
			
			game.activeParty.getActivePlayer().Target = this.activeEnemies.get(currentTargetEnemyIndex); 
		}
		else
		{
			//選択した敵を【選択済み】状態にする.
			this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
			
			currentTargetEnemyIndex++;
			if(currentTargetEnemyIndex > this.activeEnemies.size()-1)
				currentTargetEnemyIndex = 0;
			
			this.activeEnemies.get(currentTargetEnemyIndex).startSelectedAnimation();
			game.activeParty.getActivePlayer().Target = this.activeEnemies.get(currentTargetEnemyIndex);
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
			game.activeParty.getActivePlayer().Target = pl;
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
			game.activeParty.getActivePlayer().Target = pl;
			
		}
	}
	
	private String makeEndBattleString(int exp)
	{
		String res = "";
		
		for(int ii=0;ii<game.activeParty.members.size();ii++)
		{
			Player pl = game.activeParty.members.get(ii);
			
			if(pl.isAlive && pl.isLevelUp(exp))
			{
				res += pl.name + "はレベルアップ！\n";
			}
		}

		
		return res;
	}
}
