package all.jp.Game.Dungeon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cx.fam.tak0294.storage.DBHelper;

import all.jp.Game.Direction;
import all.jp.Game.Position;
import all.jp.Game.Base.GameManager;
import all.jp.util.Global;

public class Map
{
	public class MapParser
	{
		private BufferedReader br;
		private InputStream in;
		
		public MapParser(InputStream in)
		{
			this.in = in;
		}
		
		public ArrayList<String> search(String regexStr)
		{
			return this.search(regexStr, 0);
		}
		
		public ArrayList<HashMap<String, String>> getMessages(int floorNumber)
		{
			try {
				in.reset();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(in), 1024*1024*1);
			
			Pattern startPattern = Pattern.compile("\\[FloorTop\\]");
			Pattern endPattern 	 = Pattern.compile("\\[FloorBottom\\]");
			
			String str;
			int floorCount = 0;
			boolean addEnable = false;
			boolean isMessageBody = false;
			String messagePos = "";
			String messageBody = "";
			ArrayList<String> array = new ArrayList<String>();
			ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String, String>>();
			
			for(int ii=0;ii<floorNumber;ii++)
			{
				res.add(ii, new HashMap<String,String>());
			}
			
			try {
				while((str = br.readLine()) != null)
				{
					boolean startMatch 	= false;
					boolean endMatch 	= false;
					
					Matcher startMatcher = startPattern.matcher(str);
					Matcher endMatcher 	 = endPattern.matcher(str);
					if(startMatcher.matches())
					{
						addEnable  = true;
						startMatch = true;
					}
					if(endMatcher.matches())
					{
						addEnable = false;
						endMatch  = true;
					}
					
					//行ごとの処理.
					if(!startMatch && !endMatch)
					{
						Matcher floorChangeMatch  = Pattern.compile("\\[Floor").matcher(str);
						Matcher messageStartMatch = Pattern.compile("^\\w{4}$").matcher(str);
						Matcher messageEndMatch   = Pattern.compile("\\[Message_End\\]").matcher(str);

						
						if(floorChangeMatch.find())
						{
							//階の変化.
							floorCount++;
							res.add(new HashMap<String,String>());
							continue;
						}
						
						//メッセージの開始.
						if(!isMessageBody && messageStartMatch.matches())
						{
							isMessageBody = true;
							messagePos = str;
							continue;
						}
						if(isMessageBody && messageEndMatch.matches())
						{
							isMessageBody = false;
							res.get(floorCount).put(messagePos, messageBody);
							messagePos = "";
							messageBody = "";
							continue;
						}
						
						if(isMessageBody)
						{
							messageBody += str + "\n";
						}
					}
					
					if(addEnable)
						array.add(str);
					
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			
			return res;
		}
		
		public ArrayList<String> search(String regexStr, int index)
		{
			try {
				in.reset();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(in), 1024*1024*1);
			
			Pattern pattern = Pattern.compile(regexStr);
			
			
			String str;
			ArrayList<String> array = new ArrayList<String>();
			
			try {
				while((str = br.readLine()) != null)
				{
					Matcher matcher = pattern.matcher(str);
					if(matcher.matches())
					{
						array.add(matcher.group(index));
					}
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			
			return array;
		}
		
		public void parse()
		{
			
		}
	}
	public GameManager game;
	private int[][][][] mapData;
	public int[][][] eventData;
	public ArrayList<HashMap<String,String>> events;
	public String mapName;
	public ArrayList<String> floorNames;
	public int floorNumber;
	public int mapSizeX, mapSizeY;
	
	public void load(InputStream in)
	{
		MapParser mapParser = new MapParser(in);

		//迷宮名.
		ArrayList<String> mapNames = mapParser.search("MapName\\s=\\s(.*)$", 1);
		this.mapName = mapNames.get(0);

		//フロア名.
		this.floorNames = mapParser.search("FloorName\\[\\d\\]\\s=\\s(.*)$", 1);

		//フロア数.
		ArrayList<String> floorNumbers = mapParser.search("Floor\\s=\\s(.*)$", 1);
		this.floorNumber = Integer.parseInt(floorNumbers.get(0));

		//マップサイズ.
		ArrayList<String> mapSizeXs = mapParser.search("MapSizeX\\s=\\s(.*)$", 1);
		ArrayList<String> mapSizeYs = mapParser.search("MapSizeY\\s=\\s(.*)$", 1);
		this.mapSizeX = Integer.parseInt(mapSizeXs.get(0));
		this.mapSizeY = Integer.parseInt(mapSizeYs.get(0));

		//イベントデータ.
		events = mapParser.getMessages(this.floorNumber);
		eventData = new int[floorNumber+1][mapSizeY+1][mapSizeX+1];

		//マップデータ用配列
		mapData = new int[5][floorNumber+1][mapSizeY+2][mapSizeX+2];
		
		//マップデータ.
		ArrayList<String> mapDatas = mapParser.search("^\\w{40,42}$");

		//db.
		DBHelper db = new DBHelper(game.activity);
		
		int mapType		 = 0;
		int mapLineCount = 0;
		int floorCount   = 0;
		for(int ii=0;ii<mapDatas.size();ii++)
		{
			int maxLine = mapType!=1?mapSizeY+1:mapSizeY+2;
			String mapLine = mapDatas.get(ii);
			
			for(int jj=0;jj<mapLine.length();jj+=2)
			{
				String hex = mapLine.substring(jj, jj+2);
				int data = Integer.parseInt(hex, 16);
				mapData[mapType][floorCount][mapLineCount][jj/2] = data;

				//イベントの存在判定.
				if(floorCount < floorNumber && mapType == 2)
				{
					String xHexCount = Integer.toString((int)((jj/2)/16));
					String xDecCount = Integer.toHexString((jj/2)%16);
					String yHexCount = Integer.toString((int)(mapLineCount/16));
					String yDecCount = Integer.toHexString(mapLineCount%16);
					String message = events.get(floorCount).get(xHexCount + xDecCount + yHexCount + yDecCount);

					if(message != null)
					{
						//自動開始のイベント判定.
						Pattern pattern = Pattern.compile("event([0-9]+)");
						Matcher matcher = pattern.matcher(message);
						String event_id = "";
						if(matcher.find())
						{
							event_id = matcher.group(1);
						}
						HashMap<String,String> cond = new HashMap<String,String>();
						cond.put("event_code", event_id);
						ArrayList<HashMap<String,String>> res = db.get("event_mt", cond);
						if(res.get(0).get("event_autoStartFlag").equals("1"))
						{
							//特殊イベント.
							if(res.get(0).get("event_code").equals(Global.EVENTCODE_STAIRUP))
							{
								System.out.println("登りイベント " + floorCount + "階");
								eventData[floorCount][mapLineCount][jj/2] = Integer.parseInt(Global.EVENTCODE_STAIRUP);
							}
							else if(res.get(0).get("event_code").equals(Global.EVENTCODE_STAIRDOWN))
							{
								eventData[floorCount][mapLineCount][jj/2] = Integer.parseInt(Global.EVENTCODE_STAIRDOWN);
							}
							else
							{
								eventData[floorCount][mapLineCount][jj/2] = 1;
							}
						}
						else
							eventData[floorCount][mapLineCount][jj/2] = 2;
					}
					else
					{
						eventData[floorCount][mapLineCount][jj/2] = 0;
					}
				}
			}
			
			mapLineCount++;

			if(mapLineCount == maxLine)
			{
				mapLineCount = 0;
				floorCount++;
				
				if(floorCount == (floorNumber+1))
				{
					mapType++;
					floorCount = 0;
				}
			}
		}
	}
	
	
	public int[][][] getCurrentView(Position position)
	{
		int x = position.x;
		int y = position.y;
		int direction = position.direction;
		int floor = position.floor;

		int tmpX = 0;
		int tmpY = 0;
		
		int orgX = 0;
		int orgY = 0;
		
		if(direction == Direction.North)
		{
			tmpX = orgX = x - 1;
			tmpY = orgY = y - 6;
		}
		else if(direction == Direction.South)
		{
			tmpX = orgX = x - 1;
			tmpY = orgY = y - 1;
		}
		else if(direction == Direction.East)
		{
			tmpX = orgX = x;
			tmpY = orgY = y - 3;
		}
		else if(direction == Direction.West)
		{
			tmpX = orgX = x - 4;
			tmpY = orgY = y - 3;
		}
		
		int[][][] walls = new int[4][6][6];
		
		//-------------------------------------------------------
		//	縦壁.
		//-------------------------------------------------------
		int ii=0;
		int jj=0;
		for(ii=0;ii<6;ii++)
		{
			
			tmpX = orgX;
			tmpY += 1;

			for(jj=0;jj<6;jj++)
			{
				tmpX += 1;
				
				if(tmpY < 0)
				{
					walls[0][ii][jj] = 0;
					continue;
				}
				else if(tmpY > mapSizeY)
				{
					walls[0][ii][jj] = 0;
					continue;
				}
				
				if(tmpX < 0)
				{
					walls[0][ii][jj] = 0;
					continue;
				}
				else if(tmpX > mapSizeX)
				{
					walls[0][ii][jj] = 0;
					continue;
				}
				
				walls[0][ii][jj] = mapData[0][floor][tmpY][tmpX];
			}
		}
		
	
		//-------------------------------------------------------
		//	横壁.
		//-------------------------------------------------------
		ii=0;
		jj=0;
		tmpX = orgX;
		if(direction == Direction.North)
		{
			tmpY = y - 5;
		}
		else if(direction == Direction.South)
		{
			tmpY = y;
		}
		else if(direction == Direction.East)
		{
			tmpY = y - 2;
		}
		else if(direction == Direction.West)
		{
			tmpY = y - 2;
		}
		
		for(ii=0;ii<6;ii++)
		{
			tmpX = orgX;
			tmpY += 1;
			

			for(jj=0;jj<6;jj++)
			{
				tmpX += 1;
				

				if(tmpY < 0)
				{
					walls[1][ii][jj] = 0;
					continue;
				}
				else if(tmpY > mapSizeY+1)
				{
					walls[1][ii][jj] = 0;
					continue;
				}
				
				if(tmpX < 0)
				{
					walls[1][ii][jj] = 0;
					continue;
				}
				else if(tmpX > mapSizeX-1)
				{
					walls[1][ii][jj] = 0;
					continue;
				}
				
				walls[1][ii][jj] = mapData[1][floor][tmpY][tmpX];
			}
		}
		
		
		
		
		
		//-------------------------------------------------------
		//	床,イベント.
		//-------------------------------------------------------
		ii=0;
		jj=0;
		tmpX = orgX;
		if(direction == Direction.North)
		{
			tmpY = y - 6;
		}
		else if(direction == Direction.South)
		{
			tmpY = y - 1;
		}
		else if(direction == Direction.East)
		{
			tmpY = y - 3;
		}
		else if(direction == Direction.West)
		{
			tmpY = y - 3;
		}
		
		for(ii=0;ii<6;ii++)
		{
			tmpX = orgX;
			tmpY += 1;
			

			for(jj=0;jj<6;jj++)
			{
				tmpX += 1;
				

				if(tmpY < 0)
				{
					walls[2][ii][jj] = 0;
					continue;
				}
				else if(tmpY > mapSizeY)
				{
					walls[2][ii][jj] = 0;
					continue;
				}
				
				if(tmpX < 0)
				{
					walls[2][ii][jj] = 0;
					continue;
				}
				else if(tmpX > mapSizeX)
				{
					walls[2][ii][jj] = 0;
					continue;
				}
				
				walls[2][ii][jj] = mapData[2][floor][tmpY][tmpX];
				walls[3][ii][jj] = eventData[floor][tmpY][tmpX];
			}
		}
		
		return walls;
	}
}

