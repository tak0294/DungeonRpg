package all.jp.Game.Town.Shop;

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL11;

import cx.fam.tak0294.storage.DBHelper;

import all.jp.Game.Base.GameManager;
import all.jp.Game.Item.ItemBase;
import all.jp.Game.Item.ItemManager;
import all.jp.Game.Item.ItemType;
import all.jp.Game.Town.TownManager;
import all.jp.Game.Town.TownMode;
import all.jp.util.GraphicUtil;

public class ShopManager
{
	private TownManager parent;
	private GameManager game;
	public ItemManager item;
	private ArrayList<ItemBase> itemStock;
	private boolean isConfirm = false;
	
	private int MODE;
	private final int SELECT_MODE = 5; 
	private final int BUY_MODE = 10;
	private final int SELL_MODE = 20;
	
	private int selectIndex = 0;
	
	//------------------------------------
	//	コンストラクタ.
	//------------------------------------
	public ShopManager(TownManager parent, GameManager game)
	{
		this.MODE = SELECT_MODE;
		this.parent = parent;
		this.game   = game;
		//this.item   = new ItemManager(game);
		this.item     = game.item;
	}

	public void initShop()
	{
		this.itemStock = new ArrayList<ItemBase>();
		this.MODE = SELECT_MODE;
		this.selectIndex = 0;
		item.costMultiple = 1.0f;
	}

	private void setShopItemsBuy()
	{
		//debug.
		DBHelper db = new DBHelper(game.activity);
		
		//ショップのアイテムリスト復元.
		ArrayList<HashMap<String,String>> shopItems = db.get("current_shopitem", new HashMap<String,String>());
		for(int ii=0;ii<shopItems.size();ii++)
		{
			itemStock.add(game.getItemByCode(shopItems.get(ii).get("savedItem_itemCode")));
		}
		
		item.init();
		item.DISPLAY_MODE   = ItemType.ITEM_DISPLAY_MODE_SHOP;
		item.setItemSet(itemStock);
		item.setUpdateRequest();
		item.costMultiple = 1.0f;
	}
	
	private void setShopItemsSell()
	{
		item.init();
		item.DISPLAY_MODE   = ItemType.ITEM_DISPLAY_MODE_SHOP;
		item.setItemSet(game.activeParty.items);
		item.setUpdateRequest();
		item.costMultiple = 0.5f;
	}
	
	public String getGoldStr()
	{
		return "所持金：" + String.format("%10d", game.activeParty.gold) + " GOLD\n";
	}
	
	//------------------------------------
	//	描画.
	//------------------------------------
	public void draw(GL11 gl)
	{
		GraphicUtil.drawTexture(gl, -0.3f, 0.0f, 1.0f, 1.0f, parent.shopRoomTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, parent.okButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, 1.19f, -0.85f, 0.4f, 0.2f, parent.backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, 1.19f, -0.25f, 0.4f, 0.2f, parent.menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, 1.19f, -0.55f, 0.4f, -0.2f, parent.menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		
		
		if(MODE == BUY_MODE || MODE == SELL_MODE)
		{
			item.draw(gl);
		}

		if(MODE == SELECT_MODE)
		{
			float buyAlpha = selectIndex==0?1.0f:0.5f;
			float sellAlpha = selectIndex==1?1.0f:0.5f;
			GraphicUtil.drawTexture(gl, -0.3f, 0.2f, 0.8f, 0.75f, parent.buyButtonTexture, 1.0f, 1.0f, 1.0f, buyAlpha);
			GraphicUtil.drawTexture(gl, -0.3f, -0.2f, 0.8f, 0.75f, parent.sellButtonTexture, 1.0f, 1.0f, 1.0f, sellAlpha);
		}
		
		if(isConfirm)
		{
			GraphicUtil.drawTexture(gl, 0.3f, 0.2f, 1.0f, 0.5f, parent.yesButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, -0.9f, 0.2f, 1.0f, 0.5f, parent.noButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		}
	}



	//------------------------------------
	//	更新.
	//------------------------------------
	public void update()
	{
		if(isConfirm)
		{
			//YES.
			if(game.touchGlX > -0.2f && game.touchGlX < 0.8f && game.touchGlY > -0.05f && game.touchGlY < 0.45f)
			{
				ItemBase targetItem = item.get();
				
				if(MODE == BUY_MODE)
				{
					if(game.activeParty.gold > targetItem.cost)
					{
						game.activeParty.gold -= targetItem.cost;
						game.activeParty.addItem(targetItem, 1);
						parent.setTextAndShow(getGoldStr() + targetItem.name + "を買いました！");
					}
					else
					{
						parent.setTextAndShow(getGoldStr() + "GOLDが足りません！");
					}
				}
				else
				{
					game.activeParty.gold += (targetItem.cost/2);
					parent.setTextAndShow(getGoldStr() + targetItem.name + "を"+(targetItem.cost/2)+" GOLDで売りました。");
					targetItem.stock--;
					if(targetItem.stock == 0)
						item.remove();
				}
				
				isConfirm = false;
			}
			
			//NO.
			if(game.touchGlX > -1.4f && game.touchGlX < -0.4f && game.touchGlY > -0.05f && game.touchGlY < 0.45f)
			{
				isConfirm = false;	
			}
			
			
			return;
		}
		
		
		
		//決定.
		if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
		{
			if(MODE == BUY_MODE || MODE == SELL_MODE)
			{
				if(item.get() == null)
					return;
				
				if(item.MODE == ItemType.ITEM_MODE_SELECT_FILTER)
				{
					item.MODE = ItemType.ITEM_MODE_SELECT_ITEM;
					item.initPage();
					item.setUpdateRequest();
				}
				else if(item.MODE == ItemType.ITEM_MODE_SELECT_ITEM)
				{
					if(MODE == BUY_MODE)
						parent.setTextAndShow(this.getGoldStr() + item.get().name + "を買いますか？");
					else
						parent.setTextAndShow(this.getGoldStr() + item.get().name + "を"+(item.get().cost/2)+" GOLDで売りますか？\n現在" + item.get().stock + "個持っています");
					isConfirm = true;
				}
			}
			else if(MODE == SELECT_MODE)
			{
				if(selectIndex == 0)
				{
					this.setShopItemsBuy();
					MODE = BUY_MODE;
				}
				else
				{
					this.setShopItemsSell();
					MODE = SELL_MODE;
				}
			}
		}
		else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
		{
			if(MODE == BUY_MODE || MODE == SELL_MODE)
			{
				item.cursorUp();
			}
			else if(MODE == SELECT_MODE)
			{
				selectIndex--;
				if(selectIndex < 0)
					selectIndex = 1;
			}
		}
		//
		else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
		{
			if(MODE == BUY_MODE || MODE == SELL_MODE)
			{
				item.cursorDown();
			}
			else if(MODE == SELECT_MODE)
			{
				selectIndex = ++selectIndex%2;
			}
		}
		//もどる.
		else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
		{
			if(MODE == BUY_MODE || MODE == SELL_MODE)
			{
				if(item.MODE == ItemType.ITEM_MODE_SELECT_FILTER)
				{
					MODE = SELECT_MODE;
				}
				else if(item.MODE == ItemType.ITEM_MODE_SELECT_ITEM)
				{
					item.MODE = ItemType.ITEM_MODE_SELECT_FILTER;
					item.setUpdateRequest();
				}
			}
			else if(MODE == SELECT_MODE)
			{
				parent.setTextAndShow(" ");
				parent.setMode(TownMode.WaitingWhatToDo);
			}
		}

	}
}
