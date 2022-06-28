package org.apache.avro.tool;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


@RunWith(Parameterized.class)
public class CatToolRunTest {
  InputStream in;
  PrintStream out;
  PrintStream err;
  List<String> args;
  Object expectedParam;

  public CatToolRunTest(TestInput t){
    this.in = t.in;
    this. out = t.out;
    this.err = t.err;
    this.args = t.args;
    this.expectedParam = t.expectedParam;
  }

  public static Collection<TestInput[]> configure() throws IOException {

    //Parametri di configurazione
    Collection<TestInput> inputs = new ArrayList<>();
    Collection<TestInput[]> result = new ArrayList<>();

    List<String> args = new ArrayList<>();

    /*
    Test 1 -> Esecuzione con args < 2
     */
    inputs.add(new TestInput(System.in, System.out, System.err, args,0));

    /*
    Test 2 -> Esecuzione con args >= 2
     */
    args = new ArrayList<>();
    args.add("tmp.avro");
    args.add("tmp2.avro");
    inputs.add(new TestInput(System.in, System.out, System.err, args,0));

    /*
    Test 3 -> Esecuzione con limite < 0 (branch 105)
      */
    args = new ArrayList<>();
    args.add("tmp.avro");
    args.add("tmp2.avro");
    args.add("--limit");
    args.add("-1");
    inputs.add(new TestInput(System.in, System.out, System.err, args,1));

    /*
    Test 4 -> Esecuzione con offset < 0 (branch 110)
      */
    args = new ArrayList<>();
    args.add("tmp.avro");
    args.add("tmp2.avro");
    args.add("--offset");
    args.add("-1");
    inputs.add(new TestInput(System.in, System.out, System.err, args,1));

    /*
    Test 5 -> Esecuzione con samplerate < 0 (branch 115)
      */
    args = new ArrayList<>();
    args.add("tmp.avro");
    args.add("tmp2.avro");
    args.add("--samplerate");
    args.add("-1");
    inputs.add(new TestInput(System.in, System.out, System.err, args,1));

    /*
    Test 6 -> Esecuzione con samplerate > 1 (branch 115)
      */
    args = new ArrayList<>();
    args.add("tmp.avro");
    args.add("tmp2.avro");
    args.add("--samplerate");
    args.add("2");
    inputs.add(new TestInput(System.in, System.out, System.err, args,1));

    /*
    Test 7 -> Esecuzione con codec nullo
      */
    args = new ArrayList<>();
    args.add("tmpN.avro");
    args.add("tmp2.avro");
    inputs.add(new TestInput(System.in, System.out, System.err, args,0));
    /*
    Test 8 -> input nullo
      */
    inputs.add(new TestInput(System.in, System.out, System.err, null,null));



    for (TestInput e : inputs) {
      result.add(new TestInput[] { e });
    }
    return result;

  }
  @Parameters
  public static Collection<TestInput[]> getTestParameters() throws IOException {
    return configure();
  }
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void setUpEnvironment() throws IOException {
    // Create the file if does not exist (avro file format)
    if (!Files.exists(Paths.get("tmp.avro"))) {
      File tmp = new File("tmp.avro");
      tmp.createNewFile();

      //Schema schema = new Schema.Parser().parse("{\"type\":\"record\", " + "\"name\":\"myRecord\", "
        //+ "\"fields\":[ " + "{\"name\":\"value\",\"type\":\"int\"} " + "]}");
      Schema schema;
      schema = SchemaBuilder.record("myRecord").fields().name("value").type().intType().noDefault().endRecord();
      DataFileWriter<Object> writer = new DataFileWriter<>(new GenericDatumWriter<>(schema));
      writer.setMeta("METADATA_KEY", "METADATA_VALUE");
      writer.setCodec(CodecFactory.snappyCodec());
      writer.create(schema, tmp);
      for (int i = 0; i < 10; i++) {
        GenericRecord record = new GenericData.Record(schema);
        record.put("value", i);
        writer.append(record);
      }
      writer.close();
    }
    if (!Files.exists(Paths.get("tmpN.avro"))) {
      File tmpN = new File("tmpN.avro");
      tmpN.createNewFile();

      //Schema schema = new Schema.Parser().parse("{\"type\":\"record\", " + "\"name\":\"myRecord\", "
      //+ "\"fields\":[ " + "{\"name\":\"value\",\"type\":\"int\"} " + "]}");
      Schema schema;
      schema = SchemaBuilder.record("myRecord").fields().name("value").type().intType().noDefault().endRecord();
      DataFileWriter<Object> writer = new DataFileWriter<>(new GenericDatumWriter<>(schema));
      writer.setMeta("METADATA_KEY", "METADATA_VALUE");
      writer.setCodec(CodecFactory.snappyCodec());
      writer.create(schema, tmpN);
      for (int i = 0; i < 10; i++) {
        GenericRecord record = new GenericData.Record(schema);
        record.put("value", i);
        writer.append(record);
      }
      writer.close();
    }
  }

  @AfterClass
  public static void cleanUpEnvironment() throws IOException {
    // Delete the files if they exist
    if (Files.exists(Paths.get("tmp.avro"))) {
      File tmp = new File("tmp.avro");
      tmp.delete();
    }
    if (Files.exists(Paths.get("tmpN.avro"))) {
      File tmp = new File("tmpN.avro");
      tmp.delete();
    }
    if (Files.exists(Paths.get("tmp2.avro"))) {
      File tmp = new File("tmp2.avro");
      tmp.delete();
    }
    if (Files.exists(Paths.get(".tmp2.avro.crc"))) {
      File tmp = new File(".tmp2.avro.crc");
      tmp.delete();
    }
  }

  @Test
  public void testRun() throws Exception {
    if(args == null)
      thrown.expect(NullPointerException.class);

    int res = new CatTool().run(in,out,err,args);

    Assert.assertEquals(expectedParam, res);
  }

  private static class TestInput {
    InputStream in;
    PrintStream out;
    PrintStream err;
    List<String> args;
    Object expectedParam;

    public TestInput(InputStream in, PrintStream out, PrintStream err, List<String> args, Object expectedParam) {
      this.in = in;
      this.out = out;
      this.err = err;
      this.args = args;
      this.expectedParam = expectedParam;
    }
  }
}
