
package gda;

/**
 * Goal types are compromised of 3 parts:
 * 1. Attack or Defend
 * 2. Using which kinds of troops (i.e. marines, wraiths, etc)
 * 3. Type of movement (i.e. direct, stealth, dropship, etc) 
 * 
 * Note that defending does not involve movement type
 * 
 * These are essentially all possible goals the agent could pursue
 * 
 * @author dustin
 */
public enum GoalType {
   ATTACK_GROUND_DIRECT, 
   ATTACK_AIR_DIRECT,
   ATTACK_BOTH_DIRECT, // attack directly with both ground and air units
   ATTACK_GROUND_SURROUND, // surround the enemy before attacking 
   ATTACK_AIR_SURROUND,
   ATTACK_BOTH_SURROUND,
   DEFEND_GROUND,
   DEFEND_AIR,
   DEFEND_BOTH,
   RUSH_DEFEND,
   ATTACK_WORKERS_DIRECT,
   ATTACK_AIR_SNEAK,
   ATTACK_SURROUND_DISTRACT,
   ATTACK_POSITION_AIR_SNEAK,
   ATTACK_FINISH_AIR_SNEAK
}
