package all.jp.Game.Character;

import java.util.HashMap;
import java.util.Random;

import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.Base.GameManager;
import all.jp.Game.Event.Battle.BattleActType;
import all.jp.util.FontTexture;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;

public class Enemy extends CharacterBase
{
	private GameManager game;
	public HashMap<String, String> property;
	public int texture;
	public float width = 0.7f;
	public float height = 0.9f;
	public float alpha = 0.0f;
	private FontTexture fontTexture;
	private boolean isReleasedTexture = false;
	
	//------------------------
	//	選択時アニメ用設定.
	//------------------------
	public boolean isSelected = false;
	public float animationAddAlpha = 0.01f;
	public float animationAlpha = 0.0f;
	

	
	
	public Enemy(GameManager game, HashMap<String, String> property)
	{
		this.game = game;
		this.property = property;
		int maxGroup = Integer.parseInt(property.get("chara_maxGroup"));
		
		//座標設定.
		x = 0.0f;
		y = 0.05f;
		
		this.groupNum = Global.rand.nextInt(maxGroup) + 1;
		this.fontTexture = new FontTexture();
		this.name = property.get("chara_name");
		hp = Integer.parseInt(property.get("chara_hp"));
		mp = Integer.parseInt(property.get("chara_mp"));
		maxHp = hp;
		maxMp = mp;
		exp = Integer.parseInt(property.get("chara_exp"));
		level = Integer.parseInt(property.get("chara_lvl"));
		str = Integer.parseInt(property.get("chara_str"));
		def = Integer.parseInt(property.get("chara_def"));
		dex = Integer.parseInt(property.get("chara_dex"));
		gold = Integer.parseInt(property.get("chara_gold"));
		this.type = "enemy";
		
		guardDef = 0;
		
		hitAnimationFrame = 0;
		isHitAnimation = false;
		isReleasedTexture = false;
	}

	public boolean release(GL11 gl, boolean forseRelease)
	{
		if(isReleasedTexture)
			return true;
		
		if((!this.isAlive && this.alpha == 0.0f) || forseRelease)
		{
			this.fontTexture.onDestroy(gl);
			isReleasedTexture = true;
			return true;
		}
		
		return false;
	}
	
	public boolean release(GL11 gl)
	{
		return this.release(gl, false);
	}
	
	public void initTexture(GL11 gl, String texName)
	{
		this.fontTexture.createTextBuffer(gl);
		this.updateTexture(gl);
		this.texture = GraphicUtil.loadTexture(gl, texName);
	}

	public void updateTexture(GL11 gl)
	{
		String name = property.get("chara_name") + " (" + groupNum + ")";
		this.fontTexture.preDrawBegin();
		this.fontTexture.drawStringToTexture(name, 100);
		this.fontTexture.preDrawEnd(gl);
	}
	

	
	public void startSelectedAnimation()
	{
		this.isSelected 	= true;	
		this.animationAlpha = 0.1f;
	}
	
	public void stopSelectedAnimation()
	{
		this.isSelected = false;
		this.animationAlpha = 0.0f;
	}
	
	public void draw(GL11 gl)
	{
		float marginX = 0.0f;
		float marginY = 0.0f;
		
		if(isHitAnimation)
		{
			marginX = (Global.rand.nextInt(3) - 1) / 90.0f;
			marginY = (Global.rand.nextInt(3) - 1) / 90.0f;
		}
		
		//テクスチャ更新リクエスト.
		if(updateTextureRequest)
		{
			this.updateTexture(gl);
			updateTextureRequest = false;
		}
		
		GraphicUtil.drawTexture(gl, this.x + marginX, this.y + marginY, this.width, this.height, this.texture, 1.0f, 1.0f, 1.0f, alpha);


		//---------------------------------------------------
		//	選択時のアニメーション.
		//---------------------------------------------------
		if(isSelected)
		{
			GraphicUtil.drawRectangle(gl, x, y, width, height, 1.0f, 1.0f, 1.0f, animationAlpha);
			
			animationAlpha += animationAddAlpha;
			if(animationAlpha < 0.05f || animationAlpha > 0.2f)
			{
				if(animationAlpha < 0.05f)	animationAlpha = 0.05f;
				if(animationAlpha > 0.2f)	animationAlpha = 0.2f;
				animationAddAlpha *= -1;
			}
		}

		int no	= fontTexture.getTexture();
		int sx	= fontTexture.getWidth();
		int sy	= fontTexture.getHeight();
		float offset= fontTexture.getOffset();
		GraphicUtil.drawTexture(gl,x,y-0.5f,sx/160.0f,sy/130.0f,no, 0,offset/256.0f,sx/512.0f, sy/256.0f, 1.0f,1.0f,1.0f, alpha);
	}
	
	public void update()
	{
		if(isHitAnimation)
		{
			hitAnimationFrame++;
			if(hitAnimationFrame > hitAnimationFrameNum)
			{
				isHitAnimation = false;
				hitAnimationFrame = 0;
			}
		}
		else
		{
			if(isAlive)
			{
				alpha += 0.03f;
				if(alpha > 1.0f)
					alpha = 1.0f;
			}
			else
			{
				alpha -= 0.1f;
				if(alpha < 0.0f)
					alpha = 0.0f;
			}
		}
	}
	
	//------------------------------------------------
	//	戦闘中の行動を決定する.
	//------------------------------------------------
	public void decideBattleAct()
	{
		int routinType = Integer.parseInt(this.property.get("chara_routinType"));
		switch(routinType)
		{

		//---------------------------------------------------
		//	雑魚敵の行動パターン.
		//---------------------------------------------------
		case 1:
			
			this.BattleActType = all.jp.Game.Event.Battle.BattleActType.Fight;
			this.Target = game.activeParty.getAliveOne();
			
			break;
		}
	}
}
