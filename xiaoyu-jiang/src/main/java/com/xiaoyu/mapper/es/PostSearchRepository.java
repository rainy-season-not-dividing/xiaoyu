package com.xiaoyu.mapper.es;

import com.xiaoyu.vo.search.PostSearchVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostSearchVO, Long> {

    /* 完全包含：term 查询 + bool should */
    Page<PostSearchVO> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}
