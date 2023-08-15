package com.evalvis.blog.post;

import com.evalvis.blog.post.PostRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestPostRepository extends PostRepository {

    PostRepository.PostEntry findFirstById(String id);
}
