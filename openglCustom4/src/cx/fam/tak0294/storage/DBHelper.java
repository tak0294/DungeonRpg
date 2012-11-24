package cx.fam.tak0294.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.*;
import org.xmlpull.v1.XmlPullParser;
import android.util.Xml;
import au.com.bytecode.opencsv.CSVReader;

import cx.fam.tak0294.array.*;

/*
 * DB、TABLEの作成、更新処理を担当
 */
public class DBHelper extends SQLiteOpenHelper
{
	private Activity activity= null;
	
	/*
	 * コンストラクタ.
	 * @param	context		コンテキスト
	 */
	public DBHelper(Activity activity)
	{
		super(activity.getApplicationContext(), "testdb", null, 1);
		this.activity = activity;
	}
	
	/*
	 * DBへのテーブル作成処理.
	 * @param	db		操作対象のデータベース.
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
	}
	
	/*
	 * DBの更新時処理.
	 * @param	db		操作対象のデータベース.
	 * @param	oldVersion	データベースの旧バージョン.
	 * @param	newVersion	データベースの新バージョン.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		
	}
	
	public void truncate(String table)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		//query作成.
		String query = "DELETE FROM " + table + ";";
		db.execSQL(query);
	}
	
	public ArrayList<HashMap<String, String>> execQuery(String query)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		//query作成.
		Cursor cr = db.rawQuery(query, null);
		cr.moveToFirst();
		
		ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String,String>>();
		
		//結果セットが0件の場合はカラム名のみのHashMapを返す.
		if(cr.getCount() == 0)
		{
			cr.close();
			db.close();
			return res;
		}
		
		//結果セットをHashMapに格納.
		boolean hasNext = true;
		while(hasNext)
		{
			HashMap<String, String> saved	= new HashMap<String, String>();
			for(int ii=0;ii<cr.getColumnCount();ii++)
			{
				saved.put(cr.getColumnName(ii), cr.getString(ii));
			}
			
			res.add(saved);
			hasNext = cr.moveToNext();
		}
		
		cr.close();
		db.close();
		return res;
	}
	
	
	/*
	 * DB取得処理.
	 * @param	table		取得対象テーブル名
	 * @param	condition	取得条件のHashMap<String, String>
	 * @return	取得結果のArrayList<HashMap<String, String>>
	 */
	public ArrayList<HashMap<String, String>> get(String table, HashMap<String, String> condition)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<HashMap<String, String>> alist = this.getSchemeArrayList(table);
				
		String where = "";
		for(int ii=0;ii<alist.size();ii++)
		{
			String key 	= alist.get(ii).get("name");
			String type = alist.get(ii).get("type");
			
			if(condition.get(key) != null && !condition.get(key).equals(""))
			{
				String value = condition.get(key);
				
				if(!type.equals("INTEGER"))
				{
					value = "\"" + value + "\"";
				}
				
				if(!where.equals(""))
					where += " AND ";
				
				if((type.equals("DATE") || type.equals("DATETIME")) && value.toLowerCase().matches("^is"))
					where += key + " " + value;
				else
					where += key + "=" + value;
			}
		}
		
		//query作成.
		String w = !where.equals("")?" WHERE " + where:"";
		String query = "SELECT * FROM " + table + w +";";
		Cursor cr = db.rawQuery(query, null);
		cr.moveToFirst();
		
		ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String,String>>();
		
		//結果セットが0件の場合はカラム名のみのHashMapを返す.
		if(cr.getCount() == 0)
		{
			cr.close();
			if(db.isOpen())
				db.close();
			res.add(this.makeCondition(table));
			return res;
		}
		
		//結果セットをHashMapに格納.
		boolean hasNext = true;
		while(hasNext)
		{
			HashMap<String, String> saved	= new HashMap<String, String>();
			for(int ii=0;ii<alist.size();ii++)
			{
				String key 	= alist.get(ii).get("name");
				int columnIndex = cr.getColumnIndex(key);
				saved.put(key, cr.getString(columnIndex));
			}
			res.add(saved);
			hasNext = cr.moveToNext();
		}
		
		cr.close();
		db.close();
		return res;
	}
	
	/*
	 * DB更新処理.
	 * @param	table		更新対象テーブル名
	 * @param	condition	更新内容のHashMap<String, String>
	 * @return	更新結果のHashMap<String, String>
	 */
	public HashMap<String, String> set(String table, HashMap<String, String> condition)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<HashMap<String, String>> alist = this.getSchemeArrayList(table);
		ArrayList<HashMap<String, String>> pkeys = this.getPkeys(table);
		HashMap<String, String> updateValues = new HashMap<String, String>();

		String columns = "";
		String values  = "";
		String where   = "";
		String pkey	   = "";

		//Insert, Updateの判断.
		boolean update_flg = true;
		for(int ii=0;ii<pkeys.size();ii++)
		{
			String key = pkeys.get(ii).get("name");
			pkey = key;
			if(condition.get(key) == null || condition.get(key).equals(""))
			{
				update_flg = false;
				break;
			}
			//更新条件用テキスト更新.
			else
			{
				//存在するレコードの場合はUPDATEする.
				HashMap<String, String> in = new HashMap<String, String>();
				in.put(key, condition.get(key));
				ArrayList<HashMap<String, String>> res = this.get(table, in);
				if(res.get(0).get(key).equals(""))
				{
					update_flg = false;
					break;
				}
				
				if(!where.equals(""))
					where += "AND ";
				where += key + "=" + condition.get(key);
			}
		}
		
		//pkeyなしの場合はinsert.
		if(pkeys.size() == 0)
			update_flg = false;
		
		for(int ii=0;ii<alist.size();ii++)
		{
			String key 	= alist.get(ii).get("name");
			String type = alist.get(ii).get("type");
			String primary = alist.get(ii).get("primary");
		
			if(condition.get(key) != null && !condition.get(key).equals("") && update_flg)
			{
				String tmpval = condition.get(key);
				if(!type.equals("INTEGER"))
					tmpval = "\"" + tmpval + "\"";
				//updateValues += key + "=" + tmpval;
				updateValues.put(key, tmpval);
			}
			
			if((condition.get(key) == null || condition.get(key).equals("")) && !update_flg)
			{
				if(type.equals("INTEGER") && !primary.equals("true"))
				{
					condition.put(key, "0");
				}
				else
				{
					condition.put(key, "NULL");
				}
			}
			
			if(!update_flg)
			{
				columns += key;
				if(!condition.get(key).equals("NULL") && !type.equals("INTEGER"))
					values += "\"" + condition.get(key) + "\"";
				else
					values += condition.get(key);
				
				if(ii<(alist.size()-1))
				{
					columns += ",";
					values  += ",";
				}
			}
		}
		
		//query作成.
		HashMap<String, String> saved 	= new HashMap<String, String>();
		String query = "";
		Cursor cr = null;
		
		if(!db.isOpen())
			db = this.getWritableDatabase();
		
		if(!update_flg)
		{
			query = "INSERT INTO " + table + "(" + columns + ") VALUES(" + values + ");";
			db.execSQL(query);
			
			//INSERT後データ取得.
			query = "SELECT MAX("+ pkey +") as count FROM " + table + ";";
			cr = db.rawQuery(query, null);
			activity.startManagingCursor(cr);
			
			cr.moveToFirst();
			int countColumnIndex    = cr.getColumnIndex("count");
			String primaryKeyValue	 = cr.getString(countColumnIndex);
			
			query = "SELECT * FROM " + table + " WHERE " + pkey + "=" + primaryKeyValue;
			cr = db.rawQuery(query, null);
			cr.moveToFirst();
		}
		else
		{
			String update = ArrayUtil.join(",", ArrayUtil.mapToList(updateValues));
			query = "UPDATE " + table + " SET " + update + " WHERE " + where;
			db.execSQL(query);
			
			//UPDATE後データ取得.
			query = "SELECT * FROM " + table + " WHERE " + where;
			cr = db.rawQuery(query, null);
			activity.startManagingCursor(cr);
			cr.moveToFirst();
		}
		
		//結果セットが0件の場合はカラム名のみのHashMapを返す.
		if(cr.getCount() == 0)
		{
			cr.deactivate();
			cr.close();
			db.close();
			return this.makeCondition(table);
		}
		
		//結果セットをHashMapに格納.
		boolean hasNext = true;
		while(hasNext)
		{
			for(int ii=0;ii<alist.size();ii++)
			{
				String key 	= alist.get(ii).get("name");
				int columnIndex = cr.getColumnIndex(key);
				saved.put(key, cr.getString(columnIndex));
			}
			hasNext = cr.moveToNext();
		}

		cr.deactivate();
		cr.close();
		db.close();
		return saved;
	}
	
	
	/*
	 * DB更新の条件値用のHashMapを生成.
	 * @param	table	条件を使用するテーブル名
	 * @return HashMap<String, String>
	 */
	public HashMap<String, String> makeCondition(String table)
	{
		HashMap<String, String> cond = new HashMap<String, String>();
		ArrayList<HashMap<String, String>> alist = this.getSchemeArrayList(table);
		
		for(int ii=0;ii<alist.size();ii++)
		{
			String key = alist.get(ii).get("name");
			cond.put(key, "");
		}
		
		return cond;
	}
	
	/*
	 * テーブル名からprimary key一覧を取得.
	 * @param	table	テーブル名
	 * @return	ArrayList<HashMap<String, String>>
	 */
	private ArrayList<HashMap<String, String>> getPkeys(String table)
	{
		ArrayList<HashMap<String, String>> alist = this.getSchemeArrayList(table);
		ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String, String>>();
		
		for(int ii=0;ii<alist.size();ii++)
		{
			if(alist.get(ii).get("primary").equals("true"))
			{
				res.add(alist.get(ii));
			}
		}
		
		return res;
	}
	
	
	/*
	 * テーブル定義XMLファイルから column name, type, primary keyを抽出
	 * @param	fileName	テーブル定義XMLファイル名.
	 * @return	ArrayList<HashMap<String, String>>
	 */
	private ArrayList<HashMap<String, String>> getSchemeArrayList(String fileName)
	{
		ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String, String>>();

		InputStream in = null;
		AssetManager as = activity.getApplicationContext().getResources().getAssets();

		/*
		 * XMLファイルの読み込み
		 */
		try
		{
			in = as.open(fileName + ".xml");
		}
		catch(Exception e)
		{
			try
			{
				if(in != null)	in.close();
			}
			catch(Exception e2)
			{}
			
			return null;
		}
		
		
		/*
		 * XMLファイルのParse、SQL文字列作成、実行.
		 */
		try
		{
			XmlPullParser xp = null;
			xp = Xml.newPullParser();
			xp.setInput(in, "UTF-8");
			
			//XMLをパースして、テーブルのカラムを抽出
			for(int e=xp.getEventType();e!=XmlPullParser.END_DOCUMENT;e=xp.next())
			{
				HashMap<String, String> map = new HashMap<String, String>();

				if(e==XmlPullParser.START_TAG && xp.getName().equals("item"))
				{
					map.put("name", xp.getAttributeValue(null, "name"));
					map.put("type", xp.getAttributeValue(null, "type"));
					
					if(xp.getAttributeValue(null, "primary") != null)
						map.put("primary", "true");
					else
						map.put("primary", "false");
					
					res.add(map);
				}
			}
		}
		catch(Exception e)
		{
			return null;
		}
		
		return res;
	}
	
	
	/*
	 * XMLから定義を読み込み、CREATEテーブルする.
	 * @param	fileName	テーブル定義XMLファイル名
	 * @param	drop		DROPフラグ
	 */
	public void createTableWithXml(String fileName, boolean drop)
	{
		
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "";
		
		if(drop)
		{
			//DROP TABLE.
			query = "DROP TABLE IF EXISTS " + fileName + ";";
			db.execSQL(query);
		}
		
		query = "CREATE TABLE IF NOT EXISTS " + fileName + "(";
		
		ArrayList<HashMap<String,String>> alist = this.getSchemeArrayList(fileName);
		
		if(alist != null)
		{
			for(int ii=0;ii<alist.size();ii++)
			{
				query += alist.get(ii).get("name") + " ";
				query += alist.get(ii).get("type") + " ";
				if(alist.get(ii).get("primary").equals("true"))
				{
					query += " PRIMARY KEY";
				}
				
				if(ii<(alist.size()-1))
					query += ",";
			}
		}
			
		query += ");";
		
		//SQL exec.
		db.execSQL(query);
		db.close();
	}
	
	/*
	 * CSVからデータを読み込み、テーブルへINSERTする
	 * @param	fileName	データCSVファイル名
	 * @param	truncate	TRUNCATEフラグ
	 */
	public boolean insertWithCsv(String fileName, String table, boolean truncate)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "";
		if(truncate)
		{
			//DROP TABLE.
			query = "DELETE FROM " + table + ";";
			db.execSQL(query);
		}
		
		InputStream in = null;
		AssetManager as = activity.getApplicationContext().getResources().getAssets();

		/*
		 * XMLファイルの読み込み
		 */
		try
		{
			in = as.open(fileName + ".csv");
		}
		catch(Exception e)
		{
			try
			{
				if(in != null)	in.close();
			}
			catch(Exception e2)
			{}
			
			return false;
		}
		
		CSVReader csv = null;
		try {
			csv = new CSVReader(new InputStreamReader(in, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		
		String[] columns = null;
        try {
			columns = csv.readNext();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
        
        int lineNo = 1;
        String[] line;
		try {
			while ((line = csv.readNext()) != null)
			{
			    // databaseに詰める処理
				HashMap<String, String> tmpin = new HashMap<String, String>();
				for(int ii=0;ii<line.length;ii++)
				{
					tmpin.put(columns[ii], line[ii]);
					//System.out.println(columns[ii]);
					//System.out.println(line[ii]);
				}
				this.set(table, tmpin);
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		db.close();
		return true;
	}
}
