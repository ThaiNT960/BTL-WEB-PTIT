package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.Message;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public List<Message> getChatHistory(long user1Id, long user2Id, long lastMessageId, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (lastMessageId > 0) {
                return session.createQuery(
                        "FROM Message m JOIN FETCH m.sender JOIN FETCH m.receiver " +
                                "WHERE (" +
                                "   (m.sender.id = :u1 AND m.receiver.id = :u2 AND (m.deletedBySender IS NULL OR m.deletedBySender = false)) " + 
                                "   OR " +
                                "   (m.receiver.id = :u1 AND m.sender.id = :u2 AND (m.deletedByReceiver IS NULL OR m.deletedByReceiver = false))" +
                                ") AND m.id > :lastId " +
                                "ORDER BY m.timestamp ASC", Message.class)
                        .setParameter("u1", user1Id)
                        .setParameter("u2", user2Id)
                        .setParameter("lastId", lastMessageId)
                        .list();
            } else {
                List<Message> recent = session.createQuery(
                        "FROM Message m JOIN FETCH m.sender JOIN FETCH m.receiver " +
                                "WHERE (" +
                                "   (m.sender.id = :u1 AND m.receiver.id = :u2 AND (m.deletedBySender IS NULL OR m.deletedBySender = false)) " + 
                                "   OR " +
                                "   (m.receiver.id = :u1 AND m.sender.id = :u2 AND (m.deletedByReceiver IS NULL OR m.deletedByReceiver = false))" +
                                ") " +
                                "ORDER BY m.timestamp DESC", Message.class)
                        .setParameter("u1", user1Id)
                        .setParameter("u2", user2Id)
                        .setMaxResults(limit)
                        .list();
                java.util.Collections.reverse(recent);
                return recent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void clearChatHistory(long currentUserId, long otherUserId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("UPDATE Message m SET m.deletedBySender = true WHERE m.sender.id = :cur AND m.receiver.id = :oth")
                   .setParameter("cur", currentUserId).setParameter("oth", otherUserId).executeUpdate();
            session.createQuery("UPDATE Message m SET m.deletedByReceiver = true WHERE m.receiver.id = :cur AND m.sender.id = :oth")
                   .setParameter("cur", currentUserId).setParameter("oth", otherUserId).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void recallMessage(long messageId, long senderId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("UPDATE Message m SET m.isRecalled = true WHERE m.id = :msgId AND m.sender.id = :senderId")
                   .setParameter("msgId", messageId)
                   .setParameter("senderId", senderId).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public List<User> getChattedUsers(long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> receivers = session.createQuery(
                    "SELECT DISTINCT u FROM User u WHERE u.id IN (SELECT m.receiver.id FROM Message m WHERE m.sender.id = :uid AND (m.deletedBySender IS NULL OR m.deletedBySender = false))", User.class)
                    .setParameter("uid", userId)
                    .list();
            List<User> senders = session.createQuery(
                    "SELECT DISTINCT u FROM User u WHERE u.id IN (SELECT m.sender.id FROM Message m WHERE m.receiver.id = :uid AND (m.deletedByReceiver IS NULL OR m.deletedByReceiver = false))", User.class)
                    .setParameter("uid", userId)
                    .list();
            
            java.util.Set<User> set = new java.util.LinkedHashSet<>(receivers);
            set.addAll(senders);
            return new ArrayList<>(set);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void save(long senderId, long receiverId, String content, String imageUrl) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User sender = session.get(User.class, senderId);
            User receiver = session.get(User.class, receiverId);
            if (sender != null && receiver != null) {
                Message message = new Message();
                message.setSender(sender);
                message.setReceiver(receiver);
                message.setContent(content);
                message.setImageUrl(imageUrl);
                message.setTimestamp(LocalDateTime.now());
                session.save(message);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void markMessagesAsRead(long senderId, long receiverId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("UPDATE Message m SET m.isRead = true WHERE m.sender.id = :sid AND m.receiver.id = :rid AND m.isRead = false")
                    .setParameter("sid", senderId)
                    .setParameter("rid", receiverId)
                    .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public java.util.Map<Long, Long> getUnreadCounts(long userId) {
        java.util.Map<Long, Long> unreadCounts = new java.util.HashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> results = session.createQuery(
                    "SELECT m.sender.id, COUNT(m.id) FROM Message m WHERE m.receiver.id = :uid AND m.isRead = false GROUP BY m.sender.id", Object[].class)
                    .setParameter("uid", userId)
                    .list();
            for (Object[] row : results) {
                unreadCounts.put((Long) row[0], (Long) row[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unreadCounts;
    }

    public long getTotalUnreadConversations(long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(DISTINCT m.sender.id) FROM Message m WHERE m.receiver.id = :uid AND m.isRead = false", Long.class)
                    .setParameter("uid", userId)
                    .uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getLastReadMessageId(long senderId, long receiverId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long maxId = session.createQuery(
                "SELECT MAX(m.id) FROM Message m WHERE m.sender.id = :sid AND m.receiver.id = :rid AND m.isRead = true", Long.class)
                .setParameter("sid", senderId)
                .setParameter("rid", receiverId)
                .uniqueResult();
            return maxId != null ? maxId : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Message findById(long messageId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Message m JOIN FETCH m.sender JOIN FETCH m.receiver WHERE m.id = :id", Message.class)
                    .setParameter("id", messageId)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
