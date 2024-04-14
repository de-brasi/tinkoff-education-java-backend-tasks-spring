package edu.java.scrapper.domaintest;

import edu.java.domain.JdbcLinkRepository;
import edu.java.domain.entities.Link;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
public class JdbcLinkRepositoryTest extends IntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcLinkRepository linkRepository;

    @Test
    @Transactional
    @Rollback
    void addURLTest() {
        final String testLink = "https://example/add-uri-test";

        final boolean res = linkRepository.add(testLink);
        assertThat(res).isTrue();

        String query = "SELECT COUNT(*) FROM links WHERE url = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testLink);
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addEqualURLsTest() {
        final String testLink = "https://example/add-equal-urls-test";

        final boolean res = linkRepository.add(testLink);
        assertThat(res).isTrue();

        // TODO: какого черта не отлавливается ошибка в "catch" внутри метода JdbcLinkRepository::add?
        final boolean resOneMore = linkRepository.add(testLink);
        assertThat(resOneMore).isFalse();

        String query = "SELECT COUNT(*) FROM links WHERE url = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testLink);
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() {
        final String testLink = "https://example/remove-test";

        // add record
        final boolean addResult = linkRepository.add(testLink);
        assertThat(addResult).isTrue();

        // clear record
        final String removeResult = linkRepository.remove(testLink).orElseThrow();
        assertThat(removeResult).isEqualTo(testLink);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url = ?",
            Integer.class,
            testLink
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeTwiceTest() {
        final String testLink = "https://example/remove-twice-test";

        // add record
        final boolean addResult = linkRepository.add(testLink);
        assertThat(addResult).isTrue();

        // clear record once
        final String firstRemoveResult = linkRepository.remove(testLink).orElseThrow();
        assertThat(firstRemoveResult).isEqualTo(testLink);

        // clear record twice
        var secondRemoveResult = linkRepository.remove(testLink);
        assertThat(secondRemoveResult.isPresent()).isFalse();

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url = ?",
            Integer.class,
            testLink
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void getAllTest() {
        final String testLink1 = "https://en.wikipedia.org/wiki/Main_Page1";
        final String testLink2 = "https://en.wikipedia.org/wiki/Main_Page2";
        final String testLink3 = "https://en.wikipedia.org/wiki/Main_Page3";

        // add records
        final boolean addResult1 = linkRepository.add(testLink1);
        assertThat(addResult1).isTrue();
        final boolean addResult2 = linkRepository.add(testLink2);
        assertThat(addResult2).isTrue();
        final boolean addResult3 = linkRepository.add(testLink3);
        assertThat(addResult3).isTrue();

        // check result contains all
        Collection<String> gettingAll = linkRepository.findAll();
        assertThat(gettingAll).containsExactlyInAnyOrder(testLink1, testLink2, testLink3);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url IN (?, ?, ?)",
            Integer.class,
            testLink1, testLink2, testLink3
        );
        assertThat(rowCount).isEqualTo(3);
    }

    @Test
    @Transactional
    @Rollback
    void updateLastCheckTimeTest() {
        // add record
        final String testLink = "https://example/add-uri-test";
        linkRepository.add(testLink);

        Timestamp newCheckTimeExpected = Timestamp.from(Instant.now());
        linkRepository.updateLastCheckTime(testLink, newCheckTimeExpected);

        Timestamp newCheckTimeActual = jdbcTemplate.queryForObject(
            "select last_check_time from links where url = ?",
            Timestamp.class,
            testLink
        );

        assert newCheckTimeActual != null;
        assertThat(newCheckTimeExpected.toInstant()).isCloseTo(
            newCheckTimeActual.toInstant(),
            within(1, ChronoUnit.MILLIS)
        );
    }

    @Test
    @Transactional
    @Rollback
    void updateLastUpdateTimeTest() {
        // add record
        final String testLink = "https://example/add-uri-test";
        linkRepository.add(testLink);

        Timestamp newCheckTimeExpected = Timestamp.from(Instant.now());
        linkRepository.updateLastUpdateTime(testLink, newCheckTimeExpected);

        Timestamp newCheckTimeActual = jdbcTemplate.queryForObject(
            "select last_update_time from links where url = ?",
            Timestamp.class,
            testLink
        );

        assert newCheckTimeActual != null;
        assertThat(newCheckTimeExpected.toInstant()).isCloseTo(
            newCheckTimeActual.toInstant(),
            within(1, ChronoUnit.MILLIS)
        );
    }
}
