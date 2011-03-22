/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

import junit.framework.TestCase;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class StaxWriterTestCase<N> extends TestCase
{
   static final String ENCODING = "UTF-8";
   static final String VERSION = "1.0";

   private Naming<N> naming;
   protected XMLStreamWriter stream;
   protected XmlStreamingFormatter formatter;
   protected StaxWriter<N> writer;

   public abstract Naming<N> getNaming();

   @Override
   public void setUp() throws Exception
   {
      naming = getNaming();
      formatter = mock(XmlStreamingFormatter.class);
      stream = mock(XMLStreamWriter.class);
      writer = new StaxWriterImpl<N>(naming, stream, formatter, ENCODING, VERSION);
   }

   protected N createName(String localPart)
   {
      return naming.getName(null, null, localPart);
   }

   public void testEncodingAndVersion() throws Exception
   {
      String encoding = "UTF-8";
      String version = "1.0";
      StaxWriter<N> writer = new StaxWriterImpl<N>(naming, stream, formatter, encoding, version);
      writer.writeElement(createName("foo"), "blah");

      verify(stream).writeStartDocument(encoding, version);

      reset(stream);
      encoding = "encoding";
      version = "version";
      writer = new StaxWriterImpl<N>(naming, stream, formatter, encoding, version);
      writer.writeElement(createName("foo"), "blah");

      verify(stream).writeStartDocument(encoding, version);
   }

   public void testEncoding() throws Exception
   {
      String encoding = "ISO-8859-1";
      StaxWriter<N> writer = new StaxWriterImpl<N>(naming, stream, formatter, encoding, null);
      writer.writeElement(createName("foo"), "blah");

      verify(stream).writeStartDocument(encoding, null);
   }

   public void testNoEncoding() throws Exception
   {
      StaxWriter<N> writer = new StaxWriterImpl<N>(naming, stream, formatter);
      writer.writeElement(createName("foo"), "blah");

      verify(stream).writeStartDocument();
   }

   public void testStartDocument() throws Exception
   {
      writer.writeStartElement(createName("foo"));
      writer.writeComment("some comment");
      writer.writeCData("some cdata < & ...");

      verify(stream, times(1)).writeStartDocument(ENCODING, VERSION);

   }

   public void testWriteStartElement() throws Exception
   {
      writer.writeStartElement(createName("foo"));
      verify(stream).writeStartElement("foo");
   }

   public void testWriteAttribute() throws Exception
   {
      writer.writeStartElement(createName("foo")).writeAttribute("name", "value");

      verify(stream).writeAttribute("name", "value");
   }

   public void testWriteQNameAttribute() throws Exception
   {
      writer.writeStartElement(createName("foo"))
         .writeAttribute(new QName("uri", "local", "pre"), "value")
         .writeAttribute(new QName("uri", "local"), "value");

      verify(stream).writeAttribute("pre", "uri", "local", "value");
      verify(stream).writeAttribute("uri", "local", "value");
   }

   public void testWriteElement() throws Exception
   {
      writer.writeElement(createName("foo"), "content");

      verify(stream).writeStartElement("foo");
      verify(stream).writeCharacters("content");
      verify(stream).writeEndElement();
   }

   public void testWriteContent() throws Exception
   {

      writer.writeStartElement(createName("foo")).writeContent("blah");
      verify(stream).writeCharacters("blah");

      ValueType<String> vt = new ValueType<String>()
      {
         @Override
         protected String parse(String s) throws Exception
         {
            return null;
         }

         @Override
         protected String print(String value) throws StaxNavException
         {
            return "some value";
         }
      };

      writer.writeStartElement(createName("bar")).writeContent(vt, "content");
      verify(stream).writeCharacters("some value");
   }

   public void testNamespace() throws Exception
   {
      writer.writeStartElement(createName("foo")).writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);

      verify(stream).writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
   }

   public void testDefaultNamespace() throws Exception
   {
      String namespace = "http://www.example.com/ns/mynamespace";

      writer.writeStartElement(createName("foo")).writeDefaultNamespace(namespace);

      verify(stream).writeDefaultNamespace(namespace);
   }

   public void testComment() throws Exception
   {
      writer.writeComment("Some comment");
      writer.writeStartElement(createName("foo"));

      verify(stream, times(1)).writeStartDocument(ENCODING, VERSION);
      verify(stream).writeComment("Some comment");
   }

   public void testCData() throws Exception
   {
      String cdata = "Some cdata stuff here < & \" ";
      writer.writeCData(cdata);
      writer.writeStartElement(createName("foo"));

      verify(stream).writeStartDocument(ENCODING, VERSION);
      verify(stream).writeCData(cdata);
   }

   public void testFinish() throws Exception
   {
      writer.writeStartElement(createName("foo")).finish();
      
      verify(stream).flush();
      verify(stream).close();
   }

   public void testFinishNoElements() throws Exception
   {
      writer.writeComment("comment");
      writer.finish();
      verify(stream).flush();
      verify(stream).close();
   }

   public void testEndElementOnFinish() throws Exception
   {
      writer.writeStartElement(createName("foo")).
         writeStartElement(createName("bar")).
         writeElement(createName("foobar"), "stuff");

      // Even though we never explicitly call endElement for the first two start elements, finish will do that for us.
      writer.finish();

      verify(stream).writeStartElement("foo");
      verify(stream).writeStartElement("bar");
      verify(stream).writeStartElement("foobar");
      verify(stream).writeCharacters("stuff");
      verify(stream, times(3)).writeEndElement();
   }

   public static class LocalStaxWriterTestCase extends StaxWriterTestCase<String>
   {
      @Override
      public Naming<String> getNaming()
      {
         return new Naming.Local();
      }
   }

   public static class QualifiedStaxWriterTestCase extends StaxWriterTestCase<QName>
   {
      @Override
      public Naming<QName> getNaming()
      {
         return new Naming.Qualified();
      }

      public void testWriteQualifiedStartElement() throws Exception
      {
         QName bar = new QName("uri", "pre", "bar");

         writer.writeStartElement(bar);
         verify(stream).writeStartElement(bar.getPrefix(), bar.getLocalPart(), bar.getNamespaceURI());

         QName foobar = new QName("http://www.gatein.org/xml/ns/gatein_objects_1_0", "foobar");
         writer.writeStartElement(foobar);
         verify(stream).writeStartElement(foobar.getNamespaceURI(), foobar.getLocalPart());
      }
   }
}
