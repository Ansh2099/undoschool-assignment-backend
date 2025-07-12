package com.example.undoschool_assignment_backend.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {

        ObjectMapper jackson = new ObjectMapper();
        jackson.registerModule(new JavaTimeModule());

        JsonpMapper jsonpMapper = new JacksonJsonpMapper(jackson);

        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, jsonpMapper);
        return new ElasticsearchClient(transport);
    }
}