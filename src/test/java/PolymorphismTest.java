
// PolymorphismTest.java --
//
// PolymorphismTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2013 Electric Cloud, Inc.
// All rights reserved.
//

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

@RunWith(MockitoJUnitRunner.class)
public class PolymorphismTest
{

    //~ Static fields/initializers ---------------------------------------------

    private static final String JSON =
        "{\"@class\":\"PolymorphismTest$PropertySheetImpl\",\"properties\":{\"p2name\":{\"@class\":\"PolymorphismTest$StringPropertyImpl\",\"value\":\"p2value\",\"name\":\"p2name\"},\"p1name\":{\"@class\":\"PolymorphismTest$StringPropertyImpl\",\"value\":\"p1value\",\"name\":\"p1name\"}}}";

    //~ Instance fields --------------------------------------------------------

    private ObjectMapper m_mapper = new ObjectMapper();

    //~ Methods ----------------------------------------------------------------

    @Test public void deserialize()
        throws IOException
    {
        assertEquals(JSON,
            m_mapper.writeValueAsString(
                m_mapper.readValue(JSON, PropertySheet.class)));
    }

    @Test public void serialize()
        throws IOException
    {
        PropertySheet sheet = new PropertySheetImpl();

        sheet.addProperty(new StringPropertyImpl("p1name", "p1value"));
        sheet.addProperty(new StringPropertyImpl("p2name", "p2value"));
        assertEquals(JSON, m_mapper.writeValueAsString(sheet));
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
        use      = CLASS,
        include  = PROPERTY,
        property = "@class"
    )
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
        use      = CLASS,
        include  = PROPERTY,
        property = "@class"
    )
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

        private String        m_name;
        private PropertySheet m_parentSheet;

        //~ Constructors -------------------------------------------------------

        protected AbstractProperty() { }

        protected AbstractProperty(String name)
        {
            m_name = name;
        }

        //~ Methods ------------------------------------------------------------

        @Override public String getName()
        {
            return m_name;
        }

        @Override public PropertySheet getParentSheet()
        {
            return m_parentSheet;
        }

        public void setName(String name)
        {
            m_name = name;
        }

        @Override public void setParentSheet(PropertySheet parentSheet)
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
                String        name,
                PropertySheet propertySheet)
        {
            super(name);
            m_propertySheet = propertySheet;
        }

        NestedPropertySheetImpl() { }

        //~ Methods ------------------------------------------------------------

        @Override public PropertySheet getValue()
        {
            return m_propertySheet;
        }

        @Override public void setValue(PropertySheet propertySheet)
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

        @Override public void addProperty(Property property)
        {

            if (m_properties == null) {
                m_properties = new HashMap<String, Property>();
            }

            property.setParentSheet(this);
            m_properties.put(property.getName(), property);
        }

        @Override public Map<String, Property> getProperties()
        {
            return m_properties;
        }

        @Override public void setProperties(Map<String, Property> properties)
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

        StringPropertyImpl() { }

        //~ Methods ------------------------------------------------------------

        @Override public String getValue()
        {
            return m_value;
        }

        @Override public void setValue(String value)
        {
            m_value = value;
        }
    }
}
