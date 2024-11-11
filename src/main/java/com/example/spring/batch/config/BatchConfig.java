package com.example.spring.batch.config;

import com.example.spring.batch.entity.Employee;
import com.example.spring.batch.model.User;
import com.example.spring.batch.service.CustomItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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
import org.springframework.orm.jpa.JpaTransactionManager;

import java.util.Objects;

@Configuration
public class BatchConfig {
    @Autowired
    private  JobRepository jobRepository;
    
    @Autowired
    JpaTransactionManager jpaTransactionManager;

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
        employeeJpaItemWriter.setEntityManagerFactory(Objects.requireNonNull(jpaTransactionManager.getEntityManagerFactory()));
        return employeeJpaItemWriter;
    }

    @Bean
    protected Step step1(@Qualifier("jsonItemReader") ItemReader<User> reader,
                         @Qualifier("itemProcessor") ItemProcessor<User, Employee> processor,
                         @Qualifier("JpaItemWriter") ItemWriter<Employee> writer) {

        return new StepBuilder("step1", jobRepository).<User, Employee> chunk(5,jpaTransactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = "firstBatchJob")
    public Job job(@Qualifier("step1") Step step1) {
        return new JobBuilder("firstBatchJob", jobRepository)
                .preventRestart()
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }
}
