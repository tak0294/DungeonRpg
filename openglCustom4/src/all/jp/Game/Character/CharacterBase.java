package all.jp.Game.Character;

import java.util.ArrayList;
import java.util.HashMap;

import all.jp.Game.Item.ItemBase;
import all.jp.Game.Item.ItemType;
import all.jp.Game.Magic.MagicBase;

public class CharacterBase
{
	public Party party;	//所属party;
	public String type;	//"player" || "enemy"
	public String code = "";
	public int maxHp;
	public int maxMp;
	public int hp;
	public int mp;
	public int exp;
	public int level;
	public String name;
	public int groupNum;		//グループ数（Enemyのみ使用)

	public int str;
	public int def;
	public int dex;
	public int guardDef;	//一時的な防御力（防御中、魔法など）.
	public int gold = 0;

	protected boolean updateTextureRequest = false;
	
	public boolean isAlive = true;
	
	//------------------------------------
	//	座標用のプロパティ.
	//------------------------------------
	public float x;
	public float y;

	//------------------------------------
	//	装備品プロパティ.
	//------------------------------------
	public ItemBase Weapon = null;;
	public ItemBase HandEquip = null;	//こて.
	public ItemBase HeadEquip = null;	//かぶと.
	public ItemBase BodyEquip = null;	//よろい.
	public ItemBase Shield = null;		//たて
	
	//------------------------
	//	攻撃ヒット時アニメ用設定.
	//------------------------
	public boolean isHitAnimation = false;
	public int hitAnimationFrame = 0;
	public int hitAnimationFrameNum = 15;
	
	//------------------------------------
	//	戦闘時用のプロパティ.
	//------------------------------------
	public int BattleActType;
	public Object Use;
	public Object Target;
	
	public void removeAllEquip()
	{
		if(Weapon != null)
		{
			party.addItem(Weapon, 1);
			Weapon = null;
		}
		
		if(Shield != null)
		{
			party.addItem(Shield, 1);
			Shield = null;
		}
		
		if(this.BodyEquip!= null)
		{
			party.addItem(this.BodyEquip, 1);
			this.BodyEquip = null;
		}
		
		if(this.HandEquip!= null)
		{
			party.addItem(this.HandEquip, 1);
			this.HandEquip = null;
		}
		
		if(this.HeadEquip!= null)
		{
			party.addItem(this.HeadEquip, 1);
			this.HeadEquip = null;
		}
	}
	
	public void unEquipItem(int itemType)
	{
		if(itemType == ItemType.ITEM_WEAPON && Weapon != null)
		{
			party.addItem(Weapon, 1);
			Weapon = null;
		}
		else if(itemType == ItemType.ITEM_SHIELD && Shield != null)
		{
			party.addItem(Shield, 1);
			Shield = null;
		}
		else if(itemType == ItemType.ITEM_EQUIP_BODY && this.BodyEquip!= null)
		{
			party.addItem(this.BodyEquip, 1);
			this.BodyEquip = null;
		}
		else if(itemType == ItemType.ITEM_EQUIP_HAND && this.HandEquip!= null)
		{
			party.addItem(this.HandEquip, 1);
			this.HandEquip = null;
		}
		else if(itemType == ItemType.ITEM_EQUIP_HEAD && this.HeadEquip!= null)
		{
			party.addItem(this.HeadEquip, 1);
			this.HeadEquip = null;
		}	
	}
	
	//武器タイプ.
	public int getWeaponType()
	{
		if(this.Weapon == null)
			return 0;
		
		return this.Weapon.subType;
	}
	
	//装備.
	public boolean equipItem(ItemBase item)
	{
		if(item.type != ItemType.ITEM_WEAPON &&
		   item.type != ItemType.ITEM_SHIELD&&
		   item.type != ItemType.ITEM_EQUIP_BODY &&
		   item.type != ItemType.ITEM_EQUIP_HAND &&
		   item.type != ItemType.ITEM_EQUIP_HEAD
			)
		{
			return false;
		}
		
		switch(item.type)
		{
		case ItemType.ITEM_WEAPON:
			
			System.out.println(name + "は武器装備！ " + item.name);
			if(Weapon != null)
			{
				party.addItem(Weapon, 1);
			}
			Weapon = item;
			
			break;
			
		case ItemType.ITEM_SHIELD:
			
			System.out.println("盾装備！");
			if(Shield != null)
			{
				party.addItem(Shield, 1);
			}
			
			Shield = item;
			
			break;
			
		case ItemType.ITEM_EQUIP_BODY:
			
			System.out.println("体装備！");
			if(BodyEquip != null)
			{
				party.addItem(BodyEquip, 1);
			}
			
			BodyEquip = item;
			
			break;
		
		case ItemType.ITEM_EQUIP_HAND:
			
			System.out.println("手装備！");
			if(HandEquip != null)
			{
				party.addItem(HandEquip, 1);
			}
			
			HandEquip = item;
			
			break;
			
		case ItemType.ITEM_EQUIP_HEAD:
			
			System.out.println("頭装備！");
			if(HeadEquip != null)
			{
				party.addItem(HeadEquip, 1);
			}
			
			HeadEquip = item;
			
			break;
		}
		
		return true;
	}
	
	//バフ.
	public ArrayList<ItemBase> activeBuff = new ArrayList<ItemBase>();
	
	//魔法バフ.
	public ArrayList<MagicBase> activeMagicBuff = new ArrayList<MagicBase>();
	
	//魔法.
	public ArrayList<MagicBase> magics = new ArrayList<MagicBase>();
	
	public void setUpdateTexture()
	{
		this.updateTextureRequest = true;
	}
	
	//バフの追加.
	public void addBuff(ItemBase item)
	{
		//既に有効な場合、残りターン数のみ更新.
		if(this.activeBuff.contains(item))
		{
			this.activeBuff.get(this.activeBuff.indexOf(item)).buffTurnNum = item.buffTurnNum;
		}
		else
		{
			this.activeBuff.add(item);
		}
	}
	
	//魔法バフの追加.
	public void addMagicBuff(MagicBase magic)
	{
		//既に有効な場合、残りターン数のみ更新.
		if(this.activeMagicBuff.contains(magic))
		{
			this.activeMagicBuff.get(this.activeMagicBuff.indexOf(magic)).buffTurnNum = magic.buffTurnNum;
		}
		else
		{
			this.activeMagicBuff.add(magic);
		}
	}
	
	//-------------------------------------
	//	バフの残りリターン数を減らす.
	//-------------------------------------
	public void reduceBuffTurn()
	{
		for(int ii=0;ii<this.activeBuff.size();ii++)
		{
			this.activeBuff.get(ii).buffTurnNum--;
			if(this.activeBuff.get(ii).buffTurnNum == 0)
				this.activeBuff.remove(this.activeBuff.get(ii));
		}
		
		for(int ii=0;ii<this.activeMagicBuff.size();ii++)
		{
			this.activeMagicBuff.get(ii).buffTurnNum--;
			if(this.activeMagicBuff.get(ii).buffTurnNum == 0)
				this.activeMagicBuff.remove(this.activeMagicBuff.get(ii));
		}
	}

	//-------------------------------------
	//	バフ強化分のstr.
	//-------------------------------------
	public int getBuffStr()
	{
		int str = 0;
		for(int ii=0;ii<this.activeBuff.size();ii++)
		{
			str += this.activeBuff.get(ii).str;
		}

		for(int ii=0;ii<this.activeMagicBuff.size();ii++)
		{
			str += this.activeMagicBuff.get(ii).str;
		}

		return str;
	}
	
	//-------------------------------------
	//	バフ強化分のdef.
	//-------------------------------------
	public int getBuffDef()
	{
		int def = 0;
		for(int ii=0;ii<this.activeBuff.size();ii++)
		{
			def += this.activeBuff.get(ii).def;
		}
		
		for(int ii=0;ii<this.activeMagicBuff.size();ii++)
		{
			def += this.activeMagicBuff.get(ii).def;
		}
		
		return def;
	}
	
	//攻撃力.
	public int getStr()
	{
		int str = this.str;
		if(Weapon != null)
			str += Weapon.str;
		
		return str;
	}
	
	public int getDef()
	{
		return this.getDef(-1);
	}
	
	//防御力.
	public int getDef(int itemType)
	{
		int def = this.def;
		
		if(HeadEquip != null && itemType != ItemType.ITEM_EQUIP_HEAD)
			def += HeadEquip.def;
		
		if(BodyEquip != null && itemType != ItemType.ITEM_EQUIP_BODY)
			def += BodyEquip.def;
		
		if(HandEquip != null && itemType != ItemType.ITEM_EQUIP_HAND)
			def += HandEquip.def;
		
		return def;
	}
	
	//体力の回復.
	public void healHp(int heal)
	{
		this.hp += heal;
		if(this.hp > this.maxHp)
			this.hp = this.maxHp;
	}
	
	//MPの回復.
	public void healMp(int heal)
	{
		this.mp += heal;
		if(this.mp > this.maxMp)
			this.mp = this.maxMp;
	}

}
