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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class StaxNavigatorFactory
{

   public static <N> StaxNavigator<N> create(Naming<N> naming, XMLStreamReader stream) throws NullPointerException, StaxNavException
   {
      if (naming == null)
      {
         throw new NullPointerException("No null naming accepted");
      }
      if (stream == null)
      {
         throw new NullPointerException("No null stream accepted");
      }
      try
      {
         return new StaxNavigatorImpl<N>(naming, stream);
      }
      catch (XMLStreamException e)
      {
         throw new StaxNavException(e);
      }
   }

   public static <N> StaxNavigator<N> create(Naming<N> naming, InputStream is) throws NullPointerException, StaxNavException
   {
      if (naming == null)
      {
         throw new NullPointerException("No null naming accepted");
      }
      if (is == null)
      {
         throw new NullPointerException("No null input stream accepted");
      }
      try
      {
         XMLInputFactory factory = XMLInputFactory.newInstance();
         XMLStreamReader stream = factory.createXMLStreamReader(is);
         return new StaxNavigatorImpl<N>(naming, stream);
      }
      catch (XMLStreamException e)
      {
         throw new StaxNavException(e);
      }
   }

   public static <N> StaxNavigator<N> create(Naming<N> naming, Reader reader) throws NullPointerException, StaxNavException
   {
      if (naming == null)
      {
         throw new NullPointerException("No null naming accepted");
      }
      if (reader == null)
      {
         throw new NullPointerException("No null reader accepted");
      }
      try
      {
         XMLInputFactory factory = XMLInputFactory.newInstance();
         XMLStreamReader stream = factory.createXMLStreamReader(reader);
         return new StaxNavigatorImpl<N>(naming, stream);
      }
      catch (XMLStreamException e)
      {
         throw new StaxNavException(e);
      }
   }
}
