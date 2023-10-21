package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    Lemma findByLemmaAndSite(String lemma, Site site);
    ArrayList<Lemma> findByLemmaInAndSiteOrderByFrequencyAsc(Set<String> lemmaSet, Site site);
    ArrayList<Lemma> findByLemmaInOrderByFrequencyAsc(Set<String> lemmaSet);
    List<Lemma> findBySite(Site site);
    ArrayList<Lemma> findByIdIn(List<Long> ids);
}
