package com.example.mensa_app_backend.profil;
import java.util.Scanner;

public class ProfilTest {
    private static Scanner scan = new Scanner(System.in);
    public static void main(String[] args) {
        char wdh = 'j';
        while(wdh == 'j') {
            System.out.print("E-Mail Adresse: "); 
            String email = scan.nextLine();
            System.out.print("Passwort: ");
            String passwort = scan.nextLine();

            String status = Profil.ermittleStatus(email);
            Profil p;
            if(status.equals("student") || status.equals("mitarbeiter")) {
                System.out.println("Ihre Daten wurden übernommen!");
                p = new Profil(email, passwort);
            } else if(status.equals("ungueltig")) {
                System.out.println("Ungültige FH-Kennung");
                continue;
            } else {
                System.out.println("Als Gast registrieren.");
                System.out.print("Vorname: "); 
                String vorname = scan.nextLine();
                System.out.print("Nachneme: ");
                String nachname = scan.nextLine();
                p = new Profil(email, passwort, vorname, nachname);
            }
            System.out.println(p);
            System.out.print("Nochmal? (j/n): ");
            wdh = scan.nextLine().charAt(0);
        }
        
    }
}