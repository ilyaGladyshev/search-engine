package searchengine.service;

import searchengine.response.searching.SearchingResponse;

public interface SearchingService {
    SearchingResponse searchingResponse(String url, int required, int offset, String site);
}
