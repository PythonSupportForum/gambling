package Slots.Backend.untitled.src.main.java;

import java.io.File;
import java.util.Random;






public class SlotGen{






    /**
     *Gibt ein Array mit den Pfaden zu drei zufälligen Bildern zurück.
     * @param folder Der Pfad zu dem Ordner mit den Bildern.
     * @return Das String-Array.
     */
    public static String[] getSlotArray(File folder) {

        String[] output = new String[3];
        Random rand = new Random();
        File[] files = folder.listFiles();

        for(int i = 0; i<3; i++){
            assert files != null;
            output[i] = files[rand.nextInt(files.length)].getPath();
        }

        return output;
    }

    /**
     *Gibt ein Array mit den Pfaden zu drei zufälligen Bildern zurück.
     * @return Das String-Array.
     */
    public static String[] getSlotArray() {

        File folder = new File("slots_icons");
        String[] output = new String[3];
        Random rand = new Random();
        File[] files = folder.listFiles();

        for(int i = 0; i<3; i++){
            assert files != null;
            output[i] = files[rand.nextInt(files.length)].getPath();
        }

        return output;
    }



    public static boolean sendResult(){

        String[] iconPaths = getSlotArray();
        

        if((iconPaths[0]==iconPaths[1]) && (iconPaths[0]==iconPaths[2])){
            return true;
        }
        return false;

    }







}