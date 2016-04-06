package com.aezin.workshop.activiti.asyncsendreceive;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.aezin.workshop.activiti.ProcessTestOperations;

public class AsyncSendReceiveTest {

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();
	
	private ProcessTestOperations processTestOperations;
	
	@Before
	public void initProcessEnvironment() throws Exception {
		processTestOperations = new ProcessTestOperations(activitiRule);
	}
	
	@Test
	@Deployment(resources = {"com/aezin/workshop/activiti/asyncsendreceive/process_async_send_receive.bpmn20.xml"})
	public void test() {
		// Start Job executor
		processTestOperations.startJobExecutor();
	    
		// Start responser process
		String processInstanceIdRes = processTestOperations.startProcessInstance("process_responser");
		
		// Start requester process
		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("responser_process", processInstanceIdRes);
		String processInstanceIdReq = processTestOperations.startProcessInstance("process_requester", variableMap);
		
		// Wait asynchronous jobs
		processTestOperations.waitForJobExecutorToProcessAllJobs(7000, 100);
		
		// Check end processes
		processTestOperations.checkEndProcess("endevent_res", processInstanceIdRes);
		processTestOperations.checkEndProcess("endevent_req", processInstanceIdReq);
		
		// Shutdown Job executor
		processTestOperations.shutdownJobExecutor();
	}
}