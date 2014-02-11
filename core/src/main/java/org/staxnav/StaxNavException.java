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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class StaxNavException extends RuntimeException
{

   /** . */
   private final Location location;

   public Location getLocation()
   {
      return location;
   }

   public StaxNavException(Location location)
   {
      this.location = location;
   }

   public StaxNavException(Location location, String message)
   {
      super(message);

      //
      this.location = location;
   }

   public StaxNavException(Location location, Throwable cause)
   {
      super(cause);

      //
      this.location = location;
   }

   public StaxNavException(XMLStreamException cause)
   {
      super(cause);

      //
      this.location = cause.getLocation();
   }

   @Override
   public String getMessage()
   {
      String message = super.getMessage();
      if (location != null && ! (getCause() instanceof XMLStreamException))
      {
         StringBuilder sb = new StringBuilder().append(message);
         sb.append(" at [row,col]:[")
           .append(location.getLineNumber())
           .append(",")
           .append(location.getColumnNumber())
           .append("]");
         String systemId = location.getSystemId();
         if (systemId != null)
         {
            sb.append(" found at ").append(systemId);
         }
         return sb.toString();
      }
      else
      {
         return message;
      }
   }
}
