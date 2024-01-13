package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    List<Lemma> findAllByLemma(String lemma);

    @Query(value = "SELECT * from lemma l where l.lemma = :lemma and l.site_id = :site", nativeQuery = true)
    List<Lemma> findLemmaBySite(String lemma, String site);

    @Query(value = "SELECT Count(l.id) from site s " +
            "inner join lemma l " +
            "ON l.site_id = s.id " +
            "where s.url = :url", nativeQuery = true)
    int getLemmaCount(String url);

}
