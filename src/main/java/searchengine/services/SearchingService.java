package searchengine.services;

import searchengine.dto.searching.SearchingResponse;

public interface SearchingService {
    SearchingResponse searchingResponse(String url, int required, int offset, String site);
}
