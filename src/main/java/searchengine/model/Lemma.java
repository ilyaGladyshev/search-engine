package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table
@Setter
@Getter
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
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
