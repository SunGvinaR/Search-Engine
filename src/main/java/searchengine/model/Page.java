package searchengine.model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Index;

@Entity
@Getter
@Setter
@Table(indexes = {
        @Index(name = "pathIndex", columnList = "site_id,path", unique = true)
})
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @JoinColumn(name = "site_id", nullable = false)
    @ManyToOne
    private Site site;  //ID веб-сайта из таблицы site;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String path; //адрес страницы от корня сайта (должен начинаться со слэша, например: /news/372189/);
    @Column(nullable = false)
    private int code; //код HTTP-ответа, полученный при запросе страницы (например, 200, 404, 500 или другие);
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content; //контент страницы (HTML-код).
}
