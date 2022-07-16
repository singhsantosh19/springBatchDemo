package com.example.spring.batch.service;

import com.example.spring.batch.entity.Employee;
import com.example.spring.batch.model.User;
import org.springframework.batch.item.ItemProcessor;
import org.apache.commons.beanutils.BeanUtils;

public class CustomItemProcessor implements ItemProcessor<User, Employee> {

        @Override
        public Employee process(User user) throws Exception {
            Employee employee= new Employee();
            BeanUtils.copyProperties(employee,user);
            return employee;
        }
    }
