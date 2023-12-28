package searchengine.model;


import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.CascadeType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.stereotype.Component;

@Entity
@Table(name = "`index`")
@Setter
@Getter
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, optional = false)
    @JoinColumn(name = "page_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, optional = false)
    @JoinColumn(name = "lemma_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemma;

    @Column(name = "`rank`")
    private long rank;

    public Index(Page page, Lemma lemma, int rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }

    public Index() {
    }
}
