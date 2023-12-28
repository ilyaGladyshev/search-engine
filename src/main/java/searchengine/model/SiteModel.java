package searchengine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import searchengine.config.Site;

import java.time.LocalDateTime;

@Entity
@Table(name = "site")
@Setter
@Getter
@Component
@ToString
public class SiteModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "Enum('INDEXING', 'INDEXED', 'FAILED')")
    private SiteStatus status;

    @Column(nullable = false, name = "status_time", columnDefinition = "DATETIME")
    private LocalDateTime statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    public SiteModel() {
    }

    public SiteModel(String url) {
        this.name = url;
        this.url = url;
        this.status = SiteStatus.INDEXING;
        this.statusTime = LocalDateTime.now();
    }

    public SiteModel(Site site) {
        this.name = site.getName();
        this.url = site.getUrl();
        this.status = SiteStatus.INDEXING;
        this.statusTime = LocalDateTime.now();
    }

    public void renew() {
        this.status = SiteStatus.INDEXED;
        this.statusTime = LocalDateTime.now();
    }

    public void renewError(String errorText) {
        this.statusTime = LocalDateTime.now();
        this.status = SiteStatus.FAILED;
        this.lastError = errorText;
    }

    public void renewCancel() {
        this.statusTime = LocalDateTime.now();
        this.status = SiteStatus.FAILED;
        this.lastError = "Индексация отменена пользователем";
    }

}
