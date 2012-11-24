package cx.fam.tak0294.array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ArrayUtil
{
	/*
	 * ArrayListを指定のスプリッタで区切った文字列として返す.
	 * @param	split	スプリッタ文字列
	 * @param	array	対象のArrayList
	 * @retrun	String
	 */
	public static String join(String split, ArrayList<String> array)
	{
		String res = "";
		
		for(int ii=0;ii<array.size();ii++)
		{
			res += array.get(ii);
			if(ii<(array.size()-1))
				res += split;
		}
		
		return res;
	}
	
	/*
	 * Set<String>を指定のスプリッタで区切った文字列として返す.
	 * @param	split	スプリッタ文字列
	 * @param	set		対象のSet<String>
	 * @return	String
	 */
	public static String join(String split, Set<String> set)
	{
		String res = "";
		
		for(Iterator<String> it=set.iterator();it.hasNext();)
		{
			res += it.next();
			
			if(it.hasNext())
				res += split;
		}
		
		return res;
	}
	
	/*
	 * HashMap<String, String>をGETクエリ形式のArrayListに変換.
	 * @param	map		対象のHashMap<String, String>
	 * @return	ArrayList<String>
	 */
	public static ArrayList<String> mapToList(HashMap<String, String> map)
	{
		ArrayList<String> res = new ArrayList<String>();
		Set<String> keys = map.keySet();
		
		for(Iterator<String> it=keys.iterator();it.hasNext();)
		{
			String key = it.next();
			String tmp = "";
			tmp = key + "=" + map.get(key);
			res.add(tmp);
		}
		
		return res;
	}
}
