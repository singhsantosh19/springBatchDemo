package com.example.spring.batch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class BatchJobController {

 @Autowired
 private JobLauncher jobLauncher;

 @Autowired
 @Qualifier("firstBatchJob")
 private Job job;

 @RequestMapping("/launchjob")
 public String handle() throws Exception {

     JobExecution jobExecution = null;
     try {
                jobExecution = jobLauncher.run(job, new JobParameters());
     }
     catch (Exception e) {
                log.info(e.getMessage());
            }

            return "jobExecution's info: Id = " + jobExecution.getId() + " ,status = " + jobExecution.getExitStatus();
        }
    }

