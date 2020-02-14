/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package inipack;

/**
 *
 * @author pavel.svihalek
 */
public class OptionChanger {
    boolean zmena;
    String hodnota;

    public OptionChanger(boolean zmena, String hodnota) {
        this.zmena = zmena;
        this.hodnota = hodnota;
    }

    public void setHodnota(String hodnota) {
        this.hodnota = hodnota;
    }

    public boolean isZmena() {
        return zmena;
    }

    public void setZmena(boolean zmena) {
        this.zmena = zmena;
    }

    public String getHodnota() {
        return hodnota;
    }
    
    
}
