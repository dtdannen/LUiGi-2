package ontology;
/**
 * Author: William West
 * Filename: GameState.java
 * Class: CSE428 - Semantic Web
 * Assignment: Final Project
 * Description:	Class representation of a GameState object, containing
 *				three arrays, each containing objects of type ChokePoint,
 *				Player, and Region, respectively.
*/

import java.util.*;

public class GameState{
	private ArrayList<Player> players = null;
	private ArrayList<Region> regions = null;
	private ArrayList<ChokePoint> chokepoints = null;
	
	public void GameState(){
	}
	
	public void setPlayers(ArrayList<Player> p){
		players = p;
	}
	
	public void setRegions(ArrayList<Region> r){
		regions = r;
	}
	
	public void setChokepoints(ArrayList<ChokePoint> c){
		chokepoints = c;
	}
	
	public ArrayList<Player> getPlayers(){
		return players;
	}
	
	public ArrayList<Region> getRegions(){
		return regions;
	}
	
	public ArrayList<ChokePoint> getChokepoints(){
		return chokepoints;
	}
}
