package searchengine.service;

import searchengine.response.searching.SearchingResponse;

public interface SearchingService {
    SearchingResponse searchingResponse(String url, int limit, int offset, String site);
}
