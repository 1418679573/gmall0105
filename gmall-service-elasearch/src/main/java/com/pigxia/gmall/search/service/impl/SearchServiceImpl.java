package com.pigxia.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pigxia.gmall.bean.PmsSearchParam;
import com.pigxia.gmall.bean.PmsSearchSkuInfo;
import com.pigxia.gmall.bean.PmsSkuAttrValue;
import com.pigxia.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by absen on 2020/6/3 15:35
 */
@Service
public class SearchServiceImpl implements SearchService {


    @Autowired
    JestClient jestClient;
    public List<PmsSearchSkuInfo> list(PmsSearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(searchParam);
        String dsl=searchSourceBuilder.toString();
        System.out.println(dsl);
        Search build = new Search.Builder(dsl).addIndex("gmall").addType("PmsSkuInfo").build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList=new LinkedList<>();
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source=hit.source;
             Map<String, List<String>> highlight = hit.highlight;
             if(highlight!=null){
             String skuNameNew=highlight.get("skuName").get(0);
             source.setSkuName(skuNameNew);
             }
            pmsSearchSkuInfoList.add(source);
        }
        return pmsSearchSkuInfoList;
    }

    private SearchSourceBuilder getSearchSourceBuilder(PmsSearchParam searchParam) {
        String[] skuAttrValueList = searchParam.getValueId();
        String keyword=searchParam.getKeyword();
        String catalog3Id=searchParam.getCatalog3Id();
        // 进行相应的结果查询测试
        // jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // filter
        // term
        if(skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", pmsSkuAttrValue);
                //terms
                //TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("", "", "", "");
                boolQueryBuilder.filter(termQueryBuilder);
                // boolQueryBuilder.filter(termsQueryBuilder);
            }
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (StringUtils.isNotBlank(keyword)){
            //must
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        // form
        searchSourceBuilder.from(0);
        // size
        searchSourceBuilder.size(20);
        // highlight 对数据源中的字段进行高亮显示
         HighlightBuilder highlightBuilder = new HighlightBuilder();
         highlightBuilder.preTags("<span style='color:red;'>");
         highlightBuilder.field("skuName");
         highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);
        // query
        searchSourceBuilder.query(boolQueryBuilder);
        // sort 进行排序
        searchSourceBuilder.sort("id", SortOrder.DESC);
        return searchSourceBuilder;
    }
}
