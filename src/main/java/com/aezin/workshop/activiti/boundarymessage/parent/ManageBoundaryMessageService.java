package com.aezin.workshop.activiti.boundarymessage.parent;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ManageBoundaryMessageService implements JavaDelegate {
	
	public void execute(DelegateExecution execution) throws Exception {
		System.out.println("Process '" + execution.getProcessInstanceId() + "', "
				+ "ServiceTask [" + execution.getCurrentActivityName() + "]");
	}

}
