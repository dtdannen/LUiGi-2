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
	private String mapName = "";
	private int mapWidth = -1;
	private int mapHeight = -1;
	
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
	
	public void setMapName(String s) {
		this.mapName = s;
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
	
	public String getMapName() {
		return this.mapName;
	}

	public int getMapWidth() {
		return mapWidth;
	}

	public void setMapWidth(int mapWidth) {
		this.mapWidth = mapWidth;
	}

	public int getMapHeight() {
		return mapHeight;
	}

	public void setMapHeight(int mapHeight) {
		this.mapHeight = mapHeight;
	}
}
