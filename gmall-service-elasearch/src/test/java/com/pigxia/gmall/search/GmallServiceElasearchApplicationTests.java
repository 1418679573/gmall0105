package com.pigxia.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pigxia.gmall.bean.PmsSearchSkuInfo;
import com.pigxia.gmall.bean.PmsSkuInfo;
import com.pigxia.gmall.service.SkuService;
import io.searchbox.client.JestClient;


import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.lucene.queryparser.xml.builders.FilteredQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallServiceElasearchApplicationTests {

	@Autowired
	JestClient jestClient;

	@Reference
	SkuService skuService;
	@Test
	public void contextLoads() throws InvocationTargetException, IllegalAccessException, IOException {
    put();

	}

	private void select() throws IOException {
		// 进行相应的结果查询测试
		// jest的dsl工具
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		// bool
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		// filter
		// term
		TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", "43");
		//terms
		//TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("", "", "", "");
		boolQueryBuilder.filter(termQueryBuilder);
		// boolQueryBuilder.filter(termsQueryBuilder);
		//must
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","华为");
		boolQueryBuilder.must(matchQueryBuilder);
		// form
		searchSourceBuilder.from(0);
		// size
		searchSourceBuilder.size(20);
		// highlight
		searchSourceBuilder.highlight(null);
		// query
		searchSourceBuilder.query(boolQueryBuilder);
		String dsl=searchSourceBuilder.toString();
		System.err.println(dsl);
		Search build = new Search.Builder(dsl).addIndex("gmall").addType("PmsSkuInfo").build();
		SearchResult execute = jestClient.execute(build);
		List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
		List<PmsSearchSkuInfo> pmsSearchSkuInfoList=new LinkedList<>();
		for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source=hit.source;
           pmsSearchSkuInfoList.add(source);
       }
		System.out.println(pmsSearchSkuInfoList.size());
	}

	private void put() throws IllegalAccessException, InvocationTargetException, IOException {
		// 查询sql

		List<PmsSkuInfo> pmsSkuInfoList=skuService.getAll("61");
		// 将mysql中的数据结果封装到es的数据结构中
		ArrayList<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

		for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
			 PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
			 pmsSearchSkuInfo.setId(Long.parseLong(pmsSkuInfo.getId()));
			 BeanUtils.copyProperties(pmsSearchSkuInfo,pmsSkuInfo);
			 pmsSearchSkuInfos.add(pmsSearchSkuInfo);
		}

		// 导入到es
		for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
			//    Builder:数据源
			//  index :es中索引名，即数据库
			//  type:表
			//  id  id
			Index build =
					new Index.Builder(pmsSearchSkuInfo).index("gmall").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()+"").build();
		jestClient.execute(build);
		}
	}
}
