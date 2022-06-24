package org.apache.avro.io.parsing;

import org.apache.avro.Schema;
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
public class ValidatingGrammarGeneratorGenerateTest {
  Schema sc;
  Map<ValidatingGrammarGenerator.LitS, Symbol> seen;
  Object expectedParam;

  public ValidatingGrammarGeneratorGenerateTest(TestInput t){
    this.sc = t.sc;
    this.seen = t.seen;
    this.expectedParam = t.expectedParam;
  }

  public static Collection<TestInput[]> configure() {

    //Parametri di configurazione
    Collection<TestInput> inputs = new ArrayList<>();
    Collection<TestInput[]> result = new ArrayList<>();
    Schema sc;
    Map<ValidatingGrammarGenerator.LitS, Symbol> seen;

    //Schema mock = Mockito.mock(Schema.class);
    //Mockito.when(mock.getName()).thenReturn("nullTestType");

    /*
    Test 1/8 -> casi semplici di ritorno simbolo
    */
    inputs.add(new TestInput(Schema.create(Schema.Type.NULL),null,Symbol.NULL));
    inputs.add(new TestInput(Schema.create(Schema.Type.BOOLEAN),null,Symbol.BOOLEAN));
    inputs.add(new TestInput(Schema.create(Schema.Type.INT),null,Symbol.INT));
    inputs.add(new TestInput(Schema.create(Schema.Type.LONG),null,Symbol.LONG));
    inputs.add(new TestInput(Schema.create(Schema.Type.FLOAT),null,Symbol.FLOAT));
    inputs.add(new TestInput(Schema.create(Schema.Type.DOUBLE),null,Symbol.DOUBLE));
    inputs.add(new TestInput(Schema.create(Schema.Type.STRING),null,Symbol.STRING));
    inputs.add(new TestInput(Schema.create(Schema.Type.BYTES),null,Symbol.BYTES));

    /*
    Test 9/13 -> casi di FIXED, ENUM, ARRAY, MAP e UNION
    */
    inputs.add(new TestInput(Schema.createFixed("test","","",1),null, Symbol.Kind.SEQUENCE));
    inputs.add(new TestInput(Schema.createEnum("test","","", new ArrayList<>()),null, Symbol.Kind.SEQUENCE));
    inputs.add(new TestInput(Schema.createArray(Schema.create(Schema.Type.BOOLEAN)),null, Symbol.Kind.SEQUENCE));
    inputs.add(new TestInput(Schema.createMap(Schema.create(Schema.Type.BOOLEAN)),null, Symbol.Kind.SEQUENCE));

    sc = Schema.createUnion(Schema.create(Schema.Type.LONG),Schema.create(Schema.Type.INT));
    inputs.add(new TestInput(sc,null,Symbol.Kind.SEQUENCE));

    /*
    Test 14 -> Caso per RECORD: Lo schema è già presente nell'hashmap, quindi viene ritornato
    */
    sc = SchemaBuilder.record("record_writer").namespace("ns").fields().
      name("field1").type().floatType().noDefault().
      endRecord();
    seen = new HashMap<>();
    seen.put(new ValidatingGrammarGenerator.LitS(sc),Symbol.FLOAT);
    inputs.add(new TestInput(sc,seen,Symbol.FLOAT));

    /*
    Test 15 -> Caso per RECORD: Lo schema non è già presente nell'hashmap, quindi viene ritornato SEQUENCE
    */
    sc = SchemaBuilder.array().items().booleanType();
    inputs.add(new TestInput(sc,seen,Symbol.Kind.SEQUENCE));

    /*
    Test 16 -> Record con hashmap nulla
     */
    sc = SchemaBuilder.record("nullMapTest").namespace("ns").fields().
      name("field1").type().nullType().noDefault().
      endRecord();
    seen = new HashMap<>();
    inputs.add(new TestInput(sc,seen,Symbol.Kind.SEQUENCE));

    /*
    Test 17 -> Caso default
    */

    //inputs.add(new TestInput(null,null,null));




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
  public void testGenerate() {

    if (sc == null)
      thrown.expect(RuntimeException.class);

    Boolean isSequenceKind =
      sc.getType().equals(Schema.Type.FIXED) || sc.getType().equals(Schema.Type.ENUM) ||
        sc.getType().equals(Schema.Type.ARRAY) || sc.getType().equals(Schema.Type.MAP) ||
        sc.getType().equals(Schema.Type.UNION);

    ValidatingGrammarGenerator r = new ValidatingGrammarGenerator();
    Symbol s = r.generate(sc,seen);

    if (isSequenceKind || sc.getName().equals("nullMapTest"))
      Assert.assertEquals(expectedParam,s.kind);
    else
      Assert.assertEquals(expectedParam,s);
  }

  @Test
  public void testGenerate2(){
    ValidatingGrammarGenerator v = new ValidatingGrammarGenerator();
    Symbol s = v.generate(sc);
    Assert.assertNotNull(s);
  }

  private static class TestInput {
    Schema sc;
    Map<ValidatingGrammarGenerator.LitS, Symbol> seen;
    Object expectedParam;

    public TestInput(Schema sc, Map<ValidatingGrammarGenerator.LitS, Symbol> seen, Object expectedParam) {
      this.sc = sc;
      this.seen = seen;
      this.expectedParam = expectedParam;
    }
  }
}
