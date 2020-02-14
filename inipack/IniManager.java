/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package inipack;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author pavel.svihalek
 */
public class IniManager {
    private static String iniFile;
    private static Properties myProps = new  Properties();
    /**
     * mapa, kde jsou vsechny klice - ne jenom ty ulozene, ale i ty, ktere chceme
     * nahrat a nejsou tam a tak se pouzije defaultni hodnota. Mame tak 
     * k dispozici cele nastaveni
     */
    private static Map<String,OptionChanger> optionMap = new HashMap<>();

    /**
     * Vrati mapu s aktualnim nastavenim
     * @return 
     */
    public static Map<String, OptionChanger> getOptionMap() {
        return optionMap;
    }

    /**
     * Nahraje do pomocne mapy dosud nepouzite property
     */
    public static void loadUnussed() {
        for(String key:myProps.stringPropertyNames()) {
            optionMap.put(key, new OptionChanger(false, myProps.getProperty(key))); // nahrajeme do mapy i nepouzite odkazy
        }
        
    }
    
    /**
     * Vyora polozku nactenou z ini souboru. Pokud ji nenajde, tak vrati
     * defaultValue <Br>
     * Predpoklada se, ze jiz predtim bylo volano loadIni(..)
     * @param key
     * @param defaultValue
     * @return 
     */
    public static String getIniProperty(String key, String defaultValue) {
        String retP = myProps.getProperty(key);
        if(retP == null)
            retP = defaultValue;
        optionMap.put(key, new OptionChanger(false, retP)); // az otevreme options, tak at tam dame jednoduse veskere nastaveni
        return retP;
    }
    
    /**
     * Prida nebo upravi polozku v inisouboru
     * @param key
     * @param value 
     */
    public static void setIniProperty(String key, String value) {
        myProps.setProperty(key, value);
        optionMap.put(key, new OptionChanger(false, value)); // az otevreme options, tak at tam dame jednoduse veskere nastaveni
    }
    
    /**
     * Nahraje do ini souboru nastaveni ulozene v pameti
     */
    public static void saveIni() {
          System.out.println("Saving to ini file: " +iniFile);

//          Properties myProps = new  Properties();
//          myProps.setProperty("BackgroundColorRED", String.valueOf(barvaPozadi.getRed()));
//          myProps.setProperty("BackgroundColorGREEN", String.valueOf(barvaPozadi.getGreen()));
//          myProps.setProperty("BackgroundColorBLUE", String.valueOf(barvaPozadi.getBlue()));
          //myProps.setProperty("Second", "2nd");

          FileOutputStream out;
          try  {
               out = new  FileOutputStream(iniFile, false); // vytvorime celysoubor znovu
               myProps.store(out, "User last setup ini file");
               out.close();
          }
          catch  (IOException ioe) {
               System.err.println("Problem processing ini file " + iniFile+ " : "+ ioe.getMessage());
          }

    }
    
    public static void loadIni(String ini) {
        iniFile = ini; // ulozime si ho
         System.out.println("Loading file: " +iniFile);
          FileInputStream fis;
          try  {
               fis = new  FileInputStream(iniFile);
//               Properties prop = new  Properties();
               myProps.load(fis);
               fis.close();

               myProps.list(System.out);
//               System.out.println(prop.getProperty("BackgroundColorBLUE"));
//               if((myProps.getProperty("BackgroundColorBLUE") != null) &&
//                       (myProps.getProperty("BackgroundColorGREEN") != null) &&
//                       (myProps.getProperty("BackgroundColorRED") != null)) {
//                   barvaPozadi = new Color(Integer.parseInt((myProps.getProperty("BackgroundColorRED"))),
//                           Integer.parseInt((myProps.getProperty("BackgroundColorGREEN"))),
//                           Integer.parseInt((myProps.getProperty("BackgroundColorBLUE"))));
//                   changeBgColour();    // a zmenime barvu
//               }
          }
          catch  (FileNotFoundException fnfe) {
               System.err.println("The ini file " + iniFile + " was not found!");
          }
          catch  (IOException ioe) {
               System.err.println("Problem loading ini file " + iniFile);
          } catch (Exception ee) {
               System.err.println("Problem loading ini file " + ee.getMessage());
              
          }
    }

    /**
     * vrati nazev naseho ini souboru
     * @return 
     */
    public static String getIniFile() {
        return iniFile;
    }

    /**
     * Nastavi nazev ini souboru
     * @param iniFile 
     */
    public static void setIniFile(String iniFile) {
        IniManager.iniFile = iniFile;
    }
    
    
}
