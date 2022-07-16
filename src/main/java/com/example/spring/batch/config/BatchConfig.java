package com.example.spring.batch.config;

import com.example.spring.batch.entity.Employee;
import com.example.spring.batch.model.User;
import com.example.spring.batch.service.CustomItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;



@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private PersistenceConfig persistenceConfig;

    @Bean(name="jsonItemReader")
    public JsonItemReader<User> jsonItemReader() {
        return new JsonItemReaderBuilder<User>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(User.class))
                .resource(new ClassPathResource("data.json"))
                .name("userJsonItemReader")
                .build();
    }
    @Bean(name="itemProcessor")
    public ItemProcessor<User, Employee> itemProcessor() {
        return new CustomItemProcessor();
    }

    @Bean(name="JpaItemWriter")
    public JpaItemWriter<Employee> employeeJpaItemWriter() {
        JpaItemWriter<Employee> employeeJpaItemWriter = new JpaItemWriter<>();
        employeeJpaItemWriter.setEntityManagerFactory(persistenceConfig.entityManagerFactory());
        return employeeJpaItemWriter;
    }

    @Bean
    protected Step step1(@Qualifier("jsonItemReader") ItemReader<User> reader,
                         @Qualifier("itemProcessor") ItemProcessor<User, Employee> processor,
                         @Qualifier("JpaItemWriter") ItemWriter<Employee> writer) {

        return steps.get("step1").<User, Employee> chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = "firstBatchJob")
    public Job job(@Qualifier("step1") Step step1) {
        return jobs.get("firstBatchJob")
                .preventRestart()
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }
}
