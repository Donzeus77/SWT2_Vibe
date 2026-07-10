package com.example.mensa_app_backend.bezahlsystem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BestellungRepository extends JpaRepository<Bestellung, Long> { List<Bestellung> findByProfilEmailOrderByBestellnrDesc(String email); }
