package org.jboss.as.test.integration.messaging.mgmt;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ContainerResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.test.integration.common.jms.JMSOperations;
import org.jboss.as.test.integration.common.jms.JMSOperationsProvider;
import org.jboss.dmr.ModelNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunAsClient
@RunWith(Arquillian.class)
public class JmsBridgeStatisticsTestCase {

    private static final String ABORTED_MESSAGE_COUNT = "aborted-message-count";
    private static final String MESSAGE_COUNT = "message-count";

    private String jmsBridgeName = this.getClass().getSimpleName();

    @ContainerResource
    private ManagementClient managementClient;

    @Before
    public void createJmsBridge() {
        JMSOperations adminSupport = JMSOperationsProvider.getInstance(managementClient.getControllerClient());

        adminSupport.addJmsBridge(jmsBridgeName, new ModelNode());
    }

    @After
    public void removeJmsBridge() {
        JMSOperations adminSupport = JMSOperationsProvider.getInstance(managementClient.getControllerClient());

        adminSupport.removeJmsBridge(jmsBridgeName);
    }

    @Test
    public void testMessageCountRead() throws IOException {
        final ModelNode result = readJmsBridgeAttribute(managementClient, jmsBridgeName, MESSAGE_COUNT);
        verifyResultWasSuccessAndHasDefinedValue(result);
    }

    @Test
    public void testAbortedMessageCountRead() throws IOException {
        final ModelNode result = readJmsBridgeAttribute(managementClient, jmsBridgeName, ABORTED_MESSAGE_COUNT);
        verifyResultWasSuccessAndHasDefinedValue(result);
    }

    private static void verifyResultWasSuccessAndHasDefinedValue(final ModelNode result) {

    }

    private static ModelNode readJmsBridgeAttribute(final ManagementClient client, final String jmsBridgeName,
                                                    final String attributeName) throws IOException {
        final ModelNode jmsBridgeAddress =  getJmsBridgeAddress(jmsBridgeName);
        final ModelNode readOperation = Operations.createReadAttributeOperation(jmsBridgeAddress, attributeName);
        return client.getControllerClient().execute(readOperation);
    }

    private static ModelNode getJmsBridgeAddress(final String jmsBridgeName) {
        ModelNode address = new ModelNode();
        address.add("subsystem", "messaging-activemq");
        address.add("jms-bridge", jmsBridgeName);
        return address;
    }

}
