package com.demo.lucky_platform.web_nosql.sampleData.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "nosql_test_collection")
@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleDataInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    private String name;

    private String email;

    private String address;

    private Integer count;
}
