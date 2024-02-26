package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;

import java.util.List;


@Repository
public interface SiteRepository extends CrudRepository<SiteModel, Integer> {

    List<SiteModel> findAllByUrl(String url);

}
