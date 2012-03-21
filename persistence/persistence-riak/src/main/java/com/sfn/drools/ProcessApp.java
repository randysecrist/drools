package com.sfn.drools;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.persistence.PersistenceContext;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.TransactionManager;
import org.drools.persistence.TransactionSynchronization;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;

/**
 * 
 */
public final class ProcessApp {

    private static final String PROCESS_NAME = "processes/HelloHT.bpmn";
    private static final String USER_NAME = "darin";

    private static final Logger logger = Logger.getLogger(ProcessApp.class);
    private StatefulKnowledgeSession ksession;

    /**
     * 
     */
    private ProcessApp() {

        try {
            // Init Data Source (TBD)

            // Do we need to do this?  What is it, what does it cause?
            System.setProperty("jbpm.usergroup.callback", "org.jbpm.task.service.DefaultUserGroupCallbackImpl");

            // (JPA - Entity Manager)
            final EntityManager em = new RiakEntityManager();

            // (JPA - Entity Manager Factory)
            // Using a jpa name:
            //EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
            EntityManagerFactory emf = new EntityManagerFactory() {
              public boolean isOpen() { logger.info("called"); return true; }
              public void close() { logger.info("called"); }
              public EntityManager createEntityManager() { logger.info("called"); return em; }
              public EntityManager createEntityManager(Map m) { logger.info("called"); return em; }
            };


            // Create a Knowledge Session
            this.ksession = this.createKnowledgeSession(emf);
            this.ksession = this.createKnowledgeSession();

            // Get a Task Service
            TaskService taskService = this.getTaskService(ksession, null, emf);

            // Start a Process & Fire Rules
            ksession.startProcess("HelloHT");
            this.ksession.fireAllRules();

            // Get a list of tasks
            // (why en-UK?)  can this be diff?
            List<TaskSummary> taskSummaries = taskService.getTasksAssignedAsPotentialOwner(USER_NAME, "en-UK");

            // Assert at least 1 summary & print stuff
            /*
            TaskSummary taskSummary = taskSummaries.get(0);
            logger.debug(USER_NAME + " is performing the task: " + taskSummary.getId());
            taskService.start(taskSummary.getId(), USER_NAME);
            logger.debug(USER_NAME + " is completing the task: " + taskSummary.getId());
            taskService.complete(taskSummary.getId(), USER_NAME, null);
            */

            // Cleanup  (include DB cleanup - TBD - (call M/R Func))
            this.ksession.dispose();
            taskService.disconnect();
            
            logger.info("All Done!");
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        new ProcessApp();
    }

    /**
     * Overloaded
     * 
     * @param emf
     * @return
     */
    private StatefulKnowledgeSession createKnowledgeSession(EntityManagerFactory emf) {
        final KnowledgeSessionConfiguration conf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        KnowledgeBase kb = this.createKnowledgeBase();
        Environment env = createEnvironment(emf);
        return JPAKnowledgeService.newStatefulKnowledgeSession(kb, conf, env);
    }

    /**
     * 
     * @return
     */
    private StatefulKnowledgeSession createKnowledgeSession() {
        final KnowledgeBase kbase = createKnowledgeBase();
        return kbase.newStatefulKnowledgeSession();
    }

    /**
     * 
     * @return
     */
    private KnowledgeBase createKnowledgeBase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource(PROCESS_NAME, ProcessApp.class), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            if (kbuilder.getErrors().size() > 0) {
                boolean errors = false;
                for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                    logger.warn(error.toString());
                    errors = true;
                }
            }
        }
        return kbuilder.newKnowledgeBase();
    }

    /**
     * 
     * @param emf
     * @return
     */
    private static Environment createEnvironment(EntityManagerFactory emf) {
        final Transaction t = new Transaction() {
          public void setRollbackOnly() { logger.info("called"); }
          public void rollback() { logger.info("called"); }
          public void registerSynchronization(Synchronization s) { logger.info("called"); }
          public int getStatus() { logger.info("called"); return 0; }
          public boolean enlistResource(XAResource resource) { logger.info("called"); return false; }
          public boolean enlistResource(XAResource resource, int i) { logger.info("called"); return false; }
          public boolean delistResource(XAResource resource, int i) { logger.info("called"); return false; }
          public void commit() { logger.info("called"); }
        };

        TransactionManager tm = new TransactionManager() {
          public Transaction suspend() { logger.info("called"); return null; }
          public void setTransactionTimeout(int timeout) { logger.info("called"); }
          public void setRollbackOnly() { logger.info("called"); }
          public void rollback() { logger.info("called"); }
          public void rollback(boolean b) { logger.info("called"); }
          public void resume(Transaction t) { logger.info("called"); }
          public Transaction getTransaction() { logger.info("called"); return t; }
          public int getStatus() { logger.info("called"); return 0; }
          public boolean begin() { logger.info("called"); return true; }
          public void commit() { logger.info("called"); }
          public void commit(boolean b) { logger.info("called"); }
          public void registerTransactionSynchronization(TransactionSynchronization t_sync) {
        	  logger.info("called");
          }
        };

        PersistenceContextManager pcm = new PersistenceContextManager() {
          public void dispose() { logger.info("called"); }
          public void endCommandScopedEntityManager() { logger.info("called"); }
          public void beginCommandScopedEntityManager() { logger.info("called"); }
          PersistenceContext pc = new RiakPersistenceContext();
          public PersistenceContext getCommandScopedPersistenceContext() { logger.info("called"); return pc; }
          public PersistenceContext getApplicationScopedPersistenceContext() { logger.info("called"); return pc; }
        };

        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        //env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        env.set(EnvironmentName.TRANSACTION_MANAGER, tm);
        env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, pcm);
        return env;
    }

    /**
     * 
     * @param ksession
     * @param taskService
     * @param emf
     * @return
     */
    private TaskService getTaskService(StatefulKnowledgeSession ksession, org.jbpm.task.service.TaskService taskService,
            EntityManagerFactory emf) {

        if (taskService == null) {
            taskService = new org.jbpm.task.service.TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        }

        TaskServiceSession taskServiceSession = taskService.createSession();
        taskServiceSession.setTransactionType("local-JTA");
        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(new LocalTaskService(taskServiceSession), ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);
        return new LocalTaskService(taskServiceSession);
    }
}