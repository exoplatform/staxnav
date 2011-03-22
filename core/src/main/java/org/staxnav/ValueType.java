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

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;
import java.util.Date;

/**
 * The type of a value.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
//TODO: This seems more like a converter then a ValueType...possibly change the class name to something more meaningful ?
public abstract class ValueType<V>
{
   /**
    * Returns a value type for an enum class.
    *
    * @param enumClass the enum class
    * @param <E> the enum parameter type
    * @return the corresponding value type
    */
   public static <E extends Enum<E>> ValueType<E> get(Class<E> enumClass)
   {
      return new EnumType<E>(enumClass);
   }

   public static final ValueType<String> STRING = new ValueType<String>()
   {
      @Override
      protected String parse(String s) throws Exception
      {
         return s;
      }

      @Override
      protected String print(String value) throws StaxNavException
      {
         return value;
      }
   };

   public static final ValueType<String> TRIMMED_STRING = new ValueType<String>()
   {
      @Override
      protected String parse(String s) throws Exception
      {
         return s.trim();
      }

      @Override
      protected String print(String value) throws StaxNavException
      {
         return value.trim();
      }
   };

   public static final ValueType<Boolean> BOOLEAN = new ValueType<Boolean>()
   {
      @Override
      protected Boolean parse(String s) throws Exception
      {
         return Boolean.parseBoolean(s.trim());
      }

      @Override
      protected String print(Boolean value) throws StaxNavException
      {
         return value.toString();
      }
   };

   public static final ValueType<Integer> INTEGER = new ValueType<Integer>()
   {
      @Override
      protected Integer parse(String s) throws Exception
      {
         return Integer.parseInt(s.trim());
      }

      @Override
      protected String print(Integer value) throws StaxNavException
      {
         return value.toString();
      }
   };

   public static final ValueType<Date> DATE = new ValueType<Date>()
   {
      @Override
      protected Date parse(String s) throws Exception
      {
         return DatatypeConverter.parseDate(s).getTime();
      }

      @Override
      protected String print(Date value) throws StaxNavException
      {
         Calendar cal = Calendar.getInstance();
         cal.setTime(value);
         return DatatypeConverter.printDate(cal);
      }
   };

   public static final ValueType<Date> DATE_TIME = new ValueType<Date>()
   {
      @Override
      protected Date parse(String s) throws Exception
      {
         return DatatypeConverter.parseDateTime(s).getTime();
      }

      @Override
      protected String print(Date value) throws StaxNavException
      {
         Calendar cal = Calendar.getInstance();
         cal.setTime(value);
         return DatatypeConverter.printDateTime(cal);
      }
   };

   protected static class EnumType<E extends Enum<E>> extends ValueType<E>
   {

      /** . */
      private final Class<E> enumType;

      public EnumType(Class<E> enumType)
      {
         this.enumType = enumType;
      }

      @Override
      protected E parse(String s) throws Exception
      {
         return Enum.valueOf(enumType, s.trim());
      }

      @Override
      protected String print(E value) throws StaxNavException
      {
         return value.name();
      }
   }

   protected ValueType()
   {
   }

   /**
    * Parse the string to the java type for a non null parameter value.
    *
    * @param s the string value to parse
    * @return the parsed value
    * @throws Exception any exception that would prevent the type conversion to happen
    */
   protected abstract V parse(String s) throws Exception;

   // Following DatatypeConverter naming, don't like the name print tho...
   protected abstract String print(V value) throws StaxNavException;

}
