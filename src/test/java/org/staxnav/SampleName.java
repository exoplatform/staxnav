package org.staxnav;/*
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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public enum SampleName implements EnumElement<SampleName>
{

   FOO1("foo1"), FOO2("foo2"), FOO3("foo3"),

   BAR1("bar1"), BAR2("bar2"), BAR3("bar3"),

   FOOBAR1("foobar1"), FOOBAR2("foobar2"),

   DONOTEXIST("donotexist"), BLAH("blah"), BILTO("bilto");

   private String localName;

   SampleName(String localName)
   {
      this.localName = localName;
   }

   public String getLocalName()
   {
      return localName;
   }
}
