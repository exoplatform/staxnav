/*
* Copyright (C) 2003-2009 eXo Platform SAS.
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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 * @param <N> the name type
 */
public interface StaxNavigator<N>
{

   /**
    * Returns the current navigated element name.
    *
    * @return the element name
    * @throws StaxNavException any StaxNavException
    */
   N getName() throws StaxNavException;

   /**
    * Returns the location of the start tag of the currently navigated element.
    *
    * @return the element location
    * @throws StaxNavException any StaxNavException
    */
   Location getLocation() throws StaxNavException;

   /**
    * Returns the current navigated element depth.
    *
    * @return the element level
    * @throws StaxNavException any StaxNavException
    */
   int getDepth() throws StaxNavException;

   /**
    * Returns the current navigated element textual content. Note that this method is only valid when an element
    * content is not mixed, if an element has a mixed content then this method will return null instead.
    *
    * @return the element text content
    * @throws StaxNavException any StaxNavException
    */
   String getContent() throws StaxNavException;

   /**
    * Configures the content trimming when {@link #getContent()} method is invoked.
    *
    * @param trimContent true to trim content
    */
   void setTrimContent(boolean trimContent);

   /**
    * Returns the trim content configuration.
    *
    * @return the trim content value
    */
   boolean getTrimContent();

   <V> V parseContent(ValueType<V> valueType) throws IllegalStateException, NullPointerException, StaxNavException;

   /**
    * Creates a navigator scoped around the currently navigated element. The returned navigator will uses the current
    * element as navigation root and the navigation scope is the set of descendants of its root. The forked navigator
    * will use the same configuration than the navigator from which it was forked. The current navigation will be moved
    * to the next sibling of the current node.
    *
    * @return a forked navigator
    * @throws StaxNavException any StaxNavException
    */
   StaxNavigator<N> fork() throws StaxNavException;

   /**
    * Returns an iterable of stax navigator that is built according to the rules:
    * <ul>
    *    <li>Each element is found by the {@link #find(Object)}.</li>
    *    <li>When an element is found, the {@link #sibling()} method is invoked.</li>
    * </ul>
    *
    * @param name the name of the root elements of the forked navigator
    * @return an iterable of the forks
    */
   Iterable<StaxNavigator<N>> fork(N name);

   /**
    * Attemps to navigate to an element following the current one when it has the specified name.
    * If the navigation occurs, the navigator now points to that element and the method returns true.
    * Otherwise no navigation happen and the method return false.
    *
    * @param name the element name to find
    * @return true if the desired element is reached
    * @throws StaxNavException any StaxNavException
    */
   boolean find(N name) throws StaxNavException;

   /**
    * Navigates to the next element and returns its name or null if the end of the stream is reached.
    *
    * @return the element name
    * @throws StaxNavException any StaxNavException
    */
   N next() throws StaxNavException;

   /**
    * Attempt to navigate to the next element when it has the specified name.
    * If the navigation occurs, the navigator now points to that element and the method returns true.
    * Otherwise no navigation happen and the method return false.
    *
    * @param name the desired element name
    * @return true if the desired element is reached
    * @throws StaxNavException any StaxNavException
    * @throws NullPointerException if the specified name is null
    */
   boolean next(N name) throws NullPointerException, StaxNavException;

   N next(Set<N> names) throws NullPointerException, StaxNavException;

   /**
    * Attempts to navigate to the first child found and return its name. If no such child exist then null
    * is returned.
    *
    * @return the child name
    * @throws StaxNavException any StaxNavException
    */
   N child() throws StaxNavException;

   /**
    * Attempts to navigate to the first child with the specified name.
    * If the navigation occurs, the navigator now points to that element and the method returns true.
    * Otherwise no navigation happen and the method return false.
    *
    *
    * @param name the child name
    * @return true if the desired element is reached
    * @throws NullPointerException if the name argument is null
    * @throws StaxNavException any StaxNavException
    */
   boolean child(N name) throws NullPointerException, StaxNavException;

   /**
    * Returns an attribute of the current element or null if such attribute does not exist.
    *
    *
    * @param name the attribute name
    * @return the attribute value
    * @throws NullPointerException if the name argument is null
    * @throws IllegalStateException if no element is currently navigated
    * @throws StaxNavException any StaxNavException
    */
   String getAttribute(String name) throws NullPointerException, IllegalStateException, StaxNavException;

   String getAttribute(QName name) throws NullPointerException, IllegalStateException, StaxNavException;

   /**
    * Returns a namespace URI by its prefix or return null if it is not bound.
    *
    * @param prefix the prefix
    * @return the corresponding namespace URI
    * @throws NullPointerException if the prefix is null
    * @throws StaxNavException any StaxNavException
    */
   String getNamespaceByPrefix(String prefix) throws NullPointerException, StaxNavException;

   /**
    * Attempt to navigate to the next sibling and return its name. If no such sibling exists
    * then null is returned.
    *
    * @return the next sibling name
    * @throws StaxNavException any StaxNavException
    */
   N sibling() throws StaxNavException;

   /**
    * Attempts to navigate to the next sibling with the specified name.
    * If the navigation occurs, the navigator now points to that element and the method returns true.
    * Otherwise no navigation happen and the method return false.
    *
    * @param name the next sibling name
    * @return true if the desired element is reached
    * @throws NullPointerException if the name argument is null
    * @throws StaxNavException any StaxNavException
    */
   boolean sibling(N name) throws NullPointerException, StaxNavException;

   /**
    * Attempts to navigate to the first descendant with the specified name. The returned value should be interpreted as:
    * <ul>
    * <li>a negative value means that no navigation occured</li>
    * <li>any other value is the difference of depth between the two elements</li>
    * </ul>
    *
    * @param name the descendant name
    * @return the
    * @throws NullPointerException if the name is null
    * @throws StaxNavException any StaxNavException
    */
   int descendant(N name) throws NullPointerException, StaxNavException;
}
