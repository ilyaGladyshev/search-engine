package searchengine.dto.searching;

import lombok.Data;
import java.util.List;

@Data
public class SearchingResponce {
    private boolean result;
    private int count;
    private List<SearchingData> data;
    private String error;
}
