package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.ModerationSettings;
import com.ptit.socialchat.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ModerationSettingsDAO {

    /**
     * Lấy cấu hình kiểm duyệt hiện tại.
     * Nếu chưa có record nào thì tự tạo mặc định (mode = NONE).
     */
    public ModerationSettings getSettings() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ModerationSettings settings = session.get(ModerationSettings.class, 1);
            if (settings == null) {
                // Tạo bản ghi mặc định
                Transaction tx = session.beginTransaction();
                settings = new ModerationSettings();
                settings.setId(1);
                settings.setMode("NONE");
                settings.setAiServiceUrl("http://localhost:8000");
                session.save(settings);
                tx.commit();
            }
            return settings;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Fallback
        ModerationSettings fallback = new ModerationSettings();
        fallback.setMode("NONE");
        return fallback;
    }

    /**
     * Cập nhật chế độ kiểm duyệt.
     * @param mode "NONE", "MANUAL", hoặc "AUTO_AI"
     */
    public void updateMode(String mode) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ModerationSettings settings = session.get(ModerationSettings.class, 1);
            if (settings == null) {
                settings = new ModerationSettings();
                settings.setId(1);
                settings.setAiServiceUrl("http://localhost:8000");
            }
            settings.setMode(mode);
            session.saveOrUpdate(settings);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
