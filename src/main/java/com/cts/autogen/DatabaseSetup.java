package com.cts.autogen;

import com.cts.autogen.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


    @Component
    public class DatabaseSetup implements CommandLineRunner {

        private Logger logger = LoggerFactory.getLogger(getClass());

        @Autowired
        private JdbcTemplate jdbcTemplate;
        @Autowired
        private Controller controller;

        @Override
        public void run(String... args) throws Exception {
            jdbcTemplate.update("create table IF NOT EXISTS meteringPoint_settlementMethod (meteringPoint varchar2(18) not null, settlementMethod varchar2(18) not null)");
            jdbcTemplate.update("create table IF NOT EXISTS meter_meteringPoint (meter_id varchar2(18) not null, meteringPoint varchar2(18) not null)");
            try {
                controller.execute();
            }catch (Throwable t){
                logger.error("Start up processing of input folder failed", t);
            }
        }
    }
