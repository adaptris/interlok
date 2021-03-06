/**
 * Adds support for arbitarily large messages.
 * <p>
 * The AdaptrisMessage implementation is backed by a pair of files; there is minor support for the
 * existing {@link com.adaptris.core.AdaptrisMessage#getPayload()} and
 * {@link com.adaptris.core.AdaptrisMessage#getStringPayload()} methods, however, RuntimeExceptions
 * will be thrown if there is an attempt to use those methods on files above a certain size as
 * determined by {@link com.adaptris.core.lms.FileBackedMessageFactory#getMaxMemorySizeBytes()}.
 * Generally speaking you should be using {@link com.adaptris.core.AdaptrisMessage#getInputStream()}
 * and {@link com.adaptris.core.AdaptrisMessage#getOutputStream()} to read and write from the
 * payload respectively.
 * </p>
 *
 */
package com.adaptris.core.lms;
