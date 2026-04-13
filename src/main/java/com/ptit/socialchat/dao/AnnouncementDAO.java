package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.Announcement;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAO {

    /**
     * Lấy tất cả thông báo, mới nhất lên trước.
     */
    public List<Announcement> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Announcement a JOIN FETCH a.admin ORDER BY a.createdAt DESC", Announcement.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Lấy N thông báo mới nhất (cho sidebar).
     */
    public List<Announcement> findRecent(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Announcement a JOIN FETCH a.admin ORDER BY a.createdAt DESC", Announcement.class
            ).setMaxResults(limit).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Tạo thông báo mới.
     */
    public void save(String title, String content, long adminId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User admin = session.get(User.class, adminId);
            if (admin != null) {
                Announcement a = new Announcement();
                a.setTitle(title);
                a.setContent(content);
                a.setAdmin(admin);
                a.setCreatedAt(LocalDateTime.now());
                session.save(a);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Xóa thông báo.
     */
    public void delete(long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Announcement a = session.get(Announcement.class, id);
            if (a != null) session.delete(a);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}
