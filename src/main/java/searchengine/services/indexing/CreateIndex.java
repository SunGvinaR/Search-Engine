package searchengine.services.indexing;

import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.ConnectionSQL;

import java.io.IOException;
import java.util.*;

public class CreateIndex {
    private final Page page;
    private final ConnectionSQL connectionSQL;
    private final Site site;
    public CreateIndex(Page page, ConnectionSQL connectionSQL) {
        this.page = page;
        this.connectionSQL = connectionSQL;
        this.site = page.getSite();
    }

    public void builder() {
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            Map<String, Integer> lemmas = lemmaFinder.collectLemmas(page.getContent());
            ArrayList<Index> indexes = new ArrayList<>();
            ArrayList<Lemma> lemmasList = new ArrayList<>();
            for(String lemmaName : lemmas.keySet()){
                if(Indexer.getStatus().equals(Indexer.Status.Stops)){
                    return;
                }
                int count = lemmas.get(lemmaName);

                isReChecking = false;
                Lemma lemma = createLemma(lemmaName);
                lemmasList.add(lemma);

                Index index = new Index();
                index.setLemma(lemma);
                index.setPage(page);
                index.setRank(count);
                indexes.add(index);
            }
            writeLemma(lemmasList);
            connectionSQL.writeAll(indexes);
        } catch (InterruptedException | IOException e) {
            System.err.println("Поток был прерван");
            e.printStackTrace();
        }
    }

    private boolean isReChecking;
    private Lemma createLemma(String lemmaName) throws InterruptedException {
        if (Indexer.getStatus().equals(Indexer.Status.Stops)) {
            throw new InterruptedException();
        }

        try {
            return connectionSQL.getLemmaRepository().findByLemmaAndSite(lemmaName, site);
        } catch (Exception e) {
            try {
                Lemma lemma = new Lemma();
                lemma.setLemma(lemmaName);
                lemma.setSite(site);
                lemma.setFrequency(0);
                connectionSQL.getLemmaRepository().save(lemma);
                return lemma;
            } catch (Exception ex){
                if(isReChecking){
                    throw ex;
                }
                isReChecking = true;
                return createLemma(lemmaName);
            }
        }
    }
    private synchronized void writeLemma(ArrayList<Lemma> lemmas) {
        lemmas.forEach(l -> l.setFrequency(l.getFrequency() + 1));
        connectionSQL.getLemmaRepository().saveAll(lemmas);
    }
}
