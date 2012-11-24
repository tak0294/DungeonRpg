package all.jp.Game.Base;

import javax.microedition.khronos.opengles.GL11;
import java.util.ArrayList;
import java.util.HashMap;

import cx.fam.tak0294.storage.DBHelper;
import all.jp.Game.Direction;
import all.jp.Game.GameMode;
import all.jp.Game.Camp.CampManager;
import all.jp.Game.Character.Party;
import all.jp.Game.Character.Player;
import all.jp.Game.Dungeon.DungeonManager;
import all.jp.Game.Event.EventManager;
import all.jp.Game.Item.ItemBase;
import all.jp.Game.Item.ItemManager;
import all.jp.Game.Magic.MagicBase;
import all.jp.Game.Magic.MagicManager;
import all.jp.Game.Sound.BgmManager;
import all.jp.Game.Title.TitleManager;
import all.jp.Game.Town.TownManager;
import all.jp.util.GLSprite;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;
import all.jp.util.MyRenderer;
import all.jp.util.SpriteType;
import android.app.Activity;

public class GameManager
{
	public MyRenderer renderer;
	public Activity activity;
	public int gameMode;
	public DungeonManager dungeon;
	public EventManager event;
	public ItemManager item;
	public MagicManager magic;
	public CampManager camp;
	public TownManager town;
	public TitleManager title;
	
	public BgmManager bgm;
	
	//パーティ.
	public Party activeParty;
	
	//タッチ座標.
	public float touchX, touchY;
	public float touchGlX, touchGlY;
	public boolean isSurfaceTouch;
	public boolean isTouch;
	public boolean isA = false;
	public boolean isB = false;
	public boolean isUp = false;
	public boolean isDown = false;
	public boolean isLeft = false;
	public boolean isRight = false;
	public boolean isOverrayBlack = false;
	
	//コントローラ.
	public GLSprite leftButtonSprite;
	public GLSprite rightButtonSprite;
	public GLSprite upButtonSprite;
	public GLSprite downButtonSprite;
	public GLSprite checkButtonSprite;
	public GLSprite campButtonSprite;
	
	//共通ウィンドウ.
	public GLSprite bottomWindowSprite;

	public boolean isSavingOverlay = false;
	
	public GameManager(Activity activity)
	{
		this.activity = activity;

		//BGM.
		this.bgm = new BgmManager(this);
		
		//ダンジョンマネージャ初期化.
		this.dungeon = new DungeonManager(this);
		this.dungeon.loadMap("mapdata.dat");		//マップデータの読み込み.
		
		//イベントマネージャ初期化.
		this.event = new EventManager(this);
		
		//アイテムマネージャ初期化.
		this.item = new ItemManager(this);
		
		//マジックマネージャ初期化.
		this.magic = new MagicManager(this);
		
		//キャンプマネージャ初期化.
		this.camp = new CampManager(this);
		
		//パーティ.
		this.activeParty = new Party();
		
		//街.
		this.town = new TownManager(this);
		this.town.init();
		
		//タイトル.
		this.title = new TitleManager(this);
		
	}
	
	public void init()
	{
		//data load.
		
		//this.openTown();
		this.openTitle();
	}
	
	public void titleNext()
	{
		loadData();
		
		DBHelper db = new DBHelper(activity);
		ArrayList<HashMap<String,String>> res = db.get("saved_position", new HashMap<String,String>());
		if(!res.get(0).get("saved_floor").equals(""))
		{
			System.out.println("DUNGEON START");
			dungeon.currentPosition.floor = Integer.parseInt(res.get(0).get("saved_floor"));
			dungeon.currentPosition.x = Integer.parseInt(res.get(0).get("saved_x"));
			dungeon.currentPosition.y = Integer.parseInt(res.get(0).get("saved_y"));
			dungeon.currentPosition.direction = Integer.parseInt(res.get(0).get("saved_direction"));
			this.openDungeon();
		}
		else
		{
			System.out.println("TOWN START");
			dungeon.currentPosition.floor = 0;
			dungeon.currentPosition.x = 8;
			dungeon.currentPosition.y = 19;
			dungeon.currentPosition.direction = Direction.North;
			this.openTown();
		}
	}

	public void loadData()
	{
		//TODO.
		//debug.
		DBHelper db = new DBHelper(this.activity);
		
		//ショップのアイテムリスト復元.
		ArrayList<HashMap<String,String>> shopItems = db.get("saved_shopitem", new HashMap<String,String>());
		for(int ii=0;ii<shopItems.size();ii++)
		{
			if(!shopItems.get(ii).get("savedItem_code").equals(""))
			{
				HashMap<String,String> in = shopItems.get(ii);
				db.set("current_shopitem", in);
			}
		}
		
		//partyInfo.
		ArrayList<HashMap<String,String>> partyInfo = db.get("saved_party", new HashMap<String,String>());
		if(!partyInfo.get(0).get("savedParty_gold").equals(""))
		{
			this.activeParty.gold = Integer.parseInt(partyInfo.get(0).get("savedParty_gold"));
		}

		//flag.
		ArrayList<HashMap<String,String>> flags = db.get("flag_mt", new HashMap<String,String>());
		for(int ii=0;ii<flags.size();ii++)
		{
			if(!flags.get(ii).get("flag_code").equals(""))
			{
				HashMap<String, String> in = new HashMap<String, String>();
		        in.put("flag_code", flags.get(ii).get("flag_code"));
		        in.put("flag_value", flags.get(ii).get("flag_value"));
		        db.set("current_flag", in);
			}
		}
		
		//party Member.
		this.activeParty = new Party();
		ArrayList<HashMap<String, String>> res = db.get("player_mt", new HashMap<String,String>());
		if(res.get(0).get("chara_code").equals(""))
			db.insertWithCsv("playerMaster", "player_mt", true);
		
		for(int ii=1;ii<=3;ii++)
		{
			HashMap<String, String> in = new HashMap<String, String>();	        
	        in.put("chara_code", Integer.toString(ii));
	        res = db.get("player_mt", in);
			Player pl = new Player(this, res.get(0));
			
			this.activeParty.addPlayer(pl);
		}
		
		//TODO.
		//debug.
		ArrayList<HashMap<String,String>> items = db.get("saved_item", new HashMap<String,String>());
		for(int ii=0;ii<items.size();ii++)
		{
			if(!items.get(ii).get("savedItem_itemCode").equals(""))
			{
				this.activeParty.addItem(this.getItemByCode(items.get(ii).get("savedItem_itemCode")), Integer.parseInt(items.get(ii).get("savedItem_amount")));
			}
		}
		
		//装備追加.
		ArrayList<HashMap<String,String>> equips = db.get("equip_player_relation", new HashMap<String,String>());
		for(int ii=0;ii<equips.size();ii++)
		{
			if(!equips.get(ii).get("equiprel_playerCode").equals(""))
			{
				//System.out.println("equipItemCode = " + equips.get(ii).get("equiprel_itemCode"));
				this.activeParty.members.get(Integer.parseInt(equips.get(ii).get("equiprel_playerCode"))).equipItem(this.getItemByCode(equips.get(ii).get("equiprel_itemCode")));
			}
		}

		//魔法追加.
		ArrayList<HashMap<String,String>> magics = db.get("magic_player_relation", new HashMap<String,String>());
		for(int ii=0;ii<magics.size();ii++)
		{
			if(!magics.get(ii).get("magicrel_playerCode").equals(""))
			{
				this.activeParty.members.get(Integer.parseInt(magics.get(ii).get("magicrel_playerCode"))).magics.add(this.getMagicByCode(magics.get(ii).get("magicrel_magicCode")));
			}
		}
		
		
		//		this.activeParty.members.get(2).equipItem(this.getItemByCode("4"));	//ぼうし.
		//		this.activeParty.members.get(0).magics.add(this.getMagicByCode("2"));

	}
	
	public void initTextures(GL11 gl)
	{
		dungeon.initTextures(gl);
		event.initTextures(gl);
		item.initTexture(gl);
		magic.initTexture(gl);
		camp.initTextures(gl);
		town.initTextures(gl);
		title.initTextures(gl);
	}
	
	public void initSprites()
	{
		this.upButtonSprite 	= new GLSprite(-1.0f,0.75f,0.0f,0.8f,0.8f,1.0f);
		this.downButtonSprite 	= new GLSprite(-1.0f,-0.05f,0.0f,0.8f,-0.8f,1.0f);
		this.leftButtonSprite	= new GLSprite(-1.2f,0.35f,0.0f,0.4f,0.3f,1.0f);
		this.rightButtonSprite	= new GLSprite(-0.8f,0.35f,0.0f,-0.4f,0.3f,1.0f);
		this.checkButtonSprite  = new GLSprite(-1.0f,-0.45f,0.0f,0.8f,0.8f,1.0f);
		this.campButtonSprite  = new GLSprite(-1.0f,-0.8f,0.0f,0.8f,0.8f,1.0f);
		
		this.upButtonSprite.vbo = Global.primitives[SpriteType.PLANE];
		this.downButtonSprite.vbo = Global.primitives[SpriteType.PLANE];
		this.rightButtonSprite.vbo = Global.primitives[SpriteType.PLANE];
		this.leftButtonSprite.vbo = Global.primitives[SpriteType.PLANE];
		this.checkButtonSprite.vbo = Global.primitives[SpriteType.PLANE];
		this.campButtonSprite.vbo  = Global.primitives[SpriteType.PLANE];
		
		this.upButtonSprite.alpha = 0.5f;
		this.downButtonSprite.alpha = 0.5f;
		this.rightButtonSprite.alpha = 0.5f;
		this.leftButtonSprite.alpha = 0.5f;
		this.checkButtonSprite.alpha = 0.5f;
		this.campButtonSprite.alpha = 0.5f;
		
		this.bottomWindowSprite = new GLSprite(-0.3f, -0.65f, 0.0f, 2.3f, 0.6f, 1.0f);
		this.bottomWindowSprite.vbo = Global.primitives[SpriteType.PLANE];
	}
	
	public void openTown()
	{
		activeParty.setNamePlateBattlePosition();
		gameMode = GameMode.Town;
	}
	
	public void openCamp(int returnMode)
	{
		camp.redrawStatusRequest = true;
		camp.isTouch = true;
		camp.returnGameMode = returnMode;
		gameMode = GameMode.Camp;
	}
	
	public void openTitle()
	{
		gameMode = GameMode.Title;
	}
	
	public void openCamp()
	{
		this.openCamp(-1);
	}
	
	public void openDungeon()
	{
		if(dungeon.currentPosition.floor < 0)
			dungeon.currentPosition.floor = 0;
		activeParty.setNamePlateDungeonPosition();
		dungeon.updateViewRequest = true;
		gameMode = GameMode.Dungeon;
	}
	
	public String getFlag(String flag_code)
	{
		String res = "0";
		HashMap<String,String> in = new HashMap<String,String>();
		in.put("flag_code", flag_code);
		DBHelper db = new DBHelper(activity);
		ArrayList<HashMap<String,String>> flag = db.get("current_flag", in);
		
		if(flag.get(0).get("flag_code") == null || flag.get(0).get("flag_code").equals(""))
		{
			in.put("flag_value", res);
			db.set("current_flag", in);
			return res;			
		}
		else
		{
			return flag.get(0).get("flag_value");
		}
	}
	
	public void clearFlag(String flag_code)
	{
		HashMap<String,String> in = new HashMap<String,String>();
		in.put("flag_code", flag_code);
		in.put("flag_value", "0");
		DBHelper db = new DBHelper(activity);

		db.set("current_flag", in);
	}
	
	public void setFlag(String flag_code, String value)
	{
		HashMap<String,String> in = new HashMap<String,String>();
		in.put("flag_code", flag_code);
		in.put("flag_value", value);
		DBHelper db = new DBHelper(activity);

		db.set("current_flag", in);
	}
	
	public ItemBase getItemByCode(String itemCode)
	{
		HashMap<String,String> in = new HashMap<String,String>();
		in.put("item_code", itemCode);
		DBHelper db = new DBHelper(activity);
		ArrayList<HashMap<String,String>> itemData = db.get("item_mt", in);
		
		ItemBase item  = new ItemBase(itemData.get(0));
		return item;
	}
	
	public MagicBase getMagicByCode(String magicCode)
	{
		HashMap<String,String> in = new HashMap<String,String>();
		in.put("magic_code", magicCode);
		DBHelper db = new DBHelper(activity);
		ArrayList<HashMap<String,String>> itemData = db.get("magic_mt", in);
		
		MagicBase magic  = new MagicBase(itemData.get(0));
		return magic;		
	}
	
	public void setRandomEncount()
	{
		//グループ数決定(Max4グループ).
		//TODO 階層の影響も考える.
		int groupNum = Global.rand.nextInt(4)+1;
		String enemyCodes = "battle[";
		DBHelper db = new DBHelper(activity);
		
		//グループ数分敵を取得.
		for(int ii=0;ii<groupNum;ii++)
		{
			ArrayList<HashMap<String,String>> enemy = new ArrayList<HashMap<String,String>>(); 
			enemy = db.execQuery("SELECT * FROM enemy_mt WHERE chara_rarelity >= '"+getEnemyRarelity()+"' AND chara_floorLevel = '"+(dungeon.currentPosition.floor+1)+"' ORDER BY RANDOM() LIMIT 1");
			if(enemy.size() > 0
			   && !enemy.get(0).get("chara_code").equals("")
			   )
			{
				enemyCodes += enemy.get(0).get("chara_code") + ";";
			}
		}
		
		enemyCodes = enemyCodes.substring(0, enemyCodes.length()-1);
		
		enemyCodes += "]";
		
		ArrayList<HashMap<String, String>> eventData = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> eventHash = new HashMap<String, String>();
		eventHash.put("event_src", enemyCodes);
		eventData.add(eventHash);
		event.setEvent(eventData);
	}
	
	public int getTreasureNum()
	{
		int rand = Global.rand.nextInt(1000);
		int res = -1;
		if(rand <= 5)
		{
			res = 3;
		}
		else if(rand <= 30)
		{
			res = 2;
		}
		else if(rand <= 200)
		{
			res = 1;
		}
		else
		{
			res = 1;
		}
		
		return res;
	}
	
	public String getTreasureRank()
	{
		int rand = Global.rand.nextInt(1000);
		String res = "";
		if(rand <= 2)
		{
			res = "4";
		}
		else if(rand <= 30)
		{
			res = "3";
		}
		else if(rand <= 200)
		{
			res = "2";
		}
		else
		{
			res = "1";
		}
		
		return res;
	}
	
	private String getEnemyRarelity()
	{
		int rand = Global.rand.nextInt(1000);
		String res = "";
		if(rand <= 2)
		{
			res = "4";
		}
		else if(rand <= 30)
		{
			res = "3";
		}
		else if(rand <= 100)
		{
			res = "2";
		}
		else
		{
			res = "1";
		}
		
		return res;
	}
	
	public ArrayList<HashMap<String, String>> getTreasure(String rarerity, String floorNum)
	{
		DBHelper db = new DBHelper(activity);
		ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
		
		items = db.execQuery("SELECT * FROM item_mt WHERE item_rarelity = '"+rarerity+"' AND item_floorLevel = '"+floorNum+"' ORDER BY RANDOM() LIMIT 1");
		
		return items;
	}
	
	//---------------------------------------------------------
	//	SaveData.
	//---------------------------------------------------------
	public void saveData()
	{
		this.isSavingOverlay = true;
		
		//init tables;
		DBHelper db = new DBHelper(activity);
		db.truncate("saved_position");
		db.truncate("saved_item");
		db.truncate("magic_player_relation");
		db.truncate("equip_player_relation");
		db.truncate("flag_mt");
		db.truncate("saved_party");
		db.truncate("saved_shopitem");
		
		int ii,jj;
		HashMap<String,String> in = new HashMap<String,String>();
		
		//ショップアイテムの保存.
		ArrayList<HashMap<String,String>> shopItems = db.get("current_shopitem", new HashMap<String,String>());
		for(ii=0;ii<shopItems.size();ii++)
		{
			if(!shopItems.get(ii).get("savedItem_code").equals(""))
			{
				in = shopItems.get(ii);
				db.set("saved_shopitem", in);
			}
		}

		//flag.
		ArrayList<HashMap<String, String>> flags = db.get("current_flag", new HashMap<String,String>());
		for(ii=0;ii<flags.size();ii++)
		{
			if(!flags.get(ii).get("flag_value").equals(""))
			{
				in = new HashMap<String, String>();
				in.put("flag_code", flags.get(ii).get("flag_code"));
				in.put("flag_value", flags.get(ii).get("flag_value"));
				db.set("flag_mt", in);
			}
		}

		//item.
		for(ii=0;ii<this.activeParty.items.size();ii++)
		{
			ItemBase item = this.activeParty.items.get(ii);
			in = new HashMap<String,String>();
			in.put("savedItem_itemCode", item.code);
			in.put("savedItem_amount", Integer.toString(item.stock));
			db.set("saved_item", in);
		}
		


		//position.
		if(this.gameMode == GameMode.Camp && camp.returnGameMode == -1)
		{
			System.out.println("Save position");
			in = new HashMap<String,String>();
			in.put("saved_floor", Integer.toString(this.dungeon.currentPosition.floor));
			in.put("saved_x", Integer.toString(this.dungeon.currentPosition.x));
			in.put("saved_y", Integer.toString(this.dungeon.currentPosition.y));
			in.put("saved_direction", Integer.toString(this.dungeon.currentPosition.direction));
			
			db.set("saved_position", in);
		}
		
		//party info.
		in = new HashMap<String, String>();
		in.put("savedParty_gold", Integer.toString(this.activeParty.gold));
		db.set("saved_party", in);
		
		for(ii=0;ii<this.activeParty.members.size();ii++)
		{
			Player member = this.activeParty.members.get(ii);
			
			//player_mt.
			in = new HashMap<String,String>();
			in.put("chara_code", member.code);
			in.put("chara_str", Integer.toString(member.str));
			in.put("chara_def", Integer.toString(member.def));
			in.put("chara_dex", Integer.toString(member.dex));
			in.put("chara_hp", Integer.toString(member.hp));
			in.put("chara_mp", Integer.toString(member.mp));
			in.put("chara_maxHp", Integer.toString(member.maxHp));
			in.put("chara_maxMp", Integer.toString(member.maxMp));
			in.put("chara_exp", Integer.toString(member.exp));
			in.put("chara_lvl", Integer.toString(member.level));
			db.set("player_mt", in);
			
			//equip.
			in = new HashMap<String,String>();
			in.put("equiprel_playerCode", Integer.toString(ii));
			
			
			if(member.HeadEquip != null)
			{
				in.put("equiprel_itemCode", member.HeadEquip.code);
				db.set("equip_player_relation", in);
			}
			
			if(member.BodyEquip != null)
			{
				in.put("equiprel_itemCode", member.BodyEquip.code);
				db.set("equip_player_relation", in);
			}
			
			if(member.HandEquip != null)
			{
				in.put("equiprel_itemCode", member.HandEquip.code);
				db.set("equip_player_relation", in);
			}
			
			if(member.Weapon != null)
			{
				in.put("equiprel_itemCode", member.Weapon.code);
				db.set("equip_player_relation", in);
			}
			
			if(member.Shield != null)
			{
				in.put("equiprel_itemCode", member.Shield.code);
				db.set("equip_player_relation", in);
			}
			
			//magic
			for(jj=0;jj<member.magics.size();jj++)
			{
				MagicBase magic = member.magics.get(jj);
				
				in = new HashMap<String,String>();
				in.put("magicrel_playerCode", Integer.toString(ii));
				
				in.put("magicrel_magicCode", magic.code);
				db.set("magic_player_relation", in);
			}
		}
		
		this.isSavingOverlay = false;
	}
}
