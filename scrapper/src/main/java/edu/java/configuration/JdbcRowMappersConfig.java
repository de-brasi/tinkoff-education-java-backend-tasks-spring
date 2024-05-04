package edu.java.configuration;

import edu.java.domain.entities.ChatLinkBound;
import edu.java.domain.entities.Link;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

@Configuration
@SuppressWarnings("MultipleStringLiterals")
public class JdbcRowMappersConfig {
    @Bean
    public RowMapper<ChatLinkBound> chatLinkBoundRowMapper() {
        return (rs, rowNum) -> {
            Long tgChatId = rs.getLong("chat_id");
            String url = rs.getString("url");
            return new ChatLinkBound(tgChatId, url);
        };
    }

    @Bean
    public RowMapper<String> linkUrlRowMapper() {
        return (rs, rowNum) -> rs.getString("url");
    }

    @Bean
    public RowMapper<Long> chatIdRowMapper() {
        return (rs, rowNum) -> rs.getLong("chat_id");
    }

    @Bean
    public RowMapper<Link> linkRowMapper() {
        return (rs, rowNum) -> {
            final String url = rs.getString("url");
            final Integer urlId = rs.getInt("link_id");
            return new Link(urlId, URI.create(url));
        };
    }
}
