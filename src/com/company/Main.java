package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner fileScanner = null;

        try {
            File code = new File(args[0]);
            fileScanner = new Scanner(code);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(42);
        }

        while (fileScanner.hasNextLine()) {

        }
        
        System.exit(0);
    }
}
