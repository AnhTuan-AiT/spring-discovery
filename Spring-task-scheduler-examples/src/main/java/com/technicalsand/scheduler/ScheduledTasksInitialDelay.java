//package com.technicalsand.scheduler;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//public class ScheduledTasksInitialDelay {
//	final static Logger logger = LoggerFactory.getLogger(ScheduledTasksInitialDelay.class);
//
//	@Scheduled(initialDelay = 2000, fixedDelay = 5000)
//	public void scheduleTaskInitialAndFixedDelay(){
//		logger.info("scheduledTaskInitialAndFixedDelay executed at {}", LocalDateTime.now());
//	}
//}
