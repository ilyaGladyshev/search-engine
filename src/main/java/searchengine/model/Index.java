package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="`index`")
@Setter
@Getter
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "page_id")
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "lemma_id")
    private Lemma lemma;

    @Column(nullable = true,name = "`rank`")
    private long rank;

    public Index(Page page, Lemma lemma, int rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }

    public Index(){
    }
}
