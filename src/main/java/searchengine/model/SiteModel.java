package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import searchengine.config.CommonConfiguration;
import searchengine.config.Site;
import java.time.LocalDateTime;

@Entity
@Table(name="site")
@Setter
@Getter
@ToString
public class SiteModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "Enum('INDEXING', 'INDEXED', 'FAILED')")
    private SiteStatus status;

    @Column(nullable = false, name = "status_time",columnDefinition = "DATETIME")
    private LocalDateTime statusTime;

    @Column(nullable = true,name = "last_error")
    private String lastError;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(nullable = false,columnDefinition = "VARCHAR(255)")
    private String name;

    public void renew(){
        this.status = SiteStatus.INDEXING;
        this.statusTime = LocalDateTime.now();
    }

    public void renewError(String errorText){
        this.statusTime = LocalDateTime.now();
        this.status = SiteStatus.FAILED;
        this.lastError = errorText;
    }

    public void renewCancel(){
        this.statusTime = LocalDateTime.now();
        this.status = SiteStatus.FAILED;
        this.lastError = "Индексация отменена пользователем";
    }
    public SiteModel(){
    }

    public SiteModel(String url){
        this.name = CommonConfiguration.formatUrl(url);
        this.url = CommonConfiguration.formatUrl(url);
        this.status = SiteStatus.INDEXED;
        this.statusTime = LocalDateTime.now();
    }
    public SiteModel(Site site) {
        this.name = site.getName();
        this.url = site.getUrl();
        this.status = SiteStatus.INDEXED;
        this.statusTime = LocalDateTime.now();
    }
}
