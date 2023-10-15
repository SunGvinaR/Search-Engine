package searchengine.services.indexing;
import java.util.Vector;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.ConnectionSQL;

@RequiredArgsConstructor
public class Indexer extends RecursiveAction implements Runnable {

    private final String url;
    private final Site site;
    private final ConnectionSQL connectionSQL;
    private volatile static Status status = Status.Ready;
    private final Vector<String> foundUrl;
    private static final AtomicInteger countSitesAreIndexing = new AtomicInteger();

    public static void setStatus(Status currentStatus){
        status = currentStatus;
    }
    public static Status getStatus(){
        return status;
    }

    @Override
    public void run() {
        countSitesAreIndexing.incrementAndGet();
        invoke();
        switch (status) {
            case Stops -> {
                connectionSQL.write(site, "Индексация остановлена пользователем");
                System.out.println("Я ошибся");
            }
            case OnePageIndexing -> {
                System.out.println("Я готов");
            }
            default -> {
                connectionSQL.write(site, Site.Status.INDEXED);
                System.out.println("Я готов");
            }
        }
        if (countSitesAreIndexing.decrementAndGet() == 0) {
            status = Status.Ready;
            System.out.println("Я всё");
        }
    }
    @Override
    protected void compute(){
        for (Element element : indexingPage()) {
            if(status != Status.FullIndexing){
                return;
            }
            String link = element.attr("href");
            synchronized (foundUrl) {
                if (!link.matches("/.+[^#[.jpg]]") || foundUrl.contains(link)) {
                    continue;
                } else {
                    foundUrl.add(link);
                }
            }
            if (connectionSQL.isPageNotFound(link, site)) {
                Indexer nextDepth = new Indexer(link, site, connectionSQL, foundUrl);
                nextDepth.fork();
                nextDepth.join();
            } else {
                foundUrl.remove(link);
            }
        }
    }
    public Elements indexingPage(){
        try {
            Thread.sleep((int) (Math.random() * 50 + 100));

            Connection.Response connect = Jsoup.connect(site.getUrl() + url).timeout(60000).ignoreHttpErrors(true)
                    .ignoreContentType(true).referrer(url).execute();
            int statusCode = connect.statusCode();
            String title = connect.parse().select("title").text();
            String content;
            try {
                content = connect.parse().text().replaceFirst(title, title + "\s");
            } catch (IllegalArgumentException e){
                content = "1";
            }
            try {
                Page page = connectionSQL.writePage(url, statusCode, content, site);
                if(statusCode == 200){
                    new CreateIndex(page, connectionSQL).builder();
                }
            } catch (Exception e){
                connectionSQL.writePage(url, statusCode, e.getMessage(), site);
                foundUrl.remove(url);
                e.printStackTrace();
                return new Elements();
            }
            foundUrl.remove(url);

            return connect.parse().select("a");
        } catch (Exception e){
            System.err.println("Я ошибся и вернул null");
            e.printStackTrace();
            return null;
        }
    }
    enum Status {
        FullIndexing, OnePageIndexing, Stops, Ready
    }
}
