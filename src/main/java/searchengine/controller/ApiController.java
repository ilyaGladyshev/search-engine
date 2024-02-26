package searchengine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import searchengine.response.common.CommonResponse;
import searchengine.response.searching.SearchingResponse;
import searchengine.response.statistic.StatisticsResponse;
import searchengine.service.SearchingService;
import searchengine.service.StopIndexingService;
import searchengine.service.StatisticsService;
import searchengine.service.StartIndexingService;
import searchengine.service.AddIndexingPageService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final StartIndexingService startIndexingService;
    private final StopIndexingService stopIndexingService;
    private final AddIndexingPageService addIndexingPageService;
    private final SearchingService searchingService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<CommonResponse> startIndexing() {
        return ResponseEntity.ok(startIndexingService.indexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<CommonResponse> stopIndexing() {
        return ResponseEntity.ok(stopIndexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<CommonResponse> addIndexingPage(@RequestParam String url) {
        return ResponseEntity.ok(addIndexingPageService.add(url));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchingResponse> searchingResponse(@RequestParam String query, @RequestParam(required = false) int offset,
                                                               @RequestParam(required = false) int limit, @RequestParam(required = false) String site) {
        return ResponseEntity.ok(searchingService.searchingResponse(query, limit, offset, site));
    }

}
