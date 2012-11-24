package all.jp.Game.Character;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.Item.ItemBase;
import all.jp.util.Global;

public class Party
{
	public ArrayList<Player> members = new ArrayList<Player>();
	public ArrayList<ItemBase> items = new ArrayList<ItemBase>();
	public int activePlayerIndex = -1;
	public int gold = 999;				//所持金.
	public int max_item	= 20;			//アイテム所持量のMAX.
	
	public void addPlayer(Player pl)
	{
		pl.partyIndex = members.size();
		pl.party = this;
		members.add(pl);
	}

	public void setNamePlateBattlePosition()
	{
		for(int ii=0;ii<members.size();ii++)
		{
			members.get(ii).x = 0.62f * ii - 0.92f;
			members.get(ii).y = 0.74f;
		}
	}
	
	//-------------------------------------
	//	生きているメンバーを一人返す.
	//-------------------------------------
	public Player getAliveOne()
	{
		ArrayList<Player> tmp = new ArrayList<Player>();
		for(int ii=0;ii<members.size();ii++)
		{
			if(members.get(ii).isAlive)
				tmp.add(members.get(ii));
		}

		//生きているメンバーが０人.
		if(tmp.size() == 0)
			return members.get(0);
		
		return tmp.get(Global.rand.nextInt(tmp.size()));

	}
	
	public void setNamePlateDungeonPosition()
	{
		for(int ii=0;ii<members.size();ii++)
		{
			members.get(ii).x = 1.18f;
			members.get(ii).y = -0.39f * ii;
		}
	}
	
	public void draw(GL11 gl)
	{
		for(int ii=0;ii<members.size();ii++)
		{
			members.get(ii).draw(gl);
		}
	}
	
	public Player getActivePlayer()
	{
		if(activePlayerIndex > members.size()-1)
			return null;
		return members.get(activePlayerIndex);
	}
	
	//-------------------------------------
	//	死者以外全回復.
	//-------------------------------------
	public void rest()
	{
		for(int ii=0;ii<members.size();ii++)
		{
			if(members.get(ii).isAlive)
			{
				Player member = members.get(ii);
				member.hp = member.maxHp;
				member.mp = member.maxMp;
				member.setUpdateTexture();
			}
		}
	}
	
	
	//-------------------------------------
	//	アイテムを追加する.
	//-------------------------------------
	public boolean addItem(ItemBase item, int num)
	{
		boolean foundInBag = false;
		
		if(items.size() >= this.max_item)
		{
			return false;
		}
		
		//手持ちにあるか調べる.
		for(int ii=0;ii<items.size();ii++)
		{
			if(items.get(ii).code.equals(item.code) &&
			  (items.get(ii).stock < items.get(ii).maxStock))
			{
				int canAddNum = items.get(ii).canAddNum();	//追加可能な個数.

				//あふれる.
				if(canAddNum < num)
				{
					items.get(ii).stock = items.get(ii).maxStock;
					//あふれた分を追加.
					return this.addItem(item, num - canAddNum);
				}
				//あふれない.
				else
				{
					items.get(ii).stock += num;
				}
				
				foundInBag = true;
			}
		}
		
		if(!foundInBag)
		{
			if(item.maxStock >= num)
			{
				item.stock = num;
			}
			else
			{
				item.stock = item.maxStock;
				this.items.add(item);
				return addItem(item, num - item.maxStock);
			}
			
			this.items.add(item);
		}
		
		return true;
	}

}
