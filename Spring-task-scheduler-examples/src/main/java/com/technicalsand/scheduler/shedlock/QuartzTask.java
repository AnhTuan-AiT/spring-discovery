package com.technicalsand.scheduler.shedlock;

import com.technicalsand.scheduler.read.database.Configuration;
import com.technicalsand.scheduler.read.database.ConfigurationRepository;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Lazy(value = false)
@Component
public class QuartzTask implements SchedulingConfigurer {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private LockingTaskExecutor executor;

    @Autowired
    private LockProvider lockProvider;

    @Autowired
    private ConfigurationRepository repository;

    /**
     * Register multiple Spring Task timer tasks to perform different timing tasks from multiple companies
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        executor = new DefaultLockingTaskExecutor(lockProvider);

        taskRegistrar.setScheduler(scheduledExecutorService());
        taskRegistrar.setTriggerTasks(getTasksAndTriggers());
    }

    private Runnable scheduleTask() {
        return () -> {
            // Perform logical tasks
            Configuration configuration = repository.findByName("B07550001");

            System.err.println("Company: Code = B07550001 ------ previous cron time period=" + configuration.getExpression());
            System.out.println("Current system time:" + sdf.format(System.currentTimeMillis()) + "\n\n");

            configuration.setExpression("*/10 * * ? * *");
            repository.save(configuration);
        };
    }

    /**
     * Configure all dynamically scheduled task here.
     *
     * @return
     */
    public Map<String, Runnable> getTasks() {
        Map<String, Runnable> mTaskNameWithTask = new HashMap<>();

//        mTaskNameWithTask.put("A00000000", runnable);
        mTaskNameWithTask.put("B07550001", scheduleTask());
//        mTaskNameWithTask.put("C00210001", runnable);

        return mTaskNameWithTask;
    }

    /**
     * Obtain a map set of multiple timing tasks with multiple triggers
     *
     * @return
     */
    public Map<Runnable, Trigger> getTasksAndTriggers() {
        Map<Runnable, Trigger> triggerTasks = new HashMap<>();
        Map<String, Runnable> mTaskNameWithTask = getTasks();

        //Traverse the map collection
        // Task name is lock name
        mTaskNameWithTask.forEach((lockName, task) -> {
            Trigger trigger = triggerContext -> {
                // Task trigger, can modify the execution cycle of the task
                Configuration configuration = repository.findByName(lockName);
                CronTrigger cronTrigger = new CronTrigger(configuration.getExpression());
                Date nextExecutor = cronTrigger.nextExecutionTime(triggerContext);

                return nextExecutor;
            };

            // LockConfiguration: first param of  is not important, see documentation!
            // TODO: tuning the last 2 params
            triggerTasks.put(() -> {
                executor.executeWithLock(task, new LockConfiguration(Instant.now(), lockName, Duration.ofSeconds(5), Duration.ofSeconds(1)));
            }, trigger);
        });

        return triggerTasks;
    }

    private ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(20);
    }
}