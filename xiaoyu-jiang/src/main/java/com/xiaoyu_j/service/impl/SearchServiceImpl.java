package com.xiaoyu_j.service.impl;

import com.xiaoyu_j.mapper.SearchHotMapper;
import com.xiaoyu_j.service.SearchService;
import com.xiaoyu_j.vo.search.SearchStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    SearchHotMapper searchHotMapper;

    @Override
    public List<SearchStatsVO> searchHot() {
        return null;
    }
}
