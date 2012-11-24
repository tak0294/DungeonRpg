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
	public int gold = 999;				//������.
	public int max_item	= 20;			//�A�C�e�������ʂ�MAX.
	
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
	//	�����Ă��郁���o�[����l�Ԃ�.
	//-------------------------------------
	public Player getAliveOne()
	{
		ArrayList<Player> tmp = new ArrayList<Player>();
		for(int ii=0;ii<members.size();ii++)
		{
			if(members.get(ii).isAlive)
				tmp.add(members.get(ii));
		}

		//�����Ă��郁���o�[���O�l.
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
	//	���҈ȊO�S��.
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
	//	�A�C�e����ǉ�����.
	//-------------------------------------
	public boolean addItem(ItemBase item, int num)
	{
		boolean foundInBag = false;
		
		if(items.size() >= this.max_item)
		{
			return false;
		}
		
		//�莝���ɂ��邩���ׂ�.
		for(int ii=0;ii<items.size();ii++)
		{
			if(items.get(ii).code.equals(item.code) &&
			  (items.get(ii).stock < items.get(ii).maxStock))
			{
				int canAddNum = items.get(ii).canAddNum();	//�ǉ��\�Ȍ�.

				//���ӂ��.
				if(canAddNum < num)
				{
					items.get(ii).stock = items.get(ii).maxStock;
					//���ӂꂽ����ǉ�.
					return this.addItem(item, num - canAddNum);
				}
				//���ӂ�Ȃ�.
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
