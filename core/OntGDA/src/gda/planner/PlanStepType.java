/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gda.planner;

/**
 * The same plan step may be used by different plans, therefore the action to
 * take depends on the type of step.
 *
 * @author dustin
 */
public enum PlanStepType {

    PRODUCE_UNITS, // builds them if the do not have them
    GET_ALL_UNITS,
    MOVE_UNITS,
    MOVE_UNITS_SURROUND,
    ATTACK_MOVE_UNITS,
    ATTACK_MOVE_UNITS_SURROUND,
    ATTACK,
    DEFEND, 
    ACQUIRE_UNITS, // obtains units by specifying their id's
    ATTACK_WORKERS, // search for workers in a region and attack them, if no workers in region, then just continue to attack region
    CORNER_MOVE_UNITS // moves units to the nearest corner of the target region before attacking
}
