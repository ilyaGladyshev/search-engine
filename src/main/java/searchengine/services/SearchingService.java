package searchengine.services;

import searchengine.responses.searching.SearchingResponse;

public interface SearchingService {
    SearchingResponse searchingResponse(String url, int required, int offset, String site);
}
