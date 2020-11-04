package com.mssimulator.service;

import com.mssimulator.domain.Log;
import com.mssimulator.domain.LogType;
import com.mssimulator.domain.UnsuccessfulLoginEvent;
import com.mssimulator.domain.state.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.util.Date;
import java.util.Random;

@Service
public class SimulatorService {
    private State state;
    private Random random;
    private final KieContainer kieContainer;
    private KieSession kieSession;
    private static final File LOG_FILE = new File(new File(".").getAbsoluteFile().getParentFile().getAbsoluteFile().getParentFile(), "/simulator-logs.txt");

    public Random getRandom() {
        return random;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void run() {
        try {
            if (this.state instanceof NormalState) {
                Thread.sleep(10000); // insert event less often, so that rules don't get triggered
            } else if (this.state instanceof UnderAttackState) {
                Thread.sleep(5000); // insert event more more often, so that rules do get triggered
            }
            UnsuccessfulLoginEvent ule = this.state.getUnsuccessfulLoginEvent();

            this.kieSession.getAgenda().getAgendaGroup("alarm").setFocus();

            this.kieSession.insert(ule);
            Log log = this.generateLogOfRandomType(this.state.getClass());
            this.kieSession.insert(log);
            this.kieSession.fireAllRules();
            System.out.println(this.kieSession.getFactCount());
            if (ule.getUsernameRuleTriggered()) {
                Log logUsername = new Log(LogType.ERROR, "Unsuccessful login with username rule triggered!");
                logUsername.setSourceName("Alarm #1");
                writeToFile(LOG_FILE.getAbsolutePath(), logUsername.toString());
                System.out.println("Unsuccessful login with username rule triggered!");
            } else if (ule.getIpRuleTriggered()) {
                Log logIp = new Log(LogType.ERROR, "Unsuccessful login with IP rule triggered!");
                logIp.setSourceName("Alarm #2");
                writeToFile(LOG_FILE.getAbsolutePath(), logIp.toString());
                System.out.println("Unsuccessful login with IP rule triggered!");
            }
            Thread.sleep(2003);
            if (log.getErrorLogRuleTriggered()) {
                Log logErrorRuleTriggered = new Log(LogType.ERROR, "Log of type ERROR rule triggered!");
                logErrorRuleTriggered.setSourceName("Alarm #3");
                writeToFile(LOG_FILE.getAbsolutePath(), logErrorRuleTriggered.toString());
                System.out.println("Log of type ERROR rule triggered!");
            } else {
                writeToFile(LOG_FILE.getAbsolutePath(), log.toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public UnsuccessfulLoginEvent generateEvent() {
        UnsuccessfulLoginEvent ule = new UnsuccessfulLoginEvent();
        ule.setExecutionTime(new Date());
        if (this.random.nextInt(2) == 0) {
            ule.setUsername("user");
        } else {
            ule.setIp("192.168.0.1"); // some random ip
        }
        return ule;
    }

    @Autowired
    public SimulatorService(KieContainer kieContainer) {
        super();
        this.kieContainer = kieContainer;
        this.random = new Random();
    }

    @PostConstruct
    private void initializeSession() {
        KnowledgeBuilderConfiguration kbc = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        kbc.setProperty("drools.dialect.mvel.strict", "false");
        java.lang.System.setProperty("drools.dialect.mvel.strict", "false");

        KnowledgeBuilder kb = KnowledgeBuilderFactory.newKnowledgeBuilder(kbc);

        this.kieSession = this.kieContainer.newKieSession();
        this.kieSession.getAgenda().getAgendaGroup("alarm").setFocus();
    }

    @PreDestroy
    private void disposeSession() {
        this.kieSession.dispose();
    }

    public void randomizeState() {
        if (this.random.nextInt(2) == 0) {
            this.setState(new NormalState());
        } else {
            this.setState(new UnderAttackState());
        }
    }

    public void executeSimulator() throws Exception {
        while (true) {
            UnsuccessfulLoginEvent ule = new UnsuccessfulLoginEvent("user1"); // TODO: change to random
            ule.setIp(String.valueOf(this.random.nextInt(20))); // so that the ip rule doesn't get triggered
            this.setState(new NormalState(ule)); // TODO: change to random
            this.run();
        }
    }

    /**
     * If in NormalState, either an INFO or a WARN log will be generated.
     * If in UnderAttackState, an ERROR log will be generated.
     * @param stateType possible values: NormalState.class, UnderAttackState.class
     * @return generated log
     */
    public Log generateLogOfRandomType(Class<?> stateType) {
        Log l = new Log();
        int rndm = 0;
        if (stateType == NormalState.class) {
            rndm = this.random.nextInt(4);
            switch (rndm) {
                case 0:
                    l.setType(LogType.INFO);
                    l.setMessage("This is an informative message");
                    break;
                case 1:
                    l.setType(LogType.WARN);
                    l.setMessage("Warning, things are getting serious...");
                    break;
                case 2:
                    l.setType(LogType.TRACE);
                    l.setMessage("Tracing something somewhere");
                    break;
                case 3:
                    l.setType(LogType.DEBUG);
                    l.setMessage("Debugging saves kittens - do it");
                    break;
            }
        } else if (stateType == UnderAttackState.class) {
            rndm = this.random.nextInt(2);
            switch (rndm) {
                case 0:
                    l.setType(LogType.ERROR);
                    break;
                case 1:
                    l.setType(LogType.FATAL);
                    l.setMessage("A fatality has been detected!");
                    break;
            }
        }
        l.setSourceName("App #" + this.random.nextInt(10));
        return l;
    }

    public void writeToFile(String filePath, String text) {
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            out.println(text);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
