package searchengine.services;

import searchengine.dto.common.CommonResponse;

public interface AddIndexingPageService {
    CommonResponse add(String url);
}
