package se.bjurr.prnfs.settings;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

import java.net.URL;
import java.util.List;

import com.atlassian.stash.pull.PullRequestAction;
import com.google.common.base.Optional;

public class PrnfsNotification {
 private final String password;
 private final List<PullRequestAction> triggers;
 private final String url;
 private final String user;
 private final String filterString;
 private final String filterRegexp;

 public PrnfsNotification(List<PullRequestAction> triggers, String url, String user, String password,
   String filterString, String filterRegexp) throws ValidationException {
  this.password = nullToEmpty(password).trim();
  if (nullToEmpty(url).trim().isEmpty()) {
   throw new ValidationException("url", "URL not set!");
  }
  try {
   new URL(url);
  } catch (final Exception e) {
   throw new ValidationException("url", "URL not valid!");
  }
  this.url = url;
  this.user = nullToEmpty(user).trim();
  this.triggers = checkNotNull(triggers);
  this.filterString = filterString;
  this.filterRegexp = filterRegexp;
 }

 public Optional<String> getPassword() {
  return fromNullable(password);
 }

 public Optional<String> getFilterRegexp() {
  return fromNullable(filterRegexp);
 }

 public Optional<String> getFilterString() {
  return fromNullable(filterString);
 }

 public List<PullRequestAction> getTriggers() {
  return triggers;
 }

 public String getUrl() {
  return url;
 }

 public Optional<String> getUser() {
  return fromNullable(user);
 }
}
