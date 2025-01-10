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








}