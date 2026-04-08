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

    public List<Message> getChatHistory(long user1Id, long user2Id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Message m JOIN FETCH m.sender JOIN FETCH m.receiver " +
                            "WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) " +
                            "   OR (m.sender.id = :u2 AND m.receiver.id = :u1) " +
                            "ORDER BY m.timestamp ASC", Message.class)
                    .setParameter("u1", user1Id)
                    .setParameter("u2", user2Id)
                    .list();
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
}
