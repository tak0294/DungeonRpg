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
 * DB�ATABLE�̍쐬�A�X�V������S��
 */
public class DBHelper extends SQLiteOpenHelper
{
	private Activity activity= null;
	
	/*
	 * �R���X�g���N�^.
	 * @param	context		�R���e�L�X�g
	 */
	public DBHelper(Activity activity)
	{
		super(activity.getApplicationContext(), "testdb", null, 1);
		this.activity = activity;
	}
	
	/*
	 * DB�ւ̃e�[�u���쐬����.
	 * @param	db		����Ώۂ̃f�[�^�x�[�X.
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
	}
	
	/*
	 * DB�̍X�V������.
	 * @param	db		����Ώۂ̃f�[�^�x�[�X.
	 * @param	oldVersion	�f�[�^�x�[�X�̋��o�[�W����.
	 * @param	newVersion	�f�[�^�x�[�X�̐V�o�[�W����.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		
	}
	
	public void truncate(String table)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		//query�쐬.
		String query = "DELETE FROM " + table + ";";
		db.execSQL(query);
	}
	
	public ArrayList<HashMap<String, String>> execQuery(String query)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		//query�쐬.
		Cursor cr = db.rawQuery(query, null);
		cr.moveToFirst();
		
		ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String,String>>();
		
		//���ʃZ�b�g��0���̏ꍇ�̓J�������݂̂�HashMap��Ԃ�.
		if(cr.getCount() == 0)
		{
			cr.close();
			db.close();
			return res;
		}
		
		//���ʃZ�b�g��HashMap�Ɋi�[.
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
	 * DB�擾����.
	 * @param	table		�擾�Ώۃe�[�u����
	 * @param	condition	�擾������HashMap<String, String>
	 * @return	�擾���ʂ�ArrayList<HashMap<String, String>>
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
		
		//query�쐬.
		String w = !where.equals("")?" WHERE " + where:"";
		String query = "SELECT * FROM " + table + w +";";
		Cursor cr = db.rawQuery(query, null);
		cr.moveToFirst();
		
		ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String,String>>();
		
		//���ʃZ�b�g��0���̏ꍇ�̓J�������݂̂�HashMap��Ԃ�.
		if(cr.getCount() == 0)
		{
			cr.close();
			if(db.isOpen())
				db.close();
			res.add(this.makeCondition(table));
			return res;
		}
		
		//���ʃZ�b�g��HashMap�Ɋi�[.
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
	 * DB�X�V����.
	 * @param	table		�X�V�Ώۃe�[�u����
	 * @param	condition	�X�V���e��HashMap<String, String>
	 * @return	�X�V���ʂ�HashMap<String, String>
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

		//Insert, Update�̔��f.
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
			//�X�V�����p�e�L�X�g�X�V.
			else
			{
				//���݂��郌�R�[�h�̏ꍇ��UPDATE����.
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
		
		//pkey�Ȃ��̏ꍇ��insert.
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
		
		//query�쐬.
		HashMap<String, String> saved 	= new HashMap<String, String>();
		String query = "";
		Cursor cr = null;
		
		if(!db.isOpen())
			db = this.getWritableDatabase();
		
		if(!update_flg)
		{
			query = "INSERT INTO " + table + "(" + columns + ") VALUES(" + values + ");";
			db.execSQL(query);
			
			//INSERT��f�[�^�擾.
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
			
			//UPDATE��f�[�^�擾.
			query = "SELECT * FROM " + table + " WHERE " + where;
			cr = db.rawQuery(query, null);
			activity.startManagingCursor(cr);
			cr.moveToFirst();
		}
		
		//���ʃZ�b�g��0���̏ꍇ�̓J�������݂̂�HashMap��Ԃ�.
		if(cr.getCount() == 0)
		{
			cr.deactivate();
			cr.close();
			db.close();
			return this.makeCondition(table);
		}
		
		//���ʃZ�b�g��HashMap�Ɋi�[.
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
	 * DB�X�V�̏����l�p��HashMap�𐶐�.
	 * @param	table	�������g�p����e�[�u����
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
	 * �e�[�u��������primary key�ꗗ���擾.
	 * @param	table	�e�[�u����
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
	 * �e�[�u����`XML�t�@�C������ column name, type, primary key�𒊏o
	 * @param	fileName	�e�[�u����`XML�t�@�C����.
	 * @return	ArrayList<HashMap<String, String>>
	 */
	private ArrayList<HashMap<String, String>> getSchemeArrayList(String fileName)
	{
		ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String, String>>();

		InputStream in = null;
		AssetManager as = activity.getApplicationContext().getResources().getAssets();

		/*
		 * XML�t�@�C���̓ǂݍ���
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
		 * XML�t�@�C����Parse�ASQL������쐬�A���s.
		 */
		try
		{
			XmlPullParser xp = null;
			xp = Xml.newPullParser();
			xp.setInput(in, "UTF-8");
			
			//XML���p�[�X���āA�e�[�u���̃J�����𒊏o
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
	 * XML�����`��ǂݍ��݁ACREATE�e�[�u������.
	 * @param	fileName	�e�[�u����`XML�t�@�C����
	 * @param	drop		DROP�t���O
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
	 * CSV����f�[�^��ǂݍ��݁A�e�[�u����INSERT����
	 * @param	fileName	�f�[�^CSV�t�@�C����
	 * @param	truncate	TRUNCATE�t���O
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
		 * XML�t�@�C���̓ǂݍ���
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
			// TODO �����������ꂽ catch �u���b�N
			e1.printStackTrace();
		}
		
		String[] columns = null;
        try {
			columns = csv.readNext();
		} catch (IOException e) {
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}
        
        int lineNo = 1;
        String[] line;
		try {
			while ((line = csv.readNext()) != null)
			{
			    // database�ɋl�߂鏈��
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
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}
		
		db.close();
		return true;
	}
}
