package se.bjurr.prnfs.admin;

import static com.atlassian.stash.pull.PullRequestAction.OPENED;
import static se.bjurr.prnfs.admin.utils.NotificationBuilder.notificationBuilder;
import static se.bjurr.prnfs.admin.utils.PrnfsTestBuilder.prnfsTestBuilder;

import org.junit.Test;

public class NotificationsStorageTest {
 @Test
 public void testThatANewNotificationCanBeStored() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/")
        .withFieldValue(AdminFormValues.FIELDS.events, OPENED.name()).build()).store().hasNotifications(1)
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/", "0")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "0");
 }

 @Test
 public void testThatANewNotificationCanBeStoredWithWhiteSpaceInFormIdentifier() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/")
        .withFieldValue(AdminFormValues.FIELDS.events, OPENED.name())
        .withFieldValue(AdminFormValues.FIELDS.FORM_IDENTIFIER, " ").build()).store().hasNotifications(1)
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/", "0")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "0");
 }

 @Test
 public void testThatTwoNewNotificationsCanBeStored() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/")
        .withFieldValue(AdminFormValues.FIELDS.events, OPENED.name()).build())
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/?2")
        .withFieldValue(AdminFormValues.FIELDS.events, OPENED.name()).build()).store().hasNotifications(2)
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/", "0")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "0")
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/?2", "1")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "1");
 }

 @Test
 public void testThatTwoNewNotificationsCanBeStoredAndThenOneDeleted() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/")
        .withFieldValue(AdminFormValues.FIELDS.events, OPENED.name()).build())
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/?2")
        .withFieldValue(AdminFormValues.FIELDS.events, OPENED.name()).build()).store().hasNotifications(2)
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/", "0")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "0")
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/?2", "1")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "1").delete("0").hasNotifications(1)
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/?2", "1")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "1") //
    .delete("1") //
    .hasNotifications(0);
 }

 @Test
 public void testThatTwoNewNotificationsCanBeStoredAndThenOneUpdated() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/")
        .withFieldValue(AdminFormValues.FIELDS.events, OPENED.name()).build())
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/?2")
        .withFieldValue(AdminFormValues.FIELDS.events, OPENED.name()).build())
    .store()
    .hasNotifications(2)
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/", "0")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "0")
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/?2", "1")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "1")
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/?2upd")
        .withFieldValue(AdminFormValues.FIELDS.FORM_IDENTIFIER, "1").build())
    .store()
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/", "0")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "0")
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/?2upd", "1")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "1")
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.url, "http://bjurr.se/?upd")
        .withFieldValue(AdminFormValues.FIELDS.FORM_IDENTIFIER, "0").build()).store()
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/?upd", "0")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "0")
    .hasFieldValueAt(AdminFormValues.FIELDS.url, "http://bjurr.se/?2upd", "1")
    .hasNoneEmptyFieldAt(AdminFormValues.FIELDS.FORM_IDENTIFIER, "1");
 }

 @Test
 public void testThatUrlMustBeSet() {
  prnfsTestBuilder().isLoggedInAsAdmin()
    .withNotification(notificationBuilder().withFieldValue(AdminFormValues.FIELDS.events, OPENED.name()).build())
    .store().hasValidationError(AdminFormValues.FIELDS.url, "URL not set");
 }

 @Test
 public void testThatUrlMustBeValid() {
  prnfsTestBuilder()
    .isLoggedInAsAdmin()
    .withNotification(
      notificationBuilder().withFieldValue(AdminFormValues.FIELDS.events, OPENED.name())
        .withFieldValue(AdminFormValues.FIELDS.url, "notcorrect").build()).store()
    .hasValidationError(AdminFormValues.FIELDS.url, "URL not valid!");
 }

 @Test
 public void testThatValuesMustBeSet() {
  prnfsTestBuilder().isLoggedInAsAdmin().withNotification(notificationBuilder().build()).store()
    .hasValidationError(AdminFormValues.FIELDS.url, "URL not set");
 }
}
