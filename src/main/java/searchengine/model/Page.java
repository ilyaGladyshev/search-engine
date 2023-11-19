package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jsoup.Connection;
import searchengine.config.CommonConfiguration;
import searchengine.tasks.Lemmatization;

import java.io.IOException;

@Entity
@Table//(indexes = @Index(name = "pathIndex", columnList = "path"))
@Setter
@Getter
@ToString
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "site_id")
    private SiteModel site;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    public Page(){
    }

    public Page(SiteModel site, String path, CommonConfiguration common) throws IOException {
        this.site = site;
        this.path = path.substring(this.site.getUrl().length());
        Connection connection = common.getConnection(this);
        Connection.Response response = connection.execute();
        this.code = response.statusCode();
        System.out.println(this.code);
        this.content = response.body();
    }
}
