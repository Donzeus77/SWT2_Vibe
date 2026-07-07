package com.example.studentenwerk_simulator.config;

import com.example.studentenwerk_simulator.gericht.Allergen;
import com.example.studentenwerk_simulator.gericht.Beilage;
import com.example.studentenwerk_simulator.gericht.BeilageRepository;
import com.example.studentenwerk_simulator.gericht.GerichtTag;
import com.example.studentenwerk_simulator.gericht.Hauptspeise;
import com.example.studentenwerk_simulator.gericht.HauptspeiseRepository;
import com.example.studentenwerk_simulator.gericht.factory.BeilageFactory;
import com.example.studentenwerk_simulator.gericht.factory.HauptgerichtFactory;
import com.example.studentenwerk_simulator.mensa.Mensa;
import com.example.studentenwerk_simulator.mensa.MensaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;
import java.util.Set;

@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seedDatabase(
            HauptspeiseRepository hauptspeiseRepository,
            BeilageRepository beilageRepository,
            MensaRepository mensaRepository,
            HauptgerichtFactory hauptgerichtFactory,
            BeilageFactory beilageFactory) {
        return args -> {
            if (hauptspeiseRepository.count() > 0 || beilageRepository.count() > 0) {
                return;
            }

            seedHauptspeisen(hauptspeiseRepository, hauptgerichtFactory);
            seedBeilagen(beilageRepository, beilageFactory);
            seedMensen(mensaRepository);
        };
    }

    private void seedHauptspeisen(HauptspeiseRepository repo, HauptgerichtFactory factory) {
        repo.save((Hauptspeise) factory.createGericht(
                "Currywurst mit Pommes",
                "Gebratene Bratwurst in Currysauce",
                3.50, 5.50,
                Set.of(),
                Set.of()));

        repo.save((Hauptspeise) factory.createGericht(
                "Veganes Chili sin Carne",
                "Mit Kidneybohnen, Mais und Reis",
                3.20, 5.00,
                Set.of(),
                EnumSet.of(GerichtTag.VEGAN, GerichtTag.VEGETARISCH)));

        repo.save((Hauptspeise) factory.createGericht(
                "Pasta Bolognese",
                "Mit Rinderhack und Tomatensauce",
                3.80, 6.00,
                EnumSet.of(Allergen.GLUTEN, Allergen.EI, Allergen.MILCH),
                Set.of()));

        repo.save((Hauptspeise) factory.createGericht(
                "Vegane Pasta Pesto",
                "Mit Basilikum-Pesto und Cherrytomaten",
                3.50, 5.50,
                EnumSet.of(Allergen.GLUTEN),
                EnumSet.of(GerichtTag.VEGAN)));

        repo.save((Hauptspeise) factory.createGericht(
                "Hähnchenbrust mit Reis",
                "Gebratene Hähnchenbrust mit Currysauce und Reis",
                4.20, 6.50,
                Set.of(),
                Set.of()));

        repo.save((Hauptspeise) factory.createGericht(
                "Schnitzel Wiener Art",
                "Paniertes Schweineschnitzel mit Zitrone",
                4.50, 7.00,
                EnumSet.of(Allergen.GLUTEN, Allergen.EI),
                Set.of()));

        repo.save((Hauptspeise) factory.createGericht(
                "Vegane Linsensuppe",
                "Deftige Linsensuppe mit Gemuese und Kartoffeln",
                2.50, 4.00,
                Set.of(),
                EnumSet.of(GerichtTag.VEGAN, GerichtTag.VEGETARISCH)));

        repo.save((Hauptspeise) factory.createGericht(
                "Falafel-Teller",
                "Mit Hummus, Salat und Fladenbrot",
                4.00, 6.20,
                EnumSet.of(Allergen.GLUTEN, Allergen.SESAM),
                EnumSet.of(GerichtTag.VEGAN, GerichtTag.VEGETARISCH, GerichtTag.HALAL)));
    }

    private void seedBeilagen(BeilageRepository repo, BeilageFactory factory) {
        repo.save((Beilage) factory.createGericht(
                "Pommes frites",
                "Knusprige Pommes",
                1.50, 2.50,
                Set.of(),
                EnumSet.of(GerichtTag.VEGAN, GerichtTag.VEGETARISCH)));

        repo.save((Beilage) factory.createGericht(
                "Kartoffelsalat",
                "Mit Essig-Oel-Dressing",
                1.80, 2.80,
                EnumSet.of(Allergen.EI, Allergen.MILCH),
                EnumSet.of(GerichtTag.VEGETARISCH)));

        repo.save((Beilage) factory.createGericht(
                "Gemischter Salat",
                "Frische Blattsalate mit Dressing",
                2.00, 3.20,
                EnumSet.of(Allergen.MILCH),
                EnumSet.of(GerichtTag.VEGAN, GerichtTag.VEGETARISCH)));

        repo.save((Beilage) factory.createGericht(
                "Reis",
                "Basmatireis",
                1.20, 2.00,
                Set.of(),
                EnumSet.of(GerichtTag.VEGAN, GerichtTag.VEGETARISCH)));
    }

    private void seedMensen(MensaRepository repo) {
        repo.save(new Mensa("Hauptmensa", "TU Dortmund", "Von-Barlistraße 2, 44227 Dortmund", "Mo–Fr 11:15–14:15", "medium"));
        repo.save(new Mensa("Archeteria", "TU Dortmund", "Baroper Str. 281, 44227 Dortmund", "Mo–Fr 11:15–14:00", "low"));
        repo.save(new Mensa("Mensa Baroper Stern", "TU Dortmund", "Baroper Str. 293, 44227 Dortmund", "Mo–Fr 11:15–14:00", "low"));
        repo.save(new Mensa("Mensa Cantstraße", "TU Dortmund", "Cantstraße 3, 44227 Dortmund", "Mo–Fr 11:00–14:00", "medium"));
        repo.save(new Mensa("FH Dortmund Mensa", "FH Dortmund", "Emil-Figge-Str. 42, 44227 Dortmund", "Mo–Fr 11:15–14:00", "high"));
        repo.save(new Mensa("Mensa Emil-Figge-Straße", "FH Dortmund", "Emil-Figge-Str. 43, 44227 Dortmund", "Mo–Fr 11:00–14:00", "medium"));
        repo.save(new Mensa("Mensa Sonnenstraße", "FH Dortmund", "Sonnenstraße 96-100, 44135 Dortmund", "Mo–Fr 11:00–14:00", "low"));
        repo.save(new Mensa("Mensa Westfalenhütte", "TU Dortmund", "Rhenus-Allee 9, 44145 Dortmund", "Mo–Fr 11:00–14:00", "low"));
    }
}
