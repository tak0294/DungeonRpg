package all.jp.Game.Town.Inn;

import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.GameMode;
import all.jp.Game.Base.GameManager;
import all.jp.Game.Town.TownManager;
import all.jp.Game.Town.TownMode;
import all.jp.util.GraphicUtil;

public class InnManager
{
	private TownManager parent;
	private GameManager game;
	private boolean isRested = false;
	
	//------------------------------------
	//	コンストラクタ.
	//------------------------------------
	public InnManager(TownManager parent, GameManager game)
	{
		this.parent = parent;
		this.game   = game;
	}

	public void initInn()
	{
		this.isRested = false;
	}

	//------------------------------------
	//	描画.
	//------------------------------------
	public void draw(GL11 gl)
	{
		GraphicUtil.drawTexture(gl, -0.3f, 0.0f, 1.0f, 1.0f, parent.innRoomTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, parent.okButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, 1.19f, -0.85f, 0.4f, 0.2f, parent.backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
	}



	//------------------------------------
	//	更新.
	//------------------------------------
	public void update()
	{
		//決定.
		if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
		{
			if(!this.isRested)
			{
				if(game.activeParty.gold < 200)
					parent.setTextAndShow("Goldが足りません！");
				else
				{
					parent.setTextAndShow("宿泊した！HP、MPが回復した！");
					game.activeParty.rest();
					game.activeParty.gold -= 200;
					this.isRested = true;
				}
			}
			else
			{
				parent.setTextAndShow(" ");
				parent.setMode(TownMode.WaitingWhatToDo);
			}
		}
		else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
		{
			
		}
		//
		else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
		{
			
			
		}
		//もどる.
		else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
		{
			parent.setTextAndShow(" ");
			parent.setMode(TownMode.WaitingWhatToDo);
		}
	}

}
