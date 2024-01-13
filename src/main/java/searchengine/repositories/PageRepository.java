package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;


import java.util.List;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    List<Page> findAllByPath(String path);

    @Query(value = "SELECT Count(p.id) from site s " +
            "inner join page p " +
            "ON p.site_id = s.id " +
            "where s.url = :url", nativeQuery = true)
    int getPagesCount(String url);

}
