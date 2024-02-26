package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;


import java.util.List;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    List<Page> findAllByPath(String path);

    int countAllBySite_Url(String url);

}
