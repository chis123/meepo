package meepo.controller;

import com.google.common.collect.Lists;
import meepo.transform.config.TaskContext;
import meepo.transform.task.TasksManager;
import meepo.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

/**
 * Created by peiliping on 17-2-21.
 */

@Controller public class TaskController {

    @Autowired private Environment env;

    @Autowired private TasksManager tasksManager;

    @RequestMapping("/") @ResponseBody public String index() {
        return "Hello World!";
    }

    @RequestMapping("/task/preload") @ResponseBody public List<TaskContext> taskCheck() throws Exception {
        TaskContext context = initTasksContext();
        List<String> taskNames = Lists.newArrayList(context.get(Constants.PROJECT_NAME).split("\\s"));
        List<TaskContext> taskConfigs = Lists.newArrayList();
        taskNames.forEach(taskName -> taskConfigs.add(new TaskContext(taskName, context.getSubProperties("meepo." + taskName + "."))));
        return taskConfigs;
    }

    @RequestMapping("/task/list") @ResponseBody public List<TaskContext> listTasks() throws Exception {
        return tasksManager.listTasks();
    }

    @RequestMapping("/task/{taskName}/run") @ResponseBody public String taskRun(@PathVariable String taskName) throws Exception {
        TaskContext context = initTasksContext();
        List<String> taskNames = Lists.newArrayList(context.get(Constants.PROJECT_NAME).split("\\s"));
        Validate.isTrue(StringUtils.isNotBlank(taskName));
        Validate.isTrue(taskNames.contains(taskName));
        TaskContext tc = new TaskContext(taskName, context.getSubProperties("meepo." + taskName + "."));
        boolean status = tasksManager.addTask(tc);
        return status ? (taskName + " is running ...") : (taskName + " boot failed ...");
    }

    @RequestMapping("/task/{taskName}/kill") @ResponseBody public String taskKill(@PathVariable String taskName) throws Exception {
        boolean status = tasksManager.forceStopTask(taskName);
        return status ? (taskName + " is killing ...") : (taskName + " kill failed ...");
    }

    private TaskContext initTasksContext() throws IOException {
        String configPath = env.getProperty("tasks.configuration.path");
        return new TaskContext("All Tasks", configPath);
    }

}