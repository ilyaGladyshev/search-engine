package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface SiteRepository extends CrudRepository<SiteModel, Integer> {

    List<SiteModel> findAllByUrl(String url);

    @Query(value = "SELECT s.last_error from site s where url = :url", nativeQuery = true)
    String getLastErrorByUrl(String url);

    @Query(value = "SELECT s.status from site s where url = :url", nativeQuery = true)
    String getStatusByUrl(String url);

    @Query(value = "SELECT s.status_time from site s where url = :url", nativeQuery = true)
    LocalDateTime getStatusTimeByUrl(String url);
}
