package io.vertx.test.redis;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import org.junit.*;
import org.junit.runner.RunWith;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.util.UUID;

import static io.vertx.redis.client.Command.*;
import static io.vertx.redis.client.Request.cmd;

@RunWith(VertxUnitRunner.class)
public class RedisClientPikaSecureTest {

  @Rule
  public final RunTestOnContext rule = new RunTestOnContext();

  @ClassRule
  public static final GenericContainer<?> redis = new GenericContainer<>("pikadb/pika:latest")
    .withClasspathResourceMapping("pika.conf", "/pika/conf/pika.conf", BindMode.READ_ONLY)
    .withExposedPorts(9221);

  private Redis client;

  @Before
  public void before(TestContext should) {
    final Async before = should.async();

    client = Redis.createClient(
      rule.vertx(),
      new RedisOptions().setConnectionString("redis://:userpass@" + redis.getContainerIpAddress() + ":" + redis.getFirstMappedPort()));

    client.connect(onConnect -> {
      should.assertTrue(onConnect.succeeded());
      before.complete();
    });
  }

  @After
  public void after() {
    client.close();
  }

  private static String makeKey() {
    return UUID.randomUUID().toString();
  }

  @Test(timeout = 10_000L)
  public void testBasicInterop(TestContext should) {
    final Async test = should.async();
    final String nonexisting = makeKey();
    final String mykey = makeKey();

    client.send(cmd(GET).arg(nonexisting), reply0 -> {
      should.assertTrue(reply0.succeeded());
      should.assertNull(reply0.result());

      client.send(cmd(SET).arg(mykey).arg("Hello"), reply1 -> {
        should.assertTrue(reply1.succeeded());
        client.send(cmd(GET).arg(mykey), reply2 -> {
          should.assertTrue(reply2.succeeded());
          should.assertEquals("Hello", reply2.result().toString());
          test.complete();
        });
      });
    });
  }

  @Test(timeout = 10_000L)
  public void testJson(TestContext should) {
    final Async test = should.async();
    final String mykey = makeKey();

    JsonObject json = new JsonObject()
      .putNull("nullKey");

    client.send(cmd(HSET).arg(mykey).arg(json))
      .onSuccess(res -> {
        System.out.println(res);
        test.complete();
      })
      .onFailure(should::fail);
  }
}
