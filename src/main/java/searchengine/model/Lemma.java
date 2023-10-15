package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Index;

@Entity
@Getter
@Setter
@Table(indexes = {
        @Index(name = "lemmaIndex", columnList = "lemma, site_id", unique = true)
})
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //INT NOT NULL AUTO_INCREMENT;
    @JoinColumn(name = "site_id", nullable = false)
    @ManyToOne
    private Site site; //INT NOT NULL — ID веб-сайта из таблицы site;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma; //VARCHAR(255) NOT NULL — нормальная форма слова (лемма);
    @Column(nullable = false)
    private int frequency; // INT NOT NULL — количество страниц, на которых слово встречается хотя бы один раз. Максимальное значение не может превышать общее количество слов на сайте.
}
