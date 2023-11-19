package searchengine.services;

import searchengine.dto.common.CommonResponce;

public interface AddIndexingPageService {
    CommonResponce add(String url);
}
