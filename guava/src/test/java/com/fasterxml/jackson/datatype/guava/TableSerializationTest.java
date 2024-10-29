package com.fasterxml.jackson.datatype.guava;

import com.google.common.collect.HashBasedTable;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;


public class TableSerializationTest extends ModuleTestBase
{
    private final ObjectMapper MAPPER = mapperWithModule(false);
    {
        MAPPER.registerModule(new ComplexKeyModule());
    }

    static class ComplexKeyModule extends SimpleModule
    {
        private static final long serialVersionUID = 1L;

        public ComplexKeyModule()
        {
            this.addKeySerializer(ComplexKey.class, new JsonSerializer<ComplexKey>() {
                @Override
                public void serialize( final ComplexKey value, final JsonGenerator g, final SerializerProvider provider )
                    throws IOException
                {
                    g.writeFieldName(value.getKey1() + ":" + value.getKey2());
                }
            });

            this.addKeyDeserializer(ComplexKey.class, new KeyDeserializer() {
                @Override
                public Object deserializeKey( final String key, final DeserializationContext ctxt ) throws IOException
                {
                    final String[] split = key.split(":");
                    return new ComplexKey(split[0], split[1]);
                }
            });
        }
    }

    static class ComplexKey
    {
        private String key1;
        private String key2;

        public ComplexKey( final String key1, final String key2 )
        {
            super();
            this.key1 = key1;
            this.key2 = key2;
        }

        public String getKey1()
        {
            return this.key1;
        }

        public void setKey1( final String key1 )
        {
            this.key1 = key1;
        }

        public String getKey2()
        {
            return this.key2;
        }

        public void setKey2( final String key2 )
        {
            this.key2 = key2;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.key1 == null) ? 0 : this.key1.hashCode());
            result = prime * result + ((this.key2 == null) ? 0 : this.key2.hashCode());
            return result;
        }

        @Override
        public boolean equals( final Object obj )
        {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if ( !(obj instanceof ComplexKey)) {
                return false;
            }
            final ComplexKey other = (ComplexKey) obj;
            if (this.key1 == null) {
                if (other.key1 != null) {
                    return false;
                }
            }
            else if ( !this.key1.equals(other.key1)) {
                return false;
            }
            if (this.key2 == null) {
                if (other.key2 != null) {
                    return false;
                }
            }
            else if ( !this.key2.equals(other.key2)) {
                return false;
            }
            return true;
        }

    }

    public void testSimpleKeyImmutableTableSerde() throws IOException
    {
        final HashBasedTable<Integer, String, String> simpleTable = HashBasedTable.create();
        simpleTable.put(Integer.valueOf(42), "column42", "some value 42");
        simpleTable.put(Integer.valueOf(45), "column45", "some value 45");

        final String simpleJson = MAPPER.writeValueAsString(simpleTable);
        assertEquals("{\"42\":{\"column42\":\"some value 42\"},\"45\":{\"column45\":\"some value 45\"}}", simpleJson);
        
        final HashBasedTable<Integer, String, String> reconstitutedTable =
            this.MAPPER.readValue(simpleJson,
                new TypeReference<HashBasedTable<Integer, String, String>>() {
                }
            );
        assertEquals(simpleTable, reconstitutedTable);
    }

    /**
     * This test illustrates one way to use objects as keys in Tables.
     */
    public void testComplexKeyImmutableTableSerde() throws IOException
    {
        final HashBasedTable<Integer, ComplexKey, String> complexKeyTable = HashBasedTable.create();
        complexKeyTable.put(Integer.valueOf(42), new ComplexKey("field1", "field2"), "some value 42");
        complexKeyTable.put(Integer.valueOf(45), new ComplexKey("field1", "field2"), "some value 45");
        
        final TypeReference<HashBasedTable<Integer, ComplexKey, String>> tableType = new TypeReference<HashBasedTable<Integer, ComplexKey, String>>()
        {};
        
        final String ckJson = this.MAPPER.writerFor(tableType).writeValueAsString(complexKeyTable);
        assertEquals("{\"42\":{\"field1:field2\":\"some value 42\"},\"45\":{\"field1:field2\":\"some value 45\"}}", ckJson);
        
        final HashBasedTable<Integer, ComplexKey, String> reconstitutedTable = this.MAPPER.readValue(ckJson, tableType);
        assertEquals(complexKeyTable, reconstitutedTable);
    }
}
