package objects;
import java.util.ArrayList;

import common.Constants;
import common.World;
import common.World.Couple;

public class PierreAme extends Objet{
	private ArrayList<Couple<Integer, Integer>> _monsters;
	
	public PierreAme (int Guid, int qua,int template, int pos, String strStats)
	{
		this.guid = Guid;
		this.template = World.getObjTemplate(template);	//7010 = Pierre d'ame pleine
		this.quantity = 1;
		this.position = Constants.ITEM_POS_NO_EQUIPED;
		
		_monsters = new ArrayList<Couple<Integer, Integer>>();	//Couple<MonstreID,Level>
		parseStringToStats(strStats);
	}
	
	public void parseStringToStats(String monsters) //Dans le format "monstreID,lvl|monstreID,lvl..."
	{
		String[] split = monsters.split("\\|");
		for(String s : split)
		{	
			try
			{
				int monstre = Integer.parseInt(s.split(",")[0]);
				int level = Integer.parseInt(s.split(",")[1]);
				
				_monsters.add(new Couple<Integer, Integer>(monstre, level));
				
			}catch(Exception e){continue;};
		}
	}
	
	public String parseStatsString()
	{
		String stats = "";
		boolean isFirst = true;
		for(Couple<Integer, Integer> coupl : _monsters)
		{
			if(!isFirst)
				stats+=",";
			
			try
			{
				stats += "26f#0#0#"+Integer.toHexString(coupl.first);
			}catch(Exception e)
			{
				e.printStackTrace();
				continue;
			};
			
			isFirst = false;
		}
		return stats;
	}
	
	public String parseGroupData()//Format : id,lvlMin,lvlMax;id,lvlMin,lvlMax...
	{
		String toReturn = "";
		boolean isFirst = true;
		
		for(Couple<Integer, Integer> curMob : _monsters)
		{
			if(!isFirst)
				toReturn+=";";
			
			toReturn += curMob.first+","+curMob.second+","+curMob.second;
			
			isFirst = false;
		}
		return toReturn;
	}
	
	public String parseToSave()
	{
		String toReturn = "";
		boolean isFirst = true;
		for(Couple<Integer, Integer> curMob : _monsters)
		{
			if(!isFirst)
				toReturn += "|";
			toReturn += curMob.first + "," + curMob.second;
			isFirst = false;
		}
		return toReturn;
	}
}
