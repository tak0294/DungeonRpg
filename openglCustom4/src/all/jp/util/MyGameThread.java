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
	//	メンバ.
	//------------------------------------------------

	
	//------------------------------------------------
	//	コンストラクタ.
	//------------------------------------------------
	public MyGameThread(GameManager game)
	{
		this.game = game;
		
	}


	
	
	
	//------------------------------------------------------------------------------------------------
	//	更新処理.
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
			// 休憩
			try {
				Thread.sleep(10); // 負荷が大きくなりすぎるのを防ぐため、少し停止させましょう。
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!mIsActive) {
				// アクティブでなければゲームを進めない
				lastUpdateTime = System.currentTimeMillis();// 復帰時に更新処理が複数回行われないようにする
				continue;
			}
			// 1秒間に60回更新する difference = 17
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
