/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;

/**
 * This class updates the label that records how much time has passed since the
 * reasoner last run over the gamestate
 * 
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 7, 2013
 */
public class TimerActionListener implements ActionListener {

    private JLabel label = null;

    public void setLabel(JLabel label) {
        this.label = label;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Integer newTime = Integer.parseInt(this.label.getText());
        newTime++;
        this.label.setText(newTime.toString());
    }
}
