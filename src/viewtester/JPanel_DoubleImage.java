/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package viewtester;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author pavel.svihalek
 * ZPA Smart Energy a.s.
 * Trutnov
 * Czech republic
 */
public class JPanel_DoubleImage extends JPanel {
    private BufferedImage imageLeft;
    private BufferedImage imageRight;

    public JPanel_DoubleImage(BufferedImage imageLeft, BufferedImage imageRight) {
        this.imageLeft = imageLeft;
        this.imageRight = imageRight;
        this.setPreferredSize(new Dimension(imageLeft.getWidth()*2, imageLeft.getHeight()));
    }

    public BufferedImage getImageLeft() {
        return imageLeft;
    }

    public void setImageLeft(BufferedImage imageLeft) {
        this.imageLeft = imageLeft;
        Dimension d = new Dimension(imageLeft.getWidth()+imageRight.getWidth(), (imageLeft.getHeight()>imageRight.getHeight()?imageLeft.getHeight():imageRight.getHeight()));
        this.setSize(d);
        this.setPreferredSize(d);
    }

    public BufferedImage getImageRight() {
        return imageRight;
    }

    public void setImageRight(BufferedImage imageRight) {
        this.imageRight = imageRight;
        Dimension d = new Dimension(imageLeft.getWidth()+imageRight.getWidth(), (imageLeft.getHeight()>imageRight.getHeight()?imageLeft.getHeight():imageRight.getHeight()));
        this.setSize(d);
        this.setPreferredSize(d);
    }

    
    @Override
    public void paint(Graphics g) {
        super.paint(g); //To change body of generated methods, choose Tools | Templates.
        g.drawImage(imageLeft, 0, 0, this);
        g.drawImage(imageRight, imageLeft.getWidth(), 0, this);
    }
    
    
}
