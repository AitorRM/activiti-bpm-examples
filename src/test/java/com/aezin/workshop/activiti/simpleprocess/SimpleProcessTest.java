package com.aezin.workshop.activiti.simpleprocess;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.aezin.workshop.activiti.ProcessTestOperations;

/**
 * Simple process test with a UserTask.
 * 
 * @author Aitor Rodríguez
 *
 */
public class SimpleProcessTest {
	
	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();
	
	private ProcessTestOperations processTestOperations;
	
	@Before
	public void initProcessEnvironment() throws Exception {
		processTestOperations = new ProcessTestOperations(activitiRule);
	}
	
	@Test
	@Deployment(resources = {"com/aezin/workshop/activiti/simpleprocess/process_simple-process.bpmn20.xml"})
	public void test() {
		TaskService taskService = activitiRule.getTaskService();
		
		// Start process
		String processInstanceId = processTestOperations.startProcessInstance("simple-process");
		
		// Get UserTask
		Task task = processTestOperations.getCurrentTask("usertask", processInstanceId);
		
		// Complete task
		taskService.complete(task.getId());
		
		// Check end process
		processTestOperations.checkEndProcess("endevent", processInstanceId);
	}
	
}
