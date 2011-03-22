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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Naming<N>
{

   public abstract String getLocalPart(N name);

   public abstract String getURI(N name);

   public abstract String getPrefix(N name);

   public abstract N getName(QName name);

   public abstract N getName(String uri, String prefix, String localPart);

   public static class Local extends Naming<String>
   {

      @Override
      public String getName(String uri, String prefix, String localPart)
      {
         return localPart;
      }

      @Override
      public String getName(QName name)
      {
         return name == null ? null : name.getLocalPart();
      }

      @Override
      public String getPrefix(String name)
      {
         return "";
      }

      @Override
      public String getURI(String name)
      {
         return null;
      }

      @Override
      public String getLocalPart(String name)
      {
         return name;
      }
   }

   public static class Qualified extends Naming<QName>
   {

      @Override
      public QName getName(String uri, String prefix, String localPart)
      {
         return prefix != null ? new QName(uri, localPart, prefix) : new QName(uri, localPart);
      }

      @Override
      public QName getName(QName name)
      {
         return name;
      }

      @Override
      public String getURI(QName name)
      {
         return name != null ? name.getNamespaceURI() : null;
      }

      @Override
      public String getPrefix(QName name)
      {
         return name != null ? name.getPrefix() : null;
      }

      @Override
      public String getLocalPart(QName name)
      {
         return name != null ? name.getLocalPart() : null;
      }
   }

   public static abstract class Enumerated<E extends Enum<E>> extends Naming<E>
   {

      /** . */
      protected final Class<E> enumType;

      /** . */
      protected final E noSuchElement;

      protected Enumerated(Class<E> enumType, E noSuchElement)
      {
         this.enumType = enumType;
         this.noSuchElement = noSuchElement;
      }

      @Override
      public E getName(QName name)
      {
         return name == null ? null : getName(null, null, name.getLocalPart());
      }

      @Override
      public String getURI(E name)
      {
         return null;
      }

      @Override
      public String getPrefix(E name)
      {
         return "";
      }

      /**
       * <p>A naming implementation mapping enum elements to names by doing a lower case conversion
       * and substituting the hyphen character by the underscore character, for instance the enumeration:</p>
       *
       * <p><code><pre>
       * public enum MyElement
       * {
       *   FOO, BAR, FOO_BAR
       * }
       * </pre></code></p>
       *
       * <p>is mapped to the names { "foo", "bar", "foo-bar" }.</p>
       *
       * @param <E> the generic enum type
       */
      public static class Simple<E extends Enum<E>> extends Enumerated<E>
      {

         /** . */
         private final Map<String, E> toName;

         /** . */
         private final Map<E, String> toLocalPart;

         public Simple(Class<E> enumType, E noSuchElement)
         {
            super(enumType, noSuchElement);

            //
            Map<String, E> toName = new HashMap<String, E>();
            Map<E, String> toLocalPart = new EnumMap<E, String>(enumType);
            for (E value : enumType.getEnumConstants())
            {
               String localPart = value.name().toLowerCase().replace('_', '-');
               toName.put(localPart, value);
               toLocalPart.put(value, localPart);
            }

            //
            this.toName = toName;
            this.toLocalPart = toLocalPart;
         }

         @Override
         public String getLocalPart(E name)
         {
            return toLocalPart.get(name);
         }

         @Override
         public E getName(String uri, String prefix, String localPart)
         {
            E name = toName.get(localPart);
            return name != null ? name : noSuchElement;
         }
      }

      public static class Mapped<E extends Enum<E> & EnumElement<E>> extends Enumerated<E>
      {

         /** . */
         private final  E[] all;

         public Mapped(Class<E> enumType, E noSuchElement)
         {
            super(enumType, noSuchElement);

            //
            this.all = enumType.getEnumConstants();
         }

         @Override
         public String getLocalPart(E name)
         {
            return name.getLocalName();
         }

         @Override
         public E getName(String uri, String prefix, String localPart)
         {
            for (E e : all)
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

}
