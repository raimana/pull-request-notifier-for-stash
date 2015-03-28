package se.bjurr.prnfs.admin;

import static com.atlassian.stash.pull.PullRequestAction.OPENED;
import static se.bjurr.prnfs.admin.utils.NotificationBuilder.notificationBuilder;
import static se.bjurr.prnfs.admin.utils.PrnfsTestBuilder.prnfsTestBuilder;
import static se.bjurr.prnfs.admin.utils.PullRequestEventBuilder.pullRequestEventBuilder;
import static se.bjurr.prnfs.admin.utils.PullRequestRefBuilder.pullRequestRefBuilder;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.prnfs.listener.PrnfsRenderer.PrnfsVariable;

public class PrnfsPullRequestEventListenerTest {
 private static final Logger logger = LoggerFactory.getLogger(PrnfsPullRequestEventListenerTest.class);

 @Test
 public void testThatAUrlWithoutVariablesCanBeInvoked() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue("url", "http://bjurr.se/").withFieldValue("events", OPENED.name()).build())
    .store().trigger(pullRequestEventBuilder().build()).invokedUrl("http://bjurr.se/");
 }

 @Test
 public void testThatAUrlWithVariablesCanBeInvokedFrom() {
  for (final PrnfsVariable prnfsVariable : PrnfsVariable.values()) {
   if (prnfsVariable.name().contains("_TO_")) {
    continue;
   }
   logger.info(prnfsVariable.name());
   prnfsTestBuilder()
     .isLoggedInAsAdmin()
     .withNotification(
       notificationBuilder().withFieldValue("url", "http://bjurr.se/${" + prnfsVariable.name() + "}")
         .withFieldValue("events", OPENED.name()).build())
     .store()
     .trigger(
       pullRequestEventBuilder() //
         .withFromRef(
           pullRequestRefBuilder().withHash("10").withId("10").withProjectId(10).withProjectKey("10")
             .withRepositoryId(10).withRepositoryName("10").withRepositorySlug("10")) //
         .withId(10L).build()).invokedUrl("http://bjurr.se/10");
  }
 }

 @Test
 public void testThatAUrlWithVariablesCanBeInvokedTo() {
  for (final PrnfsVariable prnfsVariable : PrnfsVariable.values()) {
   if (prnfsVariable.name().contains("_FROM_")) {
    continue;
   }
   prnfsTestBuilder()
     .isLoggedInAsAdmin()
     .withNotification(
       notificationBuilder().withFieldValue("url", "http://bjurr.se/${" + prnfsVariable.name() + "}")
         .withFieldValue("events", OPENED.name()).build())
     .store()
     .trigger(
       pullRequestEventBuilder() //
         .withToRef(
           pullRequestRefBuilder().withHash("10").withId("10").withProjectId(10).withProjectKey("10")
             .withRepositoryId(10).withRepositoryName("10").withRepositorySlug("10")) //
         .withId(10L).build()).invokedUrl("http://bjurr.se/10");
  }
 }
}