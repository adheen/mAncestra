package action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import objects.Personnage;

import common.Ancestra;
import common.SocketManager;
import common.SQLManager;
import common.Constants;
import common.World;
import objects.*;
import objects.Objet.*;

public class ActionThread implements Runnable{
	private BufferedReader _in;
	private Thread _t;
	private Socket _s;
	private Personnage _player;
	
	private int _numAction, _nbAction, _playerId, _itemId;
	private String couleur = "DF0101";	//D�finit la couleur du message envoyer au client lors de l'ajout
	
	public ActionThread(Socket sock)
	{
		try
		{
			_s = sock;
			_in = new BufferedReader(new InputStreamReader(_s.getInputStream()));
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		}
		catch(IOException e)
		{
			try {
				if(!_s.isClosed())_s.close();
			} catch (IOException e1) {}
		}
	}
	
	public  void run()
	{
		try
    	{
			String packet = "";
			
			char charCur[] = new char[1];
	        
	    	while(_in.read(charCur, 0, 1)!=-1 && Ancestra.isRunning)
	    	{
	    		if (charCur[0] != ';' && charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r')
		    	{
	    			packet += charCur[0];
		    	}else if(packet != "")
		    	{
		    		ActionServer.addToSockLog("Action: Recv << "+packet);
		    		parsePacket(packet);
		    		packet = "";
		    	}
	    	}
    	}catch(IOException e)
    	{
    		try
    		{
	    		_in.close();
	    		if(!_s.isClosed())_s.close();
	    		_t.interrupt();
	    	}catch(IOException e1){};
    	}
    	finally
    	{
    		try
    		{
	    		_in.close();
	    		if(!_s.isClosed())_s.close();
	    		_t.interrupt();
	    	}catch(IOException e1){};
    	}
	}
	
	private boolean parsePacket(String packet)
	{
		String[] result = packet.split(":");	//S�pare le packet en utilisant ":" comme d�limiteur
		String sortie = "+";
		ObjTemplate t;
		Objet obj;
		
		Ancestra.addToShopLog("Packet re�u : " + packet);
		
		if(result[0].equals("ZA"))	//ZA une action (ajout xp,kamas,lvl,...)
		{
			
			
			for (int iTokn = 1; iTokn < result.length; iTokn++) //Pour boucler dans le tableau de mot que l'on viens de cr�er en s�parant le packet (ZA:Action:Nombre:PlayerID)
			{
				switch (iTokn)
				{
					case 1:	//Si on est rendu au mot #1, le mot #0 �tant ZA
						_numAction = Integer.parseInt(result[iTokn]);
						break;
					case 2:
						_nbAction = Integer.parseInt(result[iTokn]);	//Multiplicateur de l'action (XP * _nbAction)
						break;
					case 3:
						_playerId = Integer.parseInt(result[iTokn]);	//L'ID du personnage � modifier
						_player = World.getPersonnage(_playerId);	//R�cup�re le personnage � partir de son PlayerID
						if(_player == null)
						{
							SQLManager.LOAD_PERSO(_playerId);
							_player = World.getPersonnage(_playerId);
						}
						break;
				}
			}
			
			switch (_numAction)	//D�termine quoi faire selon la valeur de _numAction
				{
					case 1:	//Monter d'un level
						if(_player.get_lvl() >= Ancestra.MAX_LEVEL) return false;
						_player.levelUp(true,true,true);
						sortie+="1 Niveau";
						SQLManager.SAVE_PERSONNAGE(_player,false);		//Enregistrement du personnage dans la base de donn�es pour �viter d'avoir des informations non coh�rente entre le jeux et le site
						Ancestra.addToShopLog("Ajout d'un lvl � : " + _player.get_name());
						
						break;
					case 2:	//Ajouter X point d'experience
						_player.addXp(_nbAction,true);
						sortie+=_nbAction+" Xp a votre personnage";
						SQLManager.SAVE_PERSONNAGE(_player,false);		//Enregistrement du personnage dans la base de donn�es pour �viter d'avoir des informations non coh�rente entre le jeux et le site
						Ancestra.addToShopLog("Ajout de " + _nbAction + "xp � " + _player.get_name());
						
						break;
					case 3:	//Ajouter X kamas
						_player.addKamas(_nbAction);
						_player.kamasLog(_nbAction+"", "Acheter sur la boutique (lvl"+_player.get_lvl()+")");
						
						sortie+=_nbAction+" Kamas � votre personnage";
						Ancestra.addToShopLog("Ajout de " + _nbAction + " kamas � " + _player.get_name());
						
						break;
					case 4:	//Ajouter X point de capital
						_player.addCapital(_nbAction);
						sortie+=_nbAction+" Point de capital � votre personnage";
						Ancestra.addToShopLog("Ajout de " + _nbAction + " capital � " + _player.get_name());
						
						break;
					case 5:	//Ajouter X point de sort
						_player.addSpellPoint(_nbAction);
						sortie+=_nbAction+" Point de sort � votre personnage";
						Ancestra.addToShopLog("Ajout de " + _nbAction + " spellPoint � " + _player.get_name());
						
						break;
					case 6: //Apprendre un sort
						_player.learnSpell(_nbAction,1,false,true);
						sortie = "Un nouveau sort viens d'�tre ajout� � votre personnage";
						Ancestra.addToShopLog("Ajout du sort " + _nbAction + " � " + _player.get_name());
						
						break;
					case 7: //Ajout de PA
						_player.get_baseStats().addOneStat(Constants.STATS_ADD_PA,_nbAction);	//Ajout du PA au stats, c'est temporaire en attendant le reload des persos qui chargeras celui de la DB
						sortie += _nbAction+" PA � votre personnage";
						Ancestra.addToShopLog("Ajout d'un PA � " + _player.get_name());
						
						break;
					case 8: //Ajout de PM
						_player.get_baseStats().addOneStat(Constants.STATS_ADD_PM,_nbAction);//Ajout du PM au stats, c'est temporaire en attendant le reload des persos qui chargeras celui de la DB
						sortie += _nbAction+" PM � votre personnage";
						Ancestra.addToShopLog("Ajout d'un PM � " + _player.get_name());
						
						break;
					case 22:	//Remettre les stats � z�ro
					_player.resetStats();
					_player.setCapital((_player.get_lvl()-1) * 5);
					sortie = "Tout vos point de capital investis vous ont �t� retourn�s";
					Ancestra.addToShopLog("Remise � z�ro des stats de " + _player.get_name());
					
					break;
				}	//Fin du swtich

		}else
		if(result[0].equals("ZO"))	//Sinon si le packet est un packet ZO objet
		{
					
			for (int iTokn = 1; iTokn < result.length; iTokn++) //Pour boucler dans le tableau de mot que l'on viens de cr�er en s�parant le packet (ZO:Max:Nombre:ItemID:PlayerID)
			{
				switch (iTokn)
				{
					case 1:	//Si on est rendu au mot #1, le mot #0 �tant ZO
						_numAction = Integer.parseInt(result[iTokn]);
						break;
					
					case 2:
						_nbAction = Integer.parseInt(result[iTokn]);
						break;
						
					case 3:
						_itemId = Integer.parseInt(result[iTokn]);
						break;
						
					case 4:
						_playerId = Integer.parseInt(result[iTokn]);
						_player = World.getPersonnage(_playerId);
						if(_player == null)
						{
							SQLManager.LOAD_PERSO(_playerId);
							_player = World.getPersonnage(_playerId);
						}
						break;
					
				}
			} //Fin du for
			
			switch (_numAction)
			{
				case 20:	//Ajouter un item avec des jets al�atoire
				
					t = World.getObjTemplate(_itemId);
					
					obj = t.createNewItem(_nbAction,false); //Si mis � "true" l'objet � des jets max. Sinon ce sont des jets al�atoire
					if(_player.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
						World.addObjet(obj,true);
					_player.objetLog(obj.getTemplate().getID(), obj.getQuantity(), "Achet� sur la boutique");
					
					ActionServer.addToSockLog("Objet "+_itemId+"ajoute a "+_player.get_name()+" avec des stats aleatoire");
					sortie = "Un objet viens d'�tre ajout� � votre personnage, allez voir votre inventaire!";
					Ancestra.addToShopLog("Ajout d'un objet stats al�atoire � " + _player.get_name());
					
					break;
				case 21:	//Ajouter un item avec des jets MAX
				
					t = World.getObjTemplate(_itemId);
					
					obj = t.createNewItem(_nbAction,true); //Si mis � "true" l'objet � des jets max. Sinon ce sont des jets al�atoire
					if(_player.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
						World.addObjet(obj,true);
					_player.objetLog(obj.getTemplate().getID(), obj.getQuantity(), "Achet� sur la boutique");
					
					ActionServer.addToSockLog("Objet "+_itemId+"ajout� � "+_player.get_name()+" avec des stats MAX");
					sortie = "Un objet avec des stats maximum viens d'�tre ajout� � votre personnage, allez voir votre inventaire!";
					Ancestra.addToShopLog("Ajout d'un objet stats max � " + _player.get_name());
					
					break;
			}//Fin du switch

		}//Fin equals."ZO"
		
		if(_player.isOnline())
		{
			SocketManager.GAME_SEND_MESSAGE(_player,sortie,couleur);	//Envoie du message		(mit ici pour qu'il soit executer peu importe le packet re�u)
			SocketManager.GAME_SEND_STATS_PACKET(_player);	//Mise � jour des stats du client
		}
		else
		{
			SQLManager.SAVE_PERSONNAGE(_player, true);
			World.unloadPerso(_playerId);
		}
		return true; 
	}//Fin parsePacket
}
