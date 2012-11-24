package all.jp.Game.Sound;
import java.io.IOException;
import java.util.HashMap;

import all.jp.R;
import all.jp.Game.Base.GameManager;
import android.media.MediaPlayer;

public class BgmManager
{
	private GameManager game;
	private MediaPlayer currentBgm = null;
	
	public BgmManager(GameManager game)
	{
		this.game = game;
		
	}
	
	public void play(String key)
	{
		if(currentBgm != null && currentBgm.isPlaying())
		{
			stop();
		}
		
		currentBgm = MediaPlayer.create(game.activity, game.activity.getResources().getIdentifier(key, "raw", game.activity.getPackageName()));
		currentBgm.setLooping(true);
		currentBgm.start();
	}
	
	public void stop()
	{
		if(currentBgm == null)
			return;
		
		currentBgm.stop();
		currentBgm = null;
	}
	
	
}
