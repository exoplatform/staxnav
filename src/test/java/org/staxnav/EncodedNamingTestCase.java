package org.staxnav;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class EncodedNamingTestCase extends StaxNavigatorTestCase
{
   private StaxNavigator<EncodedElement> navigator;

   @Override
   protected void setUp() throws Exception
   {
      navigator = navigator(new EncodedNaming(), "encoded.xml");
   }

   public void testNoSuchName()
   {
      assertTrue(navigator.next(EncodedElement.BOOK_ELEMENT));
      assertEquals(EncodedElement.BOOK_ELEMENT, navigator.sibling());
      assertEquals(EncodedElement.BOOK_ELEMENT, navigator.sibling());
      assertEquals(EncodedElement.NOT_FOUND, navigator.next());
   }

   public void testValueType()
   {
      ValueType<String> decoderValueType = new ValueType<String>()
      {
         @Override
         protected String parse(String s) throws Exception
         {
            return decode(s);
         }
      };
      assertTrue(navigator.find(EncodedElement.TITLE_ELEMENT));
      assertEquals("Title A", navigator.parseContent(decoderValueType));
      assertEquals(EncodedElement.AUTHOR_ELEMENT, navigator.next());
      assertEquals("Author A", navigator.parseContent(decoderValueType));

      assertTrue(navigator.find(EncodedElement.TITLE_ELEMENT));
      assertEquals("Title B", navigator.parseContent(decoderValueType));
      assertEquals(EncodedElement.AUTHOR_ELEMENT, navigator.next());
      assertEquals("Author B", navigator.parseContent(decoderValueType));

      assertTrue(navigator.find(EncodedElement.TITLE_ELEMENT));
      assertEquals("Title C", navigator.parseContent(decoderValueType));
      assertEquals(EncodedElement.AUTHOR_ELEMENT, navigator.next());
      assertEquals("Author C", navigator.parseContent(decoderValueType));
   }

   public void testForkMechanics()
   {
      assertEquals(EncodedElement.BOOKS_ELEMENT, navigator.getName());
      assertEquals(EncodedElement.BOOK_ELEMENT, navigator.next());
      int bookCount = 0;
      for (StaxNavigator<EncodedElement> fork : navigator.fork(EncodedElement.BOOK_ELEMENT))
      {
         bookCount++;
         int titleCount = 0;
         int authorCount = 0;
         int relatedBookCount = 0;
         int relatedBookBookCount = 0;
         int notFoundCount = 0;
         while (fork.hasNext())
         {
            EncodedElement element = fork.next();
            if (element.equals(EncodedElement.TITLE_ELEMENT))
            {
               titleCount++;
            }
            else if (element.equals(EncodedElement.AUTHOR_ELEMENT))
            {
               authorCount++;
            }
            else if (element.equals(EncodedElement.RELATED_ELEMENT))
            {
               relatedBookCount++;
            }
            else if (element.equals(EncodedElement.BOOK_ELEMENT))
            {
               relatedBookBookCount++;
            }
            else if (element.equals(EncodedElement.NOT_FOUND))
            {
               notFoundCount++;
            }
         }
         assertEquals(1, titleCount);
         assertEquals(1, authorCount);
         assertEquals(1, relatedBookCount);
         assertEquals(2, relatedBookBookCount);
         assertEquals(1, notFoundCount);
      }
      assertEquals(3, bookCount);
   }

   private static class EncodedNaming extends Naming<EncodedElement>
   {
      @Override
      String getLocalPart(EncodedElement name)
      {
         return name.encoded;
      }

      @Override
      String getURI(EncodedElement name)
      {
         return null;
      }

      @Override
      String getPrefix(EncodedElement name)
      {
         return null;
      }

      @Override
      EncodedElement getName(QName name)
      {
         return (name == null) ? null : getName(name.getNamespaceURI(), name.getPrefix(), name.getLocalPart());
      }

      @Override
      EncodedElement getName(String uri, String prefix, String localPart)
      {
         EncodedElement element = EncodedElement.MAP.get(localPart);

         return (element != null) ? element : EncodedElement.NOT_FOUND;
      }
   }

   private static class EncodedElement
   {
      private String encoded;
      private String decoded;

      EncodedElement(String localName)
      {
         encoded = localName;
         decoded = decode(localName);
      }

      @Override
      public int hashCode()
      {
         return decoded.hashCode();
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof EncodedElement)
         {
            return decoded.equals(((EncodedElement) obj).decoded);
         }
         else
         {
            return super.equals(obj);
         }
      }

      @Override
      public String toString()
      {
         return decoded;
      }

      private static final EncodedElement BOOKS_ELEMENT = new EncodedElement(encode("books"));
      private static final EncodedElement BOOK_ELEMENT = new EncodedElement(encode("book"));
      private static final EncodedElement TITLE_ELEMENT = new EncodedElement(encode("title"));
      private static final EncodedElement AUTHOR_ELEMENT = new EncodedElement(encode("author"));
      private static final EncodedElement RELATED_ELEMENT = new EncodedElement(encode("related-books"));
      private static final EncodedElement NOT_FOUND = new EncodedElement(encode("not-found"));

      private static final Map<String, EncodedElement> MAP;

      static {
         Map<String, EncodedElement> map = new HashMap<String, EncodedElement>();
         map.put(BOOKS_ELEMENT.encoded, BOOKS_ELEMENT);
         map.put(BOOK_ELEMENT.encoded, BOOK_ELEMENT);
         map.put(TITLE_ELEMENT.encoded, TITLE_ELEMENT);
         map.put(AUTHOR_ELEMENT.encoded, AUTHOR_ELEMENT);
         map.put(RELATED_ELEMENT.encoded, RELATED_ELEMENT);
         MAP = map;
      }
   }

   private static String decode(String encoded)
   {
      return new StringBuilder(encoded).reverse().toString();
   }

   private static String encode(String decoded)
   {
      return new StringBuilder(decoded).reverse().toString();
   }
}
