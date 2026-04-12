package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.Comment;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.model.PostLike;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    public List<Post> findAllOrderByCreatedAtDesc() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC", Post.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Post findById(long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Using HQL to fetch the user to avoid LazyInitializationException if accessed outside session
            return session.createQuery("FROM Post p JOIN FETCH p.user WHERE p.id = :id", Post.class)
                    .setParameter("id", id)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public long save(String content, String imageUrl, long userId) {
        Transaction transaction = null;
        System.out.println("[PostDAO] save() called: userId=" + userId + ", content=" + content + ", imageUrl=" + imageUrl);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            System.out.println("[PostDAO] User lookup: " + (user != null ? user.getUsername() : "NULL"));
            if (user != null) {
                Post post = new Post();
                post.setContent(content);
                post.setImageUrl(imageUrl);
                post.setUser(user);
                post.setCreatedAt(LocalDateTime.now());
                session.save(post);
                transaction.commit();
                System.out.println("[PostDAO] Post saved successfully with id=" + post.getId());
                return post.getId();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("[PostDAO] Error saving post: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        throw new RuntimeException("User not found: " + userId);
    }

    public void delete(long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Post post = session.get(Post.class, id);
            if (post != null) {
                session.delete(post);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public List<Comment> findCommentsByPostId(long postId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId ORDER BY c.createdAt ASC", Comment.class)
                    .setParameter("postId", postId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void addComment(long postId, String content, long userId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Post post = session.get(Post.class, postId);
            User user = session.get(User.class, userId);
            if (post != null && user != null) {
                Comment comment = new Comment();
                comment.setContent(content);
                comment.setPost(post);
                comment.setUser(user);
                comment.setCreatedAt(LocalDateTime.now().toString());
                session.save(comment);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public int getLikeCount(long postId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId", Long.class)
                    .setParameter("postId", postId)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isLikedByUser(long postId, long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId AND pl.user.id = :userId", Long.class)
                    .setParameter("postId", postId)
                    .setParameter("userId", userId)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean toggleLike(long postId, long userId) {
        Transaction transaction = null;
        boolean result = false;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            PostLike postLike = session.createQuery("FROM PostLike pl WHERE pl.post.id = :postId AND pl.user.id = :userId", PostLike.class)
                    .setParameter("postId", postId)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (postLike != null) {
                // Unlike
                session.delete(postLike);
                result = false;
            } else {
                // Like
                Post post = session.get(Post.class, postId);
                User user = session.get(User.class, userId);
                if (post != null && user != null) {
                    PostLike newLike = new PostLike(post, user);
                    session.save(newLike);
                    result = true;
                }
            }
            transaction.commit();
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("[PostDAO] toggleLike constraint violation: already liked");
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException || 
                (e.getCause() != null && e.getCause().getCause() instanceof java.sql.SQLIntegrityConstraintViolationException)) {
                System.err.println("[PostDAO] toggleLike constraint violation: already liked");
                return true;
            }
            e.printStackTrace();
        }
        return result;
    }
}
