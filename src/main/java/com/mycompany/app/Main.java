package com.mycompany.app;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        PublicSchools publicSchools = null;
        try {
            publicSchools = new PublicSchools("Edmonton_Public_School_Board_2025_Small.csv");
        } catch (IOException e) {
            System.err.println("Error: can't open file");
            return;
        }
    }
}
