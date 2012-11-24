package all.jp.Game.Town.Temple;

import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.Base.GameManager;
import all.jp.Game.Character.Player;
import all.jp.Game.Town.TownManager;
import all.jp.Game.Town.TownMode;
import all.jp.util.GraphicUtil;

public class TempleManager
{
	private TownManager parent;
	private GameManager game;
	
	private int MODE = 0;
	private final int MODE_WAITING_WHATTODO		 = 1;
	private final int MODE_WAITING_TARGET_SELECT = 10;
	private final int MODE_WAITING_CONFIRM       = 20;
	private final int MODE_WAITING_COMPLETE		 = 30;
	
	private int currentPlayerIndex;
	private boolean isTouch;
	
	//------------------------------------
	//	�R���X�g���N�^.
	//------------------------------------
	public TempleManager(TownManager parent, GameManager game)
	{
		this.parent = parent;
		this.game   = game;
	}

	public void initTemple()
	{
		MODE = MODE_WAITING_WHATTODO;
		currentPlayerIndex = 0;
		isTouch = true;
	}


	//------------------------------------
	//	�`��.
	//------------------------------------
	public void draw(GL11 gl)
	{
		if(MODE == this.MODE_WAITING_WHATTODO)
		{
			GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, parent.reviveButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		}

		if(MODE == this.MODE_WAITING_TARGET_SELECT)
		{
			GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, parent.okButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.25f, 0.4f, 0.2f, parent.menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, 1.19f, -0.55f, -0.4f, 0.2f, parent.menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		}

		if(MODE ==  this.MODE_WAITING_CONFIRM
		   || MODE == this.MODE_WAITING_COMPLETE
		   )
		{
			GraphicUtil.drawTexture(gl, 1.19f, 0.05f, 0.4f, 0.2f, parent.okButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		
		GraphicUtil.drawTexture(gl, 1.19f, -0.85f, 0.4f, 0.2f, parent.backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
	}



	//------------------------------------
	//	�X�V.
	//------------------------------------
	public void update()
	{
		//���j���[1��.
		if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
		{
			if(MODE == this.MODE_WAITING_WHATTODO)
			{
				parent.setTextAndShow("�N�𕜊�������̂��ȁH");
				MODE = this.MODE_WAITING_TARGET_SELECT;
				game.activeParty.members.get(currentPlayerIndex).startSelectedAnimation();
			}
			else if(MODE == this.MODE_WAITING_TARGET_SELECT)
			{
				Player pl = game.activeParty.members.get(currentPlayerIndex);
				if(pl.isAlive)
				{
					parent.setTextAndShow(pl.name + "�͂܂������Ă���I");
				}
				else
				{
					MODE = this.MODE_WAITING_CONFIRM;
					parent.setTextAndShow(pl.name + "�𐶂��Ԃ点��ɂ�" + (pl.level*100) + "Gold���K�v���B\n�����邩�ȁH");
				}
			}
			else if(MODE == this.MODE_WAITING_CONFIRM)
			{
				Player pl = game.activeParty.members.get(currentPlayerIndex);
				if(game.activeParty.gold > (pl.level*100))
				{
					pl.isAlive = true;
					pl.hp = pl.maxHp;
					game.activeParty.gold -= (pl.level*100);
					parent.setTextAndShow(pl.name + "�͐����Ԃ����I");
					pl.setUpdateTexture();
					game.activeParty.members.get(currentPlayerIndex).stopSelectedAnimation();
					MODE = this.MODE_WAITING_COMPLETE;
				}
				else
				{
					parent.setTextAndShow("Gold������Ȃ��I");
				}
			}
			else if(MODE == this.MODE_WAITING_COMPLETE)
			{
				this.MODE = this.MODE_WAITING_WHATTODO;
				parent.setTextAndShow("�����͋���ł��B�������]�݂��ȁH");
				game.activeParty.members.get(currentPlayerIndex).stopSelectedAnimation();
			}
		}
		else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f && game.touchGlY < -0.15f)
		{
			if(this.MODE == this.MODE_WAITING_TARGET_SELECT)
			{
				game.activeParty.members.get(currentPlayerIndex).stopSelectedAnimation();
				this.currentPlayerIndex = ++this.currentPlayerIndex % 3;
				game.activeParty.members.get(currentPlayerIndex).startSelectedAnimation();
			}
		}
		//
		else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f && game.touchGlY < -0.45f)
		{
			if(this.MODE == this.MODE_WAITING_TARGET_SELECT)
			{
				game.activeParty.members.get(currentPlayerIndex).stopSelectedAnimation();
				this.currentPlayerIndex--;
				if(this.currentPlayerIndex < 0)
				{
					this.currentPlayerIndex = 2;
				}
				game.activeParty.members.get(currentPlayerIndex).startSelectedAnimation();
			}
		}
		//���ǂ�.
		else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f && game.touchGlY < -0.75f)
		{
			if(this.MODE == this.MODE_WAITING_WHATTODO)
			{
				parent.setTextAndShow(" ");
				parent.setMode(TownMode.WaitingWhatToDo);
			}
			else if(this.MODE == this.MODE_WAITING_TARGET_SELECT || this.MODE == this.MODE_WAITING_COMPLETE)
			{
				this.MODE = this.MODE_WAITING_WHATTODO;
				parent.setTextAndShow("�����͋���ł��B�������]�݂��ȁH");
				game.activeParty.members.get(currentPlayerIndex).stopSelectedAnimation();
			}
			else if(this.MODE == this.MODE_WAITING_CONFIRM)
			{
				this.MODE = MODE_WAITING_TARGET_SELECT;
				parent.setTextAndShow("�N�𕜊�������̂��ȁH");
			}
		}
	}
}
