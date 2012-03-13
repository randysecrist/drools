package org.ihc.hwcir.drools;

import java.util.Collection;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * Unit test for simple App.
 */
public class ProcessAppTest extends TestCase
{
    private final StatefulKnowledgeSession kSession;
    private final KnowledgeBase kBase;

    public ProcessAppTest(String testName) {
        super(testName);
        kBase = createKnowledgeBase();
        kSession = createStatefulSession();
    }

    public void testSimple() {
        KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(kSession, "testSimple");
        kSession.fireAllRules();
        assertTrue(true);
        logger.close();
    }

    public void testNewKSession() {
        StatefulKnowledgeSession kSession2 = kBase.newStatefulKnowledgeSession();
        KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(kSession, "testSimple2");
        kSession2.fireAllRules();
        assertTrue(true);
        logger.close();
    }

    private KnowledgeBase createKnowledgeBase() {
        KnowledgeBase kBase;
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newClassPathResource("SampleRule.drl", ProcessApp.class), ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors().toString());
            throw new RuntimeException("Unable to compile \"SampleRule.drl\".");
        }

        final Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        kBase = KnowledgeBaseFactory.newKnowledgeBase();
        kBase.addKnowledgePackages(pkgs);
        return kBase;
    }

    private StatefulKnowledgeSession createStatefulSession() {
        StatefulKnowledgeSession kSession = kBase.newStatefulKnowledgeSession();
        kSession.addEventListener(new DebugAgendaEventListener());
        kSession.addEventListener(new DebugWorkingMemoryEventListener());
        return kSession;
    }
}