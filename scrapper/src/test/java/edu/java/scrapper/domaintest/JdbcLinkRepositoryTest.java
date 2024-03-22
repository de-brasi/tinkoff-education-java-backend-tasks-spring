package edu.java.scrapper.domaintest;

import edu.java.domain.repositories.jdbc.JdbcLinkRepository;
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
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JdbcLinkRepositoryTest extends IntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcLinkRepository linkRepository;

    final Link testLink = new Link(URI.create(
            "https://stackoverflow.com/questions/70914106/" +
                "show-multiple-descriptions-for-a-response-code-using-springdoc-openapi-for-a-spr"
        ));

    @Test
    @Transactional
    @Rollback
    void addURLTest() throws MalformedURLException {
        final boolean res = linkRepository.add(testLink);
        assertThat(res).isTrue();

        String query = "SELECT COUNT(*) FROM links WHERE url = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testLink.uri().toURL().toString());
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void addEqualURLsTest() throws MalformedURLException {
        final boolean res = linkRepository.add(testLink);
        assertThat(res).isTrue();

        final boolean resOneMore = linkRepository.add(testLink);
        assertThat(resOneMore).isFalse();

        String query = "SELECT COUNT(*) FROM links WHERE url = ?";
        int rowCount = jdbcTemplate.queryForObject(query, Integer.class, testLink.uri().toURL().toString());
        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() throws MalformedURLException {
        // add record
        final boolean addResult = linkRepository.add(testLink);
        assertThat(addResult).isTrue();

        // clear record
        final Link removeResult = linkRepository.remove(testLink);
        assertThat(removeResult).isEqualTo(testLink);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url = ?",
            Integer.class,
            testLink.uri().toURL().toString()
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void removeTwiceTest() throws MalformedURLException {
        // add record
        final boolean addResult = linkRepository.add(testLink);
        assertThat(addResult).isTrue();

        // clear record once
        final Link firstRemoveResult = linkRepository.remove(testLink);
        assertThat(firstRemoveResult).isEqualTo(testLink);

        // clear record twice
        final Link secondRemoveResult = linkRepository.remove(testLink);
        assertThat(secondRemoveResult).isNull();

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url = ?",
            Integer.class,
            testLink.uri().toURL().toString()
        );
        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void getAllTest() throws MalformedURLException {
        final Link testLink1 =
            new Link(URI.create("https://stackoverflow.com/questions/78205360/how-to-add-driver-to-iso-or-bootable-usb"));
        final Link testLink2 = new Link(URI.create(
            "https://stackoverflow.com/questions/35534959/access-key-and-value-of-object-using-ngfor?rq=2"));
        final Link testLink3 = new Link(URI.create(
            "https://stackoverflow.com/questions/78205354/can-we-make-an-angular-project-in-javascript"));

        // add records
        final boolean addResult1 = linkRepository.add(testLink1);
        assertThat(addResult1).isTrue();
        final boolean addResult2 = linkRepository.add(testLink2);
        assertThat(addResult2).isTrue();
        final boolean addResult3 = linkRepository.add(testLink3);
        assertThat(addResult3).isTrue();

        // check result contains all
        Collection<Link> gettingAll = linkRepository.findAll();
        assertThat(gettingAll).containsExactlyInAnyOrder(testLink1, testLink2, testLink3);

        // check link actually was removed
        int rowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM links WHERE url IN (?, ?, ?)",
            Integer.class,
            testLink1.uri().toURL().toString(),
            testLink2.uri().toURL().toString(),
            testLink3.uri().toURL().toString()
        );
        assertThat(rowCount).isEqualTo(3);
    }
}
