package com.adaptris.core.management.vcs;

public class RevisionHistoryItem {
  
  private String revision;
  
  private String comment;
  
  public RevisionHistoryItem() {
  }
  
  public RevisionHistoryItem(String revision, String comments) {
    this.setRevision(revision);
    this.setComment(comments);
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

}
