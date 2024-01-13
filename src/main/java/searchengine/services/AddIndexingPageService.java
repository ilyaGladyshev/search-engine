package searchengine.services;

import searchengine.responses.common.CommonResponse;

public interface AddIndexingPageService {
    CommonResponse add(String url);
}
