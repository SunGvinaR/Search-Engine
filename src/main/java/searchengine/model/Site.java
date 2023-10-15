package searchengine.model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class Site implements Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "status_time", nullable = false)
    private Date statusTime;
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @Override
    public Site clone() {
        try {
            Site clone = (Site) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

//    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
//    private List<Page> pages;

    public enum Status {
        INDEXING, INDEXED, FAILED
    }

}
