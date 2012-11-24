package all.jp.Game.Item;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.Base.GameManager;
import all.jp.Game.Character.CharacterBase;
import all.jp.util.FontTexture;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;
import android.graphics.Typeface;

public class ItemManager
{
	private GameManager game;
	private FontTexture itemFontTexture;
	private FontTexture itemDescriptionTexutre;
	private int backgroundTexture;
	private int descriptionBackgroundTexture;
	private int handCursorTexture;
	private int itemMenuWeaponTexture;
	private int itemMenuDefenceTexture;
	private int itemMenuItemTexture;
	private int itemMenuAllTexture;


	//アイテム表示.
	private int     lineNum = 7;	//アイテムリストを何行表示するか.
	private boolean updateItemListRequest = false;
	private boolean updateItemDescriptionRequest = false;
	private int 	  currentItemPage = 0;
	private int	  currentItemIndex = 0;

	private int filterType = 0;
	public ArrayList<ItemBase> currentItemSet = null;
	public ArrayList<ItemBase> orginalItemSet = null;
	
	private String overRideString = "";
	private boolean isOverRideString = false;

	private float mainWidth, descriptionWidth;
	private float mainHeight, descriptionHeight;
	private float mainX, descriptionX, itemListX, descriptionTextX, itemCursorX;
	private float mainY, descriptionY, itemListY, descriptionTextY, itemCursorY;
	
	public int DISPLAY_MODE;
	public int MODE;
	
	private float marginX = 0.60f;
	
	//アイテムのフィルタ用.
	private int currentFilterIndex = 0;
	
	//アイテムのCostにかける係数.
	public float costMultiple = 1.0f;
	
	private boolean isEquipStyle = false;
	
	public ItemManager(GameManager game)
	{
		DISPLAY_MODE = ItemType.ITEM_DISPLAY_MODE_CAMP;
		MODE		 = ItemType.ITEM_MODE_SELECT_FILTER;
		
		this.game = game;
		this.itemDescriptionTexutre = new FontTexture();

		this.itemFontTexture = new FontTexture();
		this.itemFontTexture.maxLineCount = lineNum;
		this.itemFontTexture.height_margin = 5;
		init();
	}

	public void initPage()
	{
		currentItemPage = 0;
		currentItemIndex = 0;
	}
	
	public void init()
	{
		DISPLAY_MODE = ItemType.ITEM_DISPLAY_MODE_CAMP;
		MODE		 = ItemType.ITEM_MODE_SELECT_FILTER;
		currentItemPage = 0;
		currentItemIndex = 0;
		currentFilterIndex = 0;
		updateItemDescriptionRequest = true;
		updateItemListRequest = true;
		isEquipStyle = false;
		filterType = -1;
		if(game.activeParty != null)
		{
			this.setItemSet(game.activeParty.items);
		}
		this.setNormalStyle();
	}
	
	public void initWithNoUpdate()
	{
		DISPLAY_MODE = ItemType.ITEM_DISPLAY_MODE_CAMP;
		MODE		 = ItemType.ITEM_MODE_SELECT_FILTER;
		currentItemPage = 0;
		currentItemIndex = 0;
		filterType = -1;
		isEquipStyle = false;
		if(game.activeParty != null)
		{
			this.setItemSet(game.activeParty.items);
		}
		this.setNormalStyle();
		
	}
	
	public void initTexture(GL11 gl)
	{
		this.itemDescriptionTexutre.createTextBuffer(gl);
		this.itemFontTexture.createTextBuffer(gl);
		this.itemFontTexture.m_paint.setTypeface(Typeface.MONOSPACE);
		backgroundTexture  = GraphicUtil.loadTexture(gl, "itemwindow");
		handCursorTexture  = GraphicUtil.loadTexture(gl, "cursor");
		descriptionBackgroundTexture  = GraphicUtil.loadTexture(gl, "battlesubwindow");
		
		itemMenuWeaponTexture = GraphicUtil.loadTexture(gl, "item_menu_weapon");
		itemMenuDefenceTexture= GraphicUtil.loadTexture(gl, "item_menu_defence");
		itemMenuItemTexture   = GraphicUtil.loadTexture(gl, "item_menu_item");
		itemMenuAllTexture    = GraphicUtil.loadTexture(gl, "item_menu_all");
	}

	//-------------------------------------------------------------------
	//	アイテムリストを設定する.
	//-------------------------------------------------------------------
	public void setItemSet(ArrayList<ItemBase> itemSet)
	{
		this.currentItemSet = itemSet;
		this.orginalItemSet = itemSet;
	}

	
	//-------------------------------------------------------------------
	//	装備中のアイテムをリストの１つ目に挿入する.
	//-------------------------------------------------------------------
	public void setFirstItem(CharacterBase pl)
	{
		if(this.filterType == -1)
			return;
		
		ArrayList<ItemBase> tmpSet = new ArrayList<ItemBase>();
		
		if(this.filterType == ItemType.ITEM_WEAPON && pl.Weapon != null)
		{
			tmpSet.add(pl.Weapon);
		}
		else if(this.filterType == ItemType.ITEM_SHIELD && pl.Shield != null)
		{
			tmpSet.add(pl.Shield);
		}
		else if(this.filterType == ItemType.ITEM_EQUIP_BODY && pl.BodyEquip!= null)
		{
			tmpSet.add(pl.BodyEquip);
		}
		else if(this.filterType == ItemType.ITEM_EQUIP_HAND && pl.HandEquip != null)
		{
			tmpSet.add(pl.HandEquip);
		}
		else if(this.filterType == ItemType.ITEM_EQUIP_HEAD && pl.HeadEquip != null)
		{
			tmpSet.add(pl.HeadEquip);
		}

		//装備しないを挿入.
		ItemBase noEquip = game.getItemByCode(Global.ITEMCODE_NO_EQUIP);
		tmpSet.add(noEquip);

		this.currentItemSet.addAll(0, tmpSet);
		
		updateItemDescriptionRequest = true;
		updateItemListRequest = true;

	}
	
	public void setFilterType(int type)
	{
		this.filterType = type;
		this.currentItemSet = new ArrayList<ItemBase>();
		
		for(int ii=0;ii<this.orginalItemSet.size();ii++)
		{
			if(this.orginalItemSet.get(ii).type == type || type == 0)
			{
				currentItemSet.add(this.orginalItemSet.get(ii));
			}
		}
	}
	
	public void setFilterTypeByCurrentFilter()
	{
		this.currentItemSet = new ArrayList<ItemBase>();
		this.initPage();
		
		for(int ii=0;ii<this.orginalItemSet.size();ii++)
		{
			ItemBase item = this.orginalItemSet.get(ii);
			boolean isAdd = false;
			
			if(currentFilterIndex == 0)
				isAdd = true;
			else if(currentFilterIndex == 1 &&
					item.type == ItemType.ITEM_WEAPON)
			{
				isAdd = true;
			}
			else if(currentFilterIndex == 2 &&
					(item.type == ItemType.ITEM_EQUIP_BODY ||
					 item.type == ItemType.ITEM_EQUIP_HAND ||
					 item.type == ItemType.ITEM_EQUIP_HEAD ||
					 item.type == ItemType.ITEM_SHIELD))
			{
				isAdd = true;
			}
			else if(currentFilterIndex == 3 &&
					(item.type == ItemType.ITEM_ENEMY ||
					 item.type == ItemType.ITEM_PLAYER))
			{
				isAdd = true;
			}
			
			if(isAdd)
			{
				currentItemSet.add(this.orginalItemSet.get(ii));
			}
		}
	}
	
	//----------------------------------------------------------
	//	装備ウィンドウスタイル.
	//----------------------------------------------------------
	public void setEquipStyle()
	{
		this.mainWidth  = 1.1f;
		this.mainHeight = 1.01f;
		this.mainX = 0.25f;
		this.mainY = 0.05f;
		
		this.descriptionWidth = 2.3f;
		this.descriptionHeight = 0.35f;
		this.descriptionX = -0.3f;
		this.descriptionY = -0.8f;
		
		this.itemListX = 1.5f;
		this.itemListY = 1.0f;
		
		this.descriptionTextX = 2.82f;
		this.descriptionTextY = -1.3f;
		
		this.itemCursorX = -.28f;
		this.itemCursorY = 0.43f;
		
		isEquipStyle = true;
	}
	
	//----------------------------------------------------------
	//	ノーマルのアイテムウィンドウスタイル.
	//----------------------------------------------------------
	public void setNormalStyle()
	{
		this.mainWidth  = 2.3f;
		this.mainHeight = 1.01f;
		this.mainX = -0.3f;
		this.mainY = 0.05f;
		
		this.descriptionWidth = 2.3f;
		this.descriptionHeight = 0.35f;
		this.descriptionX = -0.3f;
		this.descriptionY = -0.8f;
		
		this.itemListX = 2.4f;
		this.itemListY = 1.0f;
		
		this.descriptionTextX = 2.82f;
		this.descriptionTextY = -1.3f;
		
		this.itemCursorX = -0.68f;
		this.itemCursorY = 0.43f;
		
		isEquipStyle = false;
	}
	
	
	
	private void updateItemDescription(GL11 gl)
	{
		int index = (this.currentItemPage*lineNum) + this.currentItemIndex;
		ItemBase item = this.currentItemSet.get(index);
		
		this.itemDescriptionTexutre.resetPreDrawCount();
		this.itemDescriptionTexutre.preDrawBegin();
		this.itemDescriptionTexutre.drawStringToTexture(item.description, 400);
		this.itemDescriptionTexutre.preDrawEnd(gl);
	}
	
	private void updateItemDescriptionWithString(GL11 gl, String str)
	{
		this.itemDescriptionTexutre.resetPreDrawCount();
		this.itemDescriptionTexutre.preDrawBegin();
		this.itemDescriptionTexutre.drawStringToTexture(str, 400);
		this.itemDescriptionTexutre.preDrawEnd(gl);
	}
	
	public void updateItemDescriptionWithString(String str)
	{
		this.overRideString = str;
		this.isOverRideString = true;
	}
	
	public void setUpdateRequest()
	{
		this.updateItemListRequest = true;
	}
	
	public void cursorUp()
	{
		if(MODE == ItemType.ITEM_MODE_SELECT_ITEM)
		{
			this.currentItemIndex--;
			if(this.currentItemIndex < 0)
			{
				if(this.currentItemPage > 0)
				{
					this.currentItemPage--;
					this.currentItemIndex = lineNum-1;
					this.updateItemListRequest = true;
				}
				else
					this.currentItemIndex = 0;
			}
			
			this.updateItemDescriptionRequest = true;
		}
		else if(MODE == ItemType.ITEM_MODE_SELECT_FILTER)
		{
			this.currentFilterIndex--;
			if(this.currentFilterIndex < 0)
			{
				this.currentFilterIndex = 3;
			}
			this.setFilterTypeByCurrentFilter();
			this.setUpdateRequest();
		}
	}
	
	public void cursorDown()
	{
		if(MODE == ItemType.ITEM_MODE_SELECT_ITEM)
		{
			int maxPage  = (int) Math.ceil(this.currentItemSet.size()/(double)lineNum);
			int maxIndex = lineNum-1;
			if(currentItemPage == maxPage-1)
			{
				maxIndex = this.currentItemSet.size() - (currentItemPage * lineNum) - 1;
			}
			
			this.currentItemIndex++;
			if(this.currentItemIndex > maxIndex)
			{
				if(this.currentItemPage < maxPage-1)
				{
					currentItemPage++;
					currentItemIndex = 0;
					this.updateItemListRequest = true;
				}
				else
					this.currentItemIndex = maxIndex;
			}
			
			this.updateItemDescriptionRequest = true;
		}
		else if(MODE == ItemType.ITEM_MODE_SELECT_FILTER)
		{
			this.currentFilterIndex = ++this.currentFilterIndex%4;
			this.setFilterTypeByCurrentFilter();
			this.setUpdateRequest();
		}
	}
	
	
	private String getFormatedName(String name, int length)
	{
		int len = name.length();
		String space = "";
		for(int ii=len;ii<length;ii++)	space += "　";
		return name + space;
	}
	
	public void drawItemList(GL11 gl)
	{
		
		String itemList = "";
		
		if(currentItemSet.size() == 0)
		{
			itemList = "なにもない";
		}
		else
		{
			for(int ii=(currentItemPage*lineNum);ii<(currentItemPage*lineNum) + lineNum;ii++)
			{
				if(ii >= this.currentItemSet.size())
					break;
				
				ItemBase item = this.currentItemSet.get(ii);
				int length = isEquipStyle?10:15;
				itemList += this.getFormatedName(item.name, length);
				if(!item.code.equals(Global.ITEMCODE_NO_EQUIP))
				{
					if(DISPLAY_MODE == ItemType.ITEM_DISPLAY_MODE_CAMP)
					{
						if(!isEquipStyle)
						{
							itemList += "  x ";
							itemList += item.stock;
						}
					}
					else if(DISPLAY_MODE  == ItemType.ITEM_DISPLAY_MODE_SHOP)
					{
						itemList += String.format("%5d", (int)(item.cost*this.costMultiple) ) + " GOLD";
					}
				}
				itemList += "\n";
			}
		}
		
		this.itemFontTexture.resetPreDrawCount();
		this.itemFontTexture.preDrawBegin();
		this.itemFontTexture.drawStringToTexture(itemList, 400);
		this.itemFontTexture.preDrawEnd(gl);
	}
	
	public void draw(GL11 gl)
	{
		if(updateItemListRequest)
		{
			this.drawItemList(gl);
			updateItemListRequest = false;
		}
		
		if(this.updateItemDescriptionRequest)
		{
			if(currentItemSet.size() > 0)
			{
				this.updateItemDescription(gl);
			}
			
			this.updateItemDescriptionRequest = false;
		}
		
		if(this.isOverRideString)
		{
			this.updateItemDescriptionWithString(gl, this.overRideString);
			this.isOverRideString = false;
			this.overRideString = "";
		}
		
		int no	= itemFontTexture.getTexture();
		int sx	= itemFontTexture.getWidth();
		int sy	= itemFontTexture.getHeight();
		float offset= itemFontTexture.getOffset();

		int no2	= itemDescriptionTexutre.getTexture();
		int sx2	= itemDescriptionTexutre.getWidth();
		int sy2	= itemDescriptionTexutre.getHeight();
		float offset2= itemDescriptionTexutre.getOffset();

		
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GraphicUtil.drawTexture(gl, this.mainX, this.mainY, this.mainWidth, this.mainHeight, backgroundTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, this.descriptionX, this.descriptionY, this.descriptionWidth, this.descriptionHeight, descriptionBackgroundTexture, 1.0f, 1.0f, 1.0f, 1.0f);

		GraphicUtil.drawTexture(gl,-(this.itemListX - sx/180.0f)*0.5f+this.marginX,(this.itemListY - sy/140.0f) * 0.5f,sx/180.0f,sy/140.0f,no, 0,offset/256.0f,sx/512.0f, sy/256.0f, 1.0f,1.0f,1.0f, 1.0f);
		GraphicUtil.drawTexture(gl,-(this.descriptionTextX - sx2/180.0f)*0.5f,(this.descriptionTextY - sy2/140.0f) * 0.5f,sx2/180.0f,sy2/140.0f,no2, 0,offset2/256.0f,sx2/512.0f, sy2/256.0f, 1.0f,1.0f,1.0f, 1.0f);

		if(MODE == ItemType.ITEM_MODE_SELECT_ITEM)
			GraphicUtil.drawTexture(gl, this.itemCursorX, this.itemCursorY - (currentItemIndex * 0.135f), 0.2f, 0.3f, handCursorTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		else if(MODE == ItemType.ITEM_MODE_SELECT_FILTER)
			GraphicUtil.drawTexture(gl, -1.25f, 0.40f - (currentFilterIndex * 0.2f), 0.2f, 0.3f, handCursorTexture, 1.0f, 1.0f, 1.0f, 1.0f);

		if(!isEquipStyle)
		{
			float menuAllAlpha 		= currentFilterIndex==0?1.0f:0.5f;
			float menuWeaponAlpha 	= currentFilterIndex==1?1.0f:0.5f;
			float menuDefenceAlpha 	= currentFilterIndex==2?1.0f:0.5f;
			float menuItemAlpha 	= currentFilterIndex==3?1.0f:0.5f;

			GraphicUtil.drawTexture(gl, -1.0f, 0.4f, 0.3f, 0.30f, itemMenuAllTexture, 1.0f, 1.0f, 1.0f, menuAllAlpha);
			GraphicUtil.drawTexture(gl, -1.0f, 0.2f, 0.3f, 0.30f, itemMenuWeaponTexture, 1.0f, 1.0f, 1.0f, menuWeaponAlpha);
			GraphicUtil.drawTexture(gl, -1.0f, -0.0f, 0.3f, 0.30f, itemMenuDefenceTexture, 1.0f, 1.0f, 1.0f, menuDefenceAlpha);
			GraphicUtil.drawTexture(gl, -1.0f, -0.2f, 0.3f, 0.30f, itemMenuItemTexture, 1.0f, 1.0f, 1.0f, menuItemAlpha);
		}
		
		gl.glDisable(GL11.GL_BLEND);
	}
	
	public ItemBase get()
	{
		int index = (this.currentItemPage*lineNum) + this.currentItemIndex;
		if(currentItemSet.size() == 0 || index > currentItemSet.size())
			return null;
		
		return this.currentItemSet.get(index);
	}
	
	public void remove()
	{
		int index = (this.currentItemPage*lineNum) + this.currentItemIndex;
		ItemBase itm = currentItemSet.get(index);
		game.activeParty.items.remove(itm);
		
		this.setItemSet(game.activeParty.items);
		this.setUpdateRequest();
	}
	
}
