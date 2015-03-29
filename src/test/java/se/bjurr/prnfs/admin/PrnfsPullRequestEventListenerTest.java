package se.bjurr.prnfs.admin;

import static com.atlassian.stash.pull.PullRequestAction.APPROVED;
import static com.atlassian.stash.pull.PullRequestAction.MERGED;
import static com.atlassian.stash.pull.PullRequestAction.OPENED;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Resources.getResource;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Thread.sleep;
import static java.util.Collections.sort;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.bjurr.prnfs.admin.utils.NotificationBuilder.notificationBuilder;
import static se.bjurr.prnfs.admin.utils.PrnfsTestBuilder.prnfsTestBuilder;
import static se.bjurr.prnfs.admin.utils.PullRequestEventBuilder.pullRequestEventBuilder;
import static se.bjurr.prnfs.admin.utils.PullRequestRefBuilder.pullRequestRefBuilder;
import static se.bjurr.prnfs.listener.PrnfsPullRequestEventListener.dublicateEventBug;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.prnfs.listener.PrnfsRenderer.PrnfsVariable;

import com.google.common.io.Resources;

public class PrnfsPullRequestEventListenerTest {
 private static final Logger logger = LoggerFactory.getLogger(PrnfsPullRequestEventListenerTest.class);

 @Test
 public void testThatAUrlIsOnlyInvokedForConfiguredEvents() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue("url", "http://bjurr.se/").withFieldValue("events", OPENED.name()).build())
    .store().trigger(pullRequestEventBuilder() //
      .withToRef(pullRequestRefBuilder()) //
      .withId(10L).withPullRequestAction(MERGED).build()).invokedNoUrl();
 }

 @Test
 public void testThatAUrlWithoutVariablesCanBeInvoked() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue("url", "http://bjurr.se/").withFieldValue("events", OPENED.name()).build())
    .store().trigger(pullRequestEventBuilder().withPullRequestAction(OPENED).build()).invokedUrl("http://bjurr.se/");
 }

 @Test
 public void testThatAUrlWithVariablesCanBeInvokedFrom() {
  for (final PrnfsVariable prnfsVariable : PrnfsVariable.values()) {
   if (prnfsVariable.name().contains("_TO_")) {
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
         .withFromRef(
           pullRequestRefBuilder().withHash("10").withId("10").withProjectId(10).withProjectKey("10")
             .withRepositoryId(10).withRepositoryName("10").withRepositorySlug("10")) //
         .withId(10L).withPullRequestAction(OPENED).build()).invokedUrl("http://bjurr.se/10");
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
         .withId(10L).withPullRequestAction(OPENED).build()).invokedUrl("http://bjurr.se/10");
  }
 }

 @Test
 public void testThatAUrlCanHaveSeveralVariables() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder()
        .withFieldValue(
          "url",
          "http://bjurr.se/?PULL_REQUEST_FROM_HASH=${PULL_REQUEST_FROM_HASH}&PULL_REQUEST_TO_HASH=${PULL_REQUEST_TO_HASH}&PULL_REQUEST_FROM_REPO_SLUG=${PULL_REQUEST_FROM_REPO_SLUG}&PULL_REQUEST_TO_REPO_SLUG=${PULL_REQUEST_TO_REPO_SLUG}")
        .withFieldValue("events", OPENED.name()).build())
    .store()
    .trigger(pullRequestEventBuilder() //
      .withFromRef(pullRequestRefBuilder().withHash("cde456").withRepositorySlug("fromslug")) //
      .withToRef(pullRequestRefBuilder().withHash("asd123").withRepositorySlug("toslug")) //
      .withId(10L).withPullRequestAction(OPENED).build())
    .invokedUrl(
      "http://bjurr.se/?PULL_REQUEST_FROM_HASH=cde456&PULL_REQUEST_TO_HASH=asd123&PULL_REQUEST_FROM_REPO_SLUG=fromslug&PULL_REQUEST_TO_REPO_SLUG=toslug");
 }

 @Test
 public void testThatDuplicateEventsFiredInStashAreIgnored() throws InterruptedException {
  assertEquals(FALSE, dublicateEventBug(pullRequestEventBuilder().withId(100L).withPullRequestAction(APPROVED).build()));
  assertEquals(TRUE, dublicateEventBug(pullRequestEventBuilder().withId(100L).withPullRequestAction(APPROVED).build()));
  assertEquals(FALSE, dublicateEventBug(pullRequestEventBuilder().withId(100L).withPullRequestAction(OPENED).build()));
  assertEquals(FALSE, dublicateEventBug(pullRequestEventBuilder().withId(101L).withPullRequestAction(APPROVED).build()));
  assertEquals(TRUE, dublicateEventBug(pullRequestEventBuilder().withId(100L).withPullRequestAction(OPENED).build()));
  assertEquals(TRUE, dublicateEventBug(pullRequestEventBuilder().withId(101L).withPullRequestAction(APPROVED).build()));
  sleep(5);
  assertEquals(TRUE, dublicateEventBug(pullRequestEventBuilder().withId(100L).withPullRequestAction(APPROVED).build()));
  sleep(100);
  assertEquals(FALSE, dublicateEventBug(pullRequestEventBuilder().withId(100L).withPullRequestAction(APPROVED).build()));
 }

 @Test
 public void testThatMultipleUrlsCanBeInvoked() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue("url", "http://merged.se/").withFieldValue("events", MERGED.name()).build())
    .withNotification(
      notificationBuilder().withFieldValue("url", "http://opened.se/").withFieldValue("events", OPENED.name()).build())
    .store().trigger(pullRequestEventBuilder() //
      .withToRef(pullRequestRefBuilder()) //
      .withId(10L).withPullRequestAction(MERGED).build()).invokedOnlyUrl("http://merged.se/");
 }

 @Test
 public void testThatVariablesAreImplementedForBothFromAndTo() {
  final List<String> from = newArrayList();
  final List<String> to = newArrayList();
  for (final PrnfsVariable prnfsVariable : PrnfsVariable.values()) {
   logger.info(prnfsVariable.name());
   if (prnfsVariable.name().contains("_FROM_")) {
    from.add(prnfsVariable.name());
   } else if (prnfsVariable.name().contains("_TO_")) {
    to.add(prnfsVariable.name());
   }
  }
  sort(from);
  sort(to);
  assertEquals(on(" ").join(from) + " != " + on(" ").join(to), from.size(), to.size());
 }

 @Test
 public void testThatVariablesAreMentionedInAdminGUI() throws IOException {
  final URL resource = getResource("admin.vm");
  final String adminVmContent = Resources.toString(resource, UTF_8);
  for (final PrnfsVariable prnfsVariable : PrnfsVariable.values()) {
   assertTrue(prnfsVariable.name() + " in " + resource.toString(), adminVmContent.contains(prnfsVariable.name()));
  }
 }
}
