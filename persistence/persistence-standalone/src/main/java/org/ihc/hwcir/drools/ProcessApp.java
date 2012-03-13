package org.ihc.hwcir.drools;

import java.sql.SQLException;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

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
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * 
 */
public final class ProcessApp {

    private static final String PROCESS_NAME = "processes/HelloHT.bpmn";
    private static final String USER_NAME = "darin";

    private static Logger logger = Logger.getLogger(ProcessApp.class);
    private StatefulKnowledgeSession ksession;
    private Server h2Server;

    /**
     * 
     */
    private ProcessApp() {

        try {
            UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
            PoolingDataSource ds1 = new PoolingDataSource();
            ds1.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
            ds1.setUniqueName("jdbc/jbpm-ds");
            ds1.setMaxPoolSize(5);
            ds1.setAllowLocalTransactions(true);
            ds1.getDriverProperties().setProperty("driverClassName", "org.h2.Driver");
            ds1.getDriverProperties().setProperty("url", "jdbc:h2:tcp://localhost/~/taskdb");
            ds1.getDriverProperties().setProperty("user", "sa");
            ds1.getDriverProperties().setProperty("password", "sasa");

            ds1.init();

            ut.begin();

            System.setProperty("jbpm.usergroup.callback", "org.jbpm.task.service.DefaultUserGroupCallbackImpl");

            EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");

            this.ksession = this.createKnowledgeSession(emf);

            TaskService taskService = this.getTaskService(ksession, null, emf);

            ksession.startProcess("HelloHT");
            this.ksession.fireAllRules();

            List<TaskSummary> taskSummaries = taskService.getTasksAssignedAsPotentialOwner(USER_NAME, "en-UK");

            TaskSummary taskSummary = taskSummaries.get(0);
            logger.debug(USER_NAME + " is performing the task: " + taskSummary.getId());
            taskService.start(taskSummary.getId(), USER_NAME);
            logger.debug(USER_NAME + " is completing the task: " + taskSummary.getId());
            taskService.complete(taskSummary.getId(), USER_NAME, null);

            ut.commit();

            this.ksession.dispose();
            taskService.disconnect();
            ds1.close();

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (NotSupportedException e) {
            e.printStackTrace();
        } catch (HeuristicRollbackException e) {
            e.printStackTrace();
        } catch (HeuristicMixedException e) {
            e.printStackTrace();
        } catch (RollbackException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

        Environment env = createEnvironment(emf);
        return JPAKnowledgeService.newStatefulKnowledgeSession(this.createKnowledgeBase(), conf, env);
    }

    /**
     * 
     * @return
     */
    @SuppressWarnings("unused")
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
                @SuppressWarnings("unused")
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
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        return env;
    }

    /**
     * 
     * Use this if you don't want to run a stand-alone instance of H2.
     * 
     * @param deleteOldFiles
     */
    @SuppressWarnings("unused")
    private void startH2Database(boolean deleteOldFiles) {

        try {
            if (deleteOldFiles) {
                DeleteDbFiles.execute("", "JPADroolsFlow", true);
            }
            this.h2Server = Server.createTcpServer(new String[0]);
            this.h2Server.start();
        } catch (SQLException e) {
            throw new RuntimeException("can't start h2 server db", e);
        }
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