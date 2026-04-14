package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.FriendRequest;
import com.ptit.socialchat.model.Friendship;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FriendDAO {

    public List<User> getFriendsByUserId(long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT f.friend FROM Friendship f WHERE f.user.id = :userId ORDER BY f.friend.fullName ASC", User.class)
                    .setParameter("userId", userId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean isFriend(long user1Id, long user2Id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT count(f) FROM Friendship f WHERE f.user.id = :u1 AND f.friend.id = :u2", Long.class)
                    .setParameter("u1", user1Id)
                    .setParameter("u2", user2Id)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasPendingRequest(long user1Id, long user2Id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT count(fr) FROM FriendRequest fr WHERE " +
                            "((fr.sender.id = :u1 AND fr.receiver.id = :u2) OR " +
                            "(fr.sender.id = :u2 AND fr.receiver.id = :u1)) AND " +
                            "fr.status = 'PENDING'", Long.class)
                    .setParameter("u1", user1Id)
                    .setParameter("u2", user2Id)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasPendingRequestFromTo(long senderId, long receiverId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT count(fr) FROM FriendRequest fr WHERE " +
                            "fr.sender.id = :s AND fr.receiver.id = :r AND fr.status = 'PENDING'", Long.class)
                    .setParameter("s", senderId)
                    .setParameter("r", receiverId)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addFriendship(long userId, long friendId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            User friend = session.get(User.class, friendId);
            if (user != null && friend != null) {
                Long count = session.createQuery("SELECT count(f) FROM Friendship f WHERE f.user.id = :u1 AND f.friend.id = :u2", Long.class)
                        .setParameter("u1", userId)
                        .setParameter("u2", friendId)
                        .uniqueResult();
                if (count == null || count == 0) {
                    // Add two-way friendship
                    Friendship f1 = new Friendship(user, friend);
                    Friendship f2 = new Friendship(friend, user);
                    session.save(f1);
                    session.save(f2);
                }
            }
            transaction.commit();
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.err.println("[FriendDAO] addFriendship constraint violation: already friends");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException || 
                (e.getCause() != null && e.getCause().getCause() instanceof java.sql.SQLIntegrityConstraintViolationException)) {
                System.err.println("[FriendDAO] addFriendship constraint violation: already friends");
            } else {
                e.printStackTrace();
            }
        }
    }

    public long getPendingRequestCount(long receiverId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT count(fr) FROM FriendRequest fr WHERE fr.receiver.id = :rId AND fr.status = 'PENDING'", Long.class)
                    .setParameter("rId", receiverId)
                    .uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<FriendRequest> getPendingRequests(long receiverId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM FriendRequest fr JOIN FETCH fr.sender WHERE fr.receiver.id = :receiverId AND fr.status = 'PENDING' ORDER BY fr.createdAt DESC", FriendRequest.class)
                    .setParameter("receiverId", receiverId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public FriendRequest findRequestById(long requestId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM FriendRequest fr JOIN FETCH fr.sender JOIN FETCH fr.receiver WHERE fr.id = :requestId", FriendRequest.class)
                    .setParameter("requestId", requestId)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendRequest(long senderId, long receiverId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User sender = session.get(User.class, senderId);
            User receiver = session.get(User.class, receiverId);
            if (sender != null && receiver != null) {
                FriendRequest req = new FriendRequest();
                req.setSender(sender);
                req.setReceiver(receiver);
                req.setStatus("PENDING");
                req.setCreatedAt(LocalDateTime.now());
                session.save(req);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void updateRequestStatus(long requestId, String status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            FriendRequest req = session.get(FriendRequest.class, requestId);
            if (req != null) {
                req.setStatus(status);
                session.update(req);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void deleteFriendship(long user1Id, long user2Id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM Friendship f WHERE (f.user.id = :u1 AND f.friend.id = :u2) OR (f.user.id = :u2 AND f.friend.id = :u1)")
                   .setParameter("u1", user1Id)
                   .setParameter("u2", user2Id)
                   .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void cancelFriendRequest(long senderId, long receiverId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM FriendRequest fr WHERE fr.sender.id = :senderId AND fr.receiver.id = :receiverId AND fr.status = 'PENDING'")
                   .setParameter("senderId", senderId)
                   .setParameter("receiverId", receiverId)
                   .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
