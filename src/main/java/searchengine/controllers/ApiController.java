package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import searchengine.dto.common.CommonResponce;
import searchengine.dto.searching.SearchingResponce;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final StartIndexingService startIndexingService;
    private final StopIndexingService stopIndexingService;
    private final AddIndexingPageService addIndexingPageService;
    private final SearchingService searchingService;
    public ApiController(StatisticsService statisticsService, StartIndexingService startIndexingService,
                         StopIndexingService stopIndexingService, AddIndexingPageService addIndexingPageService,
                         SearchingService searchingService) {
        this.statisticsService = statisticsService;
        this.startIndexingService = startIndexingService;
        this.stopIndexingService = stopIndexingService;
        this.addIndexingPageService = addIndexingPageService;
        this.searchingService = searchingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<CommonResponce> startIndexing(){
        return ResponseEntity.ok(startIndexingService.indexing());
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<CommonResponce> stopIndexing(){
        return ResponseEntity.ok(stopIndexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<CommonResponce> addIndexingPage(@RequestParam String url){
        return ResponseEntity.ok(addIndexingPageService.add(url));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchingResponce> searching(@RequestParam String query, @RequestParam(required = false) int offset,
                                                       @RequestParam(required = false) int limit, @RequestParam(required = false) String site) {
        return ResponseEntity.ok(searchingService.searching(query, limit, offset, site));
    }

}
