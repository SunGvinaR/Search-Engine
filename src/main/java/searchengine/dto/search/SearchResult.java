package searchengine.dto.search;

import lombok.Data;

import java.util.ArrayList;

@Data
public class SearchResult {

    private boolean result;
    private String error;
    private int count;
    private ArrayList<FindPage> data;

}
