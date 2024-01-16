package searchengine.services.helpers;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PageContainer {

    private final List<PageHelper> listPages = new ArrayList<>();
}
