package com.zackrauen.hangman.gui;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.*;

public class HangmanPicture extends JPanel {
	
	public Integer step=0;
	public Integer offsetx=0;

	private static final long serialVersionUID = 1L;
	public void paintComponent(Graphics g) {
        setBackground(Color.white);
        super.paintComponent(g);
        
        Image[] steps = new Image[6];
        Image base;
        base = new ImageIcon(this.getClass().getResource("resources/pole.jpg")).getImage();
        steps[0] = new ImageIcon(this.getClass().getResource("resources/head.png")).getImage();
        steps[1] = new ImageIcon(this.getClass().getResource("resources/step2.png")).getImage();
        steps[2] = new ImageIcon(this.getClass().getResource("resources/step3.png")).getImage();
        steps[3] = new ImageIcon(this.getClass().getResource("resources/step4.png")).getImage();
        steps[4] = new ImageIcon(this.getClass().getResource("resources/step5.png")).getImage();
        steps[5] = new ImageIcon(this.getClass().getResource("resources/final.png")).getImage();

        g.drawImage(base, 10+offsetx, 0, this);
        
        if (step>=1)
        	g.drawImage(steps[step-1], 75+offsetx, 90, this);

   }  
}
