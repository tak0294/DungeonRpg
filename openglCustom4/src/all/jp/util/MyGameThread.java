package all.jp.util;

import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.GameMode;
import all.jp.Game.Base.GameManager;
import all.jp.Game.Dungeon.DungeonManager;


public class MyGameThread extends Thread
{
	private boolean mIsActive;
	private GameManager game;
	
	
	//------------------------------------------------
	//	�����o.
	//------------------------------------------------

	
	//------------------------------------------------
	//	�R���X�g���N�^.
	//------------------------------------------------
	public MyGameThread(GameManager game)
	{
		this.game = game;
		
	}


	
	
	
	//------------------------------------------------------------------------------------------------
	//	�X�V����.
	//------------------------------------------------------------------------------------------------
	private void update()
	{
		if(game.event.isEnableEvent)
		{
			game.event.update();
		}
		else if(game.gameMode == GameMode.Dungeon)
		{
			game.dungeon.update();
		}
		else if(game.gameMode == GameMode.Camp)
		{
			game.camp.update();
		}
		else if(game.gameMode == GameMode.Town)
		{
			game.town.update();
		}
		else if(game.gameMode == GameMode.Title)
		{
			game.title.update();
		}
	}
	
	
	
	
	public void init()
	{
		mIsActive = true;
	}

	
	public void resumeGameThread() {
		mIsActive = true;
	}

	public void pauseGameThread() {
		mIsActive = false;
	}
	
	@Override
	public void run() {
		long lastUpdateTime = System.currentTimeMillis();
		while (true) {
			// �x�e
			try {
				Thread.sleep(10); // ���ׂ��傫���Ȃ肷����̂�h�����߁A������~�����܂��傤�B
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!mIsActive) {
				// �A�N�e�B�u�łȂ���΃Q�[����i�߂Ȃ�
				lastUpdateTime = System.currentTimeMillis();// ���A���ɍX�V������������s���Ȃ��悤�ɂ���
				continue;
			}
			// 1�b�Ԃ�60��X�V���� difference = 17
			long nowTime = System.currentTimeMillis();
			long difference = nowTime - lastUpdateTime;
			while (difference >= 34) {
				difference -= 34;
				update();
			}
			lastUpdateTime = nowTime - difference;
		}
	}

}
