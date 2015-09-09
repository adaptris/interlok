package com.adaptris.transform;

import java.util.ArrayList;

/**
 * <p>This provides a container class for objects that represent
 * transformation rules. A rule is added as a <b>key-value</b> pair where:</p>
 *
 * <ul type="square">
 * <li>the <b>key</b> is the rule as a <a href="Source.html"><code>Source
 * </code></a> object and</li>
 * <li>the <b>value</b> is the rule in an optimised format as an <code>Object
 * </code></li>
 * </ul>
 *
 * <p>It is at the discretion of the client application as to what
 * constitutes an optimised rule. It is therefore the responsibility
 * of the client application to appropriately cast the return value
 * when an 'optimised' rule is retrieved using
 * {@link #getValue(Source)} or {@link #getValue(int)}.</p>
 *
 * <p>Duplicate values and <code>null</code>s are not permitted.
 * Adding a duplicate key will result in the original being
 * silently removed before the new (duplicate) key and its value
 * is added. Attempting to add a <code>null</code> key or value will
 * result in a runtime exception being thrown.</p>
 *
 * <p>Note that this implementation is not synchronized. If multiple
 * threads access a <code>RuleList</code> instance concurrently, and at
 * least one of the threads modifies the list structurally, it must be
 * synchronized externally. A structural modification is any operation
 * that adds or removes one or more rules.</p>
 *
 */
public class RuleList {

  /**
   * <p>The underlying implementation to contain rules.</p>
   */
  private ArrayList<Source> ruleKeys = new ArrayList<Source>();

  /**
   * <p>The underlying implementation to contain rules.</p>
   */
  private ArrayList<Object> ruleValues = new ArrayList<Object>();

  /**
   * <p>Zero-argument default constructor.</p>
   */
  public RuleList() {
  }

  /**
   * <p>Adds a rule and its optimised form to the end of the list.</p>
   *
   * @param  key the rule to add.
   * @param  value the corresponding rule in its optimised form.
   * @see    #getValue(Source)
   * @see    #indexOfKey(Source)
   * @see    #getValue(int)
   * @see    #getKey(int)
   */
  public void add(Source key, Object value) {
    if (key == null || value == null)
      throw new IllegalArgumentException();

    // Just in case it is there already.
    remove(key);

    ruleKeys.add(key);
    ruleValues.add(value);
  }

  /**
   * <p>Returns a rule key at the specified position within
   * <code>RuleList</code>. If the index value of the key-value
   * pair is out of range then a runtime exception is thrown.</p>
   *
   * @param index the index position of the rule key to get.
   * @return the rule key at the specified position.
   * @see   #indexOfKey(Source)
   */
  public Source getKey(int index) {
    return (Source) ruleKeys.get(index);
  }

  /**
   * <p>Returns the optimised rule corresponding to <code>key</code>. If
   * <code>key</code> does not exist then <code>null</code> is returned.</p>
   *
   * @param  key the rule key.
   * @return the corresponding optimised rule or <code>null</code> if not found.
   * @see    #indexOfKey(Source)
   * @see    #getValue(int)
   */
  public Object getValue(Source key) {
    int i = indexOfKey(key);

    if (i >= 0)
      return getValue(i);

    return null;
  }

  /**
   * <p>Returns the optimised rule at the specified position
   * within <code>RuleList</code>. If the index value of the
   * key-value pair is out of range then a runtime exception
   * is thrown.</p>
   *
   * @param  index the index position of the key-value pair.
   * @return the optimised rule at the specified position.
   * @see    #indexOfKey(Source)
   * @see    #getValue(Source)
   */
  public Object getValue(int index) {
    return ruleValues.get(index);
  }

  /**
   * <p>Removes a rule key-value pair at the specified position within
   * <code>RuleList</code>. If the index value is out of range then
   * a runtime exception is thrown.</p>
   *
   * @param index the index position of the rule key-value pair.
   * @see   #indexOfKey(Source)
   * @see   #remove(Source)
   */
  public void remove(int index) {
    ruleKeys.remove(index);
    ruleValues.remove(index);
  }

  /**
   * <p>Removes a rule key-value pair from <code>RuleList</code>.</p>
   *
   * @param  key the rule key.
   * @return <code>true</code> if the rule key-value pair is found and removed
   *         from <code>RuleList</code> otherwise <code>false</code> is returned
   * @see    #indexOfKey(Source)
   * @see    #remove(int)
   */
  public boolean remove(Source key) {
    if (key != null) {
      int i = -1;

      i = indexOfKey(key);

      if (i >= 0) {
        remove(i);
        return true;
      }
    }

    return false;
  }

  /**
   * <p>Removes all the rule key-value pairs from <code>RuleList</code>.</p>
   *
   * @see   #remove(Source)
   * @see   #indexOfKey(Source)
   * @see   #remove(int)
   */
  public void removeAll() {
    ruleKeys.clear();
    ruleValues.clear();
  }

  /**
   * <p>Returns the index position of a rule key-value pair in 
   * <code>RuleList</code>.</p>
   *
   * @param  key the rule key.
   * @return the index position of the rule or -1 if not found.
   */
  public int indexOfKey(Source key) {
    return ruleKeys.indexOf(key);
  }

  /**
   * <p>Returns <code>true</code> if the specified rule key is in
   * <code>RuleList</code> otherwise it returns <code>false</code>.</p>
   * @param key the key to search for.
   * @return true if the tag exists in this rule list.
   */
  public boolean containsKey(Source key) {
    return ruleKeys.contains(key);
  }

  /**
   * <p>Returns <code>true</code> if there are no rule key-value pairs in
   * <code>RuleList</code> otherwise it returns <code>false</code>.</p>
   * @return true if the list is empty.
   */
  public boolean isEmpty() {
    return ruleKeys.isEmpty();
  }

  /**
   * Returns the number of rule key-value pairs in <code>RuleList</code>.</p>
   * @return the size of the list.
   */
  public int size() {
    return ruleKeys.size();
  }

  /** @see Object#toString()
   */
  public String toString() {
    return (getClass().getName() + '@' + Integer.toHexString(hashCode()));
  }

  // //////////////////////////////////////
  // private methods
  // //////////////////////////////////////

  /*
  ********************************
  // TV 30/04/01 : not meant to be used
  
    private void _add(Source rule)
    {
      if (rule == null)
         throw new IllegalArgumentException();
  
      remove(rule);
  
      ruleKeys.add(rule);
      ruleValues.add(rule);
    }
  
  ********************************
  */

} // class RuleList
