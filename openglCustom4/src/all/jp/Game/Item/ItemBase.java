package all.jp.Game.Item;

import java.util.HashMap;

import all.jp.Game.Character.Player;

public class ItemBase
{
	public String code;
	public String name = "";
	public int type = 0;
	public int weight = 0;
	public int str = 0;
	public int def = 0;
	public int dex = 0;
	public int hp  = 0;	//HP回復量.
	public int mp  = 0;	//MP回復量.
	public String description = "";
	public boolean usableBattle = false;
	public boolean usableDungeon = false;
	
	public int stock    = 0;
	public int maxStock = 0;
	public int buffTurnNum = 0;
	public int cost     = 0;	//価格（原価）.
	public int subType	= 0;	//サブタイプ（武器のタイプ）.
	
	public Player equipPlayer = null;
	
	public ItemBase(HashMap<String, String> item)
	{
		this.code = item.get("item_code");
		this.name = item.get("item_name");
		this.type = Integer.parseInt(item.get("item_type"));
		this.weight = Integer.parseInt(item.get("item_weight"));
		this.str    = Integer.parseInt(item.get("item_str"));
		this.def    = Integer.parseInt(item.get("item_def"));
		this.dex	= 0;
		this.hp     = Integer.parseInt(item.get("item_hp"));
		this.mp     = Integer.parseInt(item.get("item_mp"));
		this.maxStock = Integer.parseInt(item.get("item_maxStock"));
		this.description = item.get("item_description");
		this.usableBattle = Integer.parseInt(item.get("item_usableBattle"))==1?true:false;
		this.usableDungeon = Integer.parseInt(item.get("item_usableField"))==1?true:false;
		this.buffTurnNum = Integer.parseInt(item.get("item_buffTurnNum"));
		this.cost = Integer.parseInt(item.get("item_cost"));
		this.subType = Integer.parseInt(item.get("item_subtype"));
	}
	
	public int canAddNum()
	{
		return maxStock - stock;
	}
}
