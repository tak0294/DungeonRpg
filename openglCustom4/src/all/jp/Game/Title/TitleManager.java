package all.jp.Game.Title;

import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.Base.GameManager;
import all.jp.util.GLSprite;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;
import all.jp.util.SpriteType;
import android.graphics.Typeface;

public class TitleManager
{
	private GameManager game;
	private int titleTexture;
	private int soundOnButton;
	private int soundOffButton;
	private int animeOnButton;
	private int animeOffButton;
	private int startButton;
	
	private int currentSoundButton;
	private int currentAnimeButton;
	
	private boolean isTouch = true;
	
	private GLSprite title;
	
	//------------------------------------
	//	コンストラクタ.
	//------------------------------------
	public TitleManager(GameManager game)
	{
		this.game   = game;
	}

	public void initTextures(GL11 gl)
	{
		titleTexture  	= GraphicUtil.loadTexture(gl, "title");
		soundOnButton  	= GraphicUtil.loadTexture(gl, "title_sound_button_on");
		soundOffButton  = GraphicUtil.loadTexture(gl, "title_sound_button_off");
		
		animeOnButton  	= GraphicUtil.loadTexture(gl, "title_anime_button_on");
		animeOffButton  = GraphicUtil.loadTexture(gl, "title_anime_button_off");
		
		startButton  	= GraphicUtil.loadTexture(gl, "title_start_button");
		
		currentSoundButton = soundOnButton;
		currentAnimeButton = animeOnButton;

		title = new GLSprite(0.0f, 0.5f, 0.0f, 2.5f,1.5f,0.0f);
		title.vbo = Global.primitives[SpriteType.PLANE];

	}

	//------------------------------------
	//	描画.
	//------------------------------------
	public void draw(GL11 gl)
	{
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		title.draw(gl, titleTexture);
		GraphicUtil.drawTexture(gl, 0.0f, -0.2f, 1.0f, 0.8f, startButton, 1.0f, 1.0f, 1.0f, 1.0f);
		
		
		//GraphicUtil.drawTexture(gl, 0.0f, 0.5f, 2.5f, 1.5f, titleTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		
		GraphicUtil.drawTexture(gl, -0.8f, -0.7f, 1.0f, 0.8f, currentSoundButton, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, 0.8f, -0.7f, 1.0f, 0.8f, currentAnimeButton, 1.0f, 1.0f, 1.0f, 1.0f);
		
		gl.glDisable(GL11.GL_BLEND);
	}



	//------------------------------------
	//	更新.
	//------------------------------------
	public void update()
	{
		if(game.isTouch && !this.isTouch)
		{
			//StartButton.
			if(game.touchGlX > -0.5f && game.touchGlX < 0.5f &&
			   game.touchGlY > -0.6f && game.touchGlY < 0.2f)
			{
				game.titleNext();
			}
			
			//SoundButton.
			if(game.touchGlX > -1.3f && game.touchGlX < -0.3f &&
			   game.touchGlY > -1.1f && game.touchGlY < -0.3f)
			{
				if(currentSoundButton == soundOnButton)
				{
					currentSoundButton = soundOffButton;
					Global.isSound = false;
				}
				else
				{
					currentSoundButton = soundOnButton;
					Global.isSound = true;
				}
			}
			
			//AnimeButton.
			if(game.touchGlX > 0.3f && game.touchGlX < 1.3f &&
			   game.touchGlY > -1.1f && game.touchGlY < -0.3f)
			{
				if(currentAnimeButton == animeOnButton)
				{
					currentAnimeButton = animeOffButton;
					Global.isAnime = false;
				}
				else
				{
					currentAnimeButton = animeOnButton;
					Global.isAnime = true;
				}
			}
			
			//決定.
			if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f && game.touchGlY < 0.15f)
			{
				
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
			}
		}
		
		this.isTouch = game.isTouch;
	}
}
