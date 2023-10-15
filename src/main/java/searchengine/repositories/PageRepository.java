package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    List<Page> findBySite(Site site);
    List<Page> findBySiteAndUrl(Site site, String url);
    Page findByPathAndSite(String path, Site site);
}
