package all.jp.Game.Event.Battle;

import java.util.Random;

import all.jp.Game.Character.CharacterBase;
import all.jp.Game.Magic.MagicBase;

public class BattleDamageCalculator
{
	public static int getDq3Damage(CharacterBase pl, CharacterBase target)
	{
		Random rand = new Random();
		int e = (pl.getStr()+pl.getBuffStr()) - ((target.getDef()+target.getBuffDef()+target.guardDef)/2);
		int d = e*(100+rand.nextInt(52))/256;
		
		return d;
	}
	
	public static int getDq3MagicDamage(MagicBase magic, CharacterBase target)
	{
		Random rand = new Random();
		int e = (magic.str) - ((target.getDef()+target.getBuffDef()+target.guardDef)/2);
		int d = e*(100+rand.nextInt(52))/256;
		
		return d;
		
	}
}
