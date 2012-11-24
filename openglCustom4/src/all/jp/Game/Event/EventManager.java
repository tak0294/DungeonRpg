package all.jp.Game.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import cx.fam.tak0294.storage.DBHelper;

import all.jp.Game.Base.GameManager;
import all.jp.Game.Event.Battle.BattleManager;
import all.jp.Game.Event.Battle.BattleMode;
import all.jp.util.FontTexture;
import all.jp.util.GraphicUtil;
import android.graphics.Typeface;

public class EventManager
{
	private GameManager game;
	private BattleManager battle;
	private FontTexture fontTexture;
	private GL11 gl;
	private float backgroundAlpha = 0.0f;
	private Event currentEvent = null;
	
	public boolean willClose = false;
	public boolean isTouch = false;
	private boolean nextPageRequest = false;

	private int bottomWindowTexture;
	
	//現在のモード.
	private int MODE;
	
	//画像表示用.
	private int currentTexture = -1;
	private String requestTextureName = "";
	
	//文章表示用.
	private ArrayList<Integer> lineIndexes;
	private int currentLineIndex = 0;
	private int currentCharIndex = 0;
	private boolean isCursorEnd = false;
	
	private int srcRowIndex;
	private String currentEventText;
	private String[] eventScripts;
	
	//confirm文退避用.
	private int yesButtonTexture;
	private int noButtonTexture;
	private String confirmEventScript;
	
	public boolean isEnableEvent;
	
	public EventManager(GameManager game)
	{
		this.game = game;
		this.battle = new BattleManager(game);
		this.fontTexture = new FontTexture();
		this.isEnableEvent = false;
		this.willClose = false;
	}
	
	public int getMode()
	{
		return MODE;
	}
	
	public void initTextures(GL11 gl)
	{
		this.gl = (GL11) gl;
		this.battle.initTextures(gl);
		this.fontTexture.createTextBuffer(gl);
		this.fontTexture.m_paint.setTypeface(Typeface.MONOSPACE);
		this.yesButtonTexture = GraphicUtil.loadTexture(gl, "yes_button");
		this.noButtonTexture  = GraphicUtil.loadTexture(gl, "no_button");
		this.bottomWindowTexture = GraphicUtil.loadTexture(gl, "bottomwindow");
	}
	
	public void nextPage()
	{
		this.isCursorEnd = false;
		this.currentCharIndex = this.lineIndexes.get(this.currentLineIndex);
		this.currentLineIndex++;
		if(this.currentLineIndex > this.lineIndexes.size())
			this.currentLineIndex = this.lineIndexes.size();
	}
	
	private int getPreviousCharIndex(int lineIndex)
	{
		if(lineIndex == 0)
			return 0;
		else
		{
			return this.lineIndexes.get(lineIndex-1);
		}
	}
	
	public boolean readNextEvent()
	{
		srcRowIndex++;
		
		if(srcRowIndex == eventScripts.length)
			return false;
		
		if(eventScripts[srcRowIndex].equals(""))
			return readNextEvent();
		
		//TODO
		//confirmの判定をついかする.
		Pattern p = Pattern.compile("(stairsDown|stairsUp|battle|confirm|battle|message|image|set|unset)\\[(.*?)(?:\\|(.*?))?\\]");
		Matcher m = p.matcher(eventScripts[srcRowIndex]);

		if(m.find())
		{
			//戦闘.
			if(m.group(1).equals("battle"))
			{
				MODE = EventMode.Battle;
				String[] splitedIndex = m.group(2).split(",");

				//戦闘BGM.
				game.bgm.play("battle1");
				
				//出現する的グループを決定.
				Random rnd = new Random();
				int index = rnd.nextInt(splitedIndex.length);
				this.battle.setupBattle(splitedIndex[index]);
			}
			
			//選択肢.
			if(m.group(1).equals("confirm"))
			{
				System.out.println("Confirm MODE");
				//System.out.println("Message = " + m.group(2));
				MODE = EventMode.Confirm;
				this.setTextAndShow(m.group(2));
			}
			
			//メッセージ.
			if(m.group(1).equals("message"))
			{
				String message = m.group(2);
				message = message.replace("$", "\n");
				MODE = EventMode.Text;
				this.setTextAndShow(message);
			}
			
			//画像.
			if(m.group(1).equals("image"))
			{
				this.setImageAndShow(m.group(2));
				
				//--------------------------------------------
				//	画像と一緒に表示するメッセージが存在する.
				//--------------------------------------------
				if(m.group(3) != null && !m.group(3).equals(""))
				{
					MODE = EventMode.ImageAndText;
					String message = m.group(3);
					message = message.replace("$", "\n");
					this.setTextAndShow(message);
				}
				else
				{
					MODE = EventMode.Image;
				}
			}
			
			//フラグのセット.
			if(m.group(1).equals("set"))
			{
				game.setFlag(m.group(2), "1");
				
				if(!this.readNextEvent())
				{
					willClose = true;
				}
			}

			//フラグのアンセット.
			if(m.group(1).equals("unset"))
			{
				game.clearFlag(m.group(2));
				
				if(!this.readNextEvent())
				{
					willClose = true;
				}
			}
			
			//階段.
			if(m.group(1).equals("stairsUp") || m.group(1).equals("stairsDown"))
			{
				//のぼる.
				if(m.group(1).equals("stairsUp"))
				{
					game.dungeon.currentPosition.floor -= Integer.parseInt(m.group(2));
					
					//0階になったら街へ戻る.
					if(game.dungeon.currentPosition.floor < 0)
					{
						game.dungeon.currentPosition.floor = 0;
						game.openTown();
					}
					
					game.dungeon.updateViewRequest = true;
				}
				
				//くだる
				if(m.group(1).equals("stairsDown"))
				{
					game.dungeon.currentPosition.floor += Integer.parseInt(m.group(2));
					game.dungeon.updateViewRequest = true;
				}
				
				if(!this.readNextEvent())
				{
					willClose = true;
				}
			}
			
		}
		
		return true;
	}

	//------------------------------------------------------
	//	画像を設定し、表示する.
	//------------------------------------------------------
	private void setImageAndShow(String imageFileName)
	{
		if(this.currentTexture != -1)
		{
			//テクスチャの開放.
			int[] texs = {currentTexture};
			gl.glDeleteTextures(1, texs, 0);
			
			currentTexture = -1;
		}
		
		requestTextureName = imageFileName;
	}
	
	
	//------------------------------------------------------
	//	テキストを設定し、表示する.
	//------------------------------------------------------
	private void setTextAndShow(String text)
	{
		//行の折り返しIndex配列.
		this.isCursorEnd = false;
		this.currentLineIndex = 0;
		this.currentCharIndex = 0;
		this.lineIndexes = new ArrayList<Integer>();
		this.currentEventText = text;
		
		this.fontTexture.calcIndexMode = true;
		int index = 0;
		while(true)
		{
			index = this.fontTexture.drawStringToTexture(text, 280, index, 0);
			if(index != -1)
				this.lineIndexes.add(index);
			else
				break;
		}
		this.lineIndexes.add(text.length()-1);

		this.fontTexture.calcIndexMode = false;
		
		this.fontTexture.resetPreDrawCount();
//		this.fontTexture.preDrawBegin();
//		this.fontTexture.drawStringToTexture(text, 340);
//		this.fontTexture.preDrawEnd(gl);
	}

	
	//------------------------------------------------------------
	//	実行されるイベントスクリプトを抽出する.
	//------------------------------------------------------------
	private String getActiveEventScript(String eventScript)
	{
		String[] splited = eventScript.split("\\n");
		
		Pattern begin_brace = Pattern.compile("(if|else)\\(?(!?)(.*?)\\)?\\{");
		Pattern end_brace 	= Pattern.compile("\\}");
		
		int openCount 	= 0;
		boolean isFirstOpened = false;
		boolean isOpen	= false;
		boolean isTrueNested  = false;
		boolean isFalseNested = false;
		boolean isTrueMode = true;
		boolean result = false;
		
		String commonStr = "";
		String trueStr  = "";
		String falseStr = "";
		
		for(int ii=0;ii<splited.length;ii++)
		{
			Matcher begin_matcher 	= begin_brace.matcher(splited[ii]);
			Matcher end_matcher 	= end_brace.matcher(splited[ii]);

			boolean end_matched = end_matcher.find(); 
			boolean begin_matched = begin_matcher.find();
			
			if(end_matched)
			{
				openCount--;
				if(openCount == 0)
				{
					isOpen = false;
					isTrueMode = false;
				}
			}
			
			if(isOpen)
			{
				if(isTrueMode)
				{
					trueStr += splited[ii] + "\n";
				}
				else
				{
					falseStr += splited[ii] + "\n";
				}
			}
			
			
			if(begin_matched)
			{
				if(!isFirstOpened)
				{
					//System.out.println("Condition = " + begin_matcher.group(3));
					String condition  = begin_matcher.group(3);
					
					//---------------------------------------------------------
					//	選択肢表示.
					//---------------------------------------------------------
					if(condition.indexOf("confirm") > -1)
					{
						//選択肢ifを含むブロックを抽出する.
						String tmp = "";
						for(int jj=ii;jj<splited.length;jj++)
						{
							tmp += splited[jj] + "\n";
						}
						Pattern confirm_pattern = Pattern.compile("if\\(.*?\\)\\s?\\{.+", Pattern.MULTILINE | Pattern.DOTALL);
						Matcher confirm_matcher = confirm_pattern.matcher(tmp);
						confirm_matcher.find();
						//System.out.println("confirm_matcher = " + confirm_matcher.group(0));
						this.confirmEventScript = confirm_matcher.group(0);
						
						commonStr += confirm_matcher.group(0);
					}
					//---------------------------------------------------------
					//	フラグ判断.
					//---------------------------------------------------------
					else
					{
						String flag_value = "";
						if(begin_matcher.group(3).equals("TRUE"))
							flag_value = "1";
						else if(begin_matcher.group(3).equals("FALSE"))
							flag_value = "0";
						else
							flag_value = game.getFlag(begin_matcher.group(3));
		
						if((!begin_matcher.group(2).equals("") && !flag_value.equals("1")) ||
						   (begin_matcher.group(2).equals("") && flag_value.equals("1")))
						{
							result = true;
						}
						else
						{
							result = false;
						}
						

					}
					
					trueStr = commonStr + trueStr;
					falseStr = commonStr + falseStr;
					commonStr = "";
					isFirstOpened = true;
				}
				
				//------------------------------------
				//	open済みで更に開く場合はネストしている.
				//------------------------------------
				if(isOpen)
				{
					if(isTrueMode)
						isTrueNested = true;
					else
						isFalseNested = true;
				}
				
				isOpen = true;
				openCount++;
			}
			
			//-----------------------------------------------------
			//	共通メッセージ処理.
			//-----------------------------------------------------
			if(!begin_matched && !end_matched && !isOpen)
			{
				commonStr += splited[ii] + "\n";
			}
		}
		
		trueStr += commonStr;
		falseStr += commonStr;
		
		trueStr = trueStr.replaceAll("\\t", "");
		falseStr = falseStr.replaceAll("\\t", "");
		
		System.out.println("============truestr============");
		System.out.println(trueStr);
		
		System.out.println("============falsestr============");
		System.out.println(falseStr);
		
		
		if(trueStr.equals("") && falseStr.equals(""))
			return eventScript.replaceAll("\\t", "");
		
		if(result)
		{
			if(isTrueNested)
				return this.getActiveEventScript(trueStr);
			else
				return trueStr;
		}
		else
		{
			if(isFalseNested)
				return this.getActiveEventScript(falseStr);
			else
				return falseStr;
		}
	}
	
	//------------------------------------------------------
	//	イベントエンジンの初期化.
	//------------------------------------------------------
	public void setEventByCode(String event_code)
	{
		//DBからイベントを取得.
		DBHelper db = new DBHelper(game.activity);
		HashMap<String,String> in = new HashMap<String,String>();
		in.put("event_code", event_code);
		ArrayList<HashMap<String, String>> res = db.get("event_mt", in);
		
		setEvent(res);
	}
	
	public void clearEvent()
	{
		MODE = 0;
		requestTextureName = "";
	}
	
	public void setEvent(ArrayList<HashMap<String, String>> res)
	{
		//モード初期化
		MODE = 0;
		requestTextureName = "";
		
		Event e = new Event();
		e.eventText = this.getActiveEventScript(res.get(0).get("event_src"));
		System.out.println("EventText = " + e.eventText);
		
		//スクリプトが空の場合はイベントを発生させない.
		if(e.eventText.trim().equals(""))
			return;
		
		this.currentEvent = e;
		
		eventScripts = this.currentEvent.eventText.split("\\n");
		
		
		this.srcRowIndex = -1;
		this.readNextEvent();
		this.isEnableEvent = true;

		//背景の暗幕ON.
		game.isOverrayBlack = true;
	}
	
	public void draw(GL11 gl)
	{
		//------------------------------------------------------------
		//	背景.
		//------------------------------------------------------------
		if(MODE != EventMode.Battle)
		{
			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			game.bottomWindowSprite.draw(gl, bottomWindowTexture);
			gl.glDisable(GL11.GL_BLEND);
		}
		
		
		//------------------------------------------------------------
		//	テキスト描画モード.
		//------------------------------------------------------------
		if(MODE == EventMode.Text || MODE == EventMode.ImageAndText || MODE == EventMode.Confirm)
		{
			this.fontTexture.resetPreDrawCount();
			
			if(nextPageRequest)
			{
				this.nextPage();
				nextPageRequest = false;
			}
			
			try{
				if(this.lineIndexes.size() > 0 &&
				   this.currentLineIndex < this.lineIndexes.size() &&
				   this.currentCharIndex <= this.lineIndexes.get(currentLineIndex))
				{
					this.fontTexture.preDrawBegin();
					this.currentCharIndex = this.fontTexture.drawStringToTexture(this.currentEventText, 280, this.getPreviousCharIndex(currentLineIndex), this.currentCharIndex+1);
					this.fontTexture.preDrawEnd(gl);
					
					if(this.currentCharIndex == this.lineIndexes.get(currentLineIndex))
						this.isCursorEnd = true;
				}
			}
			catch(Exception e)
			{
				currentLineIndex = this.lineIndexes.size() - 1;
				this.isCursorEnd = true;
			}
			
			int no	= fontTexture.getTexture();
			int sx	= fontTexture.getWidth();
			int sy	= fontTexture.getHeight();
			float offset= fontTexture.getOffset();
	
			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GraphicUtil.drawTexture(gl,-(2.75f - sx/130.0f)*0.5f,(-0.85f - sy/130.0f) * 0.5f,sx/130.0f,sy/130.0f,no, 0,offset/256.0f,sx/512.0f, sy/256.0f, 1.0f,1.0f,1.0f, 1.0f);
			gl.glDisable(GL11.GL_BLEND);
		}
		
		//------------------------------------------------------------
		//	画像表示モード.
		//------------------------------------------------------------
		if(MODE == EventMode.Image || MODE == EventMode.ImageAndText)
		{
			//--------------------------------------
			//	画像の読み込みリクエストがあれば処理する.
			//--------------------------------------
			if(requestTextureName != "" && currentTexture == -1)
			{
				this.currentTexture = GraphicUtil.loadTexture(gl, requestTextureName);
			}
			GraphicUtil.drawTexture(gl, -0.3f, 0.4f, 1.0f, 1.0f, currentTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		
		//------------------------------------------------------------
		//	選択肢表示モード.
		//------------------------------------------------------------
		if(MODE == EventMode.Confirm)
		{
			GraphicUtil.drawTexture(gl, 0.3f, 0.2f, 1.0f, 0.5f, yesButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			GraphicUtil.drawTexture(gl, -0.9f, 0.2f, 1.0f, 0.5f, noButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		


		
		//------------------------------------------------------------
		//	戦闘モード.
		//------------------------------------------------------------
		if(MODE == EventMode.Battle)
		{
			this.battle.draw(gl);
		}

	}
	
	public void update()
	{
		if(willClose)
		{
			backgroundAlpha = 0.0f;
			willClose = false;
			isEnableEvent = false;
			game.isOverrayBlack = false;
		}
		
		
		if(game.isTouch && !this.isTouch)
		{
			System.out.println("MODE = " + MODE);
			
			//--------------------------------------
			//	テキスト表示モード.
			//--------------------------------------
			if(MODE == EventMode.Text || MODE == EventMode.ImageAndText)
			{
				//文章の表示途中の場合は一気に表示させる.
				if(!isCursorEnd)
				{
					this.currentCharIndex = this.lineIndexes.get(currentLineIndex)-1;	
				}
				else
				{
					if(this.currentLineIndex < this.lineIndexes.size()-1)
					{
						nextPageRequest = true;
					}
					else
					{
						//
						if(!this.readNextEvent())
						{
							willClose = true;
						}
					}
				}
			}
			//--------------------------------------
			//	画像表示モード.
			//--------------------------------------
			else if(MODE == EventMode.Image)
			{
				if(!this.readNextEvent())
				{
					willClose = true;
				}
			}
			//--------------------------------------
			//	選択肢表示モード.
			//--------------------------------------
			else if(MODE == EventMode.Confirm)
			{
				boolean isHit = false;
				if(game.touchGlX > -0.2f && game.touchGlX < 0.8f && game.touchGlY > -0.05f && game.touchGlY < 0.45f)
				{
					//confirm部分を1で置換する.
					this.confirmEventScript = this.confirmEventScript.replaceFirst("confirm\\[.*?\\]", "TRUE");
					isHit = true;
				}
				
				if(game.touchGlX > -1.4f && game.touchGlX < -0.4f && game.touchGlY > -0.05f && game.touchGlY < 0.45f)
				{
					//confirm部分を0で置換する.
					this.confirmEventScript = this.confirmEventScript.replaceFirst("confirm\\[.*?\\]", "FALSE");
					isHit = true;
				}
				
				if(isHit)
				{
					MODE = 0;
					eventScripts = this.getActiveEventScript(this.confirmEventScript).split("\\n");
					this.srcRowIndex = -1;
					if(!this.readNextEvent())
					{
						willClose = true;
					}
				}
			}
		}

		//--------------------------------------
		//	戦闘モード.
		//--------------------------------------
		if(MODE == EventMode.Battle)
		{
			this.battle.update();
		}
		
		this.isTouch = game.isTouch;
	}
}
