/*
package common;

import game.GameServer;
import game.GameThread.GameAction;

import java.util.ArrayList;
import java.util.Map.Entry;
import objects.*;
import objects.Sort.*;
import objects.Carte.Case;
import objects.Fight.*;

public class IA {

	public static void applyIAToFight(Fighter fighter, Fight fight)
	{
		if(fighter.getMob() == null)
			return;
		System.out.println("IA type: "+fighter.getMob().getTemplate().getIAType());
		switch(fighter.getMob().getTemplate().getIAType())
		{
			case 0://Poutch
				return;
			case 1://Agressif
				apply_type1(fighter,fight);
			break;
		}
	}

	private static void apply_type1(Fighter F, Fight fight)
	{
		Boolean stop = new Boolean(false);
		while(!stop && F.canPlay())
		{
			int PDVPER = (F.getPDV()*100)/F.getPDVMAX();
			Fighter T = getNearestEnnemy(fight, F);
			if(T == null)
				return;
			if(PDVPER > /0)
			{
				int attack = attackIfPossible(fight,F,T);
				System.out.println("Mode attaque: attack="+attack);
				if(attack != 0)//Attaquer
				{
					if(attack == 5)return;
					System.out.println("n'a pas pu attaquer");
					if(!moveNearIfPossible(fight,F,T))//Approcher
					{
						System.out.println("n'a pas pu s'approcher");
						T = getNearestFriend(fight,F);//Ami le plus proche
						if(!buffIfPossible(fight,F,T))
						{
							if(!buffIfPossible(fight,F,F))//Auto Buff
								stop = true;//on passe le tour
						}
					}
				}
			}
			else
			{
				if(!moveFarIfPossible())//Fuir
				{
					int attack = attackIfPossible(fight,F,T);
					if(attack != 0)//Attaquer
					{
						if(attack == 5)return;//Fin du tour
						T = getNearestFriend(fight,F);//Ami le plus proche
						if(!buffIfPossible(fight,F,T))//Buff alli�
						{
							if(!buffIfPossible(fight,F,F))//Auto Buff
								stop = true;//on passe le tour
						}
					}
				}
			}
		}
	}

	private static boolean moveFarIfPossible() {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean buffIfPossible(Fight fight, Fighter F,
			Fighter target) {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean moveNearIfPossible(Fight fight, Fighter F,	Fighter T)
	{
		GameServer.addToLog("Tentative d'approche par "+F.getPacketsName()+" de "+T.getPacketsName());
		int cellID = Pathfinding.getNearestCellAround(fight.get_map(),T.get_fightCell().getID(),F.get_fightCell().getID(),null);
		int distMax = (getBestSpellForTarget(fight,F,T,false)==null?0:getBestSpellForTarget(fight,F,T,false).getMaxPO());
		//On demande le chemin plus court
		ArrayList<Case> path = Pathfinding.getShortestPathBetween(fight.get_map(),F.get_fightCell().getID(),cellID,distMax);
		if(path == null)return false;
		
		ArrayList<Case> finalPath = new ArrayList<Case>();
		for(int a = 0; a<F.getPM();a++)
		{
			if(path.size() == a)break;
			finalPath.add(path.get(a));
		}
		String pathstr = "";
		try{
		int curCaseID = F.get_fightCell().getID();
		int curDir = 0;
		for(Case c : finalPath)
		{
			char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
			if(d == 0)return false;//Ne devrait pas arriver :O
			if(curDir != d)
			{
				if(finalPath.indexOf(c) != 0)
					pathstr += CryptManager.cellID_To_Code(curCaseID);
				pathstr += d;
			}
			curCaseID = c.getID();
		}
		if(curCaseID != F.get_fightCell().getID())
			pathstr += CryptManager.cellID_To_Code(curCaseID);
		}catch(Exception e){e.printStackTrace();};
		//Cr�ation d'une GameAction
		GameAction GA = new GameAction(0,1, "");
		GA._args = pathstr;
		boolean result = fight.fighterDeplace(F, GA);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		return result;
	}

	private static Fighter getNearestFriend(Fight fight, Fighter fighter)
	{
		int dist = 1000;
		Fighter curF = null;
		for(Fighter f : fight.getFighters(3))
		{
			if(f.isDead())continue;
			if(f.getTeam() == fighter.getTeam())//Si c'est un ami
			{
				int d = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
				if( d < dist)
				{
					dist = d;
					curF = f;
				}
			}
		}
		return curF;
	}

	private static Fighter getNearestEnnemy(Fight fight, Fighter fighter)
	{
		int dist = 1000;
		Fighter curF = null;
		for(Fighter f : fight.getFighters(3))
		{
			if(f.isDead())continue;
			if(f.getTeam() != fighter.getTeam())//Si c'est un ennemis
			{
				int d = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
				if( d < dist)
				{
					dist = d;
					curF = f;
				}
			}
		}
		return curF;
	}
	
	private static int attackIfPossible(Fight fight, Fighter fighter,Fighter target)//return True si attaqu�
	{
		SortStats SS = getBestSpellForTarget(fight,fighter,target,true);
		if(SS == null)
			return 1;
		else
		{
			return fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
		}
	}

	private static SortStats getBestSpellForTarget(Fight fight, Fighter F,Fighter T,boolean checkCast)
	{
		int infl = 0;
		SortStats ss = null;
		for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
		{
			if(infl < calculInfluence(SS.getValue(),F,T) && (!checkCast || fight.CanCastSpell(F, SS.getValue(), T.get_fightCell())))//Si le sort est plus interessant
			{
				ss = SS.getValue();
			}
		}
		return ss;
	}

	private static int calculInfluence(SortStats ss,Fighter C,Fighter T)
	{
		//FIXME TODO
		int infTot = 0;
		for(SpellEffect SE : ss.getEffects())
		{
			int inf = 0;
			switch(SE.getEffectID())
			{
				case 91://Vol de Vie Eau
					inf = 150 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 92://Vol de Vie Terre
					inf = 150 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 93://Vol de Vie Air
					inf = 150 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 94://Vol de Vie feu
					inf = 150 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 95://Vol de Vie neutre
					inf = 150 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 96://Dommage Eau
					inf = 100 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 97://Dommage Terre
					inf = 100 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 98://Dommage Air
					inf = 100 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 99://Dommage feu
					inf = 100 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 100://Dommage neutre
					inf = 100 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 101://Retrait PA
					inf = 75 * Formulas.getMiddleJet(SE.getJet());
				break;
				case 108://Soin
					inf = -(100 * Formulas.getMiddleJet(SE.getJet()));
				break;
				case 141://Tuer
					inf += 2000;
				break;
			}
			if(C.getTeam() == T.getTeam())//Si Amis
				infTot -= inf;
			else//Si ennemis
				infTot += inf;
		}
		return infTot;
	}

}
//*/

package common;

import game.GameServer;
import game.GameThread.GameAction;

import java.util.ArrayList;
import java.util.Map.Entry;
import objects.*;
import objects.Sort.*;
import objects.Carte.Case;
import objects.Fight.*;

public class IA {

	public static class IAThread implements Runnable
	{
		private Fight _fight;
		private Fighter _fighter;
		private static boolean stop = false;
		private Thread _t;
		
		public IAThread(Fighter fighter, Fight fight)
		{
			_fighter = fighter;
			_fight = fight;
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		}
		public void run()
		{
			stop = false;
			if(_fighter.getMob() == null)
				return;
			switch(_fighter.getMob().getTemplate().getIAType())
			{
				case 0://Poutch
					return;
				case 1://Agressif
					apply_type1(_fighter,_fight);
				break;
				case 2://Soutien
					apply_type2(_fighter,_fight);
				break;
			}
			_fight.endTurn();
		}

		private static void apply_type1(Fighter F, Fight fight)
		{
			while(!stop && F.canPlay())
			{
				int PDVPER = (F.getPDV()*100)/F.getPDVMAX();
				Fighter T = getNearestEnnemy(fight, F);
				if(T == null)
					return;
				if(PDVPER > 10)
				{
					System.out.println("Mode attaque");
					if(!attackIfPossible(fight,F,T))//Attaque
					{
						System.out.println("n'a pas pu attaquer");
						if(!moveNearIfPossible(fight,F,T))//Avancer
						{
							if(!HealIfPossible(fight,F, false))//false pour soin alli�
							{
								T = getNearestFriend(fight,F);
								if(!buffIfPossible(fight,F,T))//buff alli�
								{
									if(!HealIfPossible(fight,F,true))//true pour auto-soin
									{
										if(!buffIfPossible(fight,F,F))//auto-buff
											stop = true;
									}
								}
							}
											
						}				
					}
				}
				else
				{
					if(!HealIfPossible(fight,F,true))//auto-soin
					{
						if(!attackIfPossible(fight,F,T))//attaque
						{
							if(!buffIfPossible(fight,F,F))//auto-buff
							{
								if(!HealIfPossible(fight,F,false))//soin alli�
								{
									T = getNearestFriend(fight,F);
									if(!buffIfPossible(fight,F,T))//buff alli�
									{
										if(!moveFarIfPossible(fight, F))//fuite
											stop = true;
										
									}
								}
							}
						}
					}				
				}
			}
		}

		private static void apply_type2(Fighter F, Fight fight)
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestFriend(fight,F);
				if(!HealIfPossible(fight,F,false))//soin alli�
				{
					if(!buffIfPossible(fight,F,T))//buff alli�
					{
						if(!moveNearIfPossible(fight,F,T))//Avancer vers alli�
						{
							if(!HealIfPossible(fight,F,true))//auto-soin
							{
								if(!buffIfPossible(fight,F,F))//auto-buff
								{
									T = getNearestEnnemy(fight, F);
									if(!attackIfPossible(fight,F,T))//attaque
									{
										if(!moveFarIfPossible(fight, F))//fuite
											stop = true;
									}
								}
							}
						}
					}
				}			
			}
		}
		
		private static boolean moveFarIfPossible(Fight fight, Fighter F) 
		{
			int dist[] = {1000,1000,1000,1000,1000,1000,1000,1000,1000,1000}, cell[] = {0,0,0,0,0,0,0,0,0,0};
			for(int i = 0; i < 10 ; i++)
			{
				for(Fighter f : fight.getFighters(3))
				{
					
					if(f.isDead())continue;
					if(f == F || f.getTeam() == F.getTeam())continue;
					int cellf = f.get_fightCell().getID();
					if(cellf == cell[0] || cellf == cell[1] || cellf == cell[2] || cellf == cell[3] || cellf == cell[4] || cellf == cell[5] || cellf == cell[6] || cellf == cell[7] || cellf == cell[8] || cellf == cell[9])continue;					
					int d = 0;
					d = Pathfinding.getDistanceBetween(fight.get_map(), F.get_fightCell().getID(), f.get_fightCell().getID());
					if(d == 0)continue;
					if(d < dist[i])
					{
						dist[i] = d;
						cell[i] = cellf;
					}
					if(dist[i] == 1000)
					{
						dist[i] = 0;
						cell[i] = F.get_fightCell().getID();
					}
				}
			}
			if(dist[0] == 0)return false;
			int dist2[] = {0,0,0,0,0,0,0,0,0,0};
			int PM = F.getMob().getPM(), caseDepart = F.get_fightCell().getID(), destCase = F.get_fightCell().getID();
			for(int i = 0; i <= PM;i++)
			{
				if(destCase > 0)
					caseDepart = destCase;
				int curCase = caseDepart;
				curCase += 15;
				int infl = 0, inflF = 0;
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
				
				curCase = caseDepart + 14;
				infl = 0;
				
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
				
				curCase = caseDepart -15;
				infl = 0;
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
				
				curCase = caseDepart - 14;
				infl = 0;
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
			}
			
			if(destCase < 0 || destCase > 478 || destCase == F.get_fightCell().getID())return false;
			int cellID = Pathfinding.getNearestCellAround(fight.get_map(),F.get_fightCell().getID(),destCase,null);
			ArrayList<Case> path = Pathfinding.getShortestPathBetween(fight.get_map(),F.get_fightCell().getID(),cellID, 0);
			if(path == null)return false;
			
			/* DEBUG PATHFINDING
			System.out.println("DEBUG PATHFINDING:");
			System.out.println("startCell: "+F.get_fightCell().getID());
			System.out.println("destinationCell: "+cellID);
			
			for(Case c : path)
			{
				System.out.println("Passage par cellID: "+c.getID()+" walk: "+c.isWalkable());
			}
			//*/
			ArrayList<Case> finalPath = new ArrayList<Case>();
			for(int a = 0; a<F.getPM();a++)
			{
				if(path.size() == a)break;
				finalPath.add(path.get(a));
			}
			String pathstr = "";
			try{
			int curCaseID = F.get_fightCell().getID();
			int curDir = 0;
			for(Case c : finalPath)
			{
				char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
				if(d == 0)return false;//Ne devrait pas arriver :O
				if(curDir != d)
				{
					if(finalPath.indexOf(c) != 0)
						pathstr += CryptManager.cellID_To_Code(curCaseID);
					pathstr += d;
				}
				curCaseID = c.getID();
			}
			if(curCaseID != F.get_fightCell().getID())
				pathstr += CryptManager.cellID_To_Code(curCaseID);
			}catch(Exception e){e.printStackTrace();};
			//Cr�ation d'une GameAction
			GameAction GA = new GameAction(0,1, "");
			GA._args = pathstr;
			boolean result = fight.fighterDeplace(F, GA);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			return result;

		}

		private static boolean testCotes(int cell1, int cell2)
		{
			if ( cell1 == 15 || cell1 == 44 || cell1 == 73 || cell1 == 102 || cell1 == 131 || cell1 == 160 || cell1 == 189 || cell1 == 218 || cell1 == 247 || cell1 == 276 || cell1 == 305 || cell1 == 334 || cell1 == 363 || cell1 == 392 || cell1 == 421 || cell1 == 450 )
			{
				if( cell2 == cell1 + 14 || cell2 == cell1 - 15 )
					return false;			
			}
			if ( cell1 == 28 || cell1 == 57 || cell1 == 86 || cell1 == 115 || cell1 == 144 || cell1 == 173 || cell1 == 202 || cell1 == 231 || cell1 == 260 || cell1 == 289 || cell1 == 318 || cell1 == 347 || cell1 == 376 || cell1 == 405 || cell1 == 434 || cell1 == 463 )
			{
				if( cell2 == cell1 + 15 || cell2 == cell1 - 14 )
					return false;
			}
			return true;
		}
		
		private static boolean HealIfPossible(Fight fight, Fighter f, boolean autoSoin)//boolean pour choisir entre auto-soin ou soin alli�
		{
			if(autoSoin && (f.getPDV()*100)/f.getPDVMAX() > 95 )return false;
			Fighter target = null;
			SortStats SS = null;
			if(autoSoin)
			{
				target = f;			
				SS = getHealSpell(fight,f,target);
			}
			else//s�lection joueur ayant le moins de pv
			{
				Fighter curF = null;
				int PDVPERmin = 100;
				SortStats curSS = null;
				for(Fighter F : fight.getFighters(3))
				{					
					if(f.isDead())continue;
					if(F == f)continue;
					if(F.getTeam() == f.getTeam())
					{
						int PDVPER = (F.getPDV()*100)/F.getPDVMAX();
						if( PDVPER < PDVPERmin && PDVPER < 95)
						{
							int infl = 0;
							for(Entry<Integer, SortStats> ss : f.getMob().getSpells().entrySet())
							{
								if(infl < calculInfluenceHeal(ss.getValue()) && calculInfluenceHeal(ss.getValue()) != 0 && fight.CanCastSpell(f, ss.getValue(), F.get_fightCell()))//Si le sort est plus interessant
								{
									infl = calculInfluenceHeal(ss.getValue());
									curSS = ss.getValue();
								}
							}
							if(curSS != SS && curSS != null)
							{
								curF = F;
								SS = curSS;
								PDVPERmin = PDVPER;
							}
						}
					}
				}
				target = curF;			
			}
			if(target == null)return false;
			if(SS == null)return false;
			int heal = fight.tryCastSpell(f, SS, target.get_fightCell().getID());
			if(heal != 0)
				return false;
			
			return true;
		}
		
		private static boolean buffIfPossible(Fight fight, Fighter fighter,Fighter target) 
		{				
			if(target == null)return false;
			SortStats SS = getBuffSpell(fight,fighter,target);
			if(SS == null)return false;
			int buff = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
			if(buff != 0)return false;			
			
			return true;	
		}

		private static SortStats getBuffSpell(Fight fight, Fighter F, Fighter T)
		{
			int infl = 0;	
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
			{
				if(infl < calculInfluence(SS.getValue(),F,T) && calculInfluence(SS.getValue(),F,T) > 0 && fight.CanCastSpell(F, SS.getValue(), T.get_fightCell()))//Si le sort est plus interessant
				{
					infl = calculInfluence(SS.getValue(),F,T);
					ss = SS.getValue();
				}
			}
			return ss;				
		}
		
		private static SortStats getHealSpell(Fight fight, Fighter F, Fighter T)
		{
			int infl = 0;	
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
			{
				if(infl < calculInfluenceHeal(SS.getValue()) && calculInfluenceHeal(SS.getValue()) != 0 && fight.CanCastSpell(F, SS.getValue(), T.get_fightCell()))//Si le sort est plus interessant
				{
					infl = calculInfluenceHeal(SS.getValue());
					ss = SS.getValue();
				}
			}
			return ss;
		}
		
		private static boolean moveNearIfPossible(Fight fight, Fighter F, Fighter T)
		{
			GameServer.addToLog("Tentative d'approche par "+F.getPacketsName()+" de "+T.getPacketsName());
			int cellID = Pathfinding.getNearestCellAround(fight.get_map(),T.get_fightCell().getID(),F.get_fightCell().getID(),null);
			//On demande le chemin plus court
			ArrayList<Case> path = Pathfinding.getShortestPathBetween(fight.get_map(),F.get_fightCell().getID(),cellID,0);
			if(path == null || path.size() == 0)return false;
			
			/* DEBUG PATHFINDING
			System.out.println("DEBUG PATHFINDING:");
			System.out.println("startCell: "+F.get_fightCell().getID());
			System.out.println("destinationCell: "+cellID);
			
			for(Case c : path)
			{
				System.out.println("Passage par cellID: "+c.getID()+" walk: "+c.isWalkable());
			}
			//*/
			ArrayList<Case> finalPath = new ArrayList<Case>();
			for(int a = 0; a<F.getPM();a++)
			{
				if(path.size() == a)break;
				finalPath.add(path.get(a));
			}
			String pathstr = "";
			try{
			int curCaseID = F.get_fightCell().getID();
			int curDir = 0;
			for(Case c : finalPath)
			{
				char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
				if(d == 0)return false;//Ne devrait pas arriver :O
				if(curDir != d)
				{
					if(finalPath.indexOf(c) != 0)
						pathstr += CryptManager.cellID_To_Code(curCaseID);
					pathstr += d;
				}
				curCaseID = c.getID();
			}
			if(curCaseID != F.get_fightCell().getID())
				pathstr += CryptManager.cellID_To_Code(curCaseID);
			}catch(Exception e){e.printStackTrace();};
			//Cr�ation d'une GameAction
			GameAction GA = new GameAction(0,1, "");
			GA._args = pathstr;
			boolean result = fight.fighterDeplace(F, GA);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			return result;
		}

		private static Fighter getNearestFriend(Fight fight, Fighter fighter)
		{
			int dist = 1000;
			Fighter curF = null;
			for(Fighter f : fight.getFighters(3))
			{
				if(f.isDead())continue;
				if(f == fighter)continue;
				if(f.getTeam() == fighter.getTeam())//Si c'est un ami
				{
					int d = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
					if( d < dist)
					{
						dist = d;
						curF = f;
					}
				}
			}
			return curF;
		}
		
		private static Fighter getNearestEnnemy(Fight fight, Fighter fighter)
		{
			int dist = 1000;
			Fighter curF = null;
			for(Fighter f : fight.getFighters(3))
			{
				if(f.isDead())continue;
				if(f.getTeam() != fighter.getTeam())//Si c'est un ennemis
				{
					int d = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
					if( d < dist)
					{
						dist = d;
						curF = f;
					}
				}
			}
			return curF;
		}
		
		private static boolean attackIfPossible(Fight fight, Fighter fighter,Fighter target)//return True si attaqu�
		{					
			SortStats SS = getBestSpellForTarget(fight,fighter,target);
			if(SS == null)
				return false;
			int attack = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
			if(attack != 0)
				return false;			
					
			return true;
			
		}

		private static boolean DistantAttackIfPossible(Fight fight, Fighter fighter,Fighter target)//return True si attaqu�
		{
			System.out.println("**********************************************");
			if(target == null)return false;
			SortStats ss = null;
			int infl = 0, PM = fighter.getPM(), PMtoUse = 0, dist = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), target.get_fightCell().getID());
			String pathstrFinal = "";
			for(Entry<Integer, SortStats> SS : fighter.getMob().getSpells().entrySet())
			{
				System.out.println("test1");
				int POmax = SS.getValue().getMaxPO();
				System.out.println("testPOmax "+ POmax);
				//if(POmax > dist - PM && SS.getValue().getMinPO() < dist)continue;
				PMtoUse = dist - POmax;
				System.out.println("testPM "+ PMtoUse);
				if(PMtoUse > PM)continue;
				if(PMtoUse > 0)
				{
					System.out.println("test2");
					System.out.println(PMtoUse);
					int cellID = Pathfinding.getNearestCellAround(fight.get_map(),fighter.get_fightCell().getID(),target.get_fightCell().getID(),null);
					ArrayList<Case> path = Pathfinding.getShortestPathBetween(fight.get_map(),fighter.get_fightCell().getID(),cellID,0);
					if(path == null)return false;
					
					ArrayList<Case> finalPath = new ArrayList<Case>();
					for(int a = 0; a < PMtoUse ;a++)
					{
						if(path.size() == a)break;
						finalPath.add(path.get(a));
					}
					String pathstr = "";
					Case CaseFinal = null;
					try{
					int curDir = 0;
					for(Case c : finalPath)
					{
						int curCaseID = fighter.get_fightCell().getID();
						char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
						if(d == 0)return false;//Ne devrait pas arriver :O
						if(curDir != d)
						{
							if(finalPath.indexOf(c) != 0)
								pathstr += CryptManager.cellID_To_Code(curCaseID);
							pathstr += d;
							CaseFinal = c;
						}
						curCaseID = c.getID();
					}
					}catch(Exception e){e.printStackTrace();};	
					if(CaseFinal == null)continue;
					System.out.println("test3");
					if(!fight.CanCastSpell(fighter, SS.getValue(), CaseFinal))continue;	
					System.out.println("test4");
					System.out.println(CaseFinal.getID());
					if(infl < calculInfluence(SS.getValue(), fighter, target))
					{
						System.out.println("test5");
						pathstrFinal = pathstr;
						System.out.println(pathstrFinal);
						infl = calculInfluence(SS.getValue(), fighter, target);
						ss = SS.getValue();
					}
				}
				else
				{
					if(!fight.CanCastSpell(fighter, SS.getValue(), target.get_fightCell()))continue;
					if(infl < calculInfluence(SS.getValue(), fighter, target))
					{
						infl = calculInfluence(SS.getValue(), fighter, target);
						ss = SS.getValue();
					}
				}			
			}
			if(ss == null)return false;
			System.out.println("test5bis "+PMtoUse);
			if(PMtoUse > 0 )
			{
				System.out.println("test6");
				if(pathstrFinal == "")return false;
				GameAction GA = new GameAction(0,1, "");
				GA._args = pathstrFinal;
				boolean result = fight.fighterDeplace(fighter, GA);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}

				System.out.println("test7");
				int attack = fight.tryCastSpell(fighter, ss, target.get_fightCell().getID());
				if(attack != 0)
					return false;
				
			}
			else
			{
				int attack = fight.tryCastSpell(fighter, ss, target.get_fightCell().getID());
				if(attack != 0)
					return false;
			}
			
			return false;
		}
		
		private static SortStats getBestSpellForTarget(Fight fight, Fighter F,Fighter T)
		{
			int inflMax = 0;
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
			{
				int curInfl = 0, Infl1 = 0, Infl2 = 0;
				int PA = F.getMob().getPA();
				int usedPA[] = {0,0};
				if(!fight.CanCastSpell(F, SS.getValue(), T.get_fightCell()))continue;
				curInfl = calculInfluence(SS.getValue(),F,T);
				if(curInfl == 0)continue;
				if(curInfl > inflMax)
				{
					ss = SS.getValue();
					usedPA[0] = ss.getPACost();
					Infl1 = curInfl;
					inflMax = Infl1;
				}
				
				for(Entry<Integer, SortStats> SS2 : F.getMob().getSpells().entrySet())
				{
					if( (PA - usedPA[0]) < SS2.getValue().getPACost())continue;
					if(!fight.CanCastSpell(F, SS2.getValue(), T.get_fightCell()))continue;
					curInfl = calculInfluence(SS2.getValue(),F,T);
					if(curInfl == 0)continue;
					if((Infl1 + curInfl) > inflMax)
					{
						ss = SS.getValue();
						usedPA[1] = SS2.getValue().getPACost();
						Infl2 = curInfl;
						inflMax = Infl1 + Infl2;
					}
					for(Entry<Integer, SortStats> SS3 : F.getMob().getSpells().entrySet())
					{
						if( (PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())continue;
						if(!fight.CanCastSpell(F, SS3.getValue(), T.get_fightCell()))continue;
						curInfl = calculInfluence(SS3.getValue(),F,T);
						if(curInfl == 0)continue;
						if((curInfl+Infl1+Infl2) > inflMax)
						{
							ss = SS.getValue();
							inflMax = curInfl + Infl1 + Infl2;
						}
					}				
				}			
			}
			return ss;
		}

		private static int calculInfluenceHeal(SortStats ss)
		{
			int inf = 0;
			for(SpellEffect SE : ss.getEffects())
			{
				if(SE.getEffectID() != 108)return 0;			
				inf += 100 * Formulas.getMiddleJet(SE.getJet());
			}
			
			return inf;
		}
		
		private static int calculInfluence(SortStats ss,Fighter C,Fighter T)
		{
			//FIXME TODO
			int infTot = 0;
			for(SpellEffect SE : ss.getEffects())
			{
				int inf = 0;
				switch(SE.getEffectID())
				{
					case 91://Vol de Vie Eau
						inf = 150 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 92://Vol de Vie Terre
						inf = 150 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 93://Vol de Vie Air
						inf = 150 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 94://Vol de Vie feu
						inf = 150 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 95://Vol de Vie neutre
						inf = 150 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 96://Dommage Eau
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 97://Dommage Terre
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 98://Dommage Air
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 99://Dommage feu
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 100://Dommage neutre
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 101://retrait PA
						inf = 1000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 127://retrait PM
						inf = 1000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 84://vol PA
						inf = 1500 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 77://vol PM
						inf = 1500 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 111://+ PA
						inf = -1000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 128://+ PM
						inf = -1000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 121://+ Dom
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 138://+ %Dom
						inf = -50 * Formulas.getMiddleJet(SE.getJet());
					break;
					
				}
				if(C.getTeam() == T.getTeam())//Si Amis
					infTot -= inf;
				else//Si ennemis
					infTot += inf;
			}
			return infTot;
		}
	}
}
