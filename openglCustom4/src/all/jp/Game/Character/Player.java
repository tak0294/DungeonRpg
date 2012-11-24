package all.jp.Game.Character;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.Base.GameManager;
import all.jp.util.FontTexture;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;
import android.graphics.Typeface;

public class Player extends CharacterBase
{
	private GameManager game;
	public HashMap<String, String> property;

	public int partyIndex;
	public int faceTexture;

	public int backgroundTexture;
	public int activeBackgroundTexture;
	public FontTexture fontTex;

	//次レベルまでに必要な経験値.
	private int nextExp;

	private boolean isTextureInited = false;
	
	//------------------------
	//	選択時アニメ用設定.
	//------------------------
	public boolean isSelected = false;
	public float animationAddAlpha = 0.01f;
	public float animationAlpha = 0.0f;
	
	

	
	public Player(GameManager game, HashMap<String,String> property)
	{
		this.game = game;
		this.property = property;
		partyIndex = 0;
		name = property.get("chara_name");
		faceTexture = -1;
		code  = property.get("chara_code");
		maxHp = Integer.parseInt(property.get("chara_maxHp"));
		maxMp = Integer.parseInt(property.get("chara_maxMp"));
		hp = Integer.parseInt(property.get("chara_hp"));
		mp = Integer.parseInt(property.get("chara_mp"));
		exp = Integer.parseInt(property.get("chara_exp"));
		level = Integer.parseInt(property.get("chara_lvl"));
		str = Integer.parseInt(property.get("chara_str"));
		def = Integer.parseInt(property.get("chara_def"));
		dex = Integer.parseInt(property.get("chara_dex"));
		
		if(this.hp == 0)
			this.isAlive = false;
		
		guardDef = 0;

		hitAnimationFrame = 0;
		isHitAnimation = false;

		backgroundTexture = -1;
		fontTex = new FontTexture();
		this.type = "player";
		this.groupNum = 1;
		
		this.updateNextExp();
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
	
	public void updateNextExp()
	{
		nextExp =  (int) (10*(Math.pow(1.7, level-1.7)) / 0.7);
		System.out.println("NEXT EXP = " + nextExp);
	}
	
	
	//---------------------------------------------------------
	//	経験値を増やし、レベルアップしたかbooleanで返す.
	//---------------------------------------------------------
	public boolean isLevelUp(int exp)
	{
		this.exp += exp;
		if(this.exp > this.nextExp)
		{
			this.level++;

			//能力値上昇.
			this.maxHp  += Global.rand.nextInt(10)+1;		//HP.
			this.str += Global.rand.nextInt(6)+1;		//STR.
			this.def += Global.rand.nextInt(6)+1;		//DEF.
			this.dex += Global.rand.nextInt(3)+1;		//DEX.

			updateNextExp();
			setUpdateTexture();
			return true;
		}
		
		return false;
	}
	
	//======================================================
	//	ミニステータス表示用の文字列を取得.
	//======================================================
	private String getMiniStatusString()
	{
		String status = " Lv " + this.level + "  ";
		status += this.name + " \n ";
		if(this.hp == 0)
			status += "DEAD\n";
		else
			status += "HP:" + String.format("%4d", this.hp) + "/" + String.format("%4d", this.maxHp) + " \n ";
		status += "MP:" + String.format("%4d", this.mp) + "/" + String.format("%4d", this.maxMp);
		
		return status;
	}
	
	public void initTextures(GL10 gl)
	{
		fontTex.createTextBuffer(gl);
		fontTex.m_paint.setTypeface(Typeface.MONOSPACE);
		fontTex.preDrawBegin();
		fontTex.drawStringToTexture(this.getMiniStatusString(), 100);
		fontTex.preDrawEnd(gl);
		backgroundTexture = GraphicUtil.loadTexture(gl, "chara_background");
		activeBackgroundTexture = GraphicUtil.loadTexture(gl, "chara_background_active");

	}

	public void updateTexture(GL11 gl)
	{
		this.fontTex.preDrawBegin();
		this.fontTex.drawStringToTexture(getMiniStatusString(), 100);
		this.fontTex.preDrawEnd(gl);
	}
	
	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
	}
	
	public float getWidth()
	{
		return 0.59f;
	}
	
	public float getHeight()
	{
		return 0.35f;
	}
	
	public void draw(GL11 gl)
	{
		if(!isTextureInited)
		{
			initTextures(gl);
			isTextureInited = true;
			return;
		}
		
		if(isHitAnimation)
		{
			hitAnimationFrame++;
			if(hitAnimationFrame > hitAnimationFrameNum)
			{
				isHitAnimation = false;
				hitAnimationFrame = 0;
			}
		}
		
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
		
		if(game.activeParty.activePlayerIndex == this.partyIndex)
			GraphicUtil.drawTexture(gl, x + marginX, y + marginY, 0.59f, 0.35f, activeBackgroundTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		else
			GraphicUtil.drawTexture(gl, x + marginX, y + marginY, 0.59f, 0.35f, backgroundTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		//GraphicUtil.drawTexture(gl, x + 1.01f, -0.01f - (margin_y*partyIndex), 0.25f, 0.30f, faceTexture, 1.0f, 1.0f, 1.0f, 1.0f);

		//---------------------------------------------------
		//	選択時のアニメーション.
		//---------------------------------------------------
		if(isSelected)
		{
			GraphicUtil.drawRectangle(gl,  x + marginX, y + marginY, 0.59f, 0.35f, 1.0f, 1.0f, 1.0f, animationAlpha);
			
			animationAlpha += animationAddAlpha;
			if(animationAlpha < 0.05f || animationAlpha > 0.2f)
			{
				if(animationAlpha < 0.05f)	animationAlpha = 0.05f;
				if(animationAlpha > 0.2f)	animationAlpha = 0.2f;
				animationAddAlpha *= -1;
			}
		}
		
		fontTex.resetPreDrawCount();
		int no	= fontTex.getTexture();
		int sx	= fontTex.getWidth();
		int sy	= fontTex.getHeight();
		float offset= fontTex.getOffset();

		GraphicUtil.drawTexture(gl,x + marginX,y + marginY,sx/160.0f,sy/150.0f,no, 0,offset/256.0f,sx/512.0f, sy/256.0f, 1.0f,1.0f,1.0f, 1.0f);
	}
}
