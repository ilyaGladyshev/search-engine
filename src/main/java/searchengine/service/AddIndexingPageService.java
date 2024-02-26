package searchengine.service;

import searchengine.response.common.CommonResponse;

public interface AddIndexingPageService {
    CommonResponse add(String url);
}
