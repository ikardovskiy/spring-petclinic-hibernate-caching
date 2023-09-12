package org.springframework.samples.petclinic.cache;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.github.database.rider.spring.api.DBRider;
import org.hibernate.Session;
import org.hibernate.engine.internal.StatisticalLoggingSessionEventListener;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.util.PostgresqlDbBaseTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;


@DBRider
@DataJpaTest
@Disabled
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FirstLevelCacheTest extends PostgresqlDbBaseTest {


  @Autowired
  VetRepository vetRepository;

  @SuppressWarnings("unused")
  public ConnectionHolder connectionHolder = () -> dataSource.getConnection();

  public static Logger log = LoggerFactory.getLogger(FirstLevelCacheTest.class);

  @Test
  @DataSet(
      value = {"datasets/repeatable-read.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  @Commit
  public void repeatableRead() {
    StatisticalLoggingSessionEventListener listener = new StatisticalLoggingSessionEventListener();
    em.unwrap(Session.class).addEventListeners(listener);
    Vet vet = em.find(Vet.class, 100);
    log.debug("find1");
    listener.end();
    vet = em.find(Vet.class, 100);
    listener.end();
    vet = vetRepository.getOne(100);
    listener.end();
    vet = em.find(Vet.class, 100);
    listener.end();
  }




  private void logEntity(Class<?> aClass) {
    log.info("{} stats: {}", aClass.getCanonicalName(), em.unwrap(Session.class).getSessionFactory().getStatistics().getEntityStatistics(aClass.getCanonicalName()));
  }

  @Test
  @DataSet(
      value = {"datasets/repeatable-read.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  @Commit
  public void repeatableReadWithHql() {
    Vet vet = em.find(Vet.class, 100);
    vet = em.find(Vet.class, 100);
    vet = em.find(Vet.class, 100);
    vet = em.find(Vet.class, 100);
    em.createQuery("select v from Vet v where id=:id", Vet.class)
        .setParameter("id", 100)
        .getSingleResult();
  }

  @Test
  @DataSet(
      value = {"datasets/bulk-update.xml"},
      executeScriptsBefore = "datasets/cleanup.sql",
      strategy = SeedStrategy.INSERT
  )
  @Commit
  public void bulkUpdate() {
    Vet vet = em.find(Vet.class, 100);
    System.out.println("before: " + vet.isVip());
    em.createQuery("update Vet v set v.isVip = true").executeUpdate();
    vet.setLastName("Dowson");
    em.flush();
    System.out.println("after: " + vet.isVip());
  }

}