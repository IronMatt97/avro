package org.apache.avro.generic;

import org.apache.avro.AvroMissingFieldException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

@RunWith(Parameterized.class)
public class GenericDataGetDefaultValueTest {
  Field field;
  Object expectedParam;

  public GenericDataGetDefaultValueTest(TestInput t){
    this.field = t.field;
    this.expectedParam = t.expectedParam;
  }

  public static Collection<TestInput[]> configure() {

    //Parametri di configurazione
    Collection<TestInput> inputs = new ArrayList<>();
    Collection<TestInput[]> result = new ArrayList<>();

    Schema sc = SchemaBuilder.record("test").namespace("ns").fields().
      name("test").type().intType().intDefault(1).
      endRecord();
    Field f = sc.getField("test");

    /*
    Test 1 -> Caso normale
     */
    inputs.add(new TestInput(f,1));

    /*
    Test 2 -> Field non valido (Branch 1205)
     */
    inputs.add(new TestInput(new Field("INT",Schema.create(Schema.Type.INT)),0));

    /*
    Test 3 -> Field di tipo nullo (Branch 1207)
     */
    sc = SchemaBuilder.record("test").namespace("ns").fields().
      name("test").type().nullType().nullDefault().
      endRecord();
    f = sc.getField("test");
    inputs.add(new TestInput(f,null));

    /*
    Test 4 -> Field di tipo union con campo nullo (Branch 1208)
     */
    sc = SchemaBuilder.record("test").namespace("ns").fields().
      name("test").type().unionOf().nullType().endUnion().nullDefault().
      endRecord();
    f = sc.getField("test");
    inputs.add(new TestInput(f,null));


    for (TestInput e : inputs) {
      result.add(new TestInput[] { e });
    }
    return result;

  }
  @Parameters
  public static Collection<TestInput[]> getTestParameters() {
    return configure();
  }
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testGetDefaultValue() {

    if (field.pos() == -1)
      thrown.expect(AvroMissingFieldException.class);
    Object o = new GenericData().getDefaultValue(field);
    Assert.assertEquals(expectedParam, o);
  }

  private static class TestInput {
    Field field;
    Object expectedParam;

    public TestInput(Field field, Object expectedParam) {
      this.field = field;
      this.expectedParam = expectedParam;
    }
  }
}
