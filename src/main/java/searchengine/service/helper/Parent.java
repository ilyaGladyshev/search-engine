package searchengine.service.helper;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class Parent {
    private final String parentString;
    private List<SearchHelper> searchHelpers = new ArrayList<>();
    private List<PageHelper> pageContainer = new ArrayList<>();
}
