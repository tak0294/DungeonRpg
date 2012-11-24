package all.jp.Game.Magic;

import javax.microedition.khronos.opengles.GL11;

import all.jp.Game.Base.GameManager;
import all.jp.Game.Magic.MagicBase;
import all.jp.util.FontTexture;
import all.jp.util.GraphicUtil;
import android.graphics.Typeface;

public class MagicManager
{
	private GameManager game;
	private FontTexture itemFontTexture;
	private FontTexture itemDescriptionTexutre;
	private int backgroundTexture;
	private int descriptionBackgroundTexture;
	private int handCursorTexture;
	
	//アイテム表示.
	private int 	lineNum = 7;	//アイテムリストを何行表示するか.
	private boolean updateMagicListRequest = false;
	private boolean updateMagicDescriptionRequest = false;
	private int 	currentMagicPage  = 0;
	private int	    currentMagicIndex = 0;

	private String overRideString = "";
	private boolean isOverRideString = false;
	
	public MagicManager(GameManager game)
	{
		this.game = game;
		this.itemDescriptionTexutre = new FontTexture();

		this.itemFontTexture = new FontTexture();
		this.itemFontTexture.maxLineCount = lineNum;
		this.itemFontTexture.height_margin = 5;
		init();
	}

	public void init()
	{
		currentMagicPage = 0;
		currentMagicIndex = 0;
		updateMagicDescriptionRequest = true;
		updateMagicListRequest = true;
	}
	
	public void initTexture(GL11 gl)
	{
		this.itemDescriptionTexutre.createTextBuffer(gl);
		this.itemFontTexture.createTextBuffer(gl);
		this.itemFontTexture.m_paint.setTypeface(Typeface.MONOSPACE);
		backgroundTexture  = GraphicUtil.loadTexture(gl, "itemwindow");
		handCursorTexture  = GraphicUtil.loadTexture(gl, "cursor");
		descriptionBackgroundTexture  = GraphicUtil.loadTexture(gl, "battlesubwindow");
	}

	private void updateMagicDescription(GL11 gl)
	{
		int index = (this.currentMagicPage*lineNum) + this.currentMagicIndex;
		MagicBase magic = game.activeParty.getActivePlayer().magics.get(index);
		
		this.itemDescriptionTexutre.resetPreDrawCount();
		this.itemDescriptionTexutre.preDrawBegin();
		this.itemDescriptionTexutre.drawStringToTexture(magic.description, 400);
		this.itemDescriptionTexutre.preDrawEnd(gl);
	}
	
	private void updateMagicDescriptionWithString(GL11 gl, String str)
	{
		this.itemDescriptionTexutre.resetPreDrawCount();
		this.itemDescriptionTexutre.preDrawBegin();
		this.itemDescriptionTexutre.drawStringToTexture(str, 400);
		this.itemDescriptionTexutre.preDrawEnd(gl);
	}
	
	public void updateMagicDescriptionWithString(String str)
	{
		this.overRideString = str;
		this.isOverRideString = true;
	}
	
	public void cursorUp()
	{
		this.currentMagicIndex--;
		if(this.currentMagicIndex < 0)
		{
			if(this.currentMagicPage > 0)
			{
				this.currentMagicPage--;
				this.currentMagicIndex = lineNum-1;
				this.updateMagicListRequest = true;
			}
			else
				this.currentMagicIndex = 0;
		}
		
		this.updateMagicDescriptionRequest = true;
	}
	
	public void cursorDown()
	{
		int maxPage = (int) Math.ceil(game.activeParty.getActivePlayer().magics.size()/(double)lineNum);
		int maxIndex = lineNum-1;
		if(currentMagicPage == maxPage-1)
		{
			maxIndex = game.activeParty.getActivePlayer().magics.size() - (currentMagicPage * lineNum) - 1;
		}
		
		this.currentMagicIndex++;
		if(this.currentMagicIndex > maxIndex)
		{
			if(this.currentMagicPage < maxPage-1)
			{
				currentMagicPage++;
				currentMagicIndex = 0;
				this.updateMagicListRequest = true;
			}
			else
				this.currentMagicIndex = maxIndex;
		}
		
		this.updateMagicDescriptionRequest = true;
	}
	
	
	private String getFormatedName(String name)
	{
		int len = name.length();
		String space = "";
		for(int ii=len;ii<10;ii++)	space += "　";
		return name + space;
	}
	
	public void drawMagicList(GL11 gl)
	{
		
		String magicList = "";
		
		for(int ii=(currentMagicPage*lineNum);ii<(currentMagicPage*lineNum) + lineNum;ii++)
		{
			if(game.activeParty.getActivePlayer() == null ||
			   ii >= game.activeParty.getActivePlayer().magics.size())
				break;
			
			MagicBase magic = game.activeParty.getActivePlayer().magics.get(ii);
			magicList += this.getFormatedName(magic.name);
			magicList += "\n";
		}
		
		this.itemFontTexture.resetPreDrawCount();
		this.itemFontTexture.preDrawBegin();
		this.itemFontTexture.drawStringToTexture(magicList, 400);
		this.itemFontTexture.preDrawEnd(gl);
	}
	
	public void draw(GL11 gl)
	{
		if(updateMagicListRequest)
		{
			this.drawMagicList(gl);
			updateMagicListRequest = false;
		}
		
		if(this.updateMagicDescriptionRequest)
		{
			this.updateMagicDescription(gl);
			this.updateMagicDescriptionRequest = false;
		}
		
		if(this.isOverRideString)
		{
			this.updateMagicDescriptionWithString(gl, this.overRideString);
			this.isOverRideString = false;
			this.overRideString = "";
		}
		
		int no	= itemFontTexture.getTexture();
		int sx	= itemFontTexture.getWidth();
		int sy	= itemFontTexture.getHeight();
		float offset= itemFontTexture.getOffset();

		int no2	= itemDescriptionTexutre.getTexture();
		int sx2	= itemDescriptionTexutre.getWidth();
		int sy2	= itemDescriptionTexutre.getHeight();
		float offset2= itemDescriptionTexutre.getOffset();

		
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GraphicUtil.drawTexture(gl, -0.3f, -0.8f, 2.3f, 0.35f, descriptionBackgroundTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl, -0.3f, 0.05f, 2.3f, 1.01f, backgroundTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		GraphicUtil.drawTexture(gl,-(2.4f - sx/180.0f)*0.5f,(1.0f - sy/140.0f) * 0.5f,sx/180.0f,sy/140.0f,no, 0,offset/256.0f,sx/512.0f, sy/256.0f, 1.0f,1.0f,1.0f, 1.0f);
		GraphicUtil.drawTexture(gl,-(2.82f - sx2/180.0f)*0.5f,(-1.3f - sy2/140.0f) * 0.5f,sx2/180.0f,sy2/140.0f,no2, 0,offset2/256.0f,sx2/512.0f, sy2/256.0f, 1.0f,1.0f,1.0f, 1.0f);
		
		GraphicUtil.drawTexture(gl, -1.28f, 0.43f - (currentMagicIndex * 0.135f), 0.2f, 0.3f, handCursorTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		gl.glDisable(GL11.GL_BLEND);
	}
	
	public MagicBase get()
	{
		int index = (this.currentMagicPage*lineNum) + this.currentMagicIndex;
		return game.activeParty.getActivePlayer().magics.get(index);
	}

}
