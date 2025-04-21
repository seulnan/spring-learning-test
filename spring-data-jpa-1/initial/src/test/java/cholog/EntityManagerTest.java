package cholog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EntityManagerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 비영속 -> 영속
     */
    @Test
    void persist() {
        Customer customer = new Customer("Jack", "Bauer");
        entityManager.persist(customer);

        assertThat(entityManager.find(Customer.class, 1L)).isNotNull();
    }

    /**
     * 영속 -> DB
     */
    @Test
    void flush() {
        Customer customer = new Customer("Jack", "Bauer");
        entityManager.persist(customer);
        entityManager.flush(); // db에 있는건 jack

        Long id = customer.getId(); // 동적으로 추출
        customer.updateFirstName("Danial"); // 영속성 컨텍스트에 있는건 danial

        String sqlForSelectCustomer = "select * from customer where id = " + id;

        Customer savedCustomer = jdbcTemplate.query(sqlForSelectCustomer, rs -> {
            rs.next();
            return new Customer(
                    rs.getLong("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"));
        });
        assertThat(savedCustomer.getFirstName()).isEqualTo("Jack");

        // danial로 db에 올라감
        entityManager.flush();

        Customer updatedCustomer = jdbcTemplate.query(sqlForSelectCustomer, rs -> {
            rs.next();
            return new Customer(
                    rs.getLong("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"));
        });

        assertThat(updatedCustomer.getFirstName()).isEqualTo("Danial");
    }
}
