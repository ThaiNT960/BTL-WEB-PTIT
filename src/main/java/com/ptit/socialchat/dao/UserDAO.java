package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.User;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User ORDER BY id ASC", User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<User> searchByKeyword(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM User WHERE lower(username) LIKE :keyword OR lower(fullName) LIKE :keyword";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("ROLE_USER");
            }
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // In case the user is detached, fetch it and update OR update directly
            User existingUser = session.get(User.class, user.getId());
            if (existingUser != null) {
                existingUser.setFullName(user.getFullName());
                existingUser.setAvatar(user.getAvatar());
                existingUser.setPassword(user.getPassword());
                session.update(existingUser);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void delete(long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                // Triggers cascade delete on comments and likes associated with user's posts
                List<Post> userPosts = session.createQuery("FROM Post WHERE user.id = :id", Post.class)
                        .setParameter("id", id).list();
                for (Post p : userPosts) {
                    session.delete(p);
                }

                // Clear user dependencies that cascade type doesn't cover or to avoid FK issues
                session.createQuery("DELETE FROM PostLike pl WHERE pl.user.id = :id").setParameter("id", id)
                        .executeUpdate();
                session.createQuery("DELETE FROM Comment c WHERE c.user.id = :id").setParameter("id", id)
                        .executeUpdate();
                session.createQuery("DELETE FROM Friendship f WHERE f.user.id = :id OR f.friend.id = :id")
                        .setParameter("id", id).executeUpdate();
                session.createQuery("DELETE FROM FriendRequest fr WHERE fr.sender.id = :id OR fr.receiver.id = :id")
                        .setParameter("id", id).executeUpdate();
                session.createQuery("DELETE FROM Message m WHERE m.sender.id = :id OR m.receiver.id = :id")
                        .setParameter("id", id).executeUpdate();

                session.delete(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
