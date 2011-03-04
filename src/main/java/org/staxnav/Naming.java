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
public abstract class Naming<N>
{

   abstract String getLocalPart(N name);

   abstract String getURI(N name);

   abstract String getPrefix(N name);

   abstract N getName(QName name);

   abstract N getName(String uri, String prefix, String localPart);

   public static class Local extends Naming<String>
   {

      @Override
      protected String getName(String uri, String prefix, String localPart)
      {
         return localPart;
      }

      @Override
      String getName(QName name)
      {
         return name == null ? null : name.getLocalPart();
      }

      @Override
      protected String getPrefix(String name)
      {
         return "";
      }

      @Override
      protected String getURI(String name)
      {
         return null;
      }

      @Override
      protected String getLocalPart(String name)
      {
         return name;
      }
   }

   public static class Qualified extends Naming<QName>
   {

      @Override
      protected QName getName(String uri, String prefix, String localPart)
      {
         return prefix != null ? new QName(uri, localPart, prefix) : new QName(uri, localPart);
      }

      @Override
      QName getName(QName name)
      {
         return name;
      }

      @Override
      protected String getURI(QName name)
      {
         return name != null ? name.getNamespaceURI() : null;
      }

      @Override
      protected String getPrefix(QName name)
      {
         return name != null ? name.getPrefix() : null;
      }

      @Override
      protected String getLocalPart(QName name)
      {
         return name != null ? name.getLocalPart() : null;
      }
   }

   public static class Enumerated<E extends Enum<E> & EnumElement<E>> extends Naming<E>
   {

      /** . */
      private final Class<E> enumType;
      private final E noSuchElement;

      public Enumerated(Class<E> enumType, E noSuchElement)
      {
         this.enumType = enumType;
         this.noSuchElement = noSuchElement;
      }

      @Override
      E getName(QName name)
      {
         return name == null ? null : getName(null, null, name.getLocalPart());
      }

      @Override
      protected String getLocalPart(E name)
      {
         return name.getLocalName();
      }

      @Override
      protected String getURI(E name)
      {
         return null;
      }

      @Override
      protected String getPrefix(E name)
      {
         return "";
      }

      @Override
      protected E getName(String uri, String prefix, String localPart)
      {
         for (E e : enumType.getEnumConstants())
         {
            if (localPart.equals(e.getLocalName()))
            {
               return e;
            }
         }

         return noSuchElement;
      }
   }
}
