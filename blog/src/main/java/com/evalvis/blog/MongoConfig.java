package com.evalvis.blog;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {
    @Autowired
    private Environment env;

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongo(), env.getProperty("spring.data.mongodb.database"));
    }

    @Bean
    public MongoClient mongo() {
        ConnectionString connectionString = new ConnectionString(
                env.getProperty("spring.data.mongodb.uri")
        );
        MongoClientSettings mongoClientSettings = MongoClientSettings
                .builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }
}