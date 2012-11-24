package cx.fam.tak0294.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.LinkedHashMap;
import android.content.Context;

public class MemoryCard
{
	/*
	 * プロパティファイルの読み込み.
	 * @param	context		コンテキスト
	 * @param	fileName	読み込むファイル名.
	 * @param	return	プロパティの値が格納されたマップオブジェクト.
	 * @throws	Exception
	 */
	public static HashMap<String, String> readProperties(Context context, String fileName)
	{
		//ファイルの存在.
		if(!(new File(context.getFilesDir().getPath() + "/" + fileName)).exists())
		{
			return null;
		}
		
		InputStream in = null;
		HashMap<String, String> ret = new LinkedHashMap<String, String>();
		Properties properties 		= new Properties();
		
		try
		{
			//ファイルからInputStream取得.
			in = context.openFileInput(fileName);
			
			//プロパティファイルのロード.
			properties.load(in);
			
			//プロパティのキーと値をハッシュマップに格納.
			Set<Object> keys = properties.keySet();
			for(Iterator<Object> it = keys.iterator();it.hasNext();)
			{
				String key 		= (String)it.next();
				String value	= (String)properties.getProperty(key);
				ret.put(key, value);
			}
			
			//InputStreamクローズ.
			in.close();
			return ret;
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
	}
	
	
	/*
	 * マップオブジェクトのデータをプロパティファイルに出力.
	 * @param	context		コンテキスト.
	 * @param	map			プロパティファイルに出力するマップオブジェクト.
	 * @param	fileName	出力ファイル名.
	 * @throws	Exception
	 */
	public static void writeProperties(Context context, HashMap<String, String> map, String fileName)
	{
		OutputStream out = null;
		Properties properties = new Properties();
		
		try
		{
			//コンテキストからOutputStream取得.
			out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			
			//マップオブジェクトからキーの値を取得.
			Set<String> keys = map.keySet();
			
			//プロパティにキーと値設定.
			for(Iterator<String> it =  keys.iterator();it.hasNext();)
			{
				String key 		= (String)it.next();
				String value	= (String)map.get(key);
				properties.setProperty(key, value);
			}
			
			//プロパティの値を確定.
			properties.store(out, null);
			
			//OutputStreamをクローズ.
			out.close();
		}
		catch(Exception e)
		{
			try
			{
				if(out != null)	out.close();
			}
			catch(Exception e2){}
		}
	}
	
	
	
	/*
	 * テキストファイルの書き込み
	 * @param	context		コンテキスト
	 * @param	str			ファイルに出力する文字列
	 * @param	fileName	出力するファイル名
	 * @throws	Exception
	 */
	public static void writeFile(Context context, String str, String fileName)
	{
		writeBinaryFile(context, str.getBytes(), fileName);
	}
	
	/*
	 * バイナリデータの書き込み
	 * @param	context		コンテキスト
	 * @param	data		ファイルに出力するバイト配列.
	 * @param	fileName	出力するファイル名
	 * @throws	Exception
	 */
	public static void writeBinaryFile(Context context, byte[] data, String fileName)
	{
		OutputStream out = null;
		try
		{
			//ファイル出力ストリームを取得.
			out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			//ファイル出力ストリームにデータを出力.
			out.write(data, 0, data.length);
			//ファイル出力ストリームをクローズ.
		}
		catch(Exception e)
		{
			try
			{
				if(out != null)	out.close();
			}
			catch(Exception e2)
			{}
		}
	}
	
	/*
	 * ファイルからテキストデータの読み込み
	 * @param	context		コンテキスト
	 * @param	fileName	読み込むファイル名
	 * @return		読み込んだファイルのテキスト.
	 * @throws	Exception
	 */
	public static String readFile(Context context, String fileName)
	{
		//読み込むファイルの存在チェック.
		if(!(new File(context.getFilesDir().getPath() + "/" + fileName)).exists())
		{
			return "";
		}
		
		//ファイルをバイト配列で読み込み.
		byte[] w = readBinaryFile(context, fileName);
		return new String(w);
	}
	
	/*
	 * バイナリファイルの読み込み.
	 * @param	context		コンテキスト
	 * @param	fileName	読み込むファイル名
	 * @return	読み込んだファイルのバイト配列.
	 * @throws	Exception
	 */
	public static byte[] readBinaryFile(Context context, String fileName)
	{
		int size;
		byte[] w = new byte[128];
		InputStream in = null;
		ByteArrayOutputStream out = null;
		try
		{
			//Contextからインプットストリームの取得.
			in = context.openFileInput(fileName);
			//バイト配列のOutputStreamを取得.
			out = new ByteArrayOutputStream();
			//ファイルを読み込みByateArrayOutputStreamに貯める.
			while(true)
			{
				size = in.read(w);
				if(size <= 0)	break;
				out.write(w, 0, size);
			}
			
			//InputStraemとOutputStreamのクローズ処理.
			in.close();
			out.close();
			//ByteArrayStreamからバイト配列を取得し返却.
			return out.toByteArray();
		}
		catch(Exception e)
		{
			try
			{
				if(in!=null)	in.close();
				if(out!=null)	out.close();
			}
			catch(Exception e2)
			{}
			
			return null;
		}
	}
}




















