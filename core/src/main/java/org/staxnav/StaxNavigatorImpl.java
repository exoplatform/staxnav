/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.staxnav;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class StaxNavigatorImpl<N> implements StaxNavigator<N>
{

   /** . */
   private final Naming<N> naming;

   /** . */
   private Entry current;

   /** . */
   private final int depth;

   /** . */
   private boolean trimContent;

   StaxNavigatorImpl(Naming<N> naming, XMLStreamReader stream) throws XMLStreamException
   {
      if (naming == null)
      {
         throw new NullPointerException();
      }
      if (stream == null)
      {
         throw new NullPointerException();
      }

      //
      this.naming = naming;
      this.current = new HeadEntry(stream);
      this.depth = 0;
      this.trimContent = false;
   }

   private StaxNavigatorImpl(Naming<N> naming, Entry current, boolean trimContent)
   {
      this.naming = naming;
      this.current = current;
      this.depth = current.getElement().getDepth();
      this.trimContent = trimContent;
   }

   public N getName() throws StaxNavException
   {
      if (current == null)
      {
         return null;
      }
      return current.getElement().getName(naming);
   }

   public Naming<N> getNaming()
   {
      return naming;
   }

   public String getLocalName() throws StaxNavException
   {
      if (current == null)
      {
         return null;
      }

      return current.getElement().getName().getLocalPart();
   }

   public Location getLocation() throws StaxNavException
   {
      if (current == null)
      {
         return null;
      }
      return current.getElement().getLocation();
   }

   public int getDepth() throws StaxNavException
   {
      if (current == null)
      {
         return -1;
      }
      return current.getElement().getDepth();
   }

   public void setTrimContent(boolean trimContent)
   {
      this.trimContent = trimContent;
   }

   public boolean getTrimContent()
   {
      return trimContent;
   }

   public String getContent() throws StaxNavException
   {
      if (current == null)
      {
         return null;
      }
      return current.getElement().getContent(trimContent);
   }

   public <V> V parseContent(ValueType<V> valueType) throws IllegalStateException, NullPointerException, StaxNavException
   {
      if (valueType == null)
      {
         throw new NullPointerException();
      }
      if (current == null)
      {
         return null;
      }
      Entry element = current;
      String content = element.getElement().getContent(true);
      if (content == null)
      {
         throw new IllegalStateException("No content available for parsing");
      }
      try
      {
         return valueType.parse(content);
      }
      catch (Exception e)
      {
         if (e instanceof TypeConversionException)
         {
            throw (TypeConversionException)e;
         }
         else
         {
            throw new TypeConversionException(element.getElement().getLocation(), e, "Could not parse string value " + content);
         }
      }
   }

   public String getAttribute(String name) throws NullPointerException, IllegalStateException, StaxNavException
   {
      if (current == null)
      {
         return null;
      }
      Map<String, String> attributes = current.getElement().getAttributes();
      if (attributes.isEmpty())
      {
         return null;
      }
      else
      {
         return attributes.get(name);
      }
   }

   public StaxNavigator<N> fork() throws StaxNavException
   {
      return fork(Axis.SELF);
   }

   public StaxNavigator<N> fork(Axis axis) throws StaxNavException
   {
      if (axis == null)
      {
         throw new NullPointerException("No null axis accepted");
      }
      StaxNavigatorImpl<N> fork = new StaxNavigatorImpl<N>(naming, current, trimContent);
      Entry next = _navigate(current, axis, null);
      if (next != null)
      {
         current = next;
      }
      return fork;
   }

   public Iterable<StaxNavigator<N>> fork(N name)
   {
      return fork(Axis.FOLLOWING_SIBLING, name);
   }

   public Iterable<StaxNavigator<N>> fork(Axis axis, N name)
   {
      if (axis == null)
      {
         throw new NullPointerException("No null axis accepted");
      }
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }

      //
      List<Entry> elements;
      if (name.equals(getName()))
      {
         elements = new ArrayList<Entry>();
         elements.add(current);
      }
      else
      {
         elements = Collections.emptyList();
      }

      //
      while (navigate(axis, name))
      {
         if (elements.isEmpty())
         {
            elements = new LinkedList<Entry>();
         }
         elements.add(current);
      }

      // Freeze what we need
      final List<Entry> a = elements;
      final boolean trimContent = this.trimContent;

      //
      return new Iterable<StaxNavigator<N>>()
      {
         public Iterator<StaxNavigator<N>> iterator()
         {
            return new Iterator<StaxNavigator<N>>()
            {
               Iterator<Entry> i = a.iterator();
               public boolean hasNext()
               {
                  return i.hasNext();
               }
               public StaxNavigator<N> next()
               {
                  Entry next = i.next();
                  return new StaxNavigatorImpl<N>(naming, next, trimContent);
               }
               public void remove()
               {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   public String getAttribute(QName name) throws NullPointerException, IllegalStateException, StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null attribute name expected");
      }
      if (current == null)
      {
         return null;
      }
      if (XMLConstants.NULL_NS_URI.equals(name.getNamespaceURI()))
      {
         return getAttribute(name.getLocalPart());
      }
      else
      {
         Map<QName, String> qualifiedAttributes = current.getElement().getQualifiedAttributes();
         if (qualifiedAttributes.isEmpty())
         {
            return null;
         }
         else
         {
            return qualifiedAttributes.get(name);
         }
      }
   }

   public Map<String, String> getAttributes() throws NullPointerException, IllegalStateException, StaxNavException
   {
      if (current == null)
      {
         return null;
      }
      Map<String, String> attributes = current.getElement().getAttributes();
      if (attributes.isEmpty())
      {
         return Collections.emptyMap();
      }
      else
      {
         return attributes;
      }
   }

   public Map<QName, String> getQualifiedAttributes() throws NullPointerException, IllegalStateException, StaxNavException
   {
      if (current == null)
      {
         return null;
      }
      else
      {
         Map<QName, String> qualifiedAttributes = current.getElement().getQualifiedAttributes();
         Map<String, String> attributes = getAttributes();
         for (String key : attributes.keySet()) {
            qualifiedAttributes.put(new QName(key), attributes.get(key));
         }
         if (qualifiedAttributes.isEmpty())
         {
            return Collections.emptyMap();
         }
         else
         {
            return qualifiedAttributes;
         }
      }
   }

  public String getNamespaceByPrefix(String prefix) throws NullPointerException, StaxNavException
   {
      if (prefix == null)
      {
         throw new NullPointerException();
      }
      if (current == null)
      {
         return null;
      }
      return current.getElement().getNamespaceByPrefix(prefix);
   }

   // Axis methods

   public N next() throws StaxNavException
   {
      return navigate(Axis.NEXT);
   }

   public boolean next(N name) throws StaxNavException
   {
      return navigate(Axis.NEXT, name);
   }

   public N child() throws StaxNavException
   {
      return navigate(Axis.CHILD);
   }

   public boolean child(N name) throws NullPointerException, StaxNavException
   {
      return navigate(Axis.CHILD, name);
   }

   public N sibling() throws StaxNavException
   {
      return navigate(Axis.FOLLOWING_SIBLING);
   }

   public boolean sibling(N name) throws NullPointerException, StaxNavException
   {
      return navigate(Axis.FOLLOWING_SIBLING, name);
   }

   public N navigate(Axis axis) throws StaxNavException
   {
      Entry entry = _navigate(current, axis, null);
      if (entry != null)
      {
         current = entry;
         return getName();
      }
      else
      {
         return null;
      }
   }

   public boolean navigate(Axis axis, N name) throws StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      Entry entry = _navigate(current, axis, name);
      if (entry != null)
      {
         current = entry;
         return true;
      }
      else
      {
         return false;
      }
   }

   private Entry _navigate(Entry current, Axis axis, N name)
   {
      switch (axis)
      {
         case SELF:
            return _current(current, name);
         case NEXT:
            return _next(current, name);
         case CHILD:
            return _child(current, name);
         case FOLLOWING_SIBLING:
            return _sibling(current, name);
         case FOLLOWING:
            return _following(current, name);
         default:
            throw new AssertionError();
      }
   }

   private Entry _current(Entry current, N name) throws StaxNavException
   {
      if (current != null)
      {
         if (name == null ||name.equals(naming.getName(current.getElement().getName())))
         {
            return current;
         }
      }
      return null;
   }

   private Entry _next(Entry current, N name) throws StaxNavException
   {
      if (current != null)
      {
         Entry next = current.next(depth);
         if (next != null && (name == null || name.equals(naming.getName(next.getElement().getName()))))
         {
            current = next;
            return current;
         }
      }
      return null;
   }

   private Entry _child(Entry current, N name) throws StaxNavException
   {
      if (current != null)
      {
         Entry element = current;
         while (true)
         {
            Entry next = element.next();
            if (next != null && next.getElement().getDepth() > current.getElement().getDepth())
            {
               if (next.getElement().getDepth() == current.getElement().getDepth() + 1)
               {
                  N nextName = naming.getName(next.getElement().getName());
                  if (name == null)
                  {
                     current = next;
                     return current;
                  }
                  else if (name.equals(nextName))
                  {
                     current = next;
                     return current;
                  }
                  else
                  {
                     element = next;
                  }
               }
               else
               {
                  element = next;
               }
            }
            else
            {
               break;
            }
         }
      }
      return null;
   }

   private Entry _sibling(Entry current, N name) throws StaxNavException
   {
      if (current != null)
      {
         Entry element = current;
         while (true)
         {
            Entry next = element.next();
            if (next != null && next.getElement().getDepth() >= current.getElement().getDepth())
            {
               if (next.getElement().getDepth() == current.getElement().getDepth())
               {
                  if (name == null)
                  {
                     current = next;
                     return current;
                  }
                  else
                  {
                     N siblingName = naming.getName(next.getElement().getName());
                     if (name.equals(siblingName))
                     {
                        current = next;
                        return current;
                     }
                     else
                     {
                        element = next;
                     }
                  }
               }
               else
               {
                  element = next;
               }
            }
            else
            {
               break;
            }
         }
      }
      return null;
   }

   private Entry _following(Entry current, N name) throws StaxNavException
   {
      if (name == null)
      {
         throw new UnsupportedOperationException("todo");
      }
      if (current != null)
      {
         Entry entry = current.next();
         while (entry != null)
         {
            N findName = naming.getName(entry.getElement().getName());
            if (name.equals(findName))
            {
               current = entry;
               return current;
            }
            else
            {
               entry = entry.next();
            }
         }
      }
      return null;
   }

   // Other methods

   public boolean find(N name) throws StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      if (name.equals(naming.getName(current.getElement().getName())))
      {
         return true;
      }
      else
      {
         return navigate(Axis.FOLLOWING, name);
      }
   }

   public N next(Set<N> names) throws StaxNavException
   {
      if (names == null)
      {
         throw new NullPointerException();
      }
      if (current == null)
      {
         return null;
      }
      Entry next = current.next(depth);
      if (next == null)
      {
         return null;
      }
      N name = naming.getName(next.getElement().getName());
      if (names.contains(name))
      {
         current = next;
         return name;
      }
      else
      {
         return null;
      }
   }

   public int descendant(N name) throws NullPointerException, StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      if (current == null)
      {
         return 0;
      }
      return _descendant(name);
   }

   private int _descendant(N name) throws StaxNavException
   {
      Entry element = current;
      while (true)
      {
         Entry next = element.next();
         if (next != null && next.getElement().getDepth() >= current.getElement().getDepth())
         {
            N descendantName = naming.getName(next.getElement().getName());
            if (name.equals(descendantName))
            {
               int diff = next.getElement().getDepth() - current.getElement().getDepth();
               current = next;
               return diff;
            }
            else
            {
               element = next;
            }
         }
         else
         {
            break;
         }
      }
      return -1;
   }

   /**
    * Entry objects are a linked list Element.
    *
    * For instance for the XML stream:
    *
    * &lt;foo&gt;
    *  &lt;bar&gt;
    *  &lt;/bar&gt;
    *  &lt;juu&gt;
    *  &lt;/juu&gt;
    * &lt;/foo&gt;
    *
    * will be modeled
    *
    *  Entry -> Element foo  <-
    *    |         /|\        |
    *   \|/         |         |
    *  Entry -> Element bar   |
    *    |                    |
    *   \|/                   |
    *  Entry -> Element juu --|
    *
    * When the navigator points on the juu Element, the bar Entry, the foo Entry and the bar Element are not referenced
    * anymore and are available for the garbage collector.
    */
   private static abstract class Entry
   {

      protected abstract Element getElement() throws StaxNavException;

      protected abstract boolean hasNext(int depth) throws StaxNavException;

      protected abstract Entry next(int depth) throws StaxNavException;

      protected abstract Entry next() throws StaxNavException;

   }

   private static class HeadEntry extends Entry
   {

      /** . */
      private final XMLStreamReader stream;

      /** . */
      private Entry root;

      private HeadEntry(XMLStreamReader stream)
      {
         this.stream = stream;
         this.root = null;
      }

      private Entry get()
      {
         if (root == null)
         {
            try
            {
               while (stream.hasNext())
               {
                  int type = stream.getEventType();
                  if (type == XMLStreamConstants.START_ELEMENT)
                  {
                     root = new StreamEntry(stream, new Element(stream, null));
                     break;
                  }
                  else
                  {
                     stream.next();
                  }
               }
            }
            catch (XMLStreamException e)
            {
               throw new StaxNavException(e);
            }
         }
         if (root == null)
         {
            throw new StaxNavException(stream.getLocation(), "No head!!!!");
         }
         return root;
      }

      protected boolean hasNext(int depth) throws StaxNavException
      {
         return get().hasNext(depth);
      }

      protected Entry next(int depth) throws StaxNavException
      {
         return get().next(depth);
      }

      protected Entry next() throws StaxNavException
      {
         return get().next();
      }

      @Override
      protected Element getElement() throws StaxNavException
      {
         return get().getElement();
      }

      @Override
      public String toString()
      {
         return "HeadEntry";
      }
   }

   private static class StreamEntry extends Entry
   {

      /** . */
      private final XMLStreamReader stream;

      /** . */
      private final Element element;

      /** . */
      private StreamEntry next;

      private StreamEntry(XMLStreamReader stream, Element element)
      {
         this.stream = stream;
         this.next = null;
         this.element = element;
      }

      protected Element getElement() throws StaxNavException
      {
         return element;
      }

      protected boolean hasNext(int depth) throws StaxNavException
      {
         return next(depth) != null;
      }

      protected Entry next(int depth) throws StaxNavException
      {
         Entry next = next();
         if (next != null && next.getElement().getDepth() > depth)
         {
            return next;
         }
         else
         {
            return null;
         }
      }

      protected Entry next() throws StaxNavException
      {
         try
         {
            if (next == null)
            {
               Element parent = element;
               while (true)
               {
                  int type = stream.getEventType();
                  if (type == XMLStreamConstants.START_ELEMENT)
                  {
                     next = new StreamEntry(stream, new Element(stream, parent));
                     break;
                  }
                  else if (type == XMLStreamConstants.END_ELEMENT)
                  {
                     parent = parent.getParent();
                     stream.next();
                  }
                  else if (type == XMLStreamConstants.END_DOCUMENT)
                  {
                     break;
                  }
                  else
                  {
                     stream.next();
                  }
               }
            }
            return next;
         }
         catch (XMLStreamException e)
         {
            throw new StaxNavException(e);
         }
      }

      @Override
      public String toString()
      {
         return "StreamEntry[element=" + element + "]";
      }
   }

   private static class Element
   {

      /** . */
      private final Element parent;

      /** . */
      private final QName name;

      /** . */
      private final int depth;

      /** The content is not null. */
      private final Object content;

      /** . */
      private final Location location;

      /** . */
      private final Map<String, String> attributes;

      /** . */
      private final Map<QName, String> qualifiedAttributes;

      /** . */
      private final Map<String, String> namespaces;

      private Element(XMLStreamReader stream, Element parent) throws XMLStreamException
      {
         // We assume that the stream points to the start of the modelled element
         if (stream.getEventType() != XMLStreamConstants.START_ELEMENT)
         {
            throw new AssertionError();
         }

         //
         QName name = stream.getName();
         Location location = stream.getLocation();

         //
         Map<String, String> attributes = Collections.emptyMap();
         Map<QName, String> qualifiedAttributes = Collections.emptyMap();
         int attributeCount = stream.getAttributeCount();
         for (int i = 0;i < attributeCount;i++)
         {
            String attributeValue = stream.getAttributeValue(i);
            QName attributeName = stream.getAttributeName(i);
            if (XMLConstants.NULL_NS_URI.equals(attributeName.getNamespaceURI()))
            {
               if (attributes.isEmpty())
               {
                  attributes = new HashMap<String, String>();
               }
               attributes.put(attributeName.getLocalPart(), attributeValue);
            }
            else
            {
               if (qualifiedAttributes.isEmpty())
               {
                  qualifiedAttributes = new HashMap<QName, String>();
               }
               qualifiedAttributes.put(attributeName, attributeValue);
            }
         }

         //
         Map<String, String> namespaces;
         int namespaceCount = stream.getNamespaceCount();
         if (namespaceCount > 0)
         {
            namespaces = new HashMap<String, String>();
            for (int i = 0;i < namespaceCount;i++)
            {
               String namespacePrefix = stream.getNamespacePrefix(i);
               if (namespacePrefix == null)
               {
                  namespacePrefix = "";
               }
               String namespaceURI = stream.getNamespaceURI(i);
               namespaces.put(namespacePrefix, namespaceURI);
            }
         }
         else
         {
            namespaces = Collections.emptyMap();
         }

         // When we leave we assume that we are positionned on the next element start or the document end
         StringBuilder sb = null;
         String chunk = null;
         Object content = null;
         while (true)
         {
            stream.next();
            int type = stream.getEventType();
            if (type == XMLStreamConstants.END_DOCUMENT || type == XMLStreamConstants.START_ELEMENT)
            {
               break;
            }
            else if (type == XMLStreamConstants.CHARACTERS)
            {
               if (chunk == null)
               {
                  chunk = stream.getText();
               }
               else
               {
                  if (sb == null)
                  {
                     sb = new StringBuilder(chunk);
                  }
                  sb.append(stream.getText());
               }
            }
            else if (type == XMLStreamConstants.END_ELEMENT)
            {
               if (sb != null)
               {
                  content = sb;
               }
               else
               {
                  content = chunk;
               }
               break;
            }
         }

         //
         int depth = 1 + (parent != null ? parent.getDepth() : 0);

         //
         this.parent = parent;
         this.name = name;
         this.depth = depth;
         this.content = content;
         this.attributes = attributes;
         this.qualifiedAttributes = qualifiedAttributes;
         this.namespaces = namespaces;
         this.location = location;
      }

      protected Element getParent()
      {
         return parent;
      }

      protected <N> N getName(Naming<N> naming)
      {
         return naming.getName(getName());
      }

      protected String getNamespaceByPrefix(String namespacePrefix)
      {
         for (Element current = this;current != null;current = current.getParent())
         {
            String namespaceURI = current.getNamespaces().get(namespacePrefix);
            if (namespaceURI != null)
            {
               return namespaceURI;
            }
         }
         return null;
      }

      protected String getContent(boolean trim)
      {
         if (content != null)
         {
            String s = content.toString();
            if (trim)
            {
               s = s.trim();
            }
            return s;
         }
         else
         {
            return null;
         }
      }

      protected QName getName()
      {
         return name;
      }

      protected int getDepth()
      {
         return depth;
      }

      protected Location getLocation()
      {
         return location;
      }

      protected Map<String, String> getAttributes()
      {
         return attributes;
      }

      protected Map<QName, String> getQualifiedAttributes()
      {
         return qualifiedAttributes;
      }

      protected Map<String, String> getNamespaces()
      {
         return namespaces;
      }

      @Override
      public String toString()
      {
         return "Element[name=" + name + ",location=" + location + "]";
      }
   }
}
