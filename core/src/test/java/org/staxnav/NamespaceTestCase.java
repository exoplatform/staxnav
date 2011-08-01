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

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NamespaceTestCase extends StaxNavigatorTestCase
{

   public void testA() throws Exception
   {
      StaxNavigator<String> navigator = navigator(new Naming.Local(), "namespace1.xml");
      assertEquals("foo", navigator.getName());
      assertEquals("http://www.w3.org/TR/html4/", navigator.getNamespaceByPrefix(""));
      assertEquals(null, navigator.getNamespaceByPrefix("ns"));
      assertEquals("bar", navigator.next());
      assertEquals("http://www.w3.org/TR/html4/", navigator.getNamespaceByPrefix(""));
      assertEquals(null, navigator.getNamespaceByPrefix("ns"));
   }

   public void testB() throws Exception
   {
      StaxNavigator<String> navigator = navigator(new Naming.Local(), "namespace2.xml");
      assertEquals("foo", navigator.getName());
      assertEquals("http://www.w3.org/TR/html4/", navigator.getNamespaceByPrefix("ns"));
      assertEquals("bar", navigator.next());
      assertEquals("http://www.w3.org/2000/svg", navigator.getNamespaceByPrefix("ns"));
      assertEquals("juu", navigator.next());
      assertEquals("http://www.w3.org/2000/svg", navigator.getNamespaceByPrefix("ns"));
   }

   static enum Name implements EnumElement<Name>
   {
      FOO("foo"), BAR("bar"), JUU("juu"), NOT_FOUND(null);

      private String localName;

      Name(String localName)
      {
         this.localName = localName;
      }

      public String getLocalName()
      {
         return localName;
      }
   }

   public void testC() throws Exception
   {
      StaxNavigator<Name> navigator = navigator(new Naming.Enumerated.Mapped<Name>(Name.class, Name.NOT_FOUND), "namespace1.xml");
      assertEquals(Name.FOO, navigator.getName());
      assertEquals(true, navigator.next(Name.BAR));
      assertEquals(Name.NOT_FOUND, navigator.next());
   }

   public void testD() throws Exception
   {
      StaxNavigator<String> navigator = navigator(new Naming.Local(), "namespace1.xml");
      assertEquals("foo", navigator.getName());
      assertEquals(true, navigator.next("bar"));
   }

   public void testE() throws Exception
   {
      StaxNavigator<String> navigator = navigator(new Naming.Local(), "namespace3.xml");
      assertEquals("foo", navigator.getName());
      assertEquals(true, navigator.next("bar"));
      assertEquals("juu_value", navigator.getAttribute("juu"));
      assertEquals("juu_value", navigator.getAttribute(new QName("", "juu")));
      assertEquals("ns_juu_value", navigator.getAttribute(new QName("http://www.w3.org/2000/svg", "juu")));
   }

   public void testF() throws Exception
   {
      StaxNavigator<String> navigator = navigator(new Naming.Local(), "namespace3.xml");
      assertEquals("foo", navigator.getName());
      assertEquals(true, navigator.next("bar"));

      assertEquals(1, navigator.getAttributes().size());
      assertEquals("juu_value", navigator.getAttributes().get("juu"));

      assertEquals(2, navigator.getQualifiedAttributes().size());
      assertEquals("juu_value", navigator.getQualifiedAttributes().get(new QName("", "juu")));
      assertEquals("ns_juu_value", navigator.getQualifiedAttributes().get(new QName("http://www.w3.org/2000/svg", "juu")));
   }

   public void testG() throws Exception
   {
      StaxNavigator<String> navigator = navigator(new Naming.Local(), "namespace3.xml");
      assertEquals("foo", navigator.getName());
      assertEquals(true, navigator.find("noNs"));

      assertEquals(1, navigator.getAttributes().size());
      assertEquals(1, navigator.getQualifiedAttributes().size());
      assertEquals("juu_value", navigator.getQualifiedAttributes().get(new QName("", "juu")));
   }
}
