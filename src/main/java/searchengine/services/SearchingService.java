package searchengine.services;

import searchengine.dto.searching.SearchingResponce;

public interface SearchingService {
    SearchingResponce searching(String url, int required, int offset, String site);
}
