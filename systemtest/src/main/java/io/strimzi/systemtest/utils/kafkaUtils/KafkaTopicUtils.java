/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.systemtest.utils.kafkaUtils;

import io.strimzi.api.kafka.Crds;
import io.strimzi.systemtest.Constants;
import io.strimzi.test.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.strimzi.test.k8s.KubeClusterResource.kubeClient;

public class KafkaTopicUtils {

    private static final Logger LOGGER = LogManager.getLogger(KafkaTopicUtils.class);

    private KafkaTopicUtils() {}

    /**
     * Method which return UID for specific topic
     * @param topicName topic name
     * @return topic UID
     */
    public static String topicSnapshot(String topicName) {
        return Crds.topicOperation(kubeClient().getClient()).inNamespace(kubeClient().getNamespace()).withName(topicName).get().getMetadata().getUid();
    }

    /**
     * Method which wait until topic has rolled form one generation to another.
     * @param topicName topic name
     * @param topicUid topic UID
     * @return topic new UID
     */
    public static String waitTopicHasRolled(String topicName, String topicUid) {
        TestUtils.waitFor("Topic " + topicName + " has rolled", Constants.GLOBAL_POLL_INTERVAL, Constants.GLOBAL_TIMEOUT,
            () -> !topicUid.equals(topicSnapshot(topicName)));
        return topicSnapshot(topicName);
    }

    public static void waitForKafkaTopicCreation(String topicName) {
        LOGGER.info("Waiting for Kafka topic creation {}", topicName);
        TestUtils.waitFor("Waits for Kafka topic creation " + topicName, Constants.POLL_INTERVAL_FOR_RESOURCE_READINESS, Constants.TIMEOUT_FOR_RESOURCE_READINESS, () ->
            Crds.topicOperation(kubeClient().getClient()).inNamespace(kubeClient().getNamespace()).withName(topicName).get().getStatus().getConditions().get(0).getType().equals("Ready")
        );
    }

    public static void waitForKafkaTopicDeletion(String topicName) {
        LOGGER.info("Waiting for Kafka topic deletion {}", topicName);
        TestUtils.waitFor("Waits for Kafka topic deletion " + topicName, Constants.POLL_INTERVAL_FOR_RESOURCE_READINESS, Constants.TIMEOUT_FOR_RESOURCE_READINESS, () ->
            Crds.topicOperation(kubeClient().getClient()).inNamespace(kubeClient().getNamespace()).withName(topicName).get() == null
        );
    }

    public static void waitForKafkaTopicPartitionChange(String topicName, int partitions) {
        LOGGER.info("Waiting for Kafka topic change {}", topicName);
        TestUtils.waitFor("Waits for Kafka topic change " + topicName, Constants.POLL_INTERVAL_FOR_RESOURCE_READINESS,
            Constants.TIMEOUT_FOR_RESOURCE_READINESS, () ->
                Crds.topicOperation(kubeClient().getClient()).inNamespace(kubeClient().getNamespace())
                    .withName(topicName).get().getSpec().getPartitions() == partitions
        );
    }
}
