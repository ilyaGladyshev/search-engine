package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import searchengine.model.Index;

import java.util.List;

public interface IndexRepository extends CrudRepository<Index, Integer> {

    @Query(value = "SELECT * from `index` i where i.lemma_id = :lemma", nativeQuery = true)
    List<Index> findIndex(int lemma);

}
