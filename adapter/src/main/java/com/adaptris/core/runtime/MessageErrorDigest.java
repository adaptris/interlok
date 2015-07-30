package com.adaptris.core.runtime;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * MessageErrorDigest that contains a fixed size list of the last n errors.
 *
 * <p>
 * Note that while this list implements the {@link List} interface, the optional operations {@link List#add(int, Object)},
 * {@link List#addAll(int, Collection)} and {@link List#set(int, Object)} will throw an {@link UnsupportedOperationException}.
 * </p>
 *
 * @author lchan
 *
 */
public class MessageErrorDigest extends AbstractCollection<MessageDigestErrorEntry> implements Serializable,
    List<MessageDigestErrorEntry> {

  /**
   * Version ID
   */
  private static final long serialVersionUID = 2013081401L;

  /**
   * Default max messages = 100
   */
  public static final int DEFAULT_MAX_MESSAGES = 100;

  /**
   * Configurable maximum number of messages.
   */
  private int maxMessages;

  /**
   * Internal list of AdaptrisMessageError's.
   */
  private List<MessageDigestErrorEntry> errorMessages;

  public MessageErrorDigest() {
    maxMessages = DEFAULT_MAX_MESSAGES;
    errorMessages = Collections.synchronizedList(new ArrayList<MessageDigestErrorEntry>(DEFAULT_MAX_MESSAGES));
  }

  /**
   * Return a new MessageErrorDigest which contains this collection of {@link MessageDigestErrorEntry} objects.
   * <p>
   * The max size of the list is set to be {@link #DEFAULT_MAX_MESSAGES}
   * </p>
   * 
   * @param original the original collection of errors.
   */
  public MessageErrorDigest(Collection<MessageDigestErrorEntry> original) {
    this(DEFAULT_MAX_MESSAGES, original);
  }

  /**
   * Return a new MessageErrorDigest which contains this collection of {@link MessageDigestErrorEntry} objects.
   * 
   * @param size the size of the new list.
   * @param original the original collection of errors.
   */
  public MessageErrorDigest(int size, Collection<MessageDigestErrorEntry> original) {
    this();
    setMaxMessages(size);
    addAll(original);
  }

  /**
   * Returns a new MessageErrorDigest which contains a view of the portion of this list between the specified <tt>fromIndex</tt>,
   * inclusive, the end of the list exclusive.
   * <p>
   * The max size of the list is set to be {@link #DEFAULT_MAX_MESSAGES}
   * </p>
   *
   * @param digest the original digest
   * @param fromIndex low endpoint (inclusive) of the subList
   * @see #MessageErrorDigest(int, MessageErrorDigest, int)
   * @see #subList(int, int)
   */
  public MessageErrorDigest(MessageErrorDigest digest, int fromIndex) {
    this(DEFAULT_MAX_MESSAGES, digest, fromIndex);
  }

  /**
   * Returns a new MessageErrorDigest which contains a view of the portion of this list between the specified <tt>fromIndex</tt>,
   * inclusive, the end of the list exclusive.
   *
   * @param size the max size of the new list.
   * @param digest the original digest
   * @param fromIndex low endpoint (inclusive) of the subList
   * @see #subList(int, int)
   */
  public MessageErrorDigest(int size, MessageErrorDigest digest, int fromIndex) {
    this(size, digest.subList(fromIndex, digest.size()));
  }

  /**
   * Returns a new MessageErrorDigest which contains a view of the portion of this list between the specified <tt>fromIndex</tt>,
   * inclusive, and <tt>toIndex</tt>, exclusive.
   *
   * @param digest the original
   * @param fromIndex low endpoint (inclusive) of the subList
   * @param toIndex high endpoint (exclusive) of the subList
   * @see #subList(int, int)
   * @see #MessageErrorDigest(int, MessageErrorDigest, int, int)
   */
  public MessageErrorDigest(MessageErrorDigest digest, int fromIndex, int toIndex) {
    this(DEFAULT_MAX_MESSAGES, digest, fromIndex, toIndex);
  }

  /**
   * Returns a new MessageErrorDigest which contains a view of the portion of this list between the specified <tt>fromIndex</tt>,
   * inclusive, and <tt>toIndex</tt>, exclusive.
   * <p>
   * The max size of the list is set to be {@link #DEFAULT_MAX_MESSAGES}
   * </p>
   *
   * @param size the max size of the new list
   * @param digest the original
   * @param fromIndex low endpoint (inclusive) of the subList
   * @param toIndex high endpoint (exclusive) of the subList
   * @see #subList(int, int)
   */
  public MessageErrorDigest(int size, MessageErrorDigest digest, int fromIndex, int toIndex) {
    this(size, digest.subList(fromIndex, toIndex));
  }

  public int getMaxMessages() {
    return maxMessages;
  }

  /**
   * Set the maximum number of messages this digester will cache. If the internal cache fills up, then a First in, First out
   * algorithm is used to continue caching any messages that have failed.
   *
   * @param maxMessages the max number of messages.
   */
  public void setMaxMessages(int maxMessages) {
    if (maxMessages <= 0) {
      this.maxMessages = DEFAULT_MAX_MESSAGES;
    }
    else {
      this.maxMessages = maxMessages;
    }
  }

  @Override
  public boolean addAll(int index, Collection<? extends MessageDigestErrorEntry> c) {
    throw new UnsupportedOperationException("addAll(int, Collection) not supported");
  }

  @Override
  public boolean add(MessageDigestErrorEntry ame) {
    while (errorMessages.size() >= getMaxMessages()) {
      errorMessages.remove(0);
    }
    return errorMessages.add(ame);
  }

  @Override
  public MessageDigestErrorEntry get(int index) {
    return errorMessages.get(index);
  }

  @Override
  public MessageDigestErrorEntry set(int index, MessageDigestErrorEntry element) {
    throw new UnsupportedOperationException("set(int, AdaptrisMessageError) not supported");
  }

  @Override
  public void add(int index, MessageDigestErrorEntry element) {
    throw new UnsupportedOperationException("add(int, AdaptrisMessageError) not supported");
  }

  @Override
  public MessageDigestErrorEntry remove(int index) {
    return errorMessages.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return errorMessages.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return errorMessages.lastIndexOf(o);
  }

  @Override
  public ListIterator<MessageDigestErrorEntry> listIterator() {
    return errorMessages.listIterator();
  }

  @Override
  public ListIterator<MessageDigestErrorEntry> listIterator(int index) {
    return errorMessages.listIterator(index);
  }

  @Override
  public List<MessageDigestErrorEntry> subList(int fromIndex, int toIndex) {
    return errorMessages.subList(fromIndex, toIndex);
  }

  @Override
  public Iterator<MessageDigestErrorEntry> iterator() {
    return errorMessages.listIterator();
  }

  @Override
  public int size() {
    return errorMessages.size();
  }

}
