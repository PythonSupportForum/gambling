package Slots.Backend.untitled.src.main.java;

import java.io.File;
import java.util.Arrays;
import java.util.Random;



class Main{

    public static void main(String[] args){
        System.out.println(Arrays.toString(SlotGen.getSlotArray(new File("../../../../assets/slot-machine-icons"))));



    }



}






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