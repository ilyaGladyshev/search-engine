package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {

    List<Index> findAllByLemma_id(int lemma);
}
