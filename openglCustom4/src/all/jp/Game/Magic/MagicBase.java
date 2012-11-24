package all.jp.Game.Magic;

import java.util.HashMap;

import all.jp.Game.Character.Player;

public class MagicBase
{
	public String code;
	public String name = "";
	public int type = 0;
	public int weight = 0;
	public int str = 0;
	public int def = 0;
	public int dex = 0;
	public int mp = 0;	//MPè¡îÔó .
	public int job = 0;
	public int target = 0;
	public int getLevel = 0;
	public int cureHp  = 0;	//HPâÒïúó .
	public int cureMp  = 0;	//MPâÒïúó .
	public String description = "";
	public boolean usableBattle = false;
	public boolean usableDungeon = false;
	public int effectType = -1;
	public int stock    = 0;
	public int maxStock = 0;
	public int buffTurnNum = 0;
	
	public Player equipPlayer = null;
	
	public MagicBase(HashMap<String, String> magic)
	{
		this.code = magic.get("magic_code");
		this.name = magic.get("magic_name");
		this.mp     = Integer.parseInt(magic.get("magic_mp"));
		this.str    = Integer.parseInt(magic.get("magic_str"));
		this.cureHp = Integer.parseInt(magic.get("magic_cureHp"));
		this.cureMp = Integer.parseInt(magic.get("magic_cureMp"));
		this.type = Integer.parseInt(magic.get("magic_type"));
		this.job  = Integer.parseInt(magic.get("magic_job"));
		this.target = Integer.parseInt(magic.get("magic_target"));
		this.getLevel    = Integer.parseInt(magic.get("magic_getLevel"));
		this.effectType = Integer.parseInt(magic.get("magic_effectType"));
		this.dex	= 0;
		
		this.description = magic.get("magic_description");
		this.usableBattle = Integer.parseInt(magic.get("magic_usableBattle"))==1?true:false;
		this.usableDungeon = Integer.parseInt(magic.get("magic_usableField"))==1?true:false;
		this.buffTurnNum = Integer.parseInt(magic.get("magic_buffTurnNum"));
	}
	
	public int canAddNum()
	{
		return maxStock - stock;
	}
}