package com.adaptris.naming.adapter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.Reference;
import javax.naming.spi.NamingManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingContext implements Context, Serializable {
  
  private static transient Logger log = LoggerFactory.getLogger(NamingContext.class);

  private static final long serialVersionUID = 201409150933L;

  /**
   * JNDI Environment.
   */
  protected Hashtable<String, Object> environment;

  /**
   * Bindings in this Context.
   */
  protected HashMap<String, NamingEntry> bindings;

  /**
   * Name Parser
   */
  private static final NameParser NAME_PARSER = new NameParser() {
    @Override
    public Name parse(String name) throws NamingException {
      return new CompositeName(name);
    }
  };

  private String nameInNamespace = "";

  public NamingContext() {
    this.environment = new Hashtable<String, Object>();
    bindings = new HashMap<>();
  }

  public NamingContext(Hashtable<String, Object> environment) {
    bindings = new HashMap<>();
    if (environment == null)
      this.environment = new Hashtable<String, Object>();
    else
      this.environment = environment;
  }
  
  public NamingContext(Hashtable<String, Object> environment, Map<String, Object> objects) {
    bindings = new HashMap<>();
    if (environment == null)
      this.environment = new Hashtable<String, Object>();
    else
      this.environment = environment;
    
    for(String name : objects.keySet()) {
      try {
        this.bind(name, objects.get(name));
      } catch (NamingException ex) {
        log.warn("Cannot bind jndi object: " + name);
      }
    }
  }
  
  
  public NamingContext(Hashtable<String, Object> environment, String nameInNamespace) {
    bindings = new HashMap<>();
    if (environment == null)
      this.environment = new Hashtable<String, Object>();
    else
      this.environment = environment;
    
    this.nameInNamespace = nameInNamespace;
  }

  @Override
  public void bind(Name name, Object object) throws NamingException {
    this.bind(name, object, false);
  }

  public void bind(Name name, Object object, boolean rebind) throws NamingException {
    name = this.stripScheme(this.stripEmptyPrefix(name));
    if (name.isEmpty())
      throw new NamingException("Invalid name: " + name.toString());

    NamingEntry entry = bindings.get(name.get(0));

    if (name.size() > 1) {
      if (entry == null) {
        // bind the sub context first and then recursively call bind on the subcontext(s).
        Context subcontext = this.createSubcontext(name.get(0));
        subcontext.bind(name.getSuffix(1), object);
      } else {
        if (entry.getType().equals(NamingEntryType.CONTEXT)) {
          if (rebind) {
            ((Context) entry.getObject()).rebind(name.getSuffix(1), object);
          } else {
            ((Context) entry.getObject()).bind(name.getSuffix(1), object);
          }
        } else {
          throw new NamingException("Context expected!");
        }
      }
    } else {
      if ((!rebind) && (entry != null)) {
        throw new NameAlreadyBoundException("Name already bound: " + name.get(0).toString());
      } else {
        Object toBind = NamingManager.getStateToBind(object, name, this, this.environment);
        if (toBind instanceof Context) {
          entry = new NamingEntry(name.get(0), toBind, NamingEntryType.CONTEXT);
        } else {
          entry = new NamingEntry(name.get(0), toBind, NamingEntryType.OBJECT);
        }
        bindings.put(name.get(0), entry);
      }
    }

  }

  private Name stripScheme(Name name) throws InvalidNameException {
    if(!name.get(0).isEmpty()) {
      int indexOfSchemeMarker = name.get(0).indexOf(":");
      if(indexOfSchemeMarker > 0) {
        if(name.size() > 1)
          name = new CompositeName(name.get(0).substring(indexOfSchemeMarker + 1) + "/" + name.getSuffix(1).toString());
        else
          name = new CompositeName(name.get(0).substring(indexOfSchemeMarker + 1));
      }
    }
    return name;
  }

  @Override
  public Name composeName(Name name, Name prefix) throws NamingException {
    prefix = (Name) prefix.clone();
    return prefix.addAll(name);
  }

  @Override
  public String composeName(String name, String prefix) throws NamingException {
    CompositeName result = new CompositeName(prefix);
    result.addAll(new CompositeName(name));
    return result.toString();
  }

  @Override
  public Context createSubcontext(Name name) throws NamingException {
    NamingContext newNamingContext = new NamingContext(this.environment);
    bind(name, newNamingContext);
    return newNamingContext;
  }

  @Override
  public void destroySubcontext(Name name) throws NamingException {
    name = stripEmptyPrefix(name);
    if (name.isEmpty())
      throw new NamingException("Invalid name: " + name.toString());

    NamingEntry namingEntry = bindings.get(name.get(0));

    if (namingEntry == null)
      throw new NameNotFoundException("Name not found: " + name.get(0).toString());

    if (name.size() > 1) {
      if (namingEntry.getType() == NamingEntryType.CONTEXT)
        ((Context) namingEntry.getObject()).destroySubcontext(name.getSuffix(1));
      else
        throw new NamingException("Expected a context object: " + name.toString());
    } else {
      if (namingEntry.getType() == NamingEntryType.CONTEXT) {
        ((Context) namingEntry.getObject()).close();
        bindings.remove(name.get(0));
      } else
        throw new NotContextException("Expected a context object");
    }
  }

  @Override
  public String getNameInNamespace() throws NamingException {
    return nameInNamespace;
  }

  @Override
  public NameParser getNameParser(Name name) throws NamingException {
    return NAME_PARSER;
  }

  @Override
  public NameParser getNameParser(String name) throws NamingException {
    return NAME_PARSER;
  }

  @Override
  public NamingEnumeration list(Name name) throws NamingException {
    Object o = lookup(name);
    if (o == this) {
      return new ListEnumeration();
    } else if (o instanceof Context) {
      return ((Context) o).list("");
    } else {
      throw new NotContextException();
    }
  }

  @Override
  public NamingEnumeration list(String name) throws NamingException {
    return this.list(new CompositeName(name));
  }

  @Override
  public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
    Object o = lookup(name);
    if (o == this) {
      return new ListBindingEnumeration();
    } else if (o instanceof Context) {
      return ((Context) o).listBindings("");
    } else {
      throw new NotContextException();
    }
  }

  @Override
  public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
    return this.listBindings(new CompositeName(name));
  }

  @Override
  public Object lookup(Name name) throws NamingException {
    log.debug("NamingContext lookup (" + name.toString() + ")");
    
    name = this.stripEmptyPrefix(name);

    if (name.size() == 0) {
      return this;
    }

    Object result = bindings.get(name.get(0));
    
    if (result == null) {
      int pos = name.get(0).indexOf(':');
      if (pos > 0) {
        String scheme = name.get(0).substring(0, pos);
        Context ctx = NamingManager.getURLContext(scheme, environment);
        if (ctx == null) {
          throw new NamingException("scheme " + scheme + " not recognized");
        }
        return ctx.lookup(new CompositeName(name.get(0).substring(pos + 1) + "/" + name.getSuffix(1).toString()));
      } else {
        // Split out the first name of the path
        // and look for it in the bindings map.
        if (name.size() == 0) {
          return this;
        } else {
          String first = name.get(0);
          Object obj = bindings.get(first);
          if (obj == null) {
            throw new NamingException("Name not found exception: " + name.toString());
          } else if (obj instanceof Context && name.size() > 1) {
            Context subContext = (Context) obj;
            obj = subContext.lookup(name.getSuffix(1));
          }
          return obj;
        }
      }
    }
    
    result = ((NamingEntry) result).getObject();
    
    if (result instanceof LinkRef) {
      LinkRef ref = (LinkRef) result;
      result = lookup(ref.getLinkName());
    }
    if (result instanceof Reference) {
      try {
        result = NamingManager.getObjectInstance(result, null, null, this.environment);
      } catch (NamingException e) {
        throw e;
      } catch (Exception e) {
        throw (NamingException) new NamingException("could not look up : " + name).initCause(e);
      }
    }
    if (result instanceof NamingContext) {
      if(name.size() > 1)
        return ((NamingContext) result).lookup(name.getSuffix(1));
    }
    return result;
  }

  @Override
  public Object lookupLink(Name name) throws NamingException {
    return lookup(name);
  }

  @Override
  public void rebind(Name name, Object object) throws NamingException {
    this.bind(name, object);
  }

  @Override
  public void rename(Name oldName, Name newName) throws NamingException {
    Object value = lookup(oldName);
    bind(newName, value);
    unbind(oldName);
  }

  @Override
  public void unbind(Name name) throws NamingException {
    name = this.stripEmptyPrefix(name);
    if (name.isEmpty())
      throw new NamingException("Invalid name: " + name.toString());

    NamingEntry entry = bindings.get(name.get(0));

    if (entry == null)
      throw new NameNotFoundException("Name not bound: " + name.get(0));

    if (name.size() > 1) {
      if (entry.getType().equals(NamingEntryType.CONTEXT)) {
        ((Context) entry.getObject()).unbind(name.getSuffix(1));
      } else {
        throw new NamingException("Context expected: " + name.toString());
      }
    } else {
      bindings.remove(name.get(0));
    }
  }

  @Override
  public Object lookup(String name) throws NamingException {
    return this.lookup(new CompositeName(name));
  }

  @Override
  public Object lookupLink(String name) throws NamingException {
    return this.lookupLink(new CompositeName(name));
  }

  @Override
  public void unbind(String name) throws NamingException {
    this.unbind(new CompositeName(name));
  }

  @Override
  public void rebind(String name, Object object) throws NamingException {
    this.rebind(new CompositeName(name), object);
  }

  @Override
  public Object removeFromEnvironment(String propertyName) throws NamingException {
    return this.environment.remove(propertyName);
  }

  @Override
  public Object addToEnvironment(String propertyName, Object propertyVal) throws NamingException {
    return this.environment.put(propertyName, propertyVal);
  }

  @Override
  public Hashtable<?, ?> getEnvironment() throws NamingException {
    return this.environment;
  }

  @Override
  public void bind(String name, Object object) throws NamingException {
    this.bind(new CompositeName(name), object);
  }

  @Override
  public void close() throws NamingException {
  }

  @Override
  public Context createSubcontext(String name) throws NamingException {
    return this.createSubcontext(new CompositeName(name));
  }

  @Override
  public void destroySubcontext(String name) throws NamingException {
    this.destroySubcontext(new CompositeName(name));
  }

  @Override
  public void rename(String oldName, String newName) throws NamingException {
    this.rename(new CompositeName(oldName), new CompositeName(newName));
  }

  private Name stripEmptyPrefix(Name original) {
    while ((!original.isEmpty()) && (original.get(0).length() == 0))
      original = original.getSuffix(1);

    return original;
  }

  @SuppressWarnings("rawtypes")
  private abstract class LocalNamingEnumeration implements NamingEnumeration {
    private Iterator i = bindings.entrySet().iterator();

    public boolean hasMore() throws NamingException {
      return i.hasNext();
    }

    public boolean hasMoreElements() {
      return i.hasNext();
    }

    protected Map.Entry getNext() {
      return (Map.Entry) i.next();
    }

    public void close() throws NamingException {
    }
  }

  @SuppressWarnings("rawtypes")
  private class ListEnumeration extends LocalNamingEnumeration {
    ListEnumeration() {
    }

    public Object next() throws NamingException {
      return nextElement();
    }

    public Object nextElement() {
      Map.Entry entry = getNext();
      return new NameClassPair((String) entry.getKey(), entry.getValue().getClass().getName());
    }
  }

  @SuppressWarnings("rawtypes")
  private class ListBindingEnumeration extends LocalNamingEnumeration {
    ListBindingEnumeration() {
    }

    public Object next() throws NamingException {
      return nextElement();
    }

    public Object nextElement() {
      Map.Entry entry = getNext();
      return new Binding((String) entry.getKey(), entry.getValue());
    }
  }

}
