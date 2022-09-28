package com.simplesns.sns.service;

import com.simplesns.sns.exception.ErrorCode;
import com.simplesns.sns.exception.SnsApplicationException;
import com.simplesns.sns.model.Comment;
import com.simplesns.sns.model.Post;
import com.simplesns.sns.model.entity.CommentEntity;
import com.simplesns.sns.model.entity.LikeEntity;
import com.simplesns.sns.model.entity.PostEntity;
import com.simplesns.sns.model.entity.UserEntity;
import com.simplesns.sns.repository.CommentEntityRepository;
import com.simplesns.sns.repository.LikeEntityRepository;
import com.simplesns.sns.repository.PostEntityRepository;
import com.simplesns.sns.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final LikeEntityRepository likeEntityRepository;
    private final CommentEntityRepository commentEntityRepository;

    @Transactional
    public void create(String title, String body, String username){
        // user find
        UserEntity userEntity = getUserEntityOrException(username);
        // post save
        postEntityRepository.save(PostEntity.of(title,body,userEntity));
    }

    @Transactional
    public Post modify(String title, String body, String username, Integer postId){
        UserEntity userEntity = getUserEntityOrException(username);
        PostEntity postEntity = getPostEntityOrException(postId);

        // post permission
        if(postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", username, postId));
        }

        postEntity.setTitle(title);
        postEntity.setBody(body);

        return Post.fromEntity(postEntityRepository.saveAndFlush(postEntity));
    }

    @Transactional
    public void delete(String username, Integer postId){
        UserEntity userEntity = getUserEntityOrException(username);
        PostEntity postEntity = getPostEntityOrException(postId);

        // post permission
        if(postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", username, postId));
        }

        postEntityRepository.delete(postEntity);
    }

    public Page<Post> list(Pageable pageable){
        return postEntityRepository.findAll(pageable).map(Post::fromEntity);
    }

    public Page<Post> my(String username, Pageable pageable){
        UserEntity userEntity = getUserEntityOrException(username);

        return postEntityRepository.findAllByUser(userEntity, pageable).map(Post::fromEntity);
    }

    @Transactional
    public void like(Integer postId, String username){
        PostEntity postEntity = getPostEntityOrException(postId);
        UserEntity userEntity = getUserEntityOrException(username);

        // check liked -> Throw
        likeEntityRepository.findByUserAndPost(userEntity, postEntity).ifPresent(it -> {
            throw new SnsApplicationException(ErrorCode.ALREADY_LIKED, String.format("username %s already like post %d", username, postId));
        });
        // like save
        likeEntityRepository.save(LikeEntity.of(userEntity,postEntity));
    }

    public int likecount(Integer postId){
        PostEntity postEntity = getPostEntityOrException(postId);
        // count like
        return likeEntityRepository.countByPost(postEntity);
    }

    @Transactional
    public void comment(Integer postId, String username, String comment){
        // check post exist
        PostEntity postEntity = getPostEntityOrException(postId);
        UserEntity userEntity = getUserEntityOrException(username);

        // comment save
        commentEntityRepository.save(CommentEntity.of(userEntity,postEntity,comment));
    }

    public Page<Comment> getComments(Integer postId, Pageable pageable){
        PostEntity postEntity = getPostEntityOrException(postId);
        return commentEntityRepository.findAllByPost(postEntity, pageable).map(Comment::fromEntity);
    }




    // post exist
    private PostEntity getPostEntityOrException(Integer postId){
        return postEntityRepository.findById(postId).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("%s not founded", postId)));
    }

    // user exist
    private UserEntity getUserEntityOrException(String username){
        return userEntityRepository.findByUsername(username).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not founded", username)));
    }
}
