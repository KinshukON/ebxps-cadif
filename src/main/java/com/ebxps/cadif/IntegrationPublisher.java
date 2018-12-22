/**
 * 
 */
package com.ebxps.cadif;

import java.util.Date;

import com.ebxps.cadif.rest.HttpHelper;
import com.ebxps.cadif.rest.IntegrationException;
import com.ebxps.cadif.rest.NotificationMessage;
import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationTable;
import com.onwbp.adaptation.RequestResult;
import com.orchestranetworks.scheduler.ScheduledExecutionContext;
import com.orchestranetworks.scheduler.ScheduledTask;
import com.orchestranetworks.scheduler.ScheduledTaskInterruption;
import com.orchestranetworks.schema.ConstraintViolationException;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.service.Procedure;
import com.orchestranetworks.service.ProcedureContext;
import com.orchestranetworks.service.ProgrammaticService;
import com.orchestranetworks.service.ValueContextForUpdate;

/**
 * Schedule-able task to push messages to an integration platform.
 * 
 * @author Steve Higgins - Orchestra Networks - December 2017
 *
 */
public class IntegrationPublisher extends ScheduledTask {

	/** Component parameter: Socket timeout (in milliseconds) used by NipHelper. */
	private Integer socketTimeout = 30000;	// 30 seconds
	
	/**
	 * 
	 * 
	 * @see com.orchestranetworks.scheduler.ScheduledTask#execute(com.orchestranetworks.scheduler.ScheduledExecutionContext)
	 */
	@Override
	public void execute(ScheduledExecutionContext ctx) throws OperationException, ScheduledTaskInterruption {

		// Create a publisher object
		Publisher publisher = new Publisher(ctx);
		
		// Find the integration log table
		AdaptationTable logTable = getConfigTable(CrmpPaths._IntegrationLog.getPathInSchema());

		// Create a programmatic service for publishing and updating the log table
		ProgrammaticService svc = ProgrammaticService.createForSession(ctx.getSession(), logTable.getContainerAdaptation().getHome());
		
		// Loop through all unpublished records, publishing each one in turn
		String predicate = String.format("osd:is-null(%s)", CrmpPaths._IntegrationLog._PublishTimestamp.format());
		RequestResult resultSet = logTable.createRequestResult(predicate);
		try {
			
			Adaptation result = resultSet.nextAdaptation();		// Should this loop be inside the Procedure to minimise commit overhead?
			while (result != null) {
				
				publisher.setNotification(result);
				svc.execute(publisher);
				
				result = resultSet.nextAdaptation();
				
			}
			
		} finally {
			resultSet.close();
		}
		
		// Write a summary to the execution report
		publisher.writeSummary();
		
	}

	/**
	 * Find a configuration table using its path
	 * @param tablePath Path to the table in the CRMP dataset.
	 * @return A reference to the named table
	 */
	private AdaptationTable getConfigTable(Path tablePath) {
		
		Adaptation dataset = Tools.findCrmpDataset();
		return dataset != null ? dataset.getTable(tablePath) : null;
		
	}


	public Integer getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	/**
	 * Inner class that posts a single notification to the integration platform and records the date/time
	 * that it was successfully received. 
	 * 
	 */
	private class Publisher implements Procedure {

		private ScheduledExecutionContext ctx = null;
		private Adaptation notificationRecord = null;
		private HttpHelper httpHelper = null;
		private NotificationMessage notification = new NotificationMessage();
		private int successCount = 0;
		private int tryCount = 0;
		
		/**
		 * Create a new Publisher.
		 * @param ctx The scheduled-task execution context (for accessing the execution report)
		 */
		public Publisher(ScheduledExecutionContext ctx) {
			this.ctx = ctx;
		}
		
		/**
		 * Tell the publisher which log record to publish
		 * @param record A record from the integration log table
		 */
		public void setNotification(Adaptation record) {
			
			// Get the endpoint record & create a REST helper
			SchemaNode fkObjectTable = record.getSchemaNode().getNode(CrmpPaths._IntegrationLog._TableID);
			Adaptation publishSpec = fkObjectTable.getFacetOnTableReference().getLinkedRecord(record);
			SchemaNode fkEndpoint = publishSpec.getSchemaNode().getNode(CrmpPaths._ObjectTable._PublishEndpoint);
			Adaptation endpointSpec = fkEndpoint.getFacetOnTableReference().getLinkedRecord(publishSpec);
			this.httpHelper = new HttpHelper(endpointSpec, socketTimeout);			
			
			// Store the log record
			notificationRecord = record;
			
			// Extract field from the log record to populate a notification object
			notification.setLogIdentifier(record.get_int(CrmpPaths._IntegrationLog._LogId));
			notification.setSystemCode(record.getString(CrmpPaths._IntegrationLog._SystemCode));
			notification.setObjectCode(record.getString(CrmpPaths._IntegrationLog._ObjectCode));
			notification.setObjectId(record.getString(CrmpPaths._IntegrationLog._ObjectId));
			notification.setAction(record.getString(CrmpPaths._IntegrationLog._Action));
			notification.setExtraFields(record.getList(CrmpPaths._IntegrationLog._PublishColumns));
						
		}
		
		/**
		 * Publish a notification to the integration platform and update the publish timestamp on the log record.
		 */
		@Override
		public void execute(ProcedureContext pctx) throws Exception {

			try {

				// Send to the integration platform
				tryCount++;
				httpHelper.sendNotification(notification);
			
				// Switch to all privileges & disable triggers
				boolean allPrivs = pctx.isAllPrivileges();
				boolean triggers = pctx.isTriggerActivation();
				pctx.setAllPrivileges(true);
				pctx.setTriggerActivation(false);
				
				// Update the log record
				ValueContextForUpdate updates = pctx.getContext(notificationRecord.getAdaptationName());
				updates.setValue(new Date(), CrmpPaths._IntegrationLog._PublishTimestamp);
				pctx.doModifyContent(notificationRecord, updates);

				// Restore privileges and triggers
				pctx.setTriggerActivation(triggers);
				pctx.setAllPrivileges(allPrivs);
	
				// Increment the success counter
				successCount++;
				
			} catch (IntegrationException e) {

				String msg = String.format("Failed during processing of log record [%s] - %s",
						notification.getLogIdentifier(), e.getMessage());
				ctx.addExecutionInformation(msg);	
				
			} catch (OperationException | ConstraintViolationException e) {

				String msg = String.format("Failed when updating log record [%s] - %s", 
						notification.getLogIdentifier(), e.getMessage());
				ctx.addExecutionInformation(msg);
				
			}
			
		}
		
		/**
		 * Write a summary of this execution to the execution log
		 */
		public void writeSummary() {
			if (tryCount == successCount) {
				if (successCount == 0) {
					ctx.addExecutionInformation("Nothing to publish");
				} else {
					ctx.addExecutionInformation(String.format("Successfully processed %d log records", successCount));
				}
			} else {
				ctx.addExecutionInformation(String.format("Successfully processed %d of %d log records. %d failures.", 
						successCount, tryCount, tryCount - successCount));
			}
		}
		
	}
	
}
