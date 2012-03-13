package org.ihc.hwcir.drools;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
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
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.workitem.wsht.CommandBasedWSHumanTaskHandler;

@Singleton
public class DroolsPersistenceImpl implements DroolsPersistenceLocal, DroolsPersistenceRemote {

    private final Logger logger = Logger.getLogger(DroolsPersistenceImpl.class);

    private EntityManagerFactory emf;
    private StatefulKnowledgeSession ksession;

    @PostConstruct
    void post() {
        this.emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
    }

    @Override
    public int fireAllRules(List<Object> facts) {

        return this.ksession.fireAllRules();
    }

    @Override
    public int startProcess(String processId) {
        ProcessInstance pi = this.ksession.startProcess(processId);
        this.ksession.fireAllRules();

        return pi.getState();
    }

    @Override
    public void loadSession(int sessionId, String[] sessionStuff) {

        final KnowledgeSessionConfiguration conf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        Environment env = createEnvironment(emf);

        this.ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, this.createKnowledgeBase(sessionStuff), conf,
                env);
    }

    @Override
    public int findProcessState(long processId) {

        if (this.ksession == null) {
            return -1;
        }

        ProcessInstance pi = this.ksession.getProcessInstance(processId);

        return pi.getState();
    }

    @Override
    public int initiateSession(String[] sessionStuff) {
        this.ksession = this.createKnowledgeSession(this.emf, sessionStuff);

        this.ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new CommandBasedWSHumanTaskHandler(this.ksession));

        return this.ksession.getId();
    }

    @Override
    public long destroyProcess(long processId) {

        try {
            this.ksession.abortProcessInstance(processId);
            ProcessInstance pi = this.ksession.getProcessInstance(processId);

            if (pi != null) {
                return -1L;
            }
            else {
                return 3L;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private StatefulKnowledgeSession createKnowledgeSession(EntityManagerFactory emf, String[] sessionStuff) {

        final KnowledgeSessionConfiguration conf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();

        Environment env = createEnvironment(emf);
        return JPAKnowledgeService.newStatefulKnowledgeSession(this.createKnowledgeBase(sessionStuff), conf, env);
    }

    private static Environment createEnvironment(EntityManagerFactory emf) {
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        return env;
    }

    private KnowledgeBase createKnowledgeBase(String[] sessionStuff) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        // TODO: Add rules/process definitions here...
        kbuilder.add(ResourceFactory.newClassPathResource("processes/HelloHT.bpmn", DroolsPersistenceImpl.class),
                ResourceType.BPMN2);

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
}