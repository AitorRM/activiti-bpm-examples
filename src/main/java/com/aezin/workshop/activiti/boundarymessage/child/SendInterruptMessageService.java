package com.aezin.workshop.activiti.boundarymessage.child;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.Execution;

public class SendInterruptMessageService implements JavaDelegate {
	
	public void execute(DelegateExecution execution) throws Exception {
		System.out.println("Process '" + execution.getProcessInstanceId() + "', "
				+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
				+ "--> Sending interrupt message to parent process...");
		
		List<Execution> subscriptors = execution.getEngineServices()
				.getRuntimeService()
				.createExecutionQuery()
				.messageEventSubscriptionName("testBoundaryInterruptMessage").list();
		
		boolean msgSent = false;
		for (Execution subscriptor : subscriptors) {
			execution.getEngineServices()
					.getRuntimeService()
					.messageEventReceived("testBoundaryInterruptMessage", subscriptor.getId());
			msgSent = true;
		}
		
		if (msgSent) {
			System.out.println("Process '" + execution.getProcessInstanceId() + "', "
					+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
					+ "--> Interrupt message sent!!");
		} else {
			System.out.println("Process '" + execution.getProcessInstanceId() + "', "
					+ "ServiceTask [" + execution.getCurrentActivityName() + "] "
					+ "--> Unsent interrupt message. No process listening events.");
		}
	}

}
