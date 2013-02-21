
// Polymorphism2Test.java --
//
// Polymorphism2Test.java is part of ElectricCommander.
//
// Copyright (c) 2005-2013 Electric Cloud, Inc.
// All rights reserved.
//

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import static org.junit.Assert.assertEquals;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("UnusedDeclaration")
public class Polymorphism2Test
{

    //~ Static fields/initializers ---------------------------------------------

    private static final String JSON =
        "{\"@class\":\"Polymorphism2Test$PropertySheetImpl\","
            + "\"properties\":"
            + "{\"p1name\":{\"@class\":\"Polymorphism2Test$StringPropertyImpl\",\"value\":\"p1value\",\"name\":\"p1name\",\"id\":0},"
            + "\"p2name\":{\"@class\":\"Polymorphism2Test$StringPropertyImpl\",\"value\":\"p2value\",\"name\":\"p2name\",\"id\":0}},"
            + "\"id\":0}";

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

    interface Entity
    {

        //~ Methods ------------------------------------------------------------

        @JsonIgnore String getEntityType();

        Long getId();

        void setId(Long id);

        @JsonIgnore void setPersistable();
    }

    @JsonDeserialize(as = NestedPropertySheetImpl.class)
    interface NestedPropertySheet
        extends Property<PropertySheet>
    {

        //~ Methods ------------------------------------------------------------

        PropertySheet getValue();

        void setValue(PropertySheet propertySheet);
    }

    @JsonDeserialize(as = AbstractProperty.class)
    @JsonTypeInfo(
        use      = CLASS,
        include  = PROPERTY,
        property = "@class"
    )
    interface Property<T>
        extends Entity
    {

        //~ Methods ------------------------------------------------------------

        String getName();

        PropertySheet getParentSheet();

        T getValue();

        void setName(String name);

        void setParentSheet(PropertySheet parentSheet);
    }

    @JsonDeserialize(as = PropertySheetImpl.class)
    @JsonTypeInfo(
        use      = CLASS,
        include  = PROPERTY,
        property = "@class"
    )
    interface PropertySheet
        extends Entity
    {

        //~ Methods ------------------------------------------------------------

        void addProperty(Property property);

        Map<String, Property> getProperties();

        void setProperties(Map<String, Property> properties);
    }

    @JsonDeserialize(as = StringPropertyImpl.class)
    interface StringProperty
        extends Property<String>
    {

        //~ Methods ------------------------------------------------------------

        String getValue();

        void setValue(String value);
    }

    //~ Inner Classes ----------------------------------------------------------

    static class AbstractEntity
        implements Entity
    {

        //~ Instance fields ----------------------------------------------------

        private long m_id;

        //~ Methods ------------------------------------------------------------

        @Override public String getEntityType()
        {
            return "";
        }

        @Override public Long getId()
        {
            return m_id;
        }

        @Override public void setId(Long id)
        {
            m_id = id;
        }

        @Override public void setPersistable() { }
    }

    abstract static class AbstractProperty<T>
        extends AbstractEntity
        implements Property<T>
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

        @JsonBackReference("propertySheet-properties")
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
        extends AbstractProperty<PropertySheet>
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
        extends AbstractEntity
        implements PropertySheet
    {

        //~ Instance fields ----------------------------------------------------

        private Map<String, Property> m_properties;

        //~ Methods ------------------------------------------------------------

        @Override public void addProperty(Property property)
        {

            if (m_properties == null) {
                m_properties = new TreeMap<String, Property>();
            }

            property.setParentSheet(this);
            m_properties.put(property.getName(), property);
        }

        @JsonDeserialize(
            as        = TreeMap.class,
            keyAs     = String.class,
            contentAs = Property.class
        )
        @JsonManagedReference("propertySheet-properties")
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
        extends AbstractProperty<String>
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

    static class YetAnotherClass
        extends StringPropertyImpl { }
}
