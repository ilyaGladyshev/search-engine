package searchengine.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    @Query(value = "SELECT l.* from lemma l inner join `index` i on i.lemma_id = l.id where l.lemma = :lemma", nativeQuery = true)
    List<Lemma> findAllByLemma(String lemma);

    List<Lemma> findAllByLemmaAndSite_id(String lemma, int site_id);

    int countAllBySite_Url(String url);

}
