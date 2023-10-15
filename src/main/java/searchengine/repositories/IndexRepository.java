package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<Index, Long> {

    List<Index> findByPage(Page page);
    List<Index> findByLemma(Lemma lemma);
    boolean existsByLemmaAndPage(Lemma lemma, Page page);
    ArrayList<Index> findByLemmaInAndPage(ArrayList<Lemma> lemmas, Page page);
}
