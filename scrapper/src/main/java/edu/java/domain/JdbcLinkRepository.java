package edu.java.domain;

import edu.common.exceptions.IncorrectRequestException;
import edu.java.domain.entities.Link;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import edu.java.domain.exceptions.UnexpectedDataBaseStateException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcLinkRepository implements BaseEntityRepository<Link> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public boolean add(Link link) {

        // TODO:
        //  Почему-то если пытаться вставлять новую запись и ловить
        //  исключение DataAccessException
        //  (когда добавляется повторная запись; возникает из-за ограничение на уникальность ссылок)
        //  исключение обрабатывается (проверяется логирующим принтом),
        //  однако потом возникает снова в вызывающем коде (в тестах например).
        //  Как будто бы прокси объект обрабатывает исключение но пробрасывает его дальше.
        //  Для решения проблемы пришлось сначала проверять число записей с таким url.

        try {
            int equalLinksCount = jdbcTemplate.queryForObject(
                "select count(*) from links where url = ?",
                Integer.class,
                link.uri().toURL().toString()
            );

            if (equalLinksCount == 0) {
                int affectedRowCount = jdbcTemplate.update(
                    "insert into links(url, last_check_time, last_update_time) values (?, ?, ?)",
                    link.uri().toURL().toString(),
                    Timestamp.from(Instant.now()),
                    Timestamp.from(Instant.ofEpochSecond(0))
                );

                return (affectedRowCount == 1);
            } else {
                return false;
            }
        } catch (DataAccessException e) {
            return false;
        } catch (MalformedURLException e) {
            throw new IncorrectRequestException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public @Nullable Link remove(Link link) {
        try {
            int affectedRowCount = jdbcTemplate.update(
                "delete from links where url = (?)",
                link.uri().toURL().toString()
            );
            return (affectedRowCount == 1) ? link : null;
        } catch (MalformedURLException e) {
            throw new IncorrectRequestException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Collection<Link> findAll() {
        String sql = "select * from links";
        return jdbcTemplate.query(sql, new LinkRowMapper());
    }

    @Override
    @Transactional
    public Collection<Link> search(Predicate<Link> condition) {

        // TODO: (для задания4)
        //  У меня есть стойкое ощущение что я не понял задачу:
        //      "
        //      ...на самом деле мы могли бы сделать фильтрацию
        //      (поиск ссылок, которые давно не проверялись) на стороне БД
        //      "
        //  "На стороне БД" - будто бы имеется в виду что фильтрация
        //  должна быть осуществлена самим постгресом, а для этого (при использовании jdbc, насколько я смог узнать)
        //  нужно просто менять строку запроса; то есть как "предикат" - строка типа "where <some conditions>".
        //  Кажется это не очень разумно, потому что при написании условий просто в строке
        //  IDE не дает хинтов по типам, именам таблиц и тд. - легко ошибиться и словить какую нибудь ошибку синтаксиса,
        //  а что еще хуже - ошибку логики запроса, когда он что то будет возвращать.
        //  В любом случае сейчас для работы ссылок, требующих удаления,
        //  я не использую репозитории, а пишу запросы в классе JdbcLinkUpdater.
        //  А так как написано сейчас не кажется удобным.
        //  В любом случае - https://ru.wikipedia.org/wiki/%D0%AD%D1%82%D0%BE_%D0%BD%D0%B5%D0%BC%D0%BD%D0%BE%D0%B3%D0%BE,_%D0%BD%D0%BE_%D1%8D%D1%82%D0%BE_%D1%87%D0%B5%D1%81%D1%82%D0%BD%D0%B0%D1%8F_%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D0%B0

        return findAll()
            .stream()
            .filter(condition)
            .collect(Collectors.toList());
    }

    @Transactional
    public void updateLastCheckTime(String url, Timestamp actualTime) {
        final String query = "update links set last_check_time = ? where url = ?";
        int affectedRowsCount = jdbcTemplate.update(query, actualTime, url);

        if (affectedRowsCount != 1) {
            throw new UnexpectedDataBaseStateException(
                "Expected to update field 'last_check_time' one row with current time but no one row changed!"
            );
        }
    }

    @Transactional
    public void updateLastUpdateTime(String url, Timestamp actualTime) {
        final String query = "update links set last_update_time = ? where url = ?";
        int affectedRowsCount = jdbcTemplate.update(query, actualTime, url);

        if (affectedRowsCount != 1) {
            throw new UnexpectedDataBaseStateException(
                "Expected to update field 'last_update_time' one row with current time but no one row changed!"
            );
        }
    }

    private static class LinkRowMapper implements RowMapper<Link> {
        @Override
        public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
            String url = rs.getString("url");
            return new Link(URI.create(url));
        }
    }
}
