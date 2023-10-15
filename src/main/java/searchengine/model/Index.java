package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "indexes")
@Cacheable(value = false)
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // INT NOT NULL AUTO_INCREMENT;
    @JoinColumn(name = "page_id", nullable = false)
    @ManyToOne
    private Page page; //INT NOT NULL — идентификатор страницы;
    @JoinColumn(name = "lemma_id", nullable = false)
    @ManyToOne
    private Lemma lemma; //INT NOT NULL — идентификатор леммы;
    @Column(name = "ranking", nullable = false)
    private float rank; //FLOAT NOT NULL — количество данной леммы для данной страницы.
}
