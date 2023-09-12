package org.springframework.samples.petclinic.util;

import org.hibernate.Session;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

public abstract class PostgresqlDbBaseTest {
  public static Logger log = LoggerFactory.getLogger(PostgresqlDbBaseTest.class);
  @PersistenceContext
  public EntityManager em;

  @BeforeEach
  public void before() {
    System.out.println("------------------------- START ----------------------------");
    clearStats();
  }
  private void clearStats() {
    em.unwrap(Session.class).getSessionFactory().getStatistics().clear();
  }

  @AfterEach
  public void after() {
    System.out.println("------------------------- FINISH ----------------------------");
    logSummary();
    logEntity();
    logQueries();
  }
  private void logQueries() {
    for(String query: em.unwrap(Session.class).getSessionFactory().getStatistics().getQueries()){
      log.info("Query text: {}  stats: {}",query, em.unwrap(Session.class).getSessionFactory().getStatistics().getQueryStatistics(query));
    }
  }

  private void logSummary() {
    em.unwrap(Session.class).getSessionFactory().getStatistics().logSummary();
  }

  private void logEntity() {
    Statistics statistics = em.unwrap(Session.class).getSessionFactory().getStatistics();
    for(String name: statistics.getEntityNames()){
      EntityStatistics entityStatistics = statistics.getEntityStatistics(name);
      if(entityStatistics.getLoadCount()!=0) {
        log.info("{} stats: {}", name, entityStatistics);
      }
    }


  }

  @Autowired
  protected DataSource dataSource;

  private static final PostgreSQLContainer<?> POSTGESQL_CONTAINER = new PostgreSQLContainer<>("postgres:9.6.1")
      .withUsername("test_login")
      .withPassword("test_password")
      .withDatabaseName("petclinic")
      .withExposedPorts(5432);

  static {
    POSTGESQL_CONTAINER.start();
    Runtime.getRuntime().addShutdownHook(new Thread(POSTGESQL_CONTAINER::stop));

    String urlPattern = "jdbc:p6spy:postgresql://%s:%s/petclinic";
    String url = String.format(urlPattern, POSTGESQL_CONTAINER.getContainerIpAddress(), POSTGESQL_CONTAINER.getMappedPort(5432));
    System.setProperty("spring.datasource.url", url);
    System.setProperty("spring.datasource.username", "test_login");
    System.setProperty("spring.datasource.password", "test_password");

  }
}