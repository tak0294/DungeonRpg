package all.jp.Game.Event.Battle;

import all.jp.Game.Base.GameManager;
import all.jp.Game.Character.CharacterBase;
import all.jp.Game.Character.Enemy;
import all.jp.Game.Item.Weapon.WeaponType;
import all.jp.Game.Magic.MagicType;
import all.jp.util.Global;

public class BattleAnimation
{
	private GameManager game;
	
	public BattleAnimation(GameManager game)
	{
		this.game = game;
	}
	
	
	public void setWeaponEffect(int weaponType, CharacterBase en)
	{
		switch(weaponType)
		{
		
		case WeaponType.NO_EQUIP:
			//素手アニメ.
			for(int ii=0;ii<60;ii++)
			{
				game.renderer.particleSystem.addSparks(en.x, en.y, 0.0f, 0.03f, 0.01f, 2, ii/8);
				
			}
			break;
		
		case WeaponType.MACE:
			//メイスアニメ.
			for(int ii=0;ii<60;ii++)
			{
				game.renderer.particleSystem.addParticle(en.x - (ii*0.007f) + 0.2f, en.y - (ii*0.01f) + 0.2f, 0.0f, 0.05f, 0.0f, 0.0f, 0.0f,ii/8);
				if(ii%4==0)
				{
					game.renderer.bloodParticleSystem.addSparks(en.x - (ii*0.007f) + 0.15f, en.y - (ii*0.01f) + 0.15f, 0.0f, Global.rand.nextInt(8) / 80.0f, 0.02f, 5, 0);
				}
			}
			
			break;
		
		case WeaponType.SWORD:
			//剣武器アニメ.
			for(int ii=0;ii<60;ii++)
			{
				game.renderer.particleSystem.addParticle(en.x - (ii*0.007f) + 0.2f, en.y - (ii*0.01f) + 0.2f, 0.0f, 0.05f, 0.0f, 0.0f, 0.0f,ii/8);
				if(ii%4==0)
				{
					game.renderer.bloodParticleSystem.addSparks(en.x - (ii*0.007f) + 0.15f, en.y - (ii*0.01f) + 0.15f, 0.0f, Global.rand.nextInt(8) / 80.0f, 0.02f, 5, 0);
				}
			}
			break;
		}
	}
	
	public void setAttackMagicEffect(int magicType, CharacterBase target)
	{
		switch(magicType)
		{
		case MagicType.TASK_TYPE_FIRE:
			//炎魔法アニメ.
			for(int ii=0;ii<15;ii++)
			{
				game.renderer.bloodParticleSystem.addSparks(target.x, target.y, 0.0f, 0.1f, 0.02f, 10, 0);
			}
			break;
		}
	}
	
	public void setCureMagicEffect(int magicType, CharacterBase target)
	{
		switch(magicType)
		{
		case MagicType.TASK_TYPE_HEAL_HP:
			//HP回復魔法アニメ.
			for(int ii=0;ii<360;ii+=5)
			{
				float r = 0.1f;
				float xx = target.x + (float) (r*Math.cos(Math.PI/180 * ii));
				float yy = target.y + (float) (r*Math.sin(Math.PI/180 * ii));
				game.renderer.particleSystem.addParticle(xx, yy - 0.1f, 0.0f, Global.rand.nextFloat()/10.0f, 0.0f, Global.rand.nextFloat()/30.0f, 0.0f, Global.rand.nextInt(10));
			}
			break;
		}
	}
	
		
}
