package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Setter
@Getter
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "site_id")
    private SiteModel site;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String lemma;

    @Column(nullable = false)
    private int frequency;

    public Lemma(String lemma, SiteModel site) {
        this.lemma = lemma;
        this.site = site;
        this.frequency = 1;
    }

    public Lemma(){
    }
}
