package com.aezin.workshop.activiti.boundarymessage;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.aezin.workshop.activiti.ProcessTestOperations;

/**
 * Boundary Message Event example with two processes.
 * 
 * @author Aitor Rodríguez
 *
 */
public class BoundaryMessageEventTest {
	
	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();
	
	private ProcessTestOperations processTestOperations;
	
	@Before
	public void initProcessEnvironment() throws Exception {
		processTestOperations = new ProcessTestOperations(activitiRule);
	}
	
	@Test
	@Deployment(resources = {"com/aezin/workshop/activiti/boundarymessage/process_boundary_event_parent.bpmn20.xml",
							 "com/aezin/workshop/activiti/boundarymessage/process_boundary_event_child.bpmn20.xml"})
	public void test() {
		TaskService taskService = activitiRule.getTaskService();
		
		// Start processes
		String processInstanceIdParent = processTestOperations.startProcessInstance("process_boundary_event_parent");
		String processInstanceIdChild = processTestOperations.startProcessInstance("process_boundary_event_child");
		
		// Get parent task
		Task taskParent = processTestOperations.getCurrentTask("usertask", processInstanceIdParent);
				
		// Get and complete child task
		Task taskChild = processTestOperations.getCurrentTask("usertask", processInstanceIdChild);
		taskService.complete(taskChild.getId());
		
		// Check end processes
		processTestOperations.checkEndProcess(null, processInstanceIdParent);
		processTestOperations.checkEndProcess(null, processInstanceIdChild);
	}
	
}
