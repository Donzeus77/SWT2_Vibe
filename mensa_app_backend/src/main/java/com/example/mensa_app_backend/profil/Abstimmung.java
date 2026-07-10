package com.example.mensa_app_backend.profil;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "abstimmungen")
public class Abstimmung {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ElementCollection(fetch = FetchType.EAGER) @CollectionTable(name = "abstimmungs_stimmen") @MapKeyColumn(name = "gericht_id")
    private Map<Long, Integer> stimmen = new HashMap<>();
    @ElementCollection(fetch = FetchType.EAGER) @CollectionTable(name = "abstimmungs_nutzer")
    private Set<String> abgegebeneStimmen = new HashSet<>();
    protected Abstimmung() {}
    public boolean abstimmen(String email, Long gerichtId) {
        String schluessel = email.toLowerCase() + "#" + gerichtId;
        if (!abgegebeneStimmen.add(schluessel)) return false;
        stimmen.merge(gerichtId, 1, Integer::sum);
        return true;
    }
    public Map<Long, Integer> getStimmen() { return stimmen; }
    public Set<Long> getVotesVon(String email) {
        String prefix = email.toLowerCase() + "#";
        Set<Long> result = new HashSet<>();
        for (String eintrag : abgegebeneStimmen) if (eintrag.startsWith(prefix)) result.add(Long.valueOf(eintrag.substring(prefix.length())));
        return result;
    }
}
