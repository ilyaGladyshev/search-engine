package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "`index`")
@Setter
@Getter
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, optional = false)
    @JoinColumn(name = "page_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, optional = false)
    @JoinColumn(name = "lemma_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemma;

    @Column(nullable = true, name = "`rank`")
    private long rank;

    public Index(Page page, Lemma lemma, int rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }

    public Index() {
    }
}
