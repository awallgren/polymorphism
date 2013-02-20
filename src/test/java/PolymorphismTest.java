// PolymorphismTest.java --
//
// PolymorphismTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2013 Electric Cloud, Inc.
// All rights reserved.
//

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(MockitoJUnitRunner.class)
public class PolymorphismTest
{
    //~ Methods ----------------------------------------------------------------

    @Test
    public void polymorphism()
        throws IOException
    {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        PropertySheet sheet = new PropertySheetImpl();
        sheet.addProperty(new StringPropertyImpl("p1name", "p1value"));
        sheet.addProperty(new StringPropertyImpl("p2name", "p2value"));
        HttpOutputMessage outputMessage = mock(HttpOutputMessage.class);
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        StringWriter writer = new StringWriter();
        when(outputMessage.getHeaders()).thenReturn(new HttpHeaders());
        when(outputMessage.getBody()).thenReturn(new WriterOutputStream(
            writer));
        converter.write(sheet, APPLICATION_JSON, outputMessage);
        String output = writer.toString();
        assertEquals(
            "{\"@class\":\"PolymorphismTest$PropertySheetImpl\",\"properties\":{\"p2name\":{\"@class\":\"PolymorphismTest$StringPropertyImpl\",\"value\":\"p2value\",\"name\":\"p2name\"},\"p1name\":{\"@class\":\"PolymorphismTest$StringPropertyImpl\",\"value\":\"p1value\",\"name\":\"p1name\"}}}",
            output);
        StringReader reader = new StringReader(output);
        when(inputMessage.getBody()).thenReturn(new ReaderInputStream(reader));
        writer = new StringWriter();
        when(outputMessage.getBody()).thenReturn(new WriterOutputStream(
            writer));
        converter.write(converter.read(PropertySheet.class, inputMessage),
            APPLICATION_JSON, outputMessage);
        assertEquals(output, writer.toString());
    }
    //~ Inner Interfaces -------------------------------------------------------

    interface NestedPropertySheet
        extends Property
    {
        //~ Methods ------------------------------------------------------------

        PropertySheet getValue();

        void setValue(PropertySheet propertySheet);
    }

    @JsonTypeInfo(
        use = CLASS,
        include = PROPERTY,
        property = "@class")
    interface Property
    {
        //~ Methods ------------------------------------------------------------

        String getName();

        @JsonBackReference("propertySheet-properties")
        PropertySheet getParentSheet();

        void setName(String name);

        void setParentSheet(PropertySheet parentSheet);
    }

    @JsonTypeInfo(
        use = CLASS,
        include = PROPERTY,
        property = "@class")
    interface PropertySheet
    {
        //~ Methods ------------------------------------------------------------

        void addProperty(Property property);

        @JsonManagedReference("propertySheet-properties")
        Map<String, Property> getProperties();

        void setProperties(Map<String, Property> properties);
    }

    interface StringProperty
        extends Property
    {
        //~ Methods ------------------------------------------------------------

        String getValue();

        void setValue(String value);
    }
    //~ Inner Classes ----------------------------------------------------------

    static class AbstractProperty
        implements Property
    {
        //~ Instance fields ----------------------------------------------------
        private String m_name;
        private PropertySheet m_parentSheet;
        //~ Constructors -------------------------------------------------------

        protected AbstractProperty()
        {
        }

        protected AbstractProperty(String name)
        {
            m_name = name;
        }
        //~ Methods ------------------------------------------------------------

        @Override
        public String getName()
        {
            return m_name;
        }

        @Override
        public PropertySheet getParentSheet()
        {
            return m_parentSheet;
        }

        public void setName(String name)
        {
            m_name = name;
        }

        @Override
        public void setParentSheet(PropertySheet parentSheet)
        {
            m_parentSheet = parentSheet;
        }
    }

    static class NestedPropertySheetImpl
        extends AbstractProperty
        implements NestedPropertySheet
    {
        //~ Instance fields ----------------------------------------------------
        private PropertySheet m_propertySheet;
        //~ Constructors -------------------------------------------------------

        protected NestedPropertySheetImpl(
            String name,
            PropertySheet propertySheet)
        {
            super(name);
            m_propertySheet = propertySheet;
        }

        NestedPropertySheetImpl()
        {
        }
        //~ Methods ------------------------------------------------------------

        @Override
        public PropertySheet getValue()
        {
            return m_propertySheet;
        }

        @Override
        public void setValue(PropertySheet propertySheet)
        {
            m_propertySheet = propertySheet;
        }
    }

    static class PropertySheetImpl
        implements PropertySheet
    {
        //~ Instance fields ----------------------------------------------------
        private Map<String, Property> m_properties;
        //~ Methods ------------------------------------------------------------

        @Override
        public void addProperty(Property property)
        {
            if (m_properties == null) {
                m_properties = new HashMap<String, Property>();
            }
            property.setParentSheet(this);
            m_properties.put(property.getName(), property);
        }

        @Override
        public Map<String, Property> getProperties()
        {
            return m_properties;
        }

        @Override
        public void setProperties(Map<String, Property> properties)
        {
            m_properties = properties;
        }
    }

    static class StringPropertyImpl
        extends AbstractProperty
        implements StringProperty
    {
        //~ Instance fields ----------------------------------------------------
        private String m_value;
        //~ Constructors -------------------------------------------------------

        public StringPropertyImpl(
            String name,
            String value)
        {
            super(name);
            m_value = value;
        }

        StringPropertyImpl()
        {
        }
        //~ Methods ------------------------------------------------------------

        @Override
        public String getValue()
        {
            return m_value;
        }

        @Override
        public void setValue(String value)
        {
            m_value = value;
        }
    }
}
