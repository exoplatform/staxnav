package org.staxnav;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface StaxWriter<N>
{
   /**
    * Writes the start tag of an xml element. Requires that an element has been started first.
    *
    * @param element element to start
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   StaxWriter<N> writeStartElement(N element) throws StaxNavException;

   /**
    * Writes an attribute for an xml element. Requires that an element has been started first.
    *
    * @param name  the name of the attribute
    * @param value the value of the attribute
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   StaxWriter<N> writeAttribute(String name, String value) throws StaxNavException;

   /**
    * Writes an attribute for an xml element. Requires that an element has been started first.
    *
    * @param name  QName object representing the name of the attribute
    * @param value the value of the attribute
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   StaxWriter<N> writeAttribute(QName name, String value) throws StaxNavException;

   /**
    * Writes xml content. Requires an xml element has been started first.
    *
    * @param content content to be written
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   StaxWriter<N> writeContent(String content) throws StaxNavException;

   /**
    * Writes xml content based on the ValueType responsible for converting the content to string. Requires an xml element has been started first.
    *
    * @param valueType object responsible for writing content to string
    * @param content   content to be written
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   <V> StaxWriter<N> writeContent(ValueType<V> valueType, V content) throws StaxNavException;

   /**
    * Writes an end tag for the previously started element. Requires that an element has been started first.
    *
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   StaxWriter<N> writeEndElement() throws StaxNavException;

   /**
    * Convenience method for calling <code>writeStartElement</code>, <code>writeContent</code>, <code>writeEndElement</code>
    *
    * @param element element to write
    * @param content content to be written
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   <V> StaxWriter<N> writeElement(N element, String content) throws StaxNavException;

   /**
    * Convenience method for calling <code>writeStartElement</code>, <code>writeContent</code>, <code>writeEndElement</code>
    *
    * @param element   element to write
    * @param valueType object responsible for writing content to string
    * @param content   content to be written
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   <V> StaxWriter<N> writeElement(N element, ValueType<V> valueType, V content) throws StaxNavException;

   /**
    * Writes the namespace. If prefix is an empty string, "xmlns", or null this will delegate to writeDefaultNamespace
    * @param prefix the prefix to bind the namespace to
    * @param uri the uri to bind the prefix to
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   StaxWriter<N> writeNamespace(String prefix, String uri) throws StaxNavException;

   /**
    * Writes the default namespace
    * @param uri the uri to bind the default namespace to
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   StaxWriter<N> writeDefaultNamespace(String uri) throws StaxNavException;

   /**
    * Writes an xml comment
    * @param comment the comment to write
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   StaxWriter<N> writeComment(String comment) throws StaxNavException;

   /**
    * Writes a cdata section
    * @param cdata content of the cdata
    * @return StaxWriter
    * @throws StaxNavException if an exception occurs
    */
   StaxWriter<N> writeCData(String cdata) throws StaxNavException;

   /**
    * Calling finish will flush and close the underlying stream. It will also call any endElements for you
    * if they were never explicitly called.
    *
    * @throws StaxNavException if an exception occurs
    */
   void finish() throws StaxNavException;
}