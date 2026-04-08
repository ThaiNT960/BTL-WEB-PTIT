import com.ptit.socialchat.util.HibernateUtil;
import org.hibernate.SessionFactory;

public class HibernateTest {
    public static void main(String[] args) {
        System.out.println("Testing Hibernate initialization...");
        try {
            SessionFactory sf = HibernateUtil.getSessionFactory();
            if (sf != null) {
                System.out.println("SUCCESS: Hibernate initialized");
            } else {
                System.out.println("FAILURE: sessionFactory is null (check logs for exceptions)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
