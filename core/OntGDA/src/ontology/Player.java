package ontology;

/**
 * Author: William West
 * Filename: Player.java
 * Class: CSE428 - Semantic Web
 * Assignment: Final Project
 * Description:	Class representation of a Player object, containing
 *				an array of that player's units and their ID.
*/

import java.util.*;

public class Player{
	private int playerId = -1;
	private ArrayList<Unit> myUnits = null;
	private ArrayList<Unit> enemyUnits = null;
	private int killScore = 0;
	private int unitScore = 0;
	private int spentMinerals = 0;
	private int spentGas = 0;
	private int supplyTotal = 0;
	private int razingScore = 0;
	
	
	
	public void Player(){
	}
	
	public void setPlayerId(int id){
		playerId = id;
	}
	
	public void setMyUnits(ArrayList<Unit> u){
		myUnits = u;
	}
	
	public int getPlayerId(){
		return playerId;
	}
	
	public ArrayList<Unit> getMyUnits(){
		return myUnits;
	}

	public void setKillScore(int nextInt) {
		this.killScore = nextInt;
		
	}
	
	public int getKillScore() {
		return this.killScore;
	}

	public void setUnitScore(int nextInt) {
		this.unitScore = nextInt;
	}
	
	public int getUnitScore() {
		return this.unitScore;
	}

	public void setSpentMinerals(int nextInt) {
		this.spentMinerals = nextInt;
	}
	
	public int getSpentMinerals() {
		return this.spentMinerals;
	}

	public void setSupplyTotal(int nextInt) {
		this.supplyTotal = nextInt;
	}
	
	public int getSupplyTotal() {
		return this.supplyTotal;
	}

	public int getRazingScore() {
		return this.razingScore;
	}

	public void setRazingScore(int razingScore) {
		this.razingScore = razingScore;
	}

	public int getSpentGas() {
		return spentGas;
	}

	public void setSpentGas(int spentGas) {
		this.spentGas = spentGas;
	}

	
	
}
